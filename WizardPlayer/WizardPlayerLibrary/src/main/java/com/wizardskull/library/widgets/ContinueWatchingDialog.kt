package com.wizardskull.library.widgets

import androidx.compose.foundation.background
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.key.*
import androidx.compose.ui.unit.dp
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import com.wizardskull.library.player.config.PlayerLabels

/**
 * Dialog that asks the user whether to continue watching or restart the episode.
 *
 * @param onContinue Callback when user selects "Continuar viendo".
 * @param onRestart Callback when user selects "Reiniciar capítulo".
 * @param onDismiss Called when the dialog is dismissed (e.g. outside click).
 * @param onUserInteracted Optional callback to notify of user interaction.
 */
@Composable
fun ContinueWatchingDialog(
    labels: PlayerLabels,
    onContinue: () -> Unit,
    onRestart: () -> Unit,
    onDismiss: () -> Unit,
    onUserInteracted: (() -> Unit)? = null,
    activeColor: Color
) {
    val continueFocus = remember { FocusRequester() }
    val restartFocus = remember { FocusRequester() }

    val (isContinueFocused, setContinueFocused) = remember { mutableStateOf(false) }
    val (isRestartFocused, setRestartFocused) = remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = labels.titleContinueWatching) },
        text = null,
        confirmButton = {
            TextButton(
                onClick = {
                    onUserInteracted?.invoke()
                    onContinue()
                },
                modifier = Modifier
                    .focusRequester(continueFocus)
                    .onFocusChanged { setContinueFocused(it.isFocused) }
                    .onKeyEvent { keyEvent ->
                        if (keyEvent.type == KeyEventType.KeyUp && keyEvent.key == Key.Enter) {
                            onUserInteracted?.invoke()
                            onContinue()
                            true
                        } else {
                            onUserInteracted?.invoke()
                            false
                        }
                    }
                    .focusable()
            ) {


                Box(
                    modifier = Modifier
                        .background(
                            color = if (isContinueFocused) activeColor.copy(alpha = 0.2f) else Color.Transparent,
                            shape = CircleShape
                        )
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = labels.buttonContinueWatching,
                        color = Color.White
                    )
                }

            }
        },
        dismissButton = {
            TextButton(
                onClick = {
                    onUserInteracted?.invoke()
                    onRestart()
                },
                modifier = Modifier
                    .focusRequester(restartFocus)
                    .onFocusChanged { setRestartFocused(it.isFocused) }
                    .onKeyEvent { keyEvent ->
                        if (keyEvent.type == KeyEventType.KeyUp && keyEvent.key == Key.Enter) {
                            onUserInteracted?.invoke()
                            onRestart()
                            true
                        } else {
                            onUserInteracted?.invoke()
                            false
                        }
                    }
                    .focusable()
            ) {
                Box(
                    modifier = Modifier
                        .background(
                            color = if (isRestartFocused) activeColor.copy(alpha = 0.2f) else Color.Transparent,
                            shape = CircleShape
                        )
                        .padding(horizontal = 16.dp, vertical = 8.dp)

                ) {
                    Text(
                        text = labels.buttonResetVideo,
                        color = Color.White
                    )
                }
            }
        }
    )

    // Set initial focus to "Continuar viendo"
    LaunchedEffect(Unit) {
        try {
            continueFocus.requestFocus()
        } catch (e: Exception) {
            println("⚠️ Focus error: ${e.message}")
        }
    }
}
