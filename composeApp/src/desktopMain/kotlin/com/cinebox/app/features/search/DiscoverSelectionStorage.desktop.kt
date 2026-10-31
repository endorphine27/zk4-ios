package com.cinebox.app.features.search

import com.cinebox.app.core.storage.DesktopStorage
import com.cinebox.app.core.storage.ProfileScopedKey

internal actual object DiscoverSelectionStorage {
    private val store = DesktopStorage.store("cinebox_discover_selection")

    actual fun loadCatalogKey(): String? =
        store.getString(ProfileScopedKey.of("discover_catalog_key"))

    actual fun saveCatalogKey(catalogKey: String) {
        store.putString(ProfileScopedKey.of("discover_catalog_key"), catalogKey)
    }
}
