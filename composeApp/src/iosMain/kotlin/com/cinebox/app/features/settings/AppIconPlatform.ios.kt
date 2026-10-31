package com.cinebox.app.features.settings

import com.cinebox.app.nativebridge.CineboxIsCurrentAlternateAppIcon
import com.cinebox.app.nativebridge.CineboxSetAlternateAppIconName
import com.cinebox.app.nativebridge.CineboxSupportsAlternateAppIcons
import kotlinx.cinterop.CFunction
import kotlinx.cinterop.CPointer
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.staticCFunction
import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

@OptIn(ExperimentalForeignApi::class)
internal actual object AppIconPlatform {
    actual val isSupported: Boolean = true
    actual val requiresCloseConfirmation: Boolean = false

    private var pendingChange: CancellableContinuation<Boolean>? = null

    actual fun currentIconName(): String? = AppIconOption.entries
        .mapNotNull(AppIconOption::platformName)
        .firstOrNull { name -> CineboxIsCurrentAlternateAppIcon(name) }

    actual suspend fun activateIcon(name: String?): Boolean {
        if (!CineboxSupportsAlternateAppIcons()) return false

        return suspendCancellableCoroutine { continuation ->
            if (pendingChange != null) {
                continuation.resume(false)
                return@suspendCancellableCoroutine
            }

            pendingChange = continuation
            continuation.invokeOnCancellation {
                if (pendingChange === continuation) {
                    pendingChange = null
                }
            }

            CineboxSetAlternateAppIconName(name, appIconCompletion)
        }
    }

    fun completeChange(success: Boolean) {
        val continuation = pendingChange ?: return
        pendingChange = null
        if (continuation.isActive) {
            continuation.resume(success)
        }
    }
}

@OptIn(ExperimentalForeignApi::class)
private val appIconCompletion: CPointer<CFunction<(Boolean) -> Unit>> =
    staticCFunction { success -> AppIconPlatform.completeChange(success) }
