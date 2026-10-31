package com.cinebox.app.core.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip

@Composable
fun CineboxTokenPreviewSurface(
    modifier: Modifier = Modifier,
    themes: List<AppTheme> = AppTheme.entries,
) {
    LazyColumn(
        modifier = modifier.background(MaterialTheme.cinebox.colors.background),
        contentPadding = PaddingValues(MaterialTheme.cinebox.spacing.screenHorizontal),
        verticalArrangement = Arrangement.spacedBy(MaterialTheme.cinebox.spacing.sectionGap),
    ) {
        items(themes.size) { index ->
            val theme = themes[index]
            CineboxTheme(appTheme = theme) {
                TokenPreviewThemeSection(theme = theme)
            }
        }
    }
}

@Composable
private fun TokenPreviewThemeSection(theme: AppTheme) {
    val tokens = MaterialTheme.cinebox
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(tokens.shapes.card)
            .background(tokens.colors.surface)
            .border(tokens.borders.thin, tokens.colors.borderSubtle, tokens.shapes.card)
            .padding(tokens.spacing.cardPadding),
        verticalArrangement = Arrangement.spacedBy(tokens.spacing.listGap),
    ) {
        Text(
            text = theme.name,
            style = MaterialTheme.cineboxTypeScale.titleMd,
            color = tokens.colors.textPrimary,
        )
        Row(horizontalArrangement = Arrangement.spacedBy(tokens.spacing.controlGap)) {
            listOf(
                tokens.colors.background,
                tokens.colors.surface,
                tokens.colors.surfaceCard,
                tokens.colors.accent,
                tokens.colors.success,
                tokens.colors.warning,
                tokens.colors.danger,
            ).forEach { color ->
                Box(
                    modifier = Modifier
                        .size(CineboxTokens.Icon.xl)
                        .clip(RoundedCornerShape(CineboxTokens.Radius.sm))
                        .background(color)
                        .border(tokens.borders.hairline, tokens.colors.borderDefault, RoundedCornerShape(CineboxTokens.Radius.sm)),
                )
            }
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(CineboxTokens.Space.s8)
                .clip(tokens.shapes.chip)
                .background(tokens.colors.accent),
        )
    }
}
