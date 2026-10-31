package com.cinebox.app.features.home.components

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.cinebox.app.core.ui.catalogPosterBaseWidthDp
import com.cinebox.app.core.ui.desktopPageHorizontalPaddingForWidth
import com.cinebox.app.core.ui.landscapePosterWidth
import com.cinebox.app.isDesktop
import kotlin.math.ceil

private const val HomeCatalogBaselinePreviewLimit = 18
private const val HomeCatalogItemSpacingDp = 10f

internal fun homeSectionHorizontalPaddingForWidth(maxWidthDp: Float): Dp =
    desktopPageHorizontalPaddingForWidth(maxWidthDp)

internal fun homeCatalogPreviewLimitForWidth(
    maxWidthDp: Float,
    sectionPadding: Dp,
    basePosterWidthDp: Int,
    useLandscapeMode: Boolean,
    useDesktopSizing: Boolean = isDesktop,
): Int {
    val posterWidthDp = catalogPosterBaseWidthDp(basePosterWidthDp, useDesktopSizing).let {
        if (useLandscapeMode) landscapePosterWidth(it).value else it.toFloat()
    }
    val availableWidthDp = (maxWidthDp - sectionPadding.value * 2f).coerceAtLeast(0f)
    val visibleItems = ceil(
        (availableWidthDp + HomeCatalogItemSpacingDp) /
            (posterWidthDp + HomeCatalogItemSpacingDp),
    ).toInt()
    return maxOf(HomeCatalogBaselinePreviewLimit, visibleItems)
}
