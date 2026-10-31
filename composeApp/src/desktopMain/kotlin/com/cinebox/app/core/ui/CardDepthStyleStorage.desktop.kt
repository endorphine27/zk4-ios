package com.cinebox.app.core.ui

import com.cinebox.app.core.storage.DesktopStorage
import com.cinebox.app.core.storage.ProfileScopedKey

internal actual object CardDepthStyleStorage {
    private const val payloadKey = "card_depth_style_payload"
    private val store = DesktopStorage.store("cinebox_card_depth_style")

    actual fun loadPayload(): String? =
        store.getString(ProfileScopedKey.of(payloadKey))

    actual fun savePayload(payload: String) {
        store.putString(ProfileScopedKey.of(payloadKey), payload)
    }
}
