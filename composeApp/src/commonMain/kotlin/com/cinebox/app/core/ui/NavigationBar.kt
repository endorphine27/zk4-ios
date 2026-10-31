package com.cinebox.app.core.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeEffect
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource

/**
 * Scroll-aware state for the floating navigation bar.
 * Tracks scroll direction and exposes a label visibility fraction (1 = fully visible, 0 = hidden).
 */
import com.cinebox.app.AppScreenTab
import com.cinebox.app.features.settings.NavBarStyle

@Stable
class CineboxNavBarScrollState {
    /** 1f = labels fully visible (expanded), 0f = labels hidden (collapsed, icons only) */
    var labelVisibility by mutableFloatStateOf(1f)
        private set

    /** Accumulated scroll distance from top of page for current active tab */
    var totalScrollOffset by mutableFloatStateOf(0f)
        private set

    private var currentTab: AppScreenTab = AppScreenTab.Home
    private val tabScrollOffsets = mutableMapOf<AppScreenTab, Float>()
    private var accumulatedDelta = 0f

    /** Call when user switches tabs to restore that tab's scroll offset */
    fun switchToTab(tab: AppScreenTab) {
        currentTab = tab
        labelVisibility = 1f
        accumulatedDelta = 0f
        totalScrollOffset = tabScrollOffsets[tab] ?: 0f
    }

    /** Call to expand (show labels) – e.g. when user scrolls back to top */
    fun expand() {
        labelVisibility = 1f
        accumulatedDelta = 0f
    }

    /** Synchronize current scroll offset for a specific tab from child LazyColumn/scroll containers */
    fun updateScrollOffset(tab: AppScreenTab, offset: Float) {
        val newOffset = offset.coerceAtLeast(0f)
        tabScrollOffsets[tab] = newOffset
        if (tab == currentTab) {
            totalScrollOffset = newOffset
        }
    }

    /** Call to collapse (hide labels) */
    fun collapse() {
        labelVisibility = 0f
        accumulatedDelta = 0f
    }

    val nestedScrollConnection: NestedScrollConnection = object : NestedScrollConnection {
        override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
            val deltaY = available.y
            if (deltaY == 0f) return Offset.Zero

            accumulatedDelta += deltaY

            if (accumulatedDelta < -SCROLL_THRESHOLD && labelVisibility != 0f) {
                // Scrolling down past threshold → snap collapse
                labelVisibility = 0f
                accumulatedDelta = 0f
            } else if (accumulatedDelta > SCROLL_THRESHOLD && labelVisibility != 1f) {
                // Scrolling up past threshold → snap expand
                labelVisibility = 1f
                accumulatedDelta = 0f
            }

            // Reset accumulator if direction changed
            if (deltaY < 0f && accumulatedDelta > 0f) accumulatedDelta = deltaY
            if (deltaY > 0f && accumulatedDelta < 0f) accumulatedDelta = deltaY

            return Offset.Zero // Don't consume any scroll
        }
    }

    companion object {
        private const val SCROLL_THRESHOLD = 60f
    }
}

@Composable
fun rememberCineboxNavBarScrollState(): CineboxNavBarScrollState {
    return androidx.compose.runtime.remember { CineboxNavBarScrollState() }
}

/**
 * Floating pill-shaped navigation bar with scroll-responsive labels.
 *
 * @param hazeState Optional [HazeState] whose source is placed on the content behind this bar.
 *                  When provided, the pill gets a blur-through effect.
 */
