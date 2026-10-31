package com.cinebox.app.features.watchprogress

import android.content.Context
import android.content.SharedPreferences
import com.cinebox.app.core.storage.ProfileScopedKey

actual object ContinueWatchingPreferencesStorage {
    private const val preferencesName = "cinebox_continue_watching_preferences"
    private const val payloadKey = "continue_watching_preferences_payload"

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
