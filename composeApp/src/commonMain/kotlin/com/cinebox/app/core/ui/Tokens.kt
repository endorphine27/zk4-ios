package com.cinebox.app.core.ui

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.Easing
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

object CineboxTokens {
    object Space {
        val none = 0.dp
        val hairline = 0.5.dp
        val s1 = 1.dp
        val s2 = 2.dp
        val s3 = 3.dp
        val s4 = 4.dp
        val s5 = 5.dp
        val s6 = 6.dp
        val s7 = 7.dp
        val s8 = 8.dp
        val s10 = 10.dp
        val s12 = 12.dp
        val s14 = 14.dp
        val s16 = 16.dp
        val s18 = 18.dp
        val s20 = 20.dp
        val s22 = 22.dp
        val s24 = 24.dp
        val s28 = 28.dp
        val s32 = 32.dp
        val s36 = 36.dp
        val s40 = 40.dp
        val s48 = 48.dp
        val s56 = 56.dp
        val s64 = 64.dp
        val s72 = 72.dp
        val s80 = 80.dp
        val s96 = 96.dp
    }

    object Radius {
        val none = Space.none
        val xs = Space.s4
        val sm = Space.s6
        val md = Space.s8
        val lg = Space.s12
        val xl = Space.s16
        val xxl = Space.s24
        val full = 999.dp

        val card = xxl
        val compactCard = lg
        val sheet = xxl
        val dialog = Space.s28
        val button = xl
        val chip = full
        val poster = lg
        val avatar = full
        val playerPanel = xxl
    }

    object Border {
        val hairline = Space.hairline
        val thin = Space.s1
        val medium = Space.s2
    }

    object Elevation {
        val none = Space.none
        val raised = Space.s2
        val modal = Space.s8
        val overlay = Space.s12
        val playerControls = Space.s4
    }

    object Opacity {
        const val invisible = 0f
        const val disabled = 0.38f
        const val secondary = 0.70f
        const val muted = 0.60f
        const val selected = 0.15f
        const val hover = 0.08f
        const val pressed = 0.12f
        const val subtle = 0.06f
        const val medium = 0.52f
        const val strong = 0.75f
        const val overlayLight = 0.35f
        const val overlayMedium = 0.56f
        const val overlayHeavy = 0.82f
        const val visible = 1f
    }

    object Motion {
        const val instantMillis = 0
        const val fastMillis = 150
        const val normalMillis = 220
        const val sheetEnterMillis = 300
        const val sheetExitMillis = 250
        const val slowMillis = 400
        const val cinematicMillis = 700

        val standard: Easing = CubicBezierEasing(0.2f, 0f, 0f, 1f)
        val emphasized: Easing = CubicBezierEasing(0.2f, 0f, 0f, 1f)
        val decelerate: Easing = CubicBezierEasing(0f, 0f, 0f, 1f)
        val accelerate: Easing = CubicBezierEasing(0.3f, 0f, 1f, 1f)
    }

    object Icon {
        val xs = Space.s12
        val sm = Space.s16
        val md = Space.s20
        val lg = Space.s24
        val xl = Space.s32
        val xxl = Space.s40
    }

    object Type {
        val labelXs = 11.sp
        val labelSm = 12.sp
        val bodySm = 13.sp
        val bodyMd = 14.sp
        val bodyApp = 15.sp
        val bodyLg = 16.sp
        val titleSm = 18.sp
        val headline = 26.sp
        val titleMd = 22.sp
        val titleLg = 28.sp
        val displaySm = 32.sp
        val pageDisplay = 38.sp
        val displayMd = 48.sp
    }

    object LineHeight {
        val labelXs = 14.sp
        val labelSm = 15.sp
        val bodySm = 18.sp
        val bodyMd = 20.sp
        val bodyApp = 22.sp
        val bodyLg = 22.sp
        val titleSm = 22.sp
        val materialTitleLarge = 24.sp
        val headline = 30.sp
        val titleMd = 26.sp
        val titleLg = 32.sp
        val displaySm = 36.sp
        val pageDisplay = 42.sp
        val displayMd = 52.sp
    }

    object LetterSpacing {
        val none = 0.sp
        val pageDisplay = (-1.2).sp
        val headline = (-0.8).sp
        val label = 0.8.sp
    }

    object Breakpoint {
        val phone = 0.dp
        val largePhone = 420.dp
        val tablet = 600.dp
        val largeTablet = 840.dp
        val desktop = 1024.dp
        val playerWide = 1280.dp
    }

