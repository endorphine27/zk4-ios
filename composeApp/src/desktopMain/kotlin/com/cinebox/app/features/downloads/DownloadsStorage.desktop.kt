package com.cinebox.app.features.downloads

import com.cinebox.app.core.storage.DesktopStorage
import com.cinebox.app.core.storage.ProfileScopedKey

internal actual object DownloadsStorage {
    private val store = DesktopStorage.store("cinebox_downloads")

    actual fun loadPayload(): String? =
        store.getString(ProfileScopedKey.of("downloads"))

    actual fun savePayload(payload: String) {
        store.putString(ProfileScopedKey.of("downloads"), payload)
    }
}
