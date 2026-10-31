package com.cinebox.app.features.collection

import com.cinebox.app.core.storage.DesktopStorage
import com.cinebox.app.core.storage.ProfileScopedKey

internal actual object CollectionMobileSettingsStorage {
    private val store = DesktopStorage.store("cinebox_collection_mobile_settings")

    actual fun loadPayload(): String? =
        store.getString(ProfileScopedKey.of("collection_mobile_settings"))

    actual fun savePayload(payload: String) {
        store.putString(ProfileScopedKey.of("collection_mobile_settings"), payload)
    }
}
