package com.cinebox.app

import androidx.compose.runtime.Composable
import com.cinebox.app.features.downloads.DownloadItem
import com.cinebox.app.features.downloads.DownloadsScreen
import com.cinebox.app.navigation.DownloadShowRoute
import com.cinebox.app.navigation.DownloadsRoute
import com.cinebox.app.navigation.CineboxNavigator

@Composable
internal fun DownloadsDestination(
    route: DownloadsRoute,
    navController: CineboxNavigator,
    useNativeNavigation: Boolean,
    onOpenDownload: (DownloadItem) -> Unit,
) {
    val onBack = rememberGuardedPopBackStack(navController, route)
    DownloadsScreen(
        onBack = onBack,
        onOpenDownload = onOpenDownload,
        onNavigateToShow = if (useNativeNavigation) {
            { showId, title -> navController.navigate(DownloadShowRoute(showId, title)) }
        } else {
            null
        },
    )
}

@Composable
internal fun DownloadShowDestination(
    route: DownloadShowRoute,
    navController: CineboxNavigator,
    onOpenDownload: (DownloadItem) -> Unit,
) {
    val onBack = rememberGuardedPopBackStack(navController, route)
    DownloadsScreen(
        onBack = onBack,
        onOpenDownload = onOpenDownload,
        initialShowId = route.showId,
        onBackFromShow = onBack,
    )
}
