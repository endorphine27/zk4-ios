package com.cinebox.app.features.home

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cinebox.app.core.ui.ScreenActivityEffect
import com.cinebox.app.features.details.MetaDetails
import com.cinebox.app.features.details.MetaDetailsRepository
import com.cinebox.app.features.details.MetaVideo
import com.cinebox.app.features.shuffle.EpisodeShuffle
import com.cinebox.app.features.shuffle.EpisodeShuffleProfile
import com.cinebox.app.features.shuffle.EpisodeShuffleRepository
import com.cinebox.app.features.shuffle.ShuffleSurface
import com.cinebox.app.features.shuffle.shuffleEpisodeProgress
import com.cinebox.app.features.shuffle.watchedShuffleEpisodes
import com.cinebox.app.features.watchprogress.ContinueWatchingItem
import com.cinebox.app.features.watchprogress.WatchProgressEntry
import com.cinebox.app.features.watchprogress.WatchProgressClock
import com.cinebox.app.features.watchprogress.buildContinueWatchingEpisodeSubtitle
import com.cinebox.app.features.watchprogress.toUpNextContinueWatchingItem
import com.cinebox.app.features.watchprogress.toContinueWatchingItem
import kotlinx.coroutines.CancellationException

@Composable
internal fun rememberShuffleHomeItems(
    profileId: Int,
    items: List<ContinueWatchingItem>,
    candidates: List<CompletedSeriesCandidate>,
    progress: List<WatchProgressEntry>,
    watchedKeys: Set<String>,
    visibleProgress: List<WatchProgressEntry>,
): List<ContinueWatchingItem> {
    val profile by remember {
        EpisodeShuffleRepository.ensureLoaded()
        EpisodeShuffleRepository.uiState
    }.collectAsStateWithLifecycle()
    var visit by remember(profileId) { mutableStateOf(WatchProgressClock.nowEpochMs()) }
    var wasInactive by remember(profileId) { mutableStateOf(false) }
    ScreenActivityEffect(profileId) { active ->
        if (active && wasInactive) visit += 1
        wasInactive = !active
    }
    val content = remember(items, candidates, profile) {
        (items.map { it.parentMetaId to it.parentMetaType } + candidates.map { it.content.id to it.content.type })
            .distinctBy { it.first }.filter { (id, type) -> profile.settings(id, type).enabled }
    }
    var metadata by remember(profileId) { mutableStateOf<Map<String, MetaDetails>>(emptyMap()) }
    LaunchedEffect(profileId, content, visit) {
        for ((id, type) in content) {
            val meta = MetaDetailsRepository.peek(type, id) ?: try {
                MetaDetailsRepository.fetch(type, id)
            } catch (error: Exception) {
                if (error is CancellationException) throw error
                null
            }
            if (meta != null) metadata = metadata + (id to meta)
        }
    }
    return remember(profileId, profile, items, candidates, progress, watchedKeys, visit, metadata, visibleProgress) {
        val resumes = visibleProgress.filter {
            profile.settings(it.parentMetaId, it.parentMetaType).enabled &&
                !it.isEffectivelyCompleted && (it.lastPositionMs > 0 || it.progressFraction > 0)
        }.groupBy { it.parentMetaId }.mapValues { (_, entries) -> entries.maxBy { it.lastUpdatedEpochMs } }
        val withResumes = items.map { item ->
            val resume = resumes[item.parentMetaId]
            if (resume == null || (!item.isNextUp && item.videoId == resume.videoId)) item
            else resume.toContinueWatchingItem()
        }
        val seeds = candidates.filter { candidate ->
            items.none { it.parentMetaId == candidate.content.id } &&
                profile.settings(candidate.content.id, candidate.content.type).enabled
        }.mapNotNull { candidate ->
            val meta = metadata[candidate.content.id] ?: return@mapNotNull null
            val video = meta.videos.firstOrNull() ?: return@mapNotNull null
            candidate.toContinueWatchingSeed(meta).toUpNextContinueWatchingItem(video)
        }
        applyHomeShuffle(withResumes + seeds, profileId, profile, progress, watchedKeys,
            EpisodeShuffleRepository.shuffle, visit) { id -> metadata[id]?.videos }
    }
}

internal fun applyHomeShuffle(
    items: List<ContinueWatchingItem>,
    profileId: Int,
    profile: EpisodeShuffleProfile,
    progress: List<WatchProgressEntry>,
    watchedKeys: Set<String>,
    shuffle: EpisodeShuffle,
    visit: Long,
    videos: (String) -> List<MetaVideo>?,
): List<ContinueWatchingItem> {
    val resumable = items.filter {
        !it.isNextUp && profile.settings(it.parentMetaId, it.parentMetaType).enabled
    }.groupBy { it.parentMetaId }.mapValues { (_, entries) ->
        entries.maxBy { item -> progress.filter { it.parentMetaId == item.parentMetaId &&
            it.seasonNumber == item.seasonNumber && it.episodeNumber == item.episodeNumber }
            .maxOfOrNull { it.lastUpdatedEpochMs } ?: 0L }
    }
    return items.mapNotNull { item ->
        val settings = profile.settings(item.parentMetaId, item.parentMetaType)
        if (!settings.enabled) return@mapNotNull item
        val resume = resumable[item.parentMetaId]
        if (resume != null) return@mapNotNull item.takeIf { it == resume }?.copy(shufflePlayback = true)
        val catalogue = videos(item.parentMetaId) ?: return@mapNotNull null
        val watched = watchedShuffleEpisodes(item.parentMetaId, item.parentMetaType, catalogue, watchedKeys)
        val episodeProgress = shuffleEpisodeProgress(item.parentMetaId, progress)
        val selected = shuffle.select(
            profileId, item.parentMetaId, catalogue, settings.includeWatched,
            watched, episodeProgress, ShuffleSurface.HOME, visit = visit,
        ) ?: return@mapNotNull null
        item.copy(
            videoId = selected.id, seasonNumber = selected.season, episodeNumber = selected.episode,
            episodeTitle = selected.title, episodeThumbnail = selected.thumbnail,
            pauseDescription = selected.overview, imageUrl = selected.thumbnail ?: item.background ?: item.poster,
            subtitle = buildContinueWatchingEpisodeSubtitle(selected.season, selected.episode, selected.title),
            released = selected.released, shufflePlayback = true,
            isWatched = (selected.season to selected.episode) in watched ||
                episodeProgress[selected.season to selected.episode]?.isEffectivelyCompleted == true,
            isReleaseAlert = false, isNewSeasonRelease = false,
        )
    }.distinctBy { if (it.shufflePlayback) it.parentMetaId else "${it.parentMetaId}:${it.videoId}" }
}
