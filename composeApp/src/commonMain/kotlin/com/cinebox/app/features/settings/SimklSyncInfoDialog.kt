package com.cinebox.app.features.settings

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalUriHandler
import com.cinebox.app.core.ui.DialogButton
import com.cinebox.app.core.ui.DialogButtonStyle
import com.cinebox.app.core.ui.DialogButtons
import com.cinebox.app.core.ui.DialogSurface
import com.cinebox.app.core.ui.cinebox
import com.cinebox.app.features.simkl.SIMKL_AUTOMATIC_REFRESH_INTERVAL_MINUTES
import cinebox.composeapp.generated.resources.Res
import cinebox.composeapp.generated.resources.action_close
import cinebox.composeapp.generated.resources.settings_simkl_sync_info_activity
import cinebox.composeapp.generated.resources.settings_simkl_sync_info_description
import cinebox.composeapp.generated.resources.settings_simkl_sync_info_docs
import cinebox.composeapp.generated.resources.settings_simkl_sync_info_library_statuses
import cinebox.composeapp.generated.resources.settings_simkl_sync_info_manual
import cinebox.composeapp.generated.resources.settings_simkl_sync_info_title
import cinebox.composeapp.generated.resources.settings_trakt_failed_open_browser
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun SimklSyncInfoDialog(onDismiss: () -> Unit) {
    val uriHandler = LocalUriHandler.current
    val tokens = MaterialTheme.cinebox
    var browserError by rememberSaveable { mutableStateOf(false) }

    DialogSurface(
        onDismissRequest = onDismiss,
        title = stringResource(Res.string.settings_simkl_sync_info_title),
    ) {
        listOf(
            stringResource(Res.string.settings_simkl_sync_info_description, SIMKL_AUTOMATIC_REFRESH_INTERVAL_MINUTES),
            stringResource(Res.string.settings_simkl_sync_info_activity),
            stringResource(Res.string.settings_simkl_sync_info_manual),
            stringResource(Res.string.settings_simkl_sync_info_library_statuses),
        ).forEach { paragraph ->
            Text(
                text = paragraph,
                style = MaterialTheme.typography.bodyMedium,
                color = tokens.colors.textSecondary,
            )
        }
        if (browserError) {
            Text(
                text = stringResource(Res.string.settings_trakt_failed_open_browser),
                style = MaterialTheme.typography.bodySmall,
                color = tokens.colors.danger,
            )
        }
        DialogButtons {
            DialogButton(
                text = stringResource(Res.string.settings_simkl_sync_info_docs),
                onClick = {
                    browserError = false
                    runCatching { uriHandler.openUri(SIMKL_SYNC_GUIDE_URL) }
                        .onFailure { browserError = true }
                },
            )
            DialogButton(
                text = stringResource(Res.string.action_close),
                onClick = onDismiss,
                style = DialogButtonStyle.Primary,
            )
        }
    }
}

private const val SIMKL_SYNC_GUIDE_URL = "https://api.simkl.org/guides/sync"
