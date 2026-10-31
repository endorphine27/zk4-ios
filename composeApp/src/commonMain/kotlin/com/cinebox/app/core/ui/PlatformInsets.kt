package com.cinebox.app.core.ui

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

internal expect val cineboxPlatformExtraTopPadding: Dp
internal expect val cineboxPlatformExtraBottomPadding: Dp
internal expect val cineboxBottomNavigationExtraVerticalPadding: Dp
@Composable
internal expect fun cineboxBottomNavigationBarInsets(): WindowInsets

/** Physical display-safe top inset, excluding any enclosing native toolbar. */
@Composable
internal expect fun platformPhysicalTopInset(): Dp

@Composable
internal expect fun ResizeForKeyboard()

internal val LocalCineboxBottomNavigationOverlayPadding = staticCompositionLocalOf { 0.dp }

internal val LocalBottomInsetsConsumed = staticCompositionLocalOf { false }

/** CompositionLocal providing the shared [CineboxNavBarScrollState] so child screens can attach the nestedScrollConnection. */
val LocalCineboxNavBarScrollState = staticCompositionLocalOf<CineboxNavBarScrollState?> { null }

@Composable
internal fun cineboxSafeBottomPadding(extra: Dp = 0.dp): Dp {
	if (LocalBottomInsetsConsumed.current) return extra
	val navigationBarBottom = cineboxBottomNavigationBarInsets()
		.asPaddingValues()
		.calculateBottomPadding()
	return navigationBarBottom.coerceAtLeast(cineboxPlatformExtraBottomPadding) +
		LocalCineboxBottomNavigationOverlayPadding.current +
		extra
}
