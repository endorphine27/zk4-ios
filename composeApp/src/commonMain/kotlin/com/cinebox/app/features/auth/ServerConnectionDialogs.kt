package com.cinebox.app.features.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.cinebox.app.core.network.ServerConfiguration
import com.cinebox.app.core.network.ServerDiscoveryFailure
import com.cinebox.app.core.ui.DialogButton
import com.cinebox.app.core.ui.DialogButtons
import com.cinebox.app.core.ui.DialogButtonStyle
import com.cinebox.app.core.ui.DialogSurface
import com.cinebox.app.core.ui.Menu
import com.cinebox.app.core.ui.MenuItem
import com.cinebox.app.core.ui.CineboxLoadingIndicator
import com.cinebox.app.core.ui.CineboxModalBottomSheet
import com.cinebox.app.core.ui.CineboxTokens
import com.cinebox.app.core.ui.cinebox
import cinebox.composeapp.generated.resources.Res
import cinebox.composeapp.generated.resources.action_cancel
import cinebox.composeapp.generated.resources.server_connect_action
import cinebox.composeapp.generated.resources.server_connect_checking
import cinebox.composeapp.generated.resources.server_connect_subtitle
import cinebox.composeapp.generated.resources.server_connect_title
import cinebox.composeapp.generated.resources.server_connect_url_placeholder
import cinebox.composeapp.generated.resources.server_error_auth
import cinebox.composeapp.generated.resources.server_error_connection
import cinebox.composeapp.generated.resources.server_error_http
import cinebox.composeapp.generated.resources.server_error_invalid_document
import cinebox.composeapp.generated.resources.server_error_invalid_url
import cinebox.composeapp.generated.resources.server_error_missing_configuration
import cinebox.composeapp.generated.resources.server_error_not_self_hosted
import cinebox.composeapp.generated.resources.server_error_official_active
import cinebox.composeapp.generated.resources.server_error_official_available
import cinebox.composeapp.generated.resources.server_error_response_too_large
import cinebox.composeapp.generated.resources.server_error_restart
import cinebox.composeapp.generated.resources.server_error_save
import cinebox.composeapp.generated.resources.server_error_session_clear
import cinebox.composeapp.generated.resources.server_error_service
import cinebox.composeapp.generated.resources.server_error_version
import cinebox.composeapp.generated.resources.server_menu_change_custom
import cinebox.composeapp.generated.resources.server_menu_content_description
import cinebox.composeapp.generated.resources.server_menu_custom
import cinebox.composeapp.generated.resources.server_menu_official
import cinebox.composeapp.generated.resources.server_official_action
import cinebox.composeapp.generated.resources.server_official_body
import cinebox.composeapp.generated.resources.server_official_title
import cinebox.composeapp.generated.resources.server_review_description
import cinebox.composeapp.generated.resources.server_review_key_discovered
import cinebox.composeapp.generated.resources.server_review_key_label
import cinebox.composeapp.generated.resources.server_review_title
import cinebox.composeapp.generated.resources.server_review_trust
import cinebox.composeapp.generated.resources.server_review_verified_label
import cinebox.composeapp.generated.resources.server_switching
import cinebox.composeapp.generated.resources.server_warning_credentials
import cinebox.composeapp.generated.resources.server_warning_http
import cinebox.composeapp.generated.resources.server_warning_private
import cinebox.composeapp.generated.resources.server_warning_public
import cinebox.composeapp.generated.resources.server_warning_public_http
import cinebox.composeapp.generated.resources.server_warning_title
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun ServerConnectionMenu(
    activeServer: ServerConfiguration,
    onUseOfficial: () -> Unit,
    onConnectCustom: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var expanded by remember { mutableStateOf(false) }
    Column(modifier = modifier) {
        IconButton(onClick = { expanded = true }) {
            Icon(
                imageVector = Icons.Rounded.MoreVert,
                contentDescription = stringResource(Res.string.server_menu_content_description),
                tint = MaterialTheme.cinebox.colors.textPrimary,
            )
        }
        Menu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            MenuItem(
                text = stringResource(Res.string.server_menu_official),
                enabled = activeServer.isCustom,
                onClick = {
                    expanded = false
                    onUseOfficial()
                },
            )
            MenuItem(
                text = stringResource(
                    if (activeServer.isCustom) {
                        Res.string.server_menu_change_custom
                    } else {
                        Res.string.server_menu_custom
                    },
                ),
                onClick = {
                    expanded = false
                    onConnectCustom()
                },
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ServerConnectionSheet(
    state: ServerConnectionUiState,
    onDiscover: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    val tokens = MaterialTheme.cinebox
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var url by rememberSaveable { mutableStateOf("") }
    val error = serverDiscoveryError(state)

    CineboxModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        showDragHandle = true,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(
                    start = tokens.spacing.sheetPadding,
                    end = tokens.spacing.sheetPadding,
                    bottom = tokens.spacing.sheetPadding,
                ),
        ) {
            Text(
                text = stringResource(Res.string.server_connect_title),
                style = MaterialTheme.typography.titleLarge,
                color = tokens.colors.textPrimary,
                fontWeight = FontWeight.SemiBold,
            )
            Spacer(modifier = Modifier.height(CineboxTokens.Space.s8))
            Text(
                text = stringResource(Res.string.server_connect_subtitle),
                style = MaterialTheme.typography.bodyMedium,
                color = tokens.colors.textMuted,
            )
            Spacer(modifier = Modifier.height(CineboxTokens.Space.s20))
            OutlinedTextField(
                value = url,
                onValueChange = { url = it },
                modifier = Modifier.fillMaxWidth(),
                enabled = !state.isDiscovering,
                singleLine = true,
                placeholder = { Text(stringResource(Res.string.server_connect_url_placeholder)) },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Uri,
                    imeAction = ImeAction.Done,
                ),
                keyboardActions = KeyboardActions(
                    onDone = { if (url.isNotBlank()) onDiscover(url) },
                ),
                shape = tokens.shapes.button,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = tokens.colors.borderFocus,
                    unfocusedBorderColor = tokens.colors.borderDefault,
                    focusedTextColor = tokens.colors.textPrimary,
                    unfocusedTextColor = tokens.colors.textPrimary,
                    focusedLabelColor = tokens.colors.textSecondary,
                    unfocusedLabelColor = tokens.colors.textMuted,
                    cursorColor = tokens.colors.accent,
                ),
            )
            if (error != null) {
                Spacer(modifier = Modifier.height(CineboxTokens.Space.s10))
                Text(
                    text = error,
                    style = MaterialTheme.typography.bodySmall,
                    color = tokens.colors.danger,
                )
            }
            Spacer(modifier = Modifier.height(CineboxTokens.Space.s20))
            Button(
                onClick = { onDiscover(url) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(CineboxTokens.Space.s56),
                enabled = url.isNotBlank() && !state.isDiscovering,
                shape = tokens.shapes.button,
                colors = ButtonDefaults.buttonColors(
                    containerColor = tokens.colors.accent,
                    contentColor = tokens.colors.onAccent,
                ),
            ) {
                if (state.isDiscovering) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(CineboxTokens.Space.s8),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        CineboxLoadingIndicator(
                            modifier = Modifier.size(tokens.icons.sm),
                            color = tokens.colors.onAccent,
                        )
                        Text(stringResource(Res.string.server_connect_checking))
                    }
                } else {
                    Text(stringResource(Res.string.server_connect_action))
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ServerTrustDialog(
    server: ServerConfiguration,
    isSwitching: Boolean,
    switchFailure: ServerSwitchFailure?,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    val tokens = MaterialTheme.cinebox
    DialogSurface(
        onDismissRequest = { if (!isSwitching) onDismiss() },
        title = stringResource(Res.string.server_review_title),
    ) {
        Text(
            text = stringResource(Res.string.server_review_description),
            style = MaterialTheme.typography.bodyMedium,
            color = tokens.colors.textSecondary,
        )
        ServerDetail(
            label = stringResource(Res.string.server_review_verified_label),
            value = server.backendUrl,
        )
        ServerDetail(
            label = stringResource(Res.string.server_review_key_label),
            value = stringResource(Res.string.server_review_key_discovered),
        )
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    color = tokens.colors.warning.copy(alpha = tokens.opacity.selected),
                    shape = RoundedCornerShape(CineboxTokens.Radius.lg),
                )
                .padding(CineboxTokens.Space.s14),
            verticalArrangement = Arrangement.spacedBy(CineboxTokens.Space.s8),
        ) {
            Text(
                text = stringResource(Res.string.server_warning_title),
                style = MaterialTheme.typography.titleSmall,
                color = tokens.colors.warning,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = when {
                    !server.isSecure && server.isPublicHost -> {
                        stringResource(Res.string.server_warning_public_http)
                    }
                    !server.isSecure -> stringResource(Res.string.server_warning_http)
                    server.isPublicHost -> stringResource(Res.string.server_warning_public)
                    else -> stringResource(Res.string.server_warning_private)
                },
                style = MaterialTheme.typography.bodyMedium,
                color = tokens.colors.textSecondary,
            )
            Text(
                text = stringResource(Res.string.server_warning_credentials),
                style = MaterialTheme.typography.bodySmall,
                color = tokens.colors.textSecondary,
            )
        }
        if (switchFailure != null) {
            Text(
                text = serverSwitchError(switchFailure),
                style = MaterialTheme.typography.bodySmall,
                color = tokens.colors.danger,
            )
        }
        DialogActions(
            confirmText = stringResource(Res.string.server_review_trust),
            isSwitching = isSwitching,
            onConfirm = onConfirm,
            onDismiss = onDismiss,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun OfficialServerDialog(
    isSwitching: Boolean,
    switchFailure: ServerSwitchFailure?,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    val tokens = MaterialTheme.cinebox
    DialogSurface(
        onDismissRequest = { if (!isSwitching) onDismiss() },
        title = stringResource(Res.string.server_official_title),
    ) {
        Text(
            text = stringResource(Res.string.server_official_body),
            style = MaterialTheme.typography.bodyMedium,
            color = tokens.colors.textSecondary,
        )
        if (switchFailure != null) {
            Text(
                text = serverSwitchError(switchFailure),
                style = MaterialTheme.typography.bodySmall,
                color = tokens.colors.danger,
            )
        }
        DialogActions(
            confirmText = stringResource(Res.string.server_official_action),
            isSwitching = isSwitching,
            onConfirm = onConfirm,
            onDismiss = onDismiss,
        )
    }
}

@Composable
private fun ServerDetail(
    label: String,
    value: String,
) {
    val tokens = MaterialTheme.cinebox
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(tokens.colors.surfaceCard, tokens.shapes.compactCard)
            .padding(CineboxTokens.Space.s14),
        verticalArrangement = Arrangement.spacedBy(CineboxTokens.Space.s4),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = tokens.colors.textMuted,
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = tokens.colors.textPrimary,
        )
    }
}

@Composable
private fun DialogActions(
    confirmText: String,
    isSwitching: Boolean,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    DialogButtons {
        DialogButton(
            text = stringResource(Res.string.action_cancel),
            onClick = onDismiss,
            enabled = !isSwitching,
        )
        DialogButton(
            text = if (isSwitching) stringResource(Res.string.server_switching) else confirmText,
            onClick = onConfirm,
            style = DialogButtonStyle.Primary,
            loading = isSwitching,
        )
    }
}

@Composable
private fun serverDiscoveryError(state: ServerConnectionUiState): String? {
    return when (state.failure) {
        ServerDiscoveryFailure.InvalidUrl -> stringResource(Res.string.server_error_invalid_url)
        ServerDiscoveryFailure.OfficialServer -> stringResource(
            if (state.activeServer.isCustom) {
                Res.string.server_error_official_available
            } else {
                Res.string.server_error_official_active
            },
        )
        ServerDiscoveryFailure.ConnectionFailed -> stringResource(Res.string.server_error_connection)
        ServerDiscoveryFailure.HttpError -> stringResource(
            Res.string.server_error_http,
            state.statusCode ?: 0,
        )
        ServerDiscoveryFailure.ResponseTooLarge -> stringResource(Res.string.server_error_response_too_large)
        ServerDiscoveryFailure.InvalidDocument -> stringResource(Res.string.server_error_invalid_document)
        ServerDiscoveryFailure.UnsupportedVersion -> stringResource(Res.string.server_error_version)
        ServerDiscoveryFailure.WrongService -> stringResource(Res.string.server_error_service)
        ServerDiscoveryFailure.NotSelfHosted -> stringResource(Res.string.server_error_not_self_hosted)
        ServerDiscoveryFailure.MissingConfiguration -> stringResource(Res.string.server_error_missing_configuration)
        ServerDiscoveryFailure.UnsupportedAuthentication -> stringResource(Res.string.server_error_auth)
        null -> null
    }
}

@Composable
private fun serverSwitchError(failure: ServerSwitchFailure): String =
    when (failure) {
        ServerSwitchFailure.SessionClear -> stringResource(Res.string.server_error_session_clear)
        ServerSwitchFailure.Save -> stringResource(Res.string.server_error_save)
        ServerSwitchFailure.Restart -> stringResource(Res.string.server_error_restart)
    }
