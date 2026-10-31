package com.cinebox.app.features.notifications

internal expect object EpisodeReleaseNotificationsClock {
    fun isoDateFromEpochMs(epochMs: Long): String
}