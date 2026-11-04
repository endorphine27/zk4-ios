package com.cinebox.app.features.cast

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Cast
import androidx.compose.material.icons.rounded.CastConnected
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import cinebox.composeapp.generated.resources.Res
import cinebox.composeapp.generated.resources.cast_dialog_title
import cinebox.composeapp.generated.resources.cast_error
import cinebox.composeapp.generated.resources.cast_no_devices
import cinebox.composeapp.generated.resources.cast_pause
import cinebox.composeapp.generated.resources.cast_playing_on
import cinebox.composeapp.generated.resources.cast_resume
import cinebox.composeapp.generated.resources.cast_searching
import cinebox.composeapp.generated.resources.cast_stop
import cinebox.composeapp.generated.resources.cast_unsupported_source
import cinebox.composeapp.generated.resources.cast_close
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource

/** The device the app is currently casting to, kept so the dialog can reopen as a remote control. */
object CastSession {
    var device by mutableStateOf<CastDevice?>(null)
        internal set
}

fun isCastableUrl(url: String?): Boolean =
    url != null && (url.startsWith("http://") || url.startsWith("https://"))

@Composable
fun CastDialog(
    url: String?,
    title: String,
    positionMs: Long,
    onCastStarted: () -> Unit,
    onDismiss: () -> Unit,
) {
    val backend = remember { platformCastBackend() } ?: run {
        onDismiss()
        return
    }
    val devices by backend.devices.collectAsState()
    val scope = rememberCoroutineScope()
    var busy by remember { mutableStateOf(false) }
    var errorText by remember { mutableStateOf<String?>(null) }
    var paused by remember { mutableStateOf(false) }
    val active = CastSession.device
    val canCast = isCastableUrl(url)

    DisposableEffect(backend) {
        backend.startDiscovery()
        onDispose { backend.stopDiscovery() }
    }

    val errorLabel = stringResource(Res.string.cast_error)
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                if (active != null) stringResource(Res.string.cast_playing_on, active.name)
                else stringResource(Res.string.cast_dialog_title),
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                if (active != null) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        TextButton(onClick = {
                            scope.launch {
                                val result = if (paused) backend.resume(active) else backend.pause(active)
                                if (result.isSuccess) paused = !paused else errorText = errorLabel
                            }
                        }) {
                            Text(stringResource(if (paused) Res.string.cast_resume else Res.string.cast_pause))
                        }
                        TextButton(onClick = {
                            scope.launch {
                                backend.stop(active)
                                CastSession.device = null
                                paused = false
                            }
                        }) { Text(stringResource(Res.string.cast_stop)) }
                    }
                } else if (!canCast) {
                    Text(stringResource(Res.string.cast_unsupported_source))
                } else if (devices.isEmpty()) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        CircularProgressIndicator(modifier = Modifier.padding(2.dp), strokeWidth = 2.dp)
                        Text(stringResource(Res.string.cast_searching))
                    }
                    Text(stringResource(Res.string.cast_no_devices), style = MaterialTheme.typography.bodySmall)
                } else {
                    LazyColumn(modifier = Modifier.heightIn(max = 320.dp)) {
                        items(devices, key = { it.id }) { device ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable(enabled = !busy) {
                                        busy = true
                                        errorText = null
                                        scope.launch {
                                            val result = backend.cast(device, url.orEmpty(), title, positionMs)
                                            busy = false
                                            if (result.isSuccess) {
                                                CastSession.device = device
                                                onCastStarted()
                                            } else {
                                                errorText = errorLabel
                                            }
                                        }
                                    }
                                    .padding(vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                            ) {
                                Icon(Icons.Rounded.Cast, contentDescription = null)
                                Text(device.name)
                            }
                        }
                    }
                }
                errorText?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(Res.string.cast_close)) }
        },
    )
}
