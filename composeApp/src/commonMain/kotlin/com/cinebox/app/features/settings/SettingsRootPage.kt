package com.cinebox.app.features.settings

import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.rounded.AccountCircle
import androidx.compose.material.icons.rounded.BugReport
import androidx.compose.material.icons.rounded.CloudDownload
import androidx.compose.material.icons.rounded.Extension
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.Groups
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Link
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.People
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Policy
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.ui.platform.LocalUriHandler
import cinebox.composeapp.generated.resources.Res
import cinebox.composeapp.generated.resources.compose_settings_page_account
import cinebox.composeapp.generated.resources.compose_settings_page_advanced
import cinebox.composeapp.generated.resources.compose_settings_page_appearance
import cinebox.composeapp.generated.resources.compose_settings_page_integrations
import cinebox.composeapp.generated.resources.compose_settings_page_licenses_attributions
import cinebox.composeapp.generated.resources.compose_settings_page_notifications
import cinebox.composeapp.generated.resources.compose_settings_page_playback
import cinebox.composeapp.generated.resources.compose_settings_page_privacy_policy
import cinebox.composeapp.generated.resources.compose_settings_page_supporters_contributors
import cinebox.composeapp.generated.resources.compose_settings_root_account_description
import cinebox.composeapp.generated.resources.compose_settings_root_appearance_description
import cinebox.composeapp.generated.resources.compose_settings_root_check_updates_description
import cinebox.composeapp.generated.resources.compose_settings_root_check_updates_title
import cinebox.composeapp.generated.resources.compose_settings_root_content_discovery_description
import cinebox.composeapp.generated.resources.compose_settings_root_general_section
import cinebox.composeapp.generated.resources.compose_settings_root_integrations_description
import cinebox.composeapp.generated.resources.compose_settings_root_notifications_description
import cinebox.composeapp.generated.resources.compose_settings_root_privacy_policy_description
import cinebox.composeapp.generated.resources.compose_settings_root_switch_profile_description
import cinebox.composeapp.generated.resources.compose_settings_root_switch_profile_title
import cinebox.composeapp.generated.resources.compose_settings_root_tracking_description
import cinebox.composeapp.generated.resources.compose_settings_root_about_section
import cinebox.composeapp.generated.resources.compose_settings_root_account_section
import cinebox.composeapp.generated.resources.compose_settings_root_advanced_description
import cinebox.composeapp.generated.resources.compose_settings_root_advanced_section
import cinebox.composeapp.generated.resources.compose_settings_page_content_discovery
import cinebox.composeapp.generated.resources.compose_settings_page_tracking
import cinebox.composeapp.generated.resources.settings_playback_subtitle
import cinebox.composeapp.generated.resources.updates_debug_test_description
import cinebox.composeapp.generated.resources.updates_debug_test_title
import cinebox.composeapp.generated.resources.party_settings_description
import cinebox.composeapp.generated.resources.party_title
import cinebox.composeapp.generated.resources.about_supporters_contributors_subtitle
import cinebox.composeapp.generated.resources.about_licenses_attributions_subtitle
import org.jetbrains.compose.resources.stringResource


