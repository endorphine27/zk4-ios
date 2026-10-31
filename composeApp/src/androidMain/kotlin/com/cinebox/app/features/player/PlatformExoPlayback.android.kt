package com.cinebox.app.features.player

import android.os.Build

internal actual fun applyExoPlayerNativeMemory(enabled: Boolean) {
    CineboxExoPlayerPerformanceHelper.enabled = enabled && isExoNativeMemorySupported()
}

internal actual fun isExoNativeMemorySupported(): Boolean =
    Build.VERSION.SDK_INT >= Build.VERSION_CODES.O

internal actual fun exoNativeMemoryInfo(): ExoNativeMemoryInfo? {
    if (!isExoNativeMemorySupported()) return null
    val context = PlayerSettingsStorage.applicationContext() ?: return null
    return ExoNativeMemoryInfo(
        ramLabel = CineboxExoPlayerPerformanceHelper.getFriendlyRamLabel(context),
        safeLimitMb = CineboxExoPlayerPerformanceHelper.getSafeNativeMemoryLimitMb(context),
        warningLimitMb = CineboxExoPlayerPerformanceHelper.getWarningNativeMemoryLimitMb(context),
    )
}

internal actual fun playbackHeapBufferMaxMb(): Int {
    val maxHeapMb = Runtime.getRuntime().maxMemory() / (1024L * 1024L)
    val lowRam = maxHeapMb < 512L
    val raw = (maxHeapMb * if (lowRam) 0.65 else 0.85).toInt()
    val budget = if (lowRam) {
        raw.coerceAtMost((maxHeapMb - 210L).toInt()).coerceAtLeast(100)
    } else {
        raw
    }
    return (budget / PlaybackBufferSettings.TARGET_STEP_MB * PlaybackBufferSettings.TARGET_STEP_MB)
        .coerceIn(PlaybackBufferSettings.MIN_TARGET_MB, 4_096)
}

internal actual fun playbackCacheUsableSpaceBytes(): Long {
    val context = PlayerSettingsStorage.applicationContext() ?: return 0L
    return context.cacheDir.usableSpace.coerceAtLeast(0L)
}
