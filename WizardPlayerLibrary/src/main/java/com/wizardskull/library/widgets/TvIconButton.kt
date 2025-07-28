package com.wizardskull.library.widgets

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.key.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.wizardskull.library.utils.GeneralUtils.isTelevision


/**
 * A custom IconButton that supports D-Pad focus, TV navigation and click handling.
 *
 * @param onClick Called when the button is clicked (either via tap or Enter key).
 * @param modifier Optional Modifier to apply to the button layout.
 * @param focusRequester Optional FocusRequester to request focus programmatically.
 * @param isFocused State used to track whether this button is focused.
 * @param icon The icon to display.
 * @param description Content description for accessibility.
 * @param tint The color to apply to the icon.
 * @param onUserInteracted Optional callback when user interacts with the button.
 * @param enabled Whether the button is enabled or not.
 */
@Composable
fun TvIconButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    focusRequester: FocusRequester? = null,
    isFocused: MutableState<Boolean>,
    icon: ImageVector,
    diameterButtonCircleDp: Dp = 48.dp,
    iconSizeDp: Dp = 24.dp,
    description: String,
    activeColor: Color,
    focusColor: Color,
    onUserInteracted: (() -> Unit)? = null,
    enabled: Boolean = true
) {
    // ─────────────────────────────────────────────────────────────
    // 📡 Context & Device Capabilities
    // ─────────────────────────────────────────────────────────────

    val focusManager = LocalFocusManager.current
    val context = LocalContext.current
    val isTV = remember { context.isTelevision() }

    // ─────────────────────────────────────────────────────────────
    // 📐 Visual Configuration
    // ─────────────────────────────────────────────────────────────

    val iconSize = diameterButtonCircleDp // Diameter of the outer button circle

    // ─────────────────────────────────────────────────────────────
    // 🧱 Main Icon Button Container
    // ─────────────────────────────────────────────────────────────
    val scale by animateFloatAsState(
        targetValue = if (isFocused.value) 1.1f else 1f,
        animationSpec = tween(durationMillis = 150),
        label = "TV Button Focus Scale"
    )

    Box(
        modifier = modifier
            .graphicsLayer(scaleX = scale, scaleY = scale)
            .size(iconSize)

            // Attach optional focus requester if provided
            .then(if (focusRequester != null) Modifier.focusRequester(focusRequester) else Modifier)

            // 📌 Handle focus changes
            .onFocusChanged { focusState ->
                isFocused.value = focusState.isFocused
                if (focusState.isFocused) {
                    try {
                        onUserInteracted?.invoke()
                    } catch (e: Exception) {
                        println("⚠️ Error during focus change interaction: ${e.message}")
                    }
                }
            }

            // ⌨️ Handle D-Pad keys and click
            .onKeyEvent { keyEvent ->
                if (keyEvent.type == KeyEventType.KeyDown) {
                    try {
                        when (keyEvent.key) {
                            Key.DirectionRight -> {
                                focusManager.moveFocus(FocusDirection.Right)
                                true
                            }
                            Key.DirectionLeft -> {
                                focusManager.moveFocus(FocusDirection.Left)
                                true
                            }
                            Key.Enter, Key.NumPadEnter, Key.DirectionCenter -> {
                                onClick()
                                true
                            }
                            else -> false
                        }
                    } catch (e: Exception) {
                        println("⚠️ Error handling key event: ${e.message}")
                        false
                    }
                } else false
            }

            // 🎨 Background changes when focused on TV
            .background(
                color = when {
                    isTV && isFocused.value && !enabled -> Color.Gray.copy(alpha = 0.3f)
                    isTV && isFocused.value -> Color.White
                    else -> Color.Transparent
                },
                shape = CircleShape
            )

            .focusable() // Make this box focusable by keyboard/D-Pad

            // 🖱 Click event (TV & non-TV)
            .clickable(
                enabled = enabled,
                onClick = {
                    try {
                        onUserInteracted?.invoke()
                        onClick()
                    } catch (e: Exception) {
                        println("⚠️ Error during click: ${e.message}")
                    }
                }
            ),

        contentAlignment = Alignment.Center
    ) {
        // ─────────────────────────────────────────────────────────
        // 🎨 Icon Rendering
        // ─────────────────────────────────────────────────────────

        Icon(
            imageVector = icon,
            contentDescription = description,
            tint = when {
                !enabled -> focusColor.copy(alpha = 0.4f)                 // Disabled icon
                isFocused.value && isTV -> activeColor           // Focused on TV
                else -> focusColor                                        // Default state
            },
            modifier = Modifier.size(iconSizeDp) // Actual icon size inside circle
        )
    }
}
