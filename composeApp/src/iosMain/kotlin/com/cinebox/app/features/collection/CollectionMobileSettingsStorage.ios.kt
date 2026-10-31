package com.cinebox.app.features.collection

import com.cinebox.app.core.storage.ProfileScopedKey
import platform.Foundation.NSUserDefaults

actual object CollectionMobileSettingsStorage {
    private const val payloadKey = "collection_mobile_settings_payload"

    actual fun loadPayload(): String? =
        NSUserDefaults.standardUserDefaults.stringForKey(ProfileScopedKey.of(payloadKey))

    actual fun savePayload(payload: String) {
        NSUserDefaults.standardUserDefaults.setObject(payload, forKey = ProfileScopedKey.of(payloadKey))
    }
}