@Composable
fun CineboxNavigationBar(
    modifier: Modifier = Modifier,
    scrollState: CineboxNavBarScrollState? = null,
    hazeState: HazeState? = null,
    navBarStyle: NavBarStyle = NavBarStyle.ADAPTIVE,
    contentPadding: PaddingValues = floatingNavigationBarPadding(),
    compactSize: Boolean = false,
    content: @Composable CineboxNavigationBarScope.() -> Unit,
) {
    val targetLabelFraction = when (navBarStyle) {
        NavBarStyle.EXPANDED -> 1f
        NavBarStyle.COMPACT -> 0f
        else -> scrollState?.labelVisibility ?: 1f
    }
    val labelFraction by animateFloatAsState(
        targetValue = targetLabelFraction,
        animationSpec = tween(
            durationMillis = CineboxTokens.Motion.sheetEnterMillis,
            easing = CineboxTokens.Motion.standard,
        ),
        label = "nav_label_alpha",
    )

    // Dynamic horizontal padding: pill shrinks when labels are hidden — driven by same labelFraction
    val expandedHorizontalPadding = 28.dp
    val collapsedHorizontalPadding = 58.dp
    val horizontalPadding = expandedHorizontalPadding + (collapsedHorizontalPadding - expandedHorizontalPadding) * (1f - labelFraction)

    // Outer container — no background, just safe padding
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(contentPadding),
        contentAlignment = Alignment.BottomCenter,
    ) {
        // The floating pill
        val pillModifier = Modifier
            .padding(horizontal = horizontalPadding)
            .fillMaxWidth()
            .clip(RoundedCornerShape(CineboxTokens.Radius.full))
            .then(
                if (hazeState != null) {
                    Modifier.hazeEffect(state = hazeState) {
                        blurRadius = 24.dp
                    }
                } else {
                    Modifier
                },
            )
            .background(Color(0xFF1C1C1E).copy(alpha = if (hazeState != null) 0.55f else 0.82f))

        Box(modifier = pillModifier) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        horizontal = CineboxTokens.Space.s6,
                        vertical = CineboxTokens.Space.s4,
                    ),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                CineboxNavigationBarScopeImpl(
                    rowScope = this,
                    labelFraction = labelFraction,
                    compactSize = compactSize,
                ).content()
            }
        }
    }
}

interface CineboxNavigationBarScope {
    @Composable
    fun NavItem(
        selected: Boolean,
        onClick: () -> Unit,
        icon: ImageVector,
        contentDescription: String?,
        modifier: Modifier = Modifier,
        label: String? = null,
    )

    @Composable
    fun NavItem(
        selected: Boolean,
        onClick: () -> Unit,
        icon: DrawableResource,
        contentDescription: String?,
        modifier: Modifier = Modifier,
        label: String? = null,
    )

    @Composable
    fun NavItem(
        selected: Boolean,
        onClick: () -> Unit,
        modifier: Modifier = Modifier,
        label: String? = null,
        content: @Composable () -> Unit,
    )
}

