package com.cinebox.app.features.shuffle

import com.cinebox.app.core.storage.DesktopStorage
import com.cinebox.app.core.storage.ProfileScopedKey

internal actual object EpisodeShuffleStorage {
    private val store = DesktopStorage.store("episode_shuffle")

    actual fun load(profileId: Int): String? =
        store.getString(ProfileScopedKey.of("episode_shuffle", profileId))

    actual fun save(profileId: Int, payload: String) {
        store.putString(ProfileScopedKey.of("episode_shuffle", profileId), payload)
    }
}
