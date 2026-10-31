package com.cinebox.app.features.collection

import com.cinebox.app.core.storage.DesktopStorage
import com.cinebox.app.core.storage.ProfileScopedKey

internal actual object CollectionStorage {
    private val store = DesktopStorage.store("cinebox_collections")

    actual fun loadPayload(): String? =
        store.getString(ProfileScopedKey.of("collections"))

    actual fun savePayload(payload: String) {
        store.putString(ProfileScopedKey.of("collections"), payload)
    }
}
