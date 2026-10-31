package com.cinebox.app.features.profiles

import com.cinebox.app.core.storage.DesktopStorage

internal actual object ProfilePinCacheStorage {
    private val store = DesktopStorage.store("cinebox_profile_pin_cache")

    actual fun loadPayload(profileIndex: Int): String? =
        store.getString(key(profileIndex))

    actual fun savePayload(profileIndex: Int, payload: String) {
        store.putString(key(profileIndex), payload)
    }

    actual fun removePayload(profileIndex: Int) {
        store.remove(key(profileIndex))
    }

    private fun key(profileIndex: Int): String = "profile_pin_$profileIndex"
}
