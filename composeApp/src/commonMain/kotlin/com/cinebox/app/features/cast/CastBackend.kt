package com.cinebox.app.features.cast

import kotlinx.coroutines.flow.StateFlow

data class CastDevice(
    val id: String,
    val name: String,
    val protocol: String,
)

/** Sends a stream link to a TV or speaker on the local network and controls it remotely. */
interface CastBackend {
    val devices: StateFlow<List<CastDevice>>
    fun startDiscovery()
    fun stopDiscovery()
    suspend fun cast(
        device: CastDevice,
        url: String,
        title: String,
        startPositionMs: Long,
        subtitleUrl: String? = null,
    ): Result<Unit>
    suspend fun pause(device: CastDevice): Result<Unit>
    suspend fun resume(device: CastDevice): Result<Unit>
    suspend fun seek(device: CastDevice, positionMs: Long): Result<Unit>
    suspend fun stop(device: CastDevice): Result<Unit>
    /** Current position on the device in ms, or null when unknown. */
    suspend fun position(device: CastDevice): Long?
}

/** The cast backend for this platform, or null when casting is not available here. */
expect fun platformCastBackend(): CastBackend?
