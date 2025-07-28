package com.wizardskull.library.widgets

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.focusTarget
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.key.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.dp
import com.wizardskull.library.utils.GeneralUtils.isTelevision

@Composable
fun AdaptiveNextButton(
    label: String,
    isFocused: MutableState<Boolean>,
    enabled: Boolean,
    onClick: () -> Unit,
    activeColor: Color,
    onUserInteracted: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val isTV = remember { context.isTelevision() }
    val focusManager = LocalFocusManager.current

    if (isTV) {

        val scale by animateFloatAsState(
            targetValue = if (isFocused.value) 1.1f else 1f,
            animationSpec = tween(durationMillis = 150),
            label = "TV Focus Scale"
        )

        Box(
            modifier = modifier
                .graphicsLayer(scaleX = scale, scaleY = scale, alpha = if (enabled) 1f else 0.5f)
                .width(160.dp)
                .height(48.dp)

                .onKeyEvent {
                    if (it.type == KeyEventType.KeyDown) {
                        when (it.key) {
                            Key.Enter, Key.NumPadEnter, Key.DirectionCenter -> {
                                onClick()
                                true
                            }

                            Key.DirectionLeft -> {
                                focusManager.moveFocus(FocusDirection.Left)
                                true
                            }

                            Key.DirectionRight -> {
                                focusManager.moveFocus(FocusDirection.Right)
                                true
                            }

                            else -> false
                        }
                    } else false
                }
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
                .clickable(
                    enabled = enabled,
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() }
                )
                {
                    onUserInteracted?.invoke()
                    onClick()
                }
                .focusable()
                .focusTarget()
                .background(
                    color = if (isFocused.value) Color.White else Color.Transparent,
                    shape = CircleShape
                )
                .border(
                    border = BorderStroke(2.dp, Color.White),
                    shape = CircleShape
                ),
                //
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = label,
                color = if (isFocused.value) activeColor else Color.White
            )
        }
    } else {
        Button(
            onClick = onClick,
            enabled = enabled,
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
            modifier = modifier,
            border = BorderStroke(1.dp, activeColor),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.Transparent,
                contentColor = activeColor
            )

        ) {
            Text(
                text = label,
                color = Color.White
            )
        }
    }
}
