package com.cinebox.app.features.library

internal expect object LibraryClock {
    fun nowEpochMs(): Long
}
