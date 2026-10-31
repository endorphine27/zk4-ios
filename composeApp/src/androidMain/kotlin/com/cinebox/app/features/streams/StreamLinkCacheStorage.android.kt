package com.cinebox.app.features.streams

import android.content.Context
import android.content.SharedPreferences
import com.cinebox.app.core.storage.ProfileScopedKey

actual object StreamLinkCacheStorage {
    private const val preferencesName = "cinebox_stream_link_cache"

    private var preferences: SharedPreferences? = null

    fun initialize(context: Context) {
        preferences = context.getSharedPreferences(preferencesName, Context.MODE_PRIVATE)
    }

    actual fun loadEntry(hashedKey: String): String? =
        preferences?.getString(ProfileScopedKey.of(hashedKey), null)

    actual fun saveEntry(hashedKey: String, payload: String) {
        preferences
            ?.edit()
            ?.putString(ProfileScopedKey.of(hashedKey), payload)
            ?.apply()
    }

    actual fun removeEntry(hashedKey: String) {
        preferences
            ?.edit()
            ?.remove(ProfileScopedKey.of(hashedKey))
            ?.apply()
    }
}
