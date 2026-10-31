package com.cinebox.app.features.settings

import cinebox.composeapp.generated.resources.Res
import cinebox.composeapp.generated.resources.settings_nav_bar_style_adaptive
import cinebox.composeapp.generated.resources.settings_nav_bar_style_expanded
import cinebox.composeapp.generated.resources.settings_nav_bar_style_compact
import cinebox.composeapp.generated.resources.settings_nav_bar_style_classic
import org.jetbrains.compose.resources.StringResource

enum class NavBarStyle(
    val key: String,
    val labelRes: StringResource,
) {
    ADAPTIVE("adaptive", Res.string.settings_nav_bar_style_adaptive),
    EXPANDED("expanded", Res.string.settings_nav_bar_style_expanded),
    COMPACT("compact", Res.string.settings_nav_bar_style_compact),
    CLASSIC("classic", Res.string.settings_nav_bar_style_classic),
    ;

    companion object {
        fun fromKey(key: String?): NavBarStyle =
            entries.firstOrNull { it.key.equals(key, ignoreCase = true) } ?: ADAPTIVE
    }
}
