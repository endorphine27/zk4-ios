package com.cinebox.app.features.settings

internal expect object DesktopRendererSettings {
    val isSupported: Boolean
    val useOpenGl: Boolean
    val isExternallyControlled: Boolean

    fun setUseOpenGl(enabled: Boolean)
}
