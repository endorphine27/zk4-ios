package com.cinebox.app.features.settings

import com.cinebox.app.core.build.AppFeaturePolicy
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cinebox.app.core.ui.CineboxScreen
import com.cinebox.app.core.ui.CineboxScreenHeader
import com.cinebox.app.features.addons.AddonRepository
import com.cinebox.app.features.addons.enabledAddons
import com.cinebox.app.features.addons.firstEnabledManifestError
import com.cinebox.app.features.addons.hasPendingEnabledManifests
import com.cinebox.app.features.addons.isWaitingForFirstEnabledManifest
import com.cinebox.app.features.collection.CollectionRepository
import com.cinebox.app.features.details.MetaScreenSettingsRepository
import com.cinebox.app.features.plugins.PluginRepository
import com.cinebox.app.features.home.HomeCatalogSettingsRepository
import com.cinebox.app.features.home.buildAddonCatalogRefreshSignature
import com.cinebox.app.features.watchprogress.ContinueWatchingPreferencesRepository
import cinebox.composeapp.generated.resources.Res
import cinebox.composeapp.generated.resources.compose_settings_page_account
import cinebox.composeapp.generated.resources.compose_settings_page_addons
import cinebox.composeapp.generated.resources.compose_settings_page_continue_watching
import cinebox.composeapp.generated.resources.compose_settings_page_homescreen
import cinebox.composeapp.generated.resources.compose_settings_page_meta_screen
import cinebox.composeapp.generated.resources.compose_settings_page_plugins
import org.jetbrains.compose.resources.stringResource

@Composable
fun HomescreenSettingsScreen(
    onBack: () -> Unit,
) {
    val addonsUiState by AddonRepository.uiState.collectAsStateWithLifecycle()
    val homescreenCatalogRefreshKey = remember(addonsUiState.addons) {
        buildAddonCatalogRefreshSignature(addonsUiState.addons)
    }
    val addonManifestsLoading = addonsUiState.addons.hasPendingEnabledManifests()
    val addonManifestErrorMessage = addonsUiState.addons.firstEnabledManifestError()
    val homescreenSettingsUiState by remember {
        HomeCatalogSettingsRepository.snapshot()
        HomeCatalogSettingsRepository.uiState
    }.collectAsStateWithLifecycle()
    val collections by CollectionRepository.collections.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        AddonRepository.initialize()
        CollectionRepository.initialize()
    }

    LaunchedEffect(homescreenCatalogRefreshKey) {
        val enabledAddons = addonsUiState.addons.enabledAddons()
        if (!enabledAddons.isWaitingForFirstEnabledManifest()) {
            HomeCatalogSettingsRepository.syncCatalogs(enabledAddons)
        }
    }

    LaunchedEffect(collections) {
        HomeCatalogSettingsRepository.syncCollections(collections)
    }

    CineboxScreen(
        modifier = Modifier.fillMaxSize(),
    ) {
        stickyHeader {
            CineboxScreenHeader(
                title = stringResource(Res.string.compose_settings_page_homescreen),
                onBack = onBack,
            )
        }
        homescreenSettingsContent(
            isTablet = false,
            heroEnabled = homescreenSettingsUiState.heroEnabled,
            showCatalogType = homescreenSettingsUiState.showCatalogType,
            hideUnreleasedContent = homescreenSettingsUiState.hideUnreleasedContent,
            items = homescreenSettingsUiState.items,
            isCatalogLoading = addonManifestsLoading,
            catalogErrorMessage = addonManifestErrorMessage,
        )
    }
}

@Composable
fun MetaScreenSettingsScreen(
    onBack: () -> Unit,
) {
    val metaScreenSettingsUiState by remember {
        MetaScreenSettingsRepository.ensureLoaded()
        MetaScreenSettingsRepository.uiState
    }.collectAsStateWithLifecycle()

    CineboxScreen(
        modifier = Modifier.fillMaxSize(),
    ) {
        stickyHeader {
            CineboxScreenHeader(
                title = stringResource(Res.string.compose_settings_page_meta_screen),
                onBack = onBack,
            )
        }
        metaScreenSettingsContent(
            isTablet = false,
            uiState = metaScreenSettingsUiState,
        )
    }
}

@Composable
fun ContinueWatchingSettingsScreen(
    onBack: () -> Unit,
) {
    val continueWatchingPreferencesUiState by remember {
        ContinueWatchingPreferencesRepository.ensureLoaded()
        ContinueWatchingPreferencesRepository.uiState
    }.collectAsStateWithLifecycle()

    CineboxScreen(
        modifier = Modifier.fillMaxSize(),
    ) {
        stickyHeader {
            CineboxScreenHeader(
                title = stringResource(Res.string.compose_settings_page_continue_watching),
                onBack = onBack,
            )
        }
        continueWatchingSettingsContent(
            isTablet = false,
            isVisible = continueWatchingPreferencesUiState.isVisible,
            style = continueWatchingPreferencesUiState.style,
            upNextFromFurthestEpisode = continueWatchingPreferencesUiState.upNextFromFurthestEpisode,
            useEpisodeThumbnails = continueWatchingPreferencesUiState.useEpisodeThumbnails,
            showUnairedNextUp = continueWatchingPreferencesUiState.showUnairedNextUp,
            blurNextUp = continueWatchingPreferencesUiState.blurNextUp,
            showResumePromptOnLaunch = continueWatchingPreferencesUiState.showResumePromptOnLaunch,
            sortMode = continueWatchingPreferencesUiState.sortMode,
        )
    }
}

@Composable
fun AddonsSettingsScreen(
    onBack: () -> Unit,
) {
    LaunchedEffect(Unit) {
        AddonRepository.initialize()
    }

    CineboxScreen(
        modifier = Modifier.fillMaxSize(),
    ) {
        stickyHeader {
            CineboxScreenHeader(
                title = stringResource(Res.string.compose_settings_page_addons),
                onBack = onBack,
            )
        }
        addonsSettingsContent()
    }
}

@Composable
fun PluginsSettingsScreen(
    onBack: () -> Unit,
) {
    if (!AppFeaturePolicy.pluginsEnabled) {
        AddonsSettingsScreen(onBack = onBack)
        return
    }

    LaunchedEffect(Unit) {
        PluginRepository.initialize()
    }

    CineboxScreen(
        modifier = Modifier.fillMaxSize(),
    ) {
        stickyHeader {
            CineboxScreenHeader(
                title = stringResource(Res.string.compose_settings_page_plugins),
                onBack = onBack,
            )
        }
        pluginsSettingsContent()
    }
}

@Composable
fun AccountSettingsScreen(
    onBack: () -> Unit,
) {
    CineboxScreen(
        modifier = Modifier.fillMaxSize(),
    ) {
        stickyHeader {
            CineboxScreenHeader(
                title = stringResource(Res.string.compose_settings_page_account),
                onBack = onBack,
            )
        }
        accountSettingsContent(
            isTablet = false,
        )
    }
}
