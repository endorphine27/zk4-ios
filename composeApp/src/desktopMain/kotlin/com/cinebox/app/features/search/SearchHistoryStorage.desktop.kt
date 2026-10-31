package com.cinebox.app.features.search

import com.cinebox.app.core.storage.DesktopStorage
import com.cinebox.app.core.storage.ProfileScopedKey

internal actual object SearchHistoryStorage {
    private val store = DesktopStorage.store("cinebox_search_history")
    private const val enabledKey = "recent_searches_enabled"

    actual fun loadPayload(): String? =
        store.getString(ProfileScopedKey.of("search_history"))

    actual fun savePayload(payload: String) {
        store.putString(ProfileScopedKey.of("search_history"), payload)
    }

    actual fun loadEnabled(): Boolean? = store.getBoolean(ProfileScopedKey.of(enabledKey))

    actual fun saveEnabled(enabled: Boolean) {
        store.putBoolean(ProfileScopedKey.of(enabledKey), enabled)
    }
}
