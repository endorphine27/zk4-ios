package com.cinebox.app.features.watched

expect object WatchedStorage {
    fun loadPayload(profileId: Int): String?
    fun savePayload(profileId: Int, payload: String)
}

