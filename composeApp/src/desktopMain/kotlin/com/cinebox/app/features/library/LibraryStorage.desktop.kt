package com.cinebox.app.features.library

import com.cinebox.app.core.storage.DesktopStorage

internal actual object LibraryStorage {
    private val store = DesktopStorage.store("cinebox_library")

    actual fun loadPayload(profileId: Int): String? =
        store.getString("library_$profileId")

    actual fun savePayload(profileId: Int, payload: String) {
        store.putString("library_$profileId", payload)
    }
}