    object Z {
        const val base = 0f
        const val stickyHeader = 2f
        const val navigation = 4f
        const val sheet = 8f
        const val dialog = 10f
        const val playerOverlay = 12f
        const val toast = 16f
    }
}

@Immutable
data class CineboxColorTokens(
    val background: Color,
    val backgroundAmoled: Color,
    val surface: Color,
    val surfaceElevated: Color,
    val surfaceCard: Color,
    val surfaceSheet: Color,
    val surfaceDialog: Color,
    val surfacePopover: Color,
    val nativeChrome: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val textMuted: Color,
    val textDisabled: Color,
    val textInverse: Color,
    val accent: Color,
    val accentStrong: Color,
    val onAccent: Color,
    val focusRing: Color,
    val focusBackground: Color,
    val borderSubtle: Color,
    val borderDefault: Color,
    val borderStrong: Color,
    val borderFocus: Color,
    val borderSelected: Color,
    val success: Color,
    val warning: Color,
    val danger: Color,
    val info: Color,
    val neutral: Color,
    val overlayScrim: Color,
    val overlayHover: Color,
    val overlayPressed: Color,
    val overlaySelected: Color,
    val overlayDisabled: Color,
    val shimmer: Color,
    val skeleton: Color,
    val playerControlsBackground: Color,
    val playerControlsForeground: Color,
    val playerTimelineTrack: Color,
    val playerTimelineFill: Color,
    val playerSubtitlePreview: Color,
    val playerBuffering: Color,
    val parentalGuide: Color,
)

@Immutable
data class CineboxShapeTokens(
    val card: Shape,
    val compactCard: Shape,
    val sheet: Shape,
    val dialog: Shape,
    val button: Shape,
    val chip: Shape,
    val poster: Shape,
    val avatar: Shape,
    val playerPanel: Shape,
)

@Immutable
data class CineboxSpacingTokens(
    val screenHorizontal: Dp,
    val screenTop: Dp,
    val screenBottom: Dp,
    val sectionGap: Dp,
    val listGap: Dp,
    val railGap: Dp,
    val cardPadding: Dp,
    val cardPaddingCompact: Dp,
    val controlGap: Dp,
    val dialogPadding: Dp,
    val sheetPadding: Dp,
    val playerOverlayPadding: Dp,
)

@Immutable
data class CineboxBorderTokens(
    val hairline: Dp,
    val thin: Dp,
    val medium: Dp,
)

@Immutable
data class CineboxElevationTokens(
    val flat: Dp,
    val raised: Dp,
    val modal: Dp,
    val overlay: Dp,
    val playerControls: Dp,
)

@Immutable
data class CineboxMotionTokens(
    val instantMillis: Int,
    val fastMillis: Int,
    val normalMillis: Int,
    val sheetEnterMillis: Int,
    val sheetExitMillis: Int,
    val slowMillis: Int,
    val cinematicMillis: Int,
    val standard: Easing,
    val emphasized: Easing,
    val decelerate: Easing,
    val accelerate: Easing,
)

@Immutable
data class CineboxOpacityTokens(
    val invisible: Float,
    val disabled: Float,
    val secondary: Float,
    val muted: Float,
    val selected: Float,
    val hover: Float,
    val pressed: Float,
    val subtle: Float,
    val medium: Float,
    val strong: Float,
    val overlayLight: Float,
    val overlayMedium: Float,
    val overlayHeavy: Float,
    val visible: Float,
)

@Immutable
data class CineboxIconTokens(
    val xs: Dp,
    val sm: Dp,
    val md: Dp,
    val lg: Dp,
    val xl: Dp,
    val xxl: Dp,
)

@Immutable
data class CineboxBreakpointTokens(
    val phone: Dp,
    val largePhone: Dp,
    val tablet: Dp,
    val largeTablet: Dp,
    val desktop: Dp,
    val playerWide: Dp,
)

@Immutable
data class CineboxComponentTokens(
    val navItemShape: Shape,
    val navIconSize: Dp,
    val navItemMaxWidth: Dp,
    val sheetMaxWidth: Dp,
    val dialogMaxWidth: Dp,
    val chipHorizontalPadding: Dp,
    val chipVerticalPadding: Dp,
    val posterRadius: Dp,
    val avatarSize: Dp,
    val playerPanelMaxWidth: Dp,
)

