package com.cinebox.app.features.details

import com.cinebox.app.core.storage.ProfileScopedKey
import platform.Foundation.NSUserDefaults

internal actual object MetaScreenSettingsStorage {
    private const val payloadKey = "meta_screen_settings_payload"

    actual fun loadPayload(): String? =
        NSUserDefaults.standardUserDefaults.stringForKey(ProfileScopedKey.of(payloadKey))

    actual fun savePayload(payload: String) {
        NSUserDefaults.standardUserDefaults.setObject(payload, forKey = ProfileScopedKey.of(payloadKey))
    }
}