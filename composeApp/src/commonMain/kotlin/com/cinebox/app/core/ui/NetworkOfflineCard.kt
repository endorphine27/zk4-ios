package com.cinebox.app.core.ui

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.cinebox.app.core.network.NetworkCondition
import com.cinebox.app.core.network.messageForEmptyState
import com.cinebox.app.core.network.titleForEmptyState
import nuvio.composeapp.generated.resources.Res
import nuvio.composeapp.generated.resources.action_retry
import org.jetbrains.compose.resources.stringResource

@Composable
fun CineboxNetworkOfflineCard(
    condition: NetworkCondition,
    modifier: Modifier = Modifier,
    onRetry: (() -> Unit)? = null,
) {
    val tokens = MaterialTheme.nuvio
    CineboxSurfaceCard(modifier = modifier) {
        Text(
            text = condition.titleForEmptyState(),
            style = MaterialTheme.typography.titleLarge,
            color = tokens.colors.textPrimary,
        )
        Spacer(modifier = Modifier.height(tokens.spacing.controlGap))
        Text(
            text = condition.messageForEmptyState(),
            style = MaterialTheme.typography.bodyLarge,
            color = tokens.colors.textMuted,
        )
        if (onRetry != null) {
            Spacer(modifier = Modifier.height(tokens.spacing.screenHorizontal))
            CineboxPrimaryButton(
                text = stringResource(Res.string.action_retry),
                onClick = onRetry,
            )
        }
    }
}
