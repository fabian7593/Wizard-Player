package com.wizardskull.library.widgets

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.key.*
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp

/**
 * ScrollableDialogList displays a dialog with a scrollable list of selectable items.
 *
 * @param title Title displayed at the top of the dialog.
 * @param items List of item pairs (id, label) to display.
 * @param onItemSelected Callback when an item is selected, passing its ID.
 * @param onDismiss Callback when the dialog is dismissed.
 * @param onUserInteracted Optional callback when the user interacts (e.g., to prevent auto-hide).
 */
@Composable
fun ScrollableDialogList(
    title: String,
    items: List<Pair<Int, String>>,
    onItemSelected: (Int, String?) -> Unit,
    onDismiss: () -> Unit,
    onUserInteracted: (() -> Unit)? = null
) {
    // ─────────────────────────────────────────────────────────────
    // 📏 Dialog Size Constraints
    // ─────────────────────────────────────────────────────────────

    val screenHeight = LocalConfiguration.current.screenHeightDp.dp
    val maxDialogHeight = screenHeight * 0.6f // Limit dialog to 60% of screen height

    // Focus requester for the first item
    val firstItemFocusRequester = FocusRequester()

    // ─────────────────────────────────────────────────────────────
    // 🧱 AlertDialog Container
    // ─────────────────────────────────────────────────────────────

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = title) },
        text = {
            Box(
                modifier = Modifier.heightIn(max = maxDialogHeight)
            ) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                ) {
                    // ─────────────────────────────────────────────
                    // 🔁 List Items
                    // ─────────────────────────────────────────────

                    itemsIndexed(items) { index, (id, name) ->

                        // Create individual focus requesters
                        val itemFocusRequester =
                            if (index == 0) firstItemFocusRequester else FocusRequester()

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()

                                // 📌 Handle tap selection
                                .clickable {
                                    try {
                                        onUserInteracted?.invoke()
                                        onItemSelected(id, name)
                                        onDismiss()
                                    } catch (e: Exception) {
                                        println("⚠️ Error on item click: ${e.message}")
                                    }
                                }

                                // 🎯 Focus management per row
                                .focusRequester(itemFocusRequester)

                                // 💡 Keyboard selection support (Enter key)
                                .onKeyEvent { keyEvent ->
                                    try {
                                        if (keyEvent.type == KeyEventType.KeyUp &&
                                            keyEvent.key == Key.Enter
                                        ) {
                                            onUserInteracted?.invoke()
                                            onItemSelected(id, name)
                                            onDismiss()
                                            true
                                        } else {
                                            onUserInteracted?.invoke()
                                            false
                                        }
                                    } catch (e: Exception) {
                                        println("⚠️ Error on key event: ${e.message}")
                                        false
                                    }
                                }

                                .padding(horizontal = 16.dp, vertical = 12.dp)
                                //.focusable()
                        ) {
                            Text(text = name)
                        }

                        // Request focus for first item after composition
                        if (index == 0) {
                            LaunchedEffect(Unit) {
                                try {
                                    firstItemFocusRequester.requestFocus()
                                } catch (e: Exception) {
                                    println("⚠️ Error requesting focus: ${e.message}")
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {} // No confirm button needed (item selection confirms)
    )
}