private class CineboxNavigationBarScopeImpl(
    private val rowScope: androidx.compose.foundation.layout.RowScope,
    private val labelFraction: Float,
    private val compactSize: Boolean,
) : CineboxNavigationBarScope {
    private val iconSize = if (compactSize) 24.dp else 28.dp
    private val itemVerticalPadding = if (compactSize) 4.dp else CineboxTokens.Space.s6

    @Composable
    override fun NavItem(
        selected: Boolean,
        onClick: () -> Unit,
        icon: ImageVector,
        contentDescription: String?,
        modifier: Modifier,
        label: String?,
    ) {
        val tokens = MaterialTheme.cinebox
        val palette = MaterialTheme.themePalette
        val iconColor by animateColorAsState(
            targetValue = if (selected) tokens.colors.accent else tokens.colors.textMuted,
            label = "nav_icon_color",
        )
        // Selected item gets a pill-shaped highlight using accent at low opacity
        val selectedBgColor by animateColorAsState(
            targetValue = if (selected) tokens.colors.accent.copy(alpha = CineboxTokens.Opacity.selected)
            else Color.Transparent,
            label = "nav_bg_color",
        )

        with(rowScope) {
            Column(
                modifier = modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(CineboxTokens.Radius.full))
                    .background(selectedBgColor)
                    .selectable(
                        selected = selected,
                        enabled = true,
                        role = Role.Tab,
                        onClick = onClick,
                    )
                    .padding(vertical = itemVerticalPadding),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Icon(
                    modifier = Modifier
                        .size(iconSize)
                        .then(if (selected) Modifier.gradientMask(palette.accentBrush()) else Modifier),
                    imageVector = icon,
                    contentDescription = contentDescription,
                    tint = if (selected) Color.White else iconColor,
                )
                NavItemLabel(label = label, labelFraction = labelFraction, iconColor = iconColor, selected = selected, compactSize = compactSize)
            }
        }
    }

    @Composable
    override fun NavItem(
        selected: Boolean,
        onClick: () -> Unit,
        icon: DrawableResource,
        contentDescription: String?,
        modifier: Modifier,
        label: String?,
    ) {
        val tokens = MaterialTheme.cinebox
        val palette = MaterialTheme.themePalette
        val iconColor by animateColorAsState(
            targetValue = if (selected) tokens.colors.accent else tokens.colors.textMuted,
            label = "nav_icon_color",
        )
        val selectedBgColor by animateColorAsState(
            targetValue = if (selected) tokens.colors.accent.copy(alpha = CineboxTokens.Opacity.selected)
            else Color.Transparent,
            label = "nav_bg_color",
        )

        with(rowScope) {
            Column(
                modifier = modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(CineboxTokens.Radius.full))
                    .background(selectedBgColor)
                    .selectable(
                        selected = selected,
                        enabled = true,
                        role = Role.Tab,
                        onClick = onClick,
                    )
                    .padding(vertical = itemVerticalPadding),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Icon(
                    modifier = Modifier
                        .size(iconSize)
                        .then(if (selected) Modifier.gradientMask(palette.accentBrush()) else Modifier),
                    painter = painterResource(icon),
                    contentDescription = contentDescription,
                    tint = if (selected) Color.White else iconColor,
                )
                NavItemLabel(label = label, labelFraction = labelFraction, iconColor = iconColor, selected = selected, compactSize = compactSize)
            }
        }
    }

    @Composable
    override fun NavItem(
        selected: Boolean,
        onClick: () -> Unit,
        modifier: Modifier,
        label: String?,
        content: @Composable () -> Unit,
    ) {
        val tokens = MaterialTheme.cinebox
        val selectedBgColor by animateColorAsState(
            targetValue = if (selected) tokens.colors.accent.copy(alpha = CineboxTokens.Opacity.selected)
            else Color.Transparent,
            label = "nav_bg_color",
        )
        val iconColor by animateColorAsState(
            targetValue = if (selected) tokens.colors.accent else tokens.colors.textMuted,
            label = "nav_icon_color",
        )

        with(rowScope) {
            Column(
                modifier = modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(CineboxTokens.Radius.full))
                    .background(selectedBgColor)
                    .selectable(
                        selected = selected,
                        enabled = true,
                        role = Role.Tab,
                        onClick = onClick,
                    )
                    .padding(vertical = itemVerticalPadding),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                if (compactSize) {
                    Box(Modifier.size(iconSize), contentAlignment = Alignment.Center) { content() }
                } else {
                    content()
                }
                NavItemLabel(label = label, labelFraction = labelFraction, iconColor = iconColor, selected = selected, compactSize = compactSize)
            }
        }
    }
}

@Composable
private fun NavItemLabel(
    label: String?,
    labelFraction: Float,
    iconColor: Color,
    selected: Boolean,
    compactSize: Boolean,
) {
    if (label == null || labelFraction <= 0f) return
    Spacer(modifier = Modifier.height((if (compactSize) 2.dp else CineboxTokens.Space.s3) * labelFraction))
    Box(
        modifier = Modifier
            .height(CineboxTokens.Space.s14 * labelFraction)
            .alpha(labelFraction),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(
                fontSize = CineboxTokens.Type.labelXs,
                lineHeight = CineboxTokens.LineHeight.labelXs,
                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
            ),
            color = iconColor,
            maxLines = 1,
            overflow = TextOverflow.Clip,
        )
    }
}


/**
 * Classic flat navigation bar — the original pre-pill implementation.
 * No floating pill, no labels, no scroll behavior. Simple icon row with a top divider.
 */
