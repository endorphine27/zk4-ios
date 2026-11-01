package com.cinebox.app.features.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import cinebox.composeapp.generated.resources.Res
import cinebox.composeapp.generated.resources.action_save
import cinebox.composeapp.generated.resources.settings_trakt_client_id_description
import cinebox.composeapp.generated.resources.settings_trakt_client_id_label
import cinebox.composeapp.generated.resources.settings_trakt_client_id_title
import com.cinebox.app.features.trakt.TraktSettingsRepository
import org.jetbrains.compose.resources.stringResource

/** Field for the user's own Trakt app Client ID; Trakt shows up in the app once it is set. */
@Composable
internal fun TraktClientIdRow(isTablet: Boolean) {
    LaunchedEffect(Unit) { TraktSettingsRepository.ensureLoaded() }
    val state by TraktSettingsRepository.uiState.collectAsState()
    var draft by rememberSaveable(state.clientId) { mutableStateOf(state.clientId) }
    val normalizedDraft = draft.trim()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = if (isTablet) 20.dp else 16.dp, vertical = if (isTablet) 16.dp else 14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = stringResource(Res.string.settings_trakt_client_id_title),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Medium,
            )
            Text(
                text = stringResource(Res.string.settings_trakt_client_id_description),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        SettingsSecretTextField(
            value = draft,
            onValueChange = { draft = it },
            modifier = Modifier.fillMaxWidth(),
            label = stringResource(Res.string.settings_trakt_client_id_label),
        )
        Button(
            onClick = {
                draft = normalizedDraft
                TraktSettingsRepository.setClientId(normalizedDraft)
            },
            enabled = normalizedDraft != state.clientId,
        ) {
            Text(stringResource(Res.string.action_save))
        }
    }
}
