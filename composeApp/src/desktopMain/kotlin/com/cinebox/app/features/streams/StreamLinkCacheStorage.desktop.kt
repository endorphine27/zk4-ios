package com.cinebox.app.features.streams

import com.cinebox.app.core.storage.DesktopStorage
import com.cinebox.app.core.storage.ProfileScopedKey

internal actual object StreamLinkCacheStorage {
    private val store = DesktopStorage.store("cinebox_stream_link_cache")

    actual fun loadEntry(hashedKey: String): String? =
        store.getString(ProfileScopedKey.of(hashedKey))

    actual fun saveEntry(hashedKey: String, payload: String) {
        store.putString(ProfileScopedKey.of(hashedKey), payload)
    }

    actual fun removeEntry(hashedKey: String) {
        store.remove(ProfileScopedKey.of(hashedKey))
    }
}
