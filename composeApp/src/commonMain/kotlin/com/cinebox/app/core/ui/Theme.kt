package com.cinebox.app.core.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.LocalRippleConfiguration
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RippleConfiguration
import androidx.compose.material3.Typography
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Density
import com.cinebox.app.isDesktop
import cinebox.composeapp.generated.resources.Res
import cinebox.composeapp.generated.resources.jetbrains_sans_bold
import cinebox.composeapp.generated.resources.jetbrains_sans_regular
import cinebox.composeapp.generated.resources.jetbrains_sans_semibold
import org.jetbrains.compose.resources.Font

val LocalAppTheme = staticCompositionLocalOf { AppTheme.WHITE }
val LocalThemePalette = staticCompositionLocalOf { ThemeColors.White }

val MaterialTheme.themePalette: ThemeColorPalette
    @Composable
    @ReadOnlyComposable
    get() = LocalThemePalette.current

internal val LocalCineboxPlatformDensity = staticCompositionLocalOf<Density> {
    error("Platform density is unavailable outside CineboxTheme")
}

val MaterialTheme.appTheme: AppTheme
    @Composable
    @ReadOnlyComposable
    get() = LocalAppTheme.current

private fun contentColorFor(background: Color): Color =
    if (background.luminance() > 0.5f) Color(0xFF111111) else Color(0xFFF5F7F8)

private fun buildColorScheme(palette: ThemeColorPalette, amoled: Boolean = false) = darkColorScheme(
    primary = palette.secondary,
    onPrimary = palette.onSecondary,
    primaryContainer = palette.focusBackground,
    onPrimaryContainer = contentColorFor(palette.focusBackground),
    secondary = palette.secondaryVariant,
    onSecondary = palette.onSecondaryVariant,
    background = if (amoled) Color.Black else palette.background,
    onBackground = Color(0xFFF5F7F8),
    surface = palette.backgroundElevated,
    onSurface = Color(0xFFF5F7F8),
    surfaceVariant = palette.backgroundCard,
    onSurfaceVariant = Color(0xFF969CA3),
    outline = Color(0xFF252A2A),
    error = Color(0xFFE36A8A),
    onError = Color(0xFFFCE5EC),
)

private val JetBrainsSans: FontFamily
    @Composable
    get() = FontFamily(
        Font(Res.font.jetbrains_sans_bold, FontWeight.Bold, FontStyle.Normal),
        Font(Res.font.jetbrains_sans_semibold, FontWeight.SemiBold, FontStyle.Normal),
        Font(Res.font.jetbrains_sans_regular, FontWeight.Normal, FontStyle.Normal),
    )

private val CineboxTypography: Typography
    @Composable
    get() = Typography(
        displayLarge = TextStyle(
            fontFamily = JetBrainsSans,
            fontSize = CineboxTokens.Type.pageDisplay,
            lineHeight = CineboxTokens.LineHeight.pageDisplay,
            fontWeight = FontWeight.Bold,
            letterSpacing = CineboxTokens.LetterSpacing.pageDisplay,
        ),
        headlineLarge = TextStyle(
            fontFamily = JetBrainsSans,
            fontSize = CineboxTokens.Type.headline,
            lineHeight = CineboxTokens.LineHeight.headline,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = CineboxTokens.LetterSpacing.headline,
        ),
        titleLarge = TextStyle(
            fontFamily = JetBrainsSans,
            fontSize = CineboxTokens.Type.titleSm,
            lineHeight = CineboxTokens.LineHeight.materialTitleLarge,
            fontWeight = FontWeight.SemiBold,
        ),
        titleMedium = TextStyle(
            fontFamily = JetBrainsSans,
            fontSize = CineboxTokens.Type.bodyLg,
            lineHeight = CineboxTokens.LineHeight.bodyMd,
            fontWeight = FontWeight.SemiBold,
        ),
        bodyLarge = TextStyle(
            fontFamily = JetBrainsSans,
            fontSize = CineboxTokens.Type.bodyApp,
            lineHeight = CineboxTokens.LineHeight.bodyApp,
            fontWeight = FontWeight.Normal,
        ),
        bodyMedium = TextStyle(
            fontFamily = JetBrainsSans,
            fontSize = CineboxTokens.Type.bodyMd,
            lineHeight = CineboxTokens.LineHeight.bodyMd,
            fontWeight = FontWeight.Normal,
        ),
        labelLarge = TextStyle(
            fontFamily = JetBrainsSans,
            fontSize = CineboxTokens.Type.bodyMd,
            lineHeight = CineboxTokens.LineHeight.bodySm,
            fontWeight = FontWeight.SemiBold,
        ),
        labelMedium = TextStyle(
            fontFamily = JetBrainsSans,
            fontSize = CineboxTokens.Type.labelSm,
            lineHeight = CineboxTokens.LineHeight.labelXs,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = CineboxTokens.LetterSpacing.label,
        ),
    )

