package com.cinebox.app.features.home

import com.cinebox.app.core.storage.DesktopStorage
import com.cinebox.app.core.storage.ProfileScopedKey

internal actual object HomeCatalogSettingsStorage {
    private val store = DesktopStorage.store("cinebox_home_catalog_settings")

    actual fun loadPayload(): String? =
        store.getString(ProfileScopedKey.of("home_catalog_settings"))

    actual fun savePayload(payload: String) {
        store.putString(ProfileScopedKey.of("home_catalog_settings"), payload)
    }
}
