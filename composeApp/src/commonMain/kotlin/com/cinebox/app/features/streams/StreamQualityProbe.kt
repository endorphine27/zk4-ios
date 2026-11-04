package com.cinebox.app.features.streams

import co.touchlab.kermit.Logger
import com.cinebox.app.features.addons.AddonRepository
import com.cinebox.app.features.addons.buildAddonResourceUrl
import com.cinebox.app.features.addons.enabledAddons
import com.cinebox.app.features.addons.fetchAddonResponseText
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.time.TimeSource

/**
 * Looks up the streams addons hold for a title and reports the badges of the best one available,
 * so the details page can show what quality is on offer before anything is played.
 */
object StreamQualityProbe {
    private val log = Logger.withTag("StreamQualityProbe")
    private const val ADDON_TIMEOUT_MS = 9_000L
    private const val CACHE_TTL_MS = 10 * 60 * 1000L

    private class CachedBadges(val badges: List<StreamBadge>, val storedAt: TimeSource.Monotonic.ValueTimeMark)

    private val cacheLock = Mutex()
    private val cache = mutableMapOf<String, CachedBadges>()

    suspend fun bestBadges(type: String, videoId: String): List<StreamBadge> {
        val key = "${type.lowercase()}::$videoId"
        cacheLock.withLock {
            cache[key]?.takeIf { it.storedAt.elapsedNow().inWholeMilliseconds < CACHE_TTL_MS }?.let { return it.badges }
        }

        StreamBadgeSettingsRepository.ensureLoaded()
        val rules = StreamBadgeSettingsRepository.snapshot()
        val targets = AddonRepository.uiState.value.addons.enabledAddons().mapNotNull { addon ->
            val manifest = addon.manifest ?: return@mapNotNull null
            if (!manifest.supportsStream(type, videoId)) return@mapNotNull null
            Triple(addon.displayTitle.ifBlank { manifest.name }, addon.streamAddonInstanceId(manifest.id), manifest)
        }
        if (targets.isEmpty()) return emptyList()

        val streams = coroutineScope {
            targets.map { (name, addonId, manifest) ->
                async {
                    withTimeoutOrNull(ADDON_TIMEOUT_MS) {
                        runCatchingUnlessCancelled {
                            val url = buildAddonResourceUrl(
                                manifestUrl = manifest.transportUrl,
                                resource = "stream",
                                type = type,
                                id = videoId,
                            )
                            StreamParser.parse(
                                payload = fetchAddonResponseText(url = url, forceRefresh = false),
                                addonName = name,
                                addonId = addonId,
                                addonLogo = manifest.logoUrl,
                            )
                        }.getOrNull()
                    }.orEmpty()
                }
            }.awaitAll().flatten()
        }.filter { it.url != null || it.infoHash != null }

        val best = streams.maxByOrNull(::qualityScore)
        val badges = if (best == null) {
            emptyList()
        } else {
            val group = AddonStreamGroup(
                addonName = best.addonName,
                addonId = best.addonId,
                streams = listOf(best),
                isLoading = false,
            )
            StreamBadgePresentation.apply(listOf(group), rules).firstOrNull()?.streams?.firstOrNull()?.badges.orEmpty()
        }
        log.d { "Best of ${streams.size} streams for $type/$videoId has ${badges.size} badges" }
        cacheLock.withLock { cache[key] = CachedBadges(badges, TimeSource.Monotonic.markNow()) }
        return badges
    }

    internal fun qualityScore(stream: StreamItem): Int {
        val text = listOfNotNull(stream.name, stream.title, stream.description).joinToString(" ").lowercase()
        var score = when {
            Regex("""2160p|\b4k\b|\buhd\b""").containsMatchIn(text) -> 400
            Regex("""1440p""").containsMatchIn(text) -> 300
            Regex("""1080p""").containsMatchIn(text) -> 200
            Regex("""720p""").containsMatchIn(text) -> 100
            else -> 0
        }
        if (Regex("""dolby[ ._-]?vision|\bdv\b|\bdovi\b""").containsMatchIn(text)) score += 30
        else if (Regex("""hdr10\+""").containsMatchIn(text)) score += 25
        else if (Regex("""\bhdr""").containsMatchIn(text)) score += 20
        if (text.contains("atmos")) score += 12
        else if (Regex("""truehd|dts[ -]?(x|hd)""").containsMatchIn(text)) score += 9
        else if (Regex("""dd\+|e-?ac-?3|ddp""").containsMatchIn(text)) score += 6
        if (text.contains("remux")) score += 5
        // A cached debrid link starts right away, so it beats an uncached one of the same quality.
        if (text.contains("[rd+]") || text.contains("[tb+]") || text.contains("[pm+]") || text.contains("[ad+]") || text.contains("⚡")) score += 3
        return score
    }
}
