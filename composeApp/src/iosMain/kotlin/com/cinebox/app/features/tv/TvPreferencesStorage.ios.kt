package com.cinebox.app.features.tv

import com.cinebox.app.core.storage.ProfileScopedKey
import platform.Foundation.NSUserDefaults

internal actual object TvPreferencesStorage {
    private const val payloadKey = "tv_preferences_payload"

    actual fun loadPayload(): String? =
        NSUserDefaults.standardUserDefaults.stringForKey(ProfileScopedKey.of(payloadKey))

    actual fun savePayload(payload: String) {
        NSUserDefaults.standardUserDefaults.setObject(payload, forKey = ProfileScopedKey.of(payloadKey))
    }
}
