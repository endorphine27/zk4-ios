package com.cinebox.app.features.watchprogress

import com.cinebox.app.features.tracking.WatchProgressSource

internal fun projectWatchProgressSourceEntries(
    source: WatchProgressSource,
    cineboxEntries: Collection<WatchProgressEntry>,
    providerEntries: Collection<WatchProgressEntry>,
): List<WatchProgressEntry> = if (source.providerId == null) {
    cineboxEntries.toList()
} else {
    providerEntries.toList()
}
