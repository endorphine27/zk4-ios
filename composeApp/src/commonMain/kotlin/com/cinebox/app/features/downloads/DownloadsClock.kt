package com.cinebox.app.features.downloads

internal expect object DownloadsClock {
    fun nowEpochMs(): Long
}
