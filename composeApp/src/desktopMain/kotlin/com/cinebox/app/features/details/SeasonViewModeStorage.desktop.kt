package com.cinebox.app.features.details

import com.cinebox.app.core.storage.DesktopStorage
import com.cinebox.app.core.storage.ProfileScopedKey

internal actual object SeasonViewModeStorage {
    private val store = DesktopStorage.store("cinebox_season_view_mode")

    actual fun load(): SeasonViewMode? =
        SeasonViewMode.parse(store.getString(ProfileScopedKey.of("season_view_mode")))

    actual fun save(mode: SeasonViewMode) {
        store.putString(ProfileScopedKey.of("season_view_mode"), SeasonViewMode.persist(mode))
    }
}
