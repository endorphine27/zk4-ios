package com.cinebox.app.features.autosync

import com.cinebox.app.core.storage.DesktopStorage
import com.cinebox.app.core.storage.ProfileScopedKey

internal object AutoSyncPreferencesDesktop {
    private const val preferredSubtitleAutoSyncOnStartKey = "preferred_subtitle_auto_sync_on_start"
    private const val aggressiveModeKey = "auto_sync_aggressive_mode"
    private const val syncToleranceMsKey = "auto_sync_tolerance_ms"

    fun initialize() {
        val store = DesktopStorage.store("cinebox_autosync_settings")
        AutoSyncPreferencesRepository.installPersistence(
            load = { store.getBoolean(ProfileScopedKey.of(preferredSubtitleAutoSyncOnStartKey)) },
            save = { store.putBoolean(ProfileScopedKey.of(preferredSubtitleAutoSyncOnStartKey), it) },
            loadAggressiveMode = { store.getBoolean(ProfileScopedKey.of(aggressiveModeKey)) },
            saveAggressiveMode = { store.putBoolean(ProfileScopedKey.of(aggressiveModeKey), it) },
            loadSyncToleranceMs = { store.getInt(ProfileScopedKey.of(syncToleranceMsKey)) },
            saveSyncToleranceMs = { store.putInt(ProfileScopedKey.of(syncToleranceMsKey), it) },
        )
    }
}
