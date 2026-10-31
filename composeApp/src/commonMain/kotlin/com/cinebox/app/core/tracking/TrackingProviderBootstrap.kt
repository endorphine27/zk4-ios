package com.cinebox.app.core.tracking

import com.cinebox.app.features.simkl.SimklAuthRepository
import com.cinebox.app.features.simkl.SimklMutationRepository
import com.cinebox.app.features.simkl.SimklLibraryRepository
import com.cinebox.app.features.simkl.SimklProgressRepository
import com.cinebox.app.features.simkl.SimklTrackingLibraryProvider
import com.cinebox.app.features.simkl.SimklTrackingProgressProvider
import com.cinebox.app.features.simkl.SimklWatchedSyncAdapter
import com.cinebox.app.features.simkl.SimklSyncRepository
import com.cinebox.app.features.tracking.TrackingProviderRegistry
import com.cinebox.app.features.mdblist.MdbListTracker
import com.cinebox.app.features.trakt.TraktAuthRepository
import com.cinebox.app.features.trakt.TraktScrobbleRepository
import com.cinebox.app.features.trakt.TraktTrackingLibraryProvider
import com.cinebox.app.features.trakt.TraktTrackingProgressProvider
import com.cinebox.app.features.watching.sync.TraktWatchedSyncAdapter

fun ensureTrackingProvidersRegistered() {
    MdbListTracker.register()
    TraktAuthRepository.descriptor
    TraktScrobbleRepository.ensureRegistered()
    SimklAuthRepository.descriptor
    SimklSyncRepository.state
    SimklLibraryRepository.uiState
    SimklProgressRepository.uiState
    SimklMutationRepository.ensureRegistered()
    TrackingProviderRegistry.registerLibraryProvider(TraktTrackingLibraryProvider)
    TrackingProviderRegistry.registerLibraryProvider(SimklTrackingLibraryProvider)
    TrackingProviderRegistry.registerWatchedProvider(TraktWatchedSyncAdapter)
    TrackingProviderRegistry.registerWatchedProvider(SimklWatchedSyncAdapter)
    TrackingProviderRegistry.registerProgressProvider(TraktTrackingProgressProvider)
    TrackingProviderRegistry.registerProgressProvider(SimklTrackingProgressProvider)
}
