package com.cinebox.app.features.player

import kotlin.time.Duration.Companion.seconds
import kotlin.time.TimeSource

internal val PlayerScreenRuntime.contentLanguage: String?
    get() {
        val metadata = listOfNotNull(metaUiState.meta, playerMeta).filter {
            it.id == parentMetaId && it.type == parentMetaType
        }
        return metadata.firstNotNullOfOrNull { normalizeLanguageCode(it.language) }
            ?: normalizeLanguageCode(args.contentLanguage)
            ?: metadata.firstNotNullOfOrNull { countryToLanguageCode(it.country) }
    }

internal val PlayerScreenRuntime.preferredAudioLanguageTargets: List<String>
    get() = resolvePreferredAudioLanguageTargets(
        preferredAudioLanguage = playerSettingsUiState.preferredAudioLanguage,
        secondaryPreferredAudioLanguage = playerSettingsUiState.secondaryPreferredAudioLanguage,
        deviceLanguages = DeviceLanguagePreferences.preferredLanguageCodes(),
        contentOriginalLanguage = contentLanguage,
    )

internal data class AppliedAudioPreferences(
    val controller: PlayerEngineController,
    val languages: List<String>,
    val tracks: List<AudioTrack>,
)

internal fun PlayerScreenRuntime.applyPreferredAudioTrack(targets: List<String>) {
    if (isUserExplicitAudioSelection) return
    val controller = playerController ?: return
    val preferences = AppliedAudioPreferences(
        controller = controller,
        languages = targets,
        tracks = audioTracks.map { it.copy(isSelected = false) },
    )
    if (appliedAudioPreferences != preferences) {
        controller.applyAudioLanguagePreferences(targets)
        appliedAudioPreferences = preferences
        preferredAudioSelectionApplied = false
    } else {
        preferredAudioSelectionApplied = true
    }
}

internal fun PlayerScreenRuntime.restorePersistedAudioPreference(preference: PersistedPlayerTrackPreference) {
    if (audioTracks.isEmpty()) return
    val restoredIndex = findPersistedAudioTrackIndex(audioTracks, preference)
    if (restoredIndex < 0) return
    playerController?.selectAudioTrack(restoredIndex)
    selectedAudioIndex = restoredIndex
    isUserExplicitAudioSelection = true
    preferredAudioSelectionApplied = true
}

internal fun PlayerScreenRuntime.refreshAudioTracksIfChanged() {
    if (playbackSnapshot.isLoading) return
    val controller = playerController ?: return
    if (!preferredAudioSelectionApplied) {
        refreshTracks()
        return
    }
    // Snapshots arrive several times a second but the track list almost never changes, and reading it asks
    // the native player for every track: compare it at most every couple of seconds.
    val lastCheck = lastAudioTracksCheck
    if (lastCheck != null && lastCheck.elapsedNow() < AudioTracksRecheckInterval) return
    lastAudioTracksCheck = TimeSource.Monotonic.markNow()
    if (controller.getAudioTracks() != audioTracks) {
        refreshTracks()
    }
}

private val AudioTracksRecheckInterval = 2.seconds
