package com.cinebox.app.features.trakt

import android.content.Context
import android.content.SharedPreferences
import com.cinebox.app.core.storage.ProfileScopedKey

internal actual object TraktLibraryStorage {
    private const val preferencesName = "cinebox_trakt_library"
    private const val payloadKey = "trakt_library_payload"

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