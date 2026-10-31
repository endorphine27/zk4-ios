package com.cinebox.app.features.library

import android.content.Context
import android.content.SharedPreferences

actual object LibraryStorage {
    private const val preferencesName = "cinebox_library"
    private fun payloadKey(profileId: Int) = "library_payload_$profileId"

    private var preferences: SharedPreferences? = null

    fun initialize(context: Context) {
        preferences = context.getSharedPreferences(preferencesName, Context.MODE_PRIVATE)
    }

    actual fun loadPayload(profileId: Int): String? =
        preferences?.getString(payloadKey(profileId), null)

    actual fun savePayload(profileId: Int, payload: String) {
        preferences
            ?.edit()
            ?.putString(payloadKey(profileId), payload)
            ?.apply()
    }
}
