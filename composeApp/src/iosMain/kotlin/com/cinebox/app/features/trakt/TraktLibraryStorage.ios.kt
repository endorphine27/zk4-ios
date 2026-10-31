package com.cinebox.app.features.trakt

import com.cinebox.app.core.storage.ProfileScopedKey
import platform.Foundation.NSUserDefaults

internal actual object TraktLibraryStorage {
    private const val payloadKey = "trakt_library_payload"

    actual fun loadPayload(): String? =
        NSUserDefaults.standardUserDefaults.stringForKey(ProfileScopedKey.of(payloadKey))

    actual fun savePayload(payload: String) {
        NSUserDefaults.standardUserDefaults.setObject(payload, forKey = ProfileScopedKey.of(payloadKey))
    }
}