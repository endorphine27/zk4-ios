package com.cinebox.app.features.settings

import cinebox.composeapp.generated.resources.Res
import cinebox.composeapp.generated.resources.settings_appearance_desktop_navigation_sidebar
import cinebox.composeapp.generated.resources.settings_appearance_desktop_navigation_top_bar
import org.jetbrains.compose.resources.StringResource

enum class DesktopNavigationLayout(
    val labelRes: StringResource,
) {
    Sidebar(Res.string.settings_appearance_desktop_navigation_sidebar),
    TopBar(Res.string.settings_appearance_desktop_navigation_top_bar),
    ;

    companion object {
        val Default = Sidebar

        fun fromName(name: String?): DesktopNavigationLayout =
            entries.firstOrNull { it.name.equals(name, ignoreCase = true) } ?: Default
    }
}
