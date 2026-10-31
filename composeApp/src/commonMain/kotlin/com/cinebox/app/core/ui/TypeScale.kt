package com.cinebox.app.core.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight

data class CineboxTypeScale(
    val labelXs: TextStyle,
    val labelSm: TextStyle,
    val bodySm: TextStyle,
    val bodyMd: TextStyle,
    val bodyLg: TextStyle,
    val titleSm: TextStyle,
    val titleMd: TextStyle,
    val titleLg: TextStyle,
    val displaySm: TextStyle,
    val displayMd: TextStyle,
)

internal val LocalCineboxTypeScale = staticCompositionLocalOf {
    CineboxTypeScale(
        labelXs = TextStyle(fontSize = CineboxTokens.Type.labelXs, lineHeight = CineboxTokens.LineHeight.labelXs, fontWeight = FontWeight.Medium),
        labelSm = TextStyle(fontSize = CineboxTokens.Type.labelSm, lineHeight = CineboxTokens.LineHeight.labelSm, fontWeight = FontWeight.Medium),
        bodySm = TextStyle(fontSize = CineboxTokens.Type.bodySm, lineHeight = CineboxTokens.LineHeight.bodySm, fontWeight = FontWeight.Normal),
        bodyMd = TextStyle(fontSize = CineboxTokens.Type.bodyMd, lineHeight = CineboxTokens.LineHeight.bodyMd, fontWeight = FontWeight.Normal),
        bodyLg = TextStyle(fontSize = CineboxTokens.Type.bodyLg, lineHeight = CineboxTokens.LineHeight.bodyLg, fontWeight = FontWeight.Medium),
        titleSm = TextStyle(fontSize = CineboxTokens.Type.titleSm, lineHeight = CineboxTokens.LineHeight.titleSm, fontWeight = FontWeight.Bold),
        titleMd = TextStyle(fontSize = CineboxTokens.Type.titleMd, lineHeight = CineboxTokens.LineHeight.titleMd, fontWeight = FontWeight.Bold),
        titleLg = TextStyle(fontSize = CineboxTokens.Type.titleLg, lineHeight = CineboxTokens.LineHeight.titleLg, fontWeight = FontWeight.Bold),
        displaySm = TextStyle(fontSize = CineboxTokens.Type.displaySm, lineHeight = CineboxTokens.LineHeight.displaySm, fontWeight = FontWeight.ExtraBold),
        displayMd = TextStyle(fontSize = CineboxTokens.Type.displayMd, lineHeight = CineboxTokens.LineHeight.displayMd, fontWeight = FontWeight.ExtraBold),
    )
}

val MaterialTheme.cineboxTypeScale: CineboxTypeScale
    @Composable
    get() = LocalCineboxTypeScale.current