@Immutable
data class CineboxThemeTokens(
    val colors: CineboxColorTokens,
    val spacing: CineboxSpacingTokens,
    val shapes: CineboxShapeTokens,
    val borders: CineboxBorderTokens,
    val elevation: CineboxElevationTokens,
    val motion: CineboxMotionTokens,
    val opacity: CineboxOpacityTokens,
    val icons: CineboxIconTokens,
    val breakpoints: CineboxBreakpointTokens,
    val components: CineboxComponentTokens,
)

internal val LocalNuvioThemeTokens = staticCompositionLocalOf {
    defaultNuvioThemeTokens(ThemeColors.White, amoled = false, colorScheme = null)
}

val MaterialTheme.nuvio: CineboxThemeTokens
    @Composable
    @Stable
    get() = LocalNuvioThemeTokens.current

internal fun defaultNuvioThemeTokens(
    palette: ThemeColorPalette,
    amoled: Boolean,
    colorScheme: ColorScheme?,
): CineboxThemeTokens {
    val background = if (amoled) Color.Black else palette.background
    val textPrimary = Color(0xFFF5F7F8)
    val textSecondary = Color(0xFFB8BEC5)
    val textMuted = Color(0xFF969CA3)
    val surface = palette.backgroundElevated
    val surfaceCard = palette.backgroundCard
    val accent = palette.secondary
    val accentStrong = palette.secondaryVariant
    val borderSubtle = Color(0xFF252A2A).copy(alpha = 0.55f)
    val borderDefault = Color(0xFF252A2A)
    val overlayScrim = Color.Black.copy(alpha = CineboxTokens.Opacity.overlayMedium)

    return CineboxThemeTokens(
        colors = CineboxColorTokens(
            background = background,
            backgroundAmoled = Color.Black,
            surface = surface,
            surfaceElevated = surface,
            surfaceCard = surfaceCard,
            surfaceSheet = surface,
            surfaceDialog = surface,
            surfacePopover = surfaceCard,
            nativeChrome = background,
            textPrimary = textPrimary,
            textSecondary = textSecondary,
            textMuted = textMuted,
            textDisabled = textMuted.copy(alpha = CineboxTokens.Opacity.disabled),
            textInverse = Color(0xFF111111),
            accent = accent,
            accentStrong = accentStrong,
            onAccent = palette.onSecondary,
            focusRing = palette.focusRing,
            focusBackground = palette.focusBackground,
            borderSubtle = borderSubtle,
            borderDefault = borderDefault,
            borderStrong = Color(0xFF3A4040),
            borderFocus = palette.focusRing,
            borderSelected = accent.copy(alpha = CineboxTokens.Opacity.strong),
            success = Color(0xFF66BB6A),
            warning = Color(0xFFFFC857),
            danger = colorScheme?.error ?: Color(0xFFE36A8A),
            info = Color(0xFF42A5F5),
            neutral = textMuted,
            overlayScrim = overlayScrim,
            overlayHover = Color.White.copy(alpha = CineboxTokens.Opacity.hover),
            overlayPressed = Color.White.copy(alpha = CineboxTokens.Opacity.pressed),
            overlaySelected = Color.White.copy(alpha = CineboxTokens.Opacity.selected),
            overlayDisabled = Color.Black.copy(alpha = CineboxTokens.Opacity.disabled),
            shimmer = Color.White.copy(alpha = 0.10f),
            skeleton = Color.White.copy(alpha = 0.06f),
            playerControlsBackground = Color.Black.copy(alpha = 0.72f),
            playerControlsForeground = Color.White,
            playerTimelineTrack = Color.White.copy(alpha = 0.30f),
            playerTimelineFill = accent,
            playerSubtitlePreview = Color.Black.copy(alpha = 0.55f),
            playerBuffering = accent,
            parentalGuide = Color(0xFF5D1F1F),
        ),
        spacing = CineboxSpacingTokens(
            screenHorizontal = CineboxTokens.Space.s16,
            screenTop = CineboxTokens.Space.s10,
            screenBottom = CineboxTokens.Space.s18,
            sectionGap = CineboxTokens.Space.s24,
            listGap = CineboxTokens.Space.s12,
            railGap = CineboxTokens.Space.s14,
            cardPadding = CineboxTokens.Space.s18,
            cardPaddingCompact = CineboxTokens.Space.s14,
            controlGap = CineboxTokens.Space.s8,
            dialogPadding = CineboxTokens.Space.s20,
            sheetPadding = CineboxTokens.Space.s20,
            playerOverlayPadding = CineboxTokens.Space.s24,
        ),
        shapes = CineboxShapeTokens(
            card = RoundedCornerShape(CineboxTokens.Radius.card),
            compactCard = RoundedCornerShape(CineboxTokens.Radius.compactCard),
            sheet = RoundedCornerShape(CineboxTokens.Radius.sheet),
            dialog = RoundedCornerShape(CineboxTokens.Radius.dialog),
            button = RoundedCornerShape(CineboxTokens.Radius.button),
            chip = RoundedCornerShape(CineboxTokens.Radius.chip),
            poster = RoundedCornerShape(CineboxTokens.Radius.poster),
            avatar = RoundedCornerShape(CineboxTokens.Radius.avatar),
            playerPanel = RoundedCornerShape(CineboxTokens.Radius.playerPanel),
        ),
        borders = CineboxBorderTokens(
            hairline = CineboxTokens.Border.hairline,
            thin = CineboxTokens.Border.thin,
            medium = CineboxTokens.Border.medium,
        ),
        elevation = CineboxElevationTokens(
            flat = CineboxTokens.Elevation.none,
            raised = CineboxTokens.Elevation.raised,
            modal = CineboxTokens.Elevation.modal,
            overlay = CineboxTokens.Elevation.overlay,
            playerControls = CineboxTokens.Elevation.playerControls,
        ),
        motion = CineboxMotionTokens(
            instantMillis = CineboxTokens.Motion.instantMillis,
            fastMillis = CineboxTokens.Motion.fastMillis,
            normalMillis = CineboxTokens.Motion.normalMillis,
            sheetEnterMillis = CineboxTokens.Motion.sheetEnterMillis,
            sheetExitMillis = CineboxTokens.Motion.sheetExitMillis,
            slowMillis = CineboxTokens.Motion.slowMillis,
            cinematicMillis = CineboxTokens.Motion.cinematicMillis,
            standard = CineboxTokens.Motion.standard,
            emphasized = CineboxTokens.Motion.emphasized,
            decelerate = CineboxTokens.Motion.decelerate,
            accelerate = CineboxTokens.Motion.accelerate,
        ),
        opacity = CineboxOpacityTokens(
            invisible = CineboxTokens.Opacity.invisible,
            disabled = CineboxTokens.Opacity.disabled,
            secondary = CineboxTokens.Opacity.secondary,
            muted = CineboxTokens.Opacity.muted,
            selected = CineboxTokens.Opacity.selected,
            hover = CineboxTokens.Opacity.hover,
            pressed = CineboxTokens.Opacity.pressed,
            subtle = CineboxTokens.Opacity.subtle,
            medium = CineboxTokens.Opacity.medium,
            strong = CineboxTokens.Opacity.strong,
            overlayLight = CineboxTokens.Opacity.overlayLight,
            overlayMedium = CineboxTokens.Opacity.overlayMedium,
            overlayHeavy = CineboxTokens.Opacity.overlayHeavy,
            visible = CineboxTokens.Opacity.visible,
        ),
        icons = CineboxIconTokens(
            xs = CineboxTokens.Icon.xs,
            sm = CineboxTokens.Icon.sm,
            md = CineboxTokens.Icon.md,
            lg = CineboxTokens.Icon.lg,
            xl = CineboxTokens.Icon.xl,
            xxl = CineboxTokens.Icon.xxl,
        ),
        breakpoints = CineboxBreakpointTokens(
            phone = CineboxTokens.Breakpoint.phone,
            largePhone = CineboxTokens.Breakpoint.largePhone,
            tablet = CineboxTokens.Breakpoint.tablet,
            largeTablet = CineboxTokens.Breakpoint.largeTablet,
            desktop = CineboxTokens.Breakpoint.desktop,
            playerWide = CineboxTokens.Breakpoint.playerWide,
        ),
        components = CineboxComponentTokens(
            navItemShape = RoundedCornerShape(CineboxTokens.Radius.xl),
            navIconSize = CineboxTokens.Icon.xl,
            navItemMaxWidth = 150.dp,
            sheetMaxWidth = 520.dp,
            dialogMaxWidth = 460.dp,
            chipHorizontalPadding = CineboxTokens.Space.s14,
            chipVerticalPadding = CineboxTokens.Space.s8,
            posterRadius = CineboxTokens.Radius.poster,
            avatarSize = CineboxTokens.Space.s48,
            playerPanelMaxWidth = 600.dp,
        ),
    )
}
