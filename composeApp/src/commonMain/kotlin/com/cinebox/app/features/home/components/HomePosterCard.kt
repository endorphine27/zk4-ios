package com.cinebox.app.features.home.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.cinebox.app.core.format.formatReleaseDateForDisplay
import com.cinebox.app.core.ui.CineboxPosterCard
import com.cinebox.app.core.ui.CineboxPosterShape
import com.cinebox.app.core.ui.desktopCatalogShelfPosterBaseWidthDp
import com.cinebox.app.core.ui.rememberPosterCardStyleUiState
import com.cinebox.app.features.home.MetaPreview
import com.cinebox.app.features.home.PosterShape

@Composable
fun HomePosterCard(
    item: MetaPreview,
    modifier: Modifier = Modifier,
    useLandscapeBackdropMode: Boolean = false,
    isWatched: Boolean = false,
    onClick: (() -> Unit)? = null,
    onLongClick: (() -> Unit)? = null,
    showLandscapeOverlay: Boolean = true,
) {
    val posterCardStyle = rememberPosterCardStyleUiState()
    val isLandscapeMode = useLandscapeBackdropMode || posterCardStyle.catalogLandscapeModeEnabled
    val effectiveLandscapePoster = if (posterCardStyle.alwaysShowLandscapeClearlogo) null else item.landscapePoster
    val imageUrl = if (isLandscapeMode) {
        effectiveLandscapePoster ?: item.banner ?: item.poster
    } else item.poster
    val fallbackImageUrl = if (isLandscapeMode && !effectiveLandscapePoster.isNullOrBlank()) {
        // Landscape custom poster -> fall back to original backdrop, then portrait
        item.banner ?: item.rawPosterUrl
    } else {
        item.rawPosterUrl
    }

    HomePosterHoverPreview(
        item = item,
        isWatched = isWatched,
        onClick = onClick,
        onLongClick = onLongClick,
    ) { hoverModifier ->
        CineboxPosterCard(
            title = item.name,
            imageUrl = imageUrl,
            modifier = modifier.then(hoverModifier),
            fallbackImageUrl = fallbackImageUrl,
            basePosterWidthDp = desktopCatalogShelfPosterBaseWidthDp(posterCardStyle.widthDp),
            shape = if (isLandscapeMode) CineboxPosterShape.Landscape else item.posterShape.toCineboxPosterShape(),
            detailLine = if (isLandscapeMode || posterCardStyle.hideLabelsEnabled) null else item.releaseInfo?.let { formatReleaseDateForDisplay(it) },
            showTitleBelow = !posterCardStyle.hideLabelsEnabled,
            bottomLeftLogoUrl = if (isLandscapeMode && showLandscapeOverlay && effectiveLandscapePoster.isNullOrBlank()) item.logo else null,
            bottomLeftText = if (isLandscapeMode && showLandscapeOverlay && effectiveLandscapePoster.isNullOrBlank() && item.logo.isNullOrBlank() && !posterCardStyle.hideLabelsEnabled) item.name else null,
            isWatched = isWatched,
            onClick = onClick,
            onLongClick = onLongClick,
        )
    }
}

private fun PosterShape.toCineboxPosterShape(): CineboxPosterShape =
    when (this) {
        PosterShape.Poster -> CineboxPosterShape.Poster
        PosterShape.Square -> CineboxPosterShape.Square
        PosterShape.Landscape -> CineboxPosterShape.Landscape
    }