private val CineboxTypeTokens: CineboxTypeScale
    @Composable
    get() = CineboxTypeScale(
        labelXs = TextStyle(
            fontFamily = JetBrainsSans,
            fontSize = CineboxTokens.Type.labelXs,
            lineHeight = CineboxTokens.LineHeight.labelXs,
            fontWeight = FontWeight.SemiBold,
        ),
        labelSm = TextStyle(
            fontFamily = JetBrainsSans,
            fontSize = CineboxTokens.Type.labelSm,
            lineHeight = CineboxTokens.LineHeight.labelSm,
            fontWeight = FontWeight.SemiBold,
        ),
        bodySm = TextStyle(
            fontFamily = JetBrainsSans,
            fontSize = CineboxTokens.Type.bodySm,
            lineHeight = CineboxTokens.LineHeight.bodySm,
            fontWeight = FontWeight.Normal,
        ),
        bodyMd = TextStyle(
            fontFamily = JetBrainsSans,
            fontSize = CineboxTokens.Type.bodyMd,
            lineHeight = CineboxTokens.LineHeight.bodyMd,
            fontWeight = FontWeight.Normal,
        ),
        bodyLg = TextStyle(
            fontFamily = JetBrainsSans,
            fontSize = CineboxTokens.Type.bodyLg,
            lineHeight = CineboxTokens.LineHeight.bodyLg,
            fontWeight = FontWeight.Normal,
        ),
        titleSm = TextStyle(
            fontFamily = JetBrainsSans,
            fontSize = CineboxTokens.Type.titleSm,
            lineHeight = CineboxTokens.LineHeight.titleSm,
            fontWeight = FontWeight.SemiBold,
        ),
        titleMd = TextStyle(
            fontFamily = JetBrainsSans,
            fontSize = CineboxTokens.Type.titleMd,
            lineHeight = CineboxTokens.LineHeight.titleMd,
            fontWeight = FontWeight.SemiBold,
        ),
        titleLg = TextStyle(
            fontFamily = JetBrainsSans,
            fontSize = CineboxTokens.Type.titleLg,
            lineHeight = CineboxTokens.LineHeight.titleLg,
            fontWeight = FontWeight.SemiBold,
        ),
        displaySm = TextStyle(
            fontFamily = JetBrainsSans,
            fontSize = CineboxTokens.Type.displaySm,
            lineHeight = CineboxTokens.LineHeight.displaySm,
            fontWeight = FontWeight.Bold,
        ),
        displayMd = TextStyle(
            fontFamily = JetBrainsSans,
            fontSize = CineboxTokens.Type.displayMd,
            lineHeight = CineboxTokens.LineHeight.displayMd,
            fontWeight = FontWeight.Bold,
        ),
    )

private val CineboxRippleConfiguration = RippleConfiguration(
    color = Color.Black,
)

private const val CineboxDesktopFontScale = 1.08f
private const val CineboxDesktopBaseWidthDp = 1280f
private const val CineboxDesktopBaseHeightDp = 820f
private const val CineboxDesktopMinUiScale = 1f
private const val CineboxDesktopMaxUiScale = 1.18f

internal fun desktopUiScaleForWindow(widthDp: Float, heightDp: Float): Float {
    if (!isDesktop || widthDp <= 0f || heightDp <= 0f) return CineboxDesktopMinUiScale

    val rawScale = minOf(
        widthDp / CineboxDesktopBaseWidthDp,
        heightDp / CineboxDesktopBaseHeightDp,
    )
    return rawScale.coerceIn(CineboxDesktopMinUiScale, CineboxDesktopMaxUiScale)
}

@Composable
fun CineboxTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    appTheme: AppTheme = AppTheme.WHITE,
    amoled: Boolean = false,
    desktopUiScale: Float = CineboxDesktopMinUiScale,
    customThemeColors: CustomThemeColors = CustomThemeColors.Default,
    content: @Composable () -> Unit,
) {
    val palette = remember(appTheme, customThemeColors) {
        ThemeColors.getColorPalette(appTheme, customThemeColors)
    }
    val colorScheme = buildColorScheme(palette, amoled = amoled)
    val tokens = defaultCineboxThemeTokens(palette, amoled = amoled, colorScheme = colorScheme)

    val density = LocalDensity.current
    val effectiveDesktopUiScale = if (isDesktop) {
        desktopUiScale.coerceIn(CineboxDesktopMinUiScale, CineboxDesktopMaxUiScale)
    } else {
        CineboxDesktopMinUiScale
    }
    CompositionLocalProvider(
        LocalCineboxPlatformDensity provides density,
        LocalDensity provides Density(
            density = density.density * effectiveDesktopUiScale,
            fontScale = if (isDesktop) CineboxDesktopFontScale else 1f,
        ),
        LocalCineboxThemeTokens provides tokens,
        LocalCineboxTypeScale provides CineboxTypeTokens,
        LocalRippleConfiguration provides CineboxRippleConfiguration,
        LocalAppTheme provides appTheme,
        LocalThemePalette provides palette,
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = CineboxTypography,
        ) {
            SkeletonAnimationProvider(content = content)
        }
    }
}
