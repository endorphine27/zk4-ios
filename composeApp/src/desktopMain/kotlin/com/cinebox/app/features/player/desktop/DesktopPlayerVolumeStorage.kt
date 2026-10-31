package com.cinebox.app.features.player.desktop

import com.cinebox.app.core.storage.DesktopStorage
import javax.swing.Timer

internal object DesktopPlayerVolumeStorage {
    private const val VolumeLevelKey = "volume_level"
    private const val PersistDelayMs = 250

    private val store = DesktopStorage.store("cinebox_player_runtime")

    private var pendingVolumeLevel: Float? = null

    private val persistTimer = Timer(PersistDelayMs) {
        pendingVolumeLevel?.let { level ->
            store.putFloat(VolumeLevelKey, level)
            pendingVolumeLevel = null
        }
    }.apply {
        isRepeats = false
    }

    fun loadVolumeLevel(): Float? =
        store.getFloat(VolumeLevelKey)?.coerceDesktopPlayerVolumeLevel()

    fun saveVolumeLevel(level: Float) {
        pendingVolumeLevel = level.coerceDesktopPlayerVolumeLevel()
        persistTimer.restart()
    }
}
