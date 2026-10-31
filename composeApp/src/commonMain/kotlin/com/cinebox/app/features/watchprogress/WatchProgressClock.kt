package com.cinebox.app.features.watchprogress

internal expect object WatchProgressClock {
    fun nowEpochMs(): Long
}
