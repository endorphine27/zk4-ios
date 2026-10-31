package com.cinebox.app.features.watchprogress

import com.cinebox.app.features.tracking.WatchProgressSource
import kotlin.test.Test
import kotlin.test.assertEquals

class WatchProgressSourceProjectionTest {
    @Test
    fun `remote source excludes every Cinebox progress entry`() {
        val cineboxEntries = listOf(
            entry(parentMetaId = "shared", updatedAt = 200L),
            entry(parentMetaId = "cinebox-only", updatedAt = 300L),
        )
        val providerEntries = listOf(
            entry(parentMetaId = "shared", updatedAt = 100L),
        )

        listOf(WatchProgressSource.TRAKT, WatchProgressSource.SIMKL).forEach { source ->
            val projected = projectWatchProgressSourceEntries(
                source = source,
                cineboxEntries = cineboxEntries,
                providerEntries = providerEntries,
            )

            assertEquals(providerEntries, projected)
        }
    }

    @Test
    fun `Cinebox source excludes every provider progress entry`() {
        val cineboxEntries = listOf(entry(parentMetaId = "cinebox"))
        val providerEntries = listOf(entry(parentMetaId = "provider"))

        val projected = projectWatchProgressSourceEntries(
            source = WatchProgressSource.CINEBOX_SYNC,
            cineboxEntries = cineboxEntries,
            providerEntries = providerEntries,
        )

        assertEquals(cineboxEntries, projected)
    }

    private fun entry(
        parentMetaId: String,
        updatedAt: Long = 1L,
    ): WatchProgressEntry = WatchProgressEntry(
        contentType = "series",
        parentMetaId = parentMetaId,
        parentMetaType = "series",
        videoId = "$parentMetaId:1:1",
        title = parentMetaId,
        seasonNumber = 1,
        episodeNumber = 1,
        lastPositionMs = 10L,
        durationMs = 100L,
        lastUpdatedEpochMs = updatedAt,
    )
}