@Composable
fun CineboxClassicNavigationBar(
    modifier: Modifier = Modifier,
    content: @Composable CineboxNavigationBarScope.() -> Unit,
) {
    val tokens = MaterialTheme.cinebox
    Column(modifier.fillMaxWidth()) {
        androidx.compose.material3.HorizontalDivider(
            thickness = CineboxTokens.Space.hairline,
            color = tokens.colors.borderDefault,
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(cineboxBottomNavigationBarInsets().asPaddingValues())
                .padding(horizontal = CineboxTokens.Space.s4, vertical = cineboxBottomNavigationExtraVerticalPadding),
            horizontalArrangement = Arrangement.spacedBy(tokens.spacing.controlGap, Alignment.CenterHorizontally),
        ) {
            CineboxClassicNavigationBarScopeImpl(this).content()
        }
    }
}

private class CineboxClassicNavigationBarScopeImpl(
    private val rowScope: androidx.compose.foundation.layout.RowScope,
) : CineboxNavigationBarScope {

    @Composable
    override fun NavItem(
        selected: Boolean,
        onClick: () -> Unit,
        icon: ImageVector,
        contentDescription: String?,
        modifier: Modifier,
        label: String?,
    ) {
        val tokens = MaterialTheme.cinebox
        val palette = MaterialTheme.themePalette
        val iconColor by animateColorAsState(
            targetValue = if (selected) tokens.colors.accent else tokens.colors.textMuted,
            label = "classic_nav_icon_color",
        )
        with(rowScope) {
            Icon(
                modifier = modifier
                    .widthIn(max = tokens.components.navItemMaxWidth)
                    .fillMaxWidth()
                    .weight(1f, fill = false)
                    .clip(tokens.components.navItemShape)
                    .selectable(
                        selected = selected,
                        enabled = true,
                        role = Role.Tab,
                        onClick = onClick,
                    )
                    .padding(CineboxTokens.Space.s10)
                    .size(tokens.components.navIconSize)
                    .then(if (selected) Modifier.gradientMask(palette.accentBrush()) else Modifier),
                imageVector = icon,
                contentDescription = contentDescription,
                tint = if (selected) Color.White else iconColor,
            )
        }
    }

    @Composable
    override fun NavItem(
        selected: Boolean,
        onClick: () -> Unit,
        icon: DrawableResource,
        contentDescription: String?,
        modifier: Modifier,
        label: String?,
    ) {
        val tokens = MaterialTheme.cinebox
        val palette = MaterialTheme.themePalette
        val iconColor by animateColorAsState(
            targetValue = if (selected) tokens.colors.accent else tokens.colors.textMuted,
            label = "classic_nav_icon_color",
        )
        with(rowScope) {
            Icon(
                modifier = modifier
                    .widthIn(max = tokens.components.navItemMaxWidth)
                    .fillMaxWidth()
                    .weight(1f, fill = false)
                    .clip(tokens.components.navItemShape)
                    .selectable(
                        selected = selected,
                        enabled = true,
                        role = Role.Tab,
                        onClick = onClick,
                    )
                    .padding(CineboxTokens.Space.s10)
                    .size(tokens.components.navIconSize)
                    .then(if (selected) Modifier.gradientMask(palette.accentBrush()) else Modifier),
                painter = painterResource(icon),
                contentDescription = contentDescription,
                tint = if (selected) Color.White else iconColor,
            )
        }
    }

    @Composable
    override fun NavItem(
        selected: Boolean,
        onClick: () -> Unit,
        modifier: Modifier,
        label: String?,
        content: @Composable () -> Unit,
    ) {
        val tokens = MaterialTheme.cinebox
        with(rowScope) {
            Box(
                modifier = modifier
                    .widthIn(max = tokens.components.navItemMaxWidth)
                    .fillMaxWidth()
                    .weight(1f, fill = false)
                    .clip(tokens.components.navItemShape)
                    .selectable(
                        selected = selected,
                        enabled = true,
                        role = Role.Tab,
                        onClick = onClick,
                    )
                    .padding(CineboxTokens.Space.s10),
                contentAlignment = Alignment.Center,
            ) {
                content()
            }
        }
    }
}
