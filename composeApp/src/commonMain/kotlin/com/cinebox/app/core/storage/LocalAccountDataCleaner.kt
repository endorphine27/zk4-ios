package com.cinebox.app.core.storage

import com.cinebox.app.core.build.AppFeaturePolicy
import com.cinebox.app.core.sync.SyncManager
import com.cinebox.app.core.sync.ProfileSettingsSync
import com.cinebox.app.core.tracking.ensureTrackingProvidersRegistered
import com.cinebox.app.features.addons.AddonRepository
import com.cinebox.app.features.catalog.CatalogRepository
import com.cinebox.app.features.collection.CollectionMobileSettingsRepository
import com.cinebox.app.features.collection.CollectionRepository
import com.cinebox.app.features.details.MetaDetailsRepository
import com.cinebox.app.features.details.MetaScreenSettingsRepository
import com.cinebox.app.features.home.HomeCatalogSettingsRepository
import com.cinebox.app.features.home.HomeRepository
import com.cinebox.app.features.library.LibraryRepository
import com.cinebox.app.features.membership.MemberAccessRepository
import com.cinebox.app.features.library.LibraryDisplaySettingsRepository
import com.cinebox.app.features.notifications.EpisodeReleaseNotificationsRepository
import com.cinebox.app.features.player.PlayerLaunchStore
import com.cinebox.app.features.player.PlayerSettingsRepository
import com.cinebox.app.features.p2p.P2pSettingsRepository
import com.cinebox.app.features.plugins.PluginRepository
import com.cinebox.app.features.player.SubtitleRepository
import com.cinebox.app.features.profiles.ProfileRepository
import com.cinebox.app.features.profiles.MAX_PROFILES
import com.cinebox.app.features.search.SearchRepository
import com.cinebox.app.features.servers.ServerRepository
import com.cinebox.app.features.settings.ThemeSettingsRepository
import com.cinebox.app.features.streams.StreamContextStore
import com.cinebox.app.features.streams.StreamBadgeSettingsRepository
import com.cinebox.app.features.streams.StreamLaunchStore
import com.cinebox.app.features.streams.StreamsRepository
import com.cinebox.app.features.tracking.TrackingProviderRegistry
import com.cinebox.app.features.tracking.TrackingSettingsRepository
import com.cinebox.app.core.ui.CardDepthStyleRepository
import com.cinebox.app.core.ui.PosterCardStyleRepository
import com.cinebox.app.features.watchprogress.ContinueWatchingPreferencesRepository
import com.cinebox.app.features.watchprogress.ContinueWatchingEnrichmentCache
import com.cinebox.app.features.watchprogress.WatchProgressRepository
import com.cinebox.app.features.watchprogress.WatchProgressSourceCoordinator
import com.cinebox.app.features.watched.WatchedRepository

internal object LocalAccountDataCleaner {
    fun wipe() {
        ensureTrackingProvidersRegistered()
        TrackingProviderRegistry.removeStoredProfiles(1..MAX_PROFILES)
        SyncManager.cancelAccountSync()
        WatchProgressSourceCoordinator.clearLocalState()
        ProfileSettingsSync.clearAccountState()
        ContinueWatchingEnrichmentCache.clearLocalState()
        WatchProgressRepository.clearLocalState()
        WatchedRepository.clearLocalState()
        LibraryRepository.runAccountStorageWipe {
            wipePlatformStorage()
        }

        ProfileRepository.clearInMemory()
        MemberAccessRepository.clearLocalState()
        AddonRepository.clearLocalState()
        ServerRepository.clearLocalState()
        if (AppFeaturePolicy.pluginsEnabled) {
            PluginRepository.clearLocalState()
        }
        HomeRepository.clear()
        HomeCatalogSettingsRepository.clearLocalState()
        MetaScreenSettingsRepository.clearLocalState()
        com.cinebox.app.features.shuffle.EpisodeShuffleRepository.clearLocalState()
        LibraryRepository.clearLocalState()
        LibraryDisplaySettingsRepository.clearLocalState()
        ContinueWatchingPreferencesRepository.clearLocalState()
        EpisodeReleaseNotificationsRepository.clearLocalState()
        CollectionMobileSettingsRepository.clearLocalState()
        CollectionRepository.clearLocalState()
        ThemeSettingsRepository.clearLocalState()
        PosterCardStyleRepository.clearLocalState()
        CardDepthStyleRepository.clearLocalState()
        TrackingProviderRegistry.clearLocalState()
        TrackingSettingsRepository.clearLocalState()
        PlayerSettingsRepository.clearLocalState()
        StreamBadgeSettingsRepository.clearLocalState()
        P2pSettingsRepository.clearLocalState()
        CatalogRepository.clear()
        StreamsRepository.clear()
        MetaDetailsRepository.clear()
        SearchRepository.reset()
        SubtitleRepository.clear()
        PlayerLaunchStore.clear()
        StreamLaunchStore.clear()
        StreamContextStore.clear()
    }

    internal fun wipePlatformStorage(wipeStorage: () -> Unit = PlatformLocalAccountDataCleaner::wipe) {
        try {
            wipeStorage()
        } finally {
            ContinueWatchingEnrichmentCache.clearLocalState()
        }
    }
}

internal expect object PlatformLocalAccountDataCleaner {
    fun wipe()
}
