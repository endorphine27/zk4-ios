package com.cinebox.app.features.profiles

import com.cinebox.app.core.storage.DesktopStorage

internal actual object ProfileStorage {
    private val store = DesktopStorage.store("cinebox_profiles")

    actual fun loadPayload(): String? = store.getString("profiles")

    actual fun savePayload(payload: String) {
        store.putString("profiles", payload)
    }
}
