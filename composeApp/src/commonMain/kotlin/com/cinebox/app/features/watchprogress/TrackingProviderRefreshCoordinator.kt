package com.cinebox.app.features.watchprogress

import com.cinebox.app.features.tracking.TrackingProviderId

internal suspend fun coordinateTrackingProviderRefresh(
    providerId: TrackingProviderId,
    refreshProvider: suspend () -> Boolean,
    activeProviderId: () -> TrackingProviderId?,
    refreshActiveReadModels: suspend () -> Boolean,
): Boolean {
    if (!refreshProvider()) return false
    if (activeProviderId() != providerId) return true
    return refreshActiveReadModels()
}
