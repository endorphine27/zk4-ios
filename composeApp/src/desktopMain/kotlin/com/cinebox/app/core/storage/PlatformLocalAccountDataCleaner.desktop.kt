package com.cinebox.app.core.storage

internal actual object PlatformLocalAccountDataCleaner {
    actual fun wipe() {
        DesktopStorage.wipe()
    }
}
