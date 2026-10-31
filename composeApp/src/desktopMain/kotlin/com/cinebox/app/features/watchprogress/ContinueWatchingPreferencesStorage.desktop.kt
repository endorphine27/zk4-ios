package com.cinebox.app.features.watchprogress

import com.cinebox.app.core.storage.DesktopStorage
import com.cinebox.app.core.storage.ProfileScopedKey

internal actual object ContinueWatchingPreferencesStorage {
    private val store = DesktopStorage.store("cinebox_continue_watching_preferences")

    actual fun loadPayload(): String? =
        store.getString(ProfileScopedKey.of("continue_watching_preferences"))

    actual fun savePayload(payload: String) {
        store.putString(ProfileScopedKey.of("continue_watching_preferences"), payload)
    }
}
