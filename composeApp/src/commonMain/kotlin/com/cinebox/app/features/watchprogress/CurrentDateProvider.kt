package com.cinebox.app.features.watchprogress

expect object CurrentDateProvider {
    fun todayIsoDate(): String
    fun localStartOfDayEpochMs(isoDate: String): Long?
}
