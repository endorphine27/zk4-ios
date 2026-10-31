package com.cinebox.app.features.updater

import com.cinebox.app.core.build.AppVersionConfig
import kotlinx.coroutines.runBlocking
import cinebox.composeapp.generated.resources.Res
import cinebox.composeapp.generated.resources.updates_not_available
import org.jetbrains.compose.resources.getString

actual object AppUpdaterPlatform {
    actual val isSupported: Boolean = false
    actual val isDebugBuild: Boolean = false
    actual val hasSingleUpdateChannel: Boolean = false

    actual val releaseSource: AppUpdateReleaseSource = AppUpdateReleaseSource(
        owner = "endorphine27",
        repo = "zk4-ios",
        channelBranch = null,
        userAgent = "CINEBOXMobile",
    )

    actual val assetSelector: AppUpdateAssetSelector = AppUpdateAssetSelector(
        fileExtensions = emptyList(),
    )

    actual val currentVersionName: String = AppVersionConfig.VERSION_NAME

    actual fun getSupportedAbis(): List<String> = emptyList()

    actual fun getIgnoredTag(): String? = null

    actual fun setIgnoredTag(tag: String?) = Unit

    actual suspend fun downloadUpdateAsset(
        assetUrl: String,
        assetName: String,
        onProgress: (downloadedBytes: Long, totalBytes: Long?) -> Unit,
    ): Result<String> = Result.failure(IllegalStateException(getString(Res.string.updates_not_available)))

    actual fun getUpdateChannel(): String? = null

    actual fun setUpdateChannel(channel: String) = Unit

    actual fun deleteDownloadedUpdate(path: String) = Unit

    actual fun canInstallDownloadedUpdate(): Boolean = false

    actual fun openInstallPermissionSettings() = Unit

    actual fun installDownloadedUpdate(path: String): Result<Unit> =
        Result.failure(IllegalStateException(runBlocking { getString(Res.string.updates_not_available) }))
}
