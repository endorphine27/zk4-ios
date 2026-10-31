package com.cinebox.app.features.tv

/** The TV tab is only shown on desktop, so mobile keeps these in memory. */
internal actual object TvPreferencesStorage {
    private var payload: String? = null

    actual fun loadPayload(): String? = payload

    actual fun savePayload(payload: String) {
        this.payload = payload
    }
}
