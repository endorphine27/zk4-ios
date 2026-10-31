package com.cinebox.app.features.collection

import android.content.Context
import android.content.SharedPreferences
import com.cinebox.app.core.storage.ProfileScopedKey

actual object CollectionMobileSettingsStorage {
    private const val preferencesName = "cinebox_collection_mobile_settings"
    private const val payloadKey = "collection_mobile_settings_payload"

    private var preferences: SharedPreferences? = null

    fun initialize(context: Context) {
        preferences = context.getSharedPreferences(preferencesName, Context.MODE_PRIVATE)
    }

    actual fun loadPayload(): String? =
        preferences?.getString(ProfileScopedKey.of(payloadKey), null)

    actual fun savePayload(payload: String) {
        preferences
            ?.edit()
            ?.putString(ProfileScopedKey.of(payloadKey), payload)
            ?.apply()
    }
}
