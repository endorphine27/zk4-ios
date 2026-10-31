package com.cinebox.app.core.sync

import com.cinebox.app.core.storage.DesktopStorage

internal actual object SyncClientIdentityStorage {
    private val store = DesktopStorage.store("cinebox_sync_client_identity")

    actual fun loadClientId(): String? =
        store.getString("client_instance_id")

    actual fun saveClientId(clientId: String) {
        store.putString("client_instance_id", clientId)
    }
}
