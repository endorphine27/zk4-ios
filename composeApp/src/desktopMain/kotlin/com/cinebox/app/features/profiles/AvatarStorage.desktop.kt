package com.cinebox.app.features.profiles

import com.cinebox.app.core.storage.DesktopStorage

internal actual object AvatarStorage {
    private val store = DesktopStorage.store("cinebox_avatars")

    actual fun loadPayload(): String? = store.getString("avatars")

    actual fun savePayload(payload: String) {
        store.putString("avatars", payload)
    }
}
