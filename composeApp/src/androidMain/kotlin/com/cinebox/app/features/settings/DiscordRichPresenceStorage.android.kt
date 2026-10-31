package com.cinebox.app.features.settings

internal actual object DiscordRichPresencePlatform {
    actual val isSupported: Boolean = false
}

internal actual object DiscordRichPresenceStorage {
    actual fun loadEnabled(): Boolean? = null
    actual fun saveEnabled(enabled: Boolean) = Unit
}