internal fun LazyListScope.settingsRootContent(
    isTablet: Boolean,
    onPlaybackClick: () -> Unit,
    onAppearanceClick: () -> Unit,
    onAdvancedClick: () -> Unit,
    onNotificationsClick: () -> Unit,
    onContentDiscoveryClick: () -> Unit,
    onIntegrationsClick: () -> Unit,
    onTrackingClick: () -> Unit,
    onSupportersContributorsClick: () -> Unit,
    onLicensesAttributionsClick: () -> Unit,
    onCheckForUpdatesClick: (() -> Unit)? = null,
    onTestUpdateBannerClick: (() -> Unit)? = null,
    onAccountClick: () -> Unit,
    onSwitchProfileClick: (() -> Unit)? = null,
    showNotificationsEntry: Boolean = true,
    onWatchPartyClick: (() -> Unit)? = null,
    showAccountSection: Boolean = true,
    showGeneralSection: Boolean = true,
    showAboutSection: Boolean = true,
    showAdvancedSection: Boolean = true,
    showSupportersContributorsPage: Boolean = true,
    showAttribution: Boolean = true,
) {
    if (showAccountSection) {
        item {
            SettingsSection(
                title = stringResource(Res.string.compose_settings_root_account_section),
                isTablet = isTablet,
            ) {
                SettingsGroup(isTablet = isTablet) {
                    if (onSwitchProfileClick != null) {
                        SettingsNavigationRow(
                            title = stringResource(Res.string.compose_settings_root_switch_profile_title),
                            description = stringResource(Res.string.compose_settings_root_switch_profile_description),
                            icon = Icons.Rounded.People,
                            isTablet = isTablet,
                            onClick = onSwitchProfileClick,
                        )
                        SettingsGroupDivider(isTablet = isTablet)
                    }
                    SettingsNavigationRow(
                        title = stringResource(Res.string.compose_settings_page_account),
                        description = stringResource(Res.string.compose_settings_root_account_description),
                        icon = Icons.Rounded.AccountCircle,
                        isTablet = isTablet,
                        onClick = onAccountClick,
                    )
                    if (onWatchPartyClick != null) {
                        SettingsGroupDivider(isTablet = isTablet)
                        SettingsNavigationRow(
                            title = stringResource(Res.string.party_title),
                            description = stringResource(Res.string.party_settings_description),
                            icon = Icons.Rounded.Groups,
                            isTablet = isTablet,
                            onClick = onWatchPartyClick,
                        )
                    }
                }
            }
        }
    }
    if (showGeneralSection) {
        item {
            SettingsSection(
                title = stringResource(Res.string.compose_settings_root_general_section),
                isTablet = isTablet,
            ) {
                SettingsGroup(isTablet = isTablet) {
                    SettingsNavigationRow(
                        title = stringResource(Res.string.compose_settings_page_appearance),
                        description = stringResource(Res.string.compose_settings_root_appearance_description),
                        icon = Icons.Rounded.Palette,
                        isTablet = isTablet,
                        onClick = onAppearanceClick,
                    )
                    SettingsGroupDivider(isTablet = isTablet)
                    SettingsNavigationRow(
                        title = stringResource(Res.string.compose_settings_page_content_discovery),
                        description = stringResource(Res.string.compose_settings_root_content_discovery_description),
                        icon = Icons.Rounded.Extension,
                        isTablet = isTablet,
                        onClick = onContentDiscoveryClick,
                    )
                    SettingsGroupDivider(isTablet = isTablet)
                    SettingsNavigationRow(
                        title = stringResource(Res.string.compose_settings_page_playback),
                        description = stringResource(Res.string.settings_playback_subtitle),
                        icon = Icons.Rounded.PlayArrow,
                        isTablet = isTablet,
                        onClick = onPlaybackClick,
                    )
                    SettingsGroupDivider(isTablet = isTablet)
                    SettingsNavigationRow(
                        title = stringResource(Res.string.compose_settings_page_integrations),
                        description = stringResource(Res.string.compose_settings_root_integrations_description),
                        icon = Icons.Rounded.Link,
                        isTablet = isTablet,
                        onClick = onIntegrationsClick,
                    )
                    if (showNotificationsEntry) {
                        SettingsGroupDivider(isTablet = isTablet)
                        SettingsNavigationRow(
                            title = stringResource(Res.string.compose_settings_page_notifications),
                            description = stringResource(Res.string.compose_settings_root_notifications_description),
                            icon = Icons.Rounded.Notifications,
                            isTablet = isTablet,
                            onClick = onNotificationsClick,
                        )
                    }
                }
            }
        }
    }
    if (showAboutSection) {
        item {
            val uriHandler = LocalUriHandler.current
            SettingsSection(
                title = stringResource(Res.string.compose_settings_root_about_section),
                isTablet = isTablet,
            ) {
                SettingsGroup(isTablet = isTablet) {
                    if (onCheckForUpdatesClick != null) {
                        UpdateChannelSettingsRow(isTablet = isTablet)
                        SettingsGroupDivider(isTablet = isTablet)
                        SettingsNavigationRow(
                            title = stringResource(Res.string.compose_settings_root_check_updates_title),
                            description = stringResource(Res.string.compose_settings_root_check_updates_description),
                            icon = Icons.Rounded.CloudDownload,
                            isTablet = isTablet,
                            onClick = onCheckForUpdatesClick,
                        )
                    }
                    if (onTestUpdateBannerClick != null) {
                        SettingsGroupDivider(isTablet = isTablet)
                        SettingsNavigationRow(
                            title = stringResource(Res.string.updates_debug_test_title),
                            description = stringResource(Res.string.updates_debug_test_description),
                            icon = Icons.Rounded.BugReport,
                            isTablet = isTablet,
                            onClick = onTestUpdateBannerClick,
                        )
                    }
                }
            }
        }
    }
    if (showAdvancedSection) {
        item {
            SettingsSection(
                title = stringResource(Res.string.compose_settings_root_advanced_section),
                isTablet = isTablet,
            ) {
                SettingsGroup(isTablet = isTablet) {
                    SettingsNavigationRow(
                        title = stringResource(Res.string.compose_settings_page_advanced),
                        description = stringResource(Res.string.compose_settings_root_advanced_description),
                        icon = Icons.Rounded.Tune,
                        isTablet = isTablet,
                        onClick = onAdvancedClick,
                    )
                }
            }
        }
    }
    if (showAttribution) {
        item(key = "settings-attribution") {
            SettingsAttribution(isTablet = isTablet)
        }
    }
}
