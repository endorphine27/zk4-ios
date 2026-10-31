package com.cinebox.app.core.ui

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

@Composable
internal actual fun CineboxDesktopVerticalScrollbar(
    state: LazyListState,
    modifier: Modifier,
    backgroundColor: Color?,
) = Unit

@Composable
internal actual fun CineboxDesktopVerticalScrollbar(
    state: LazyGridState,
    modifier: Modifier,
    backgroundColor: Color?,
) = Unit

@Composable
internal actual fun CineboxDesktopVerticalScrollbar(
    state: ScrollState,
    modifier: Modifier,
    backgroundColor: Color?,
) = Unit
