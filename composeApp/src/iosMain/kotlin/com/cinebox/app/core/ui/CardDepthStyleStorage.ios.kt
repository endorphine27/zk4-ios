package com.cinebox.app.core.ui

import com.cinebox.app.core.storage.ProfileScopedKey
import platform.Foundation.NSUserDefaults

internal actual object CardDepthStyleStorage {
    private const val payloadKey = "card_depth_style_payload"

    actual fun loadPayload(): String? =
        NSUserDefaults.standardUserDefaults.stringForKey(ProfileScopedKey.of(payloadKey))

    actual fun savePayload(payload: String) {
        NSUserDefaults.standardUserDefaults.setObject(payload, forKey = ProfileScopedKey.of(payloadKey))
    }
}
