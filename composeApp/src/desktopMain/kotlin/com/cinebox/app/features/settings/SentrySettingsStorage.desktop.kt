package com.cinebox.app.features.settings

import com.cinebox.app.core.diagnostics.SentryConfig
import com.cinebox.app.core.storage.DesktopStorage

internal actual object SentrySettingsPlatform {
    actual val crashReportsSupported: Boolean = SentryConfig.DESKTOP_DSN.isNotBlank()
    actual val usesDesktopCopy: Boolean = true
}

internal actual object SentrySettingsStorage {
    private const val enabledKey = "enabled"
    private val store = DesktopStorage.store("cinebox_sentry_settings")

    actual fun loadEnabled(): Boolean? =
        if (store.contains(enabledKey)) store.getBoolean(enabledKey) else null

    actual fun saveEnabled(enabled: Boolean) {
        store.putBoolean(enabledKey, enabled)
    }
}
