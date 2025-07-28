package com.wizardskull.library.widgets

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.focusable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.*
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp

/**
 * Custom video slider that supports keyboard, touch, and drag interactions.
 *
 * @param currentTime Current playback time in seconds.
 * @param videoLength Total video duration in seconds.
 * @param onSeekChanged Called continuously while user is changing the seek position.
 * @param onSeekFinished Called once when user finishes seeking.
 * @param focusColor Color of the thumb when focused or dragging (default: white).
 * @param inactiveColor Color of the track's background (default: gray).
 * @param activeColor Color of the filled track (default: theme's primary).
 * @param onFocusDown Triggered when user presses the DOWN key (useful for D-pad).
 * @param onUserInteracted Called when the user interacts, to keep UI controls visible.
 * @param modifier Modifier to apply to the slider.
 */
@Composable
fun CustomVideoSlider(
    currentTime: Long,
    videoLength: Long,
    onSeekChanged: (Long) -> Unit,
    onSeekFinished: (Long) -> Unit,
    focusColor: Color? = null,
    inactiveColor: Color? = null,
    activeColor: Color? = null,
    onFocusDown: (() -> Unit)? = null,
    onUserInteracted: (() -> Unit)? = null,
    enabled: Boolean = true,
    modifier: Modifier = Modifier
) {
    // ─────────────────────────────────────────────────────────────
    // ▶️ State & Initialization
    // ─────────────────────────────────────────────────────────────

    var sliderPosition by remember { mutableStateOf(currentTime.toFloat()) } // Seek position in seconds
    var isDragging by remember { mutableStateOf(false) }                     // Whether user is dragging
    val isFocused = remember { mutableStateOf(false) }                       // Whether component is focused
    val keyHoldStartTime = remember { mutableStateOf<Long?>(null) }         // Track how long key is held
    val focusRequester = remember { FocusRequester() }

    // ─────────────────────────────────────────────────────────────
    // 🎨 Visual Parameters
    // ─────────────────────────────────────────────────────────────

    val density = LocalDensity.current
    val finalFocusColor = focusColor ?: Color.White
    val finalInactiveColor = inactiveColor ?: Color.Gray
    val finalActiveColor = activeColor ?: Color.Cyan

    val trackHeight = 4.dp
    val thumbRadius = 10.dp
    val thumbColor = if (isFocused.value || isDragging) finalFocusColor else finalActiveColor

    // ─────────────────────────────────────────────────────────────
    // 🔁 Update sliderPosition when not dragging
    // ─────────────────────────────────────────────────────────────

    LaunchedEffect(currentTime) {
        if (!isDragging && !isFocused.value) {
            sliderPosition = currentTime.toFloat()
        }
    }

    // ─────────────────────────────────────────────────────────────
    // 📦 Main Container
    // ─────────────────────────────────────────────────────────────

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp)
            .height(thumbRadius * 2)
            .alpha(if (enabled) 1f else 0.4f)
            .focusRequester(focusRequester)

            // 🧠 Track focus state (for keyboard)
            .onFocusChanged { focusState ->
                isFocused.value = focusState.isFocused
                if (!focusState.isFocused) {
                    keyHoldStartTime.value = null
                }
            }

            // ⌨️ Handle D-pad key events (Left/Right/Down)
            .onKeyEvent { event ->
                if (!isFocused.value || !enabled) return@onKeyEvent false

                try {
                    onUserInteracted?.invoke()

                    when {
                        event.key == Key.DirectionRight && event.type == KeyEventType.KeyDown -> {
                            val now = System.currentTimeMillis()
                            val start = keyHoldStartTime.value ?: now
                            keyHoldStartTime.value = start

                            val held = now - start
                            val step = when {
                                held >= 7_000 -> 30L
                                held >= 5_000 -> 20L
                                else -> 15L
                            }

                            sliderPosition = (sliderPosition + step).coerceAtMost(videoLength.toFloat())
                            onSeekChanged(sliderPosition.toLong())
                            onSeekFinished(sliderPosition.toLong())
                            true
                        }

                        event.key == Key.DirectionLeft && event.type == KeyEventType.KeyDown -> {
                            val now = System.currentTimeMillis()
                            val start = keyHoldStartTime.value ?: now
                            keyHoldStartTime.value = start

                            val held = now - start
                            val step = when {
                                held >= 10_000 -> 30L
                                held >= 5_000 -> 20L
                                else -> 10L
                            }

                            sliderPosition = (sliderPosition - step).coerceAtLeast(0f)
                            onSeekChanged(sliderPosition.toLong())
                            onSeekFinished(sliderPosition.toLong())
                            true
                        }

                        event.key == Key.DirectionDown && event.type == KeyEventType.KeyDown -> {
                            onFocusDown?.invoke()
                            true
                        }

                        else -> false
                    }
                } catch (e: Exception) {
                    println("⚠️ Error processing key event: ${e.message}")
                    false
                }
            }

            // 🖱 Handle taps to seek
            .pointerInput(videoLength) {
                if (!enabled) return@pointerInput

                detectTapGestures(onTap = { offset ->
                    try {
                        val width = size.width.toFloat()
                        val positionFraction = (offset.x / width).coerceIn(0f, 1f)
                        val newTime = (positionFraction * videoLength).toLong()

                        sliderPosition = newTime.toFloat()
                        onSeekChanged(newTime)
                        onSeekFinished(newTime)
                        onUserInteracted?.invoke()
                    } catch (e: Exception) {
                        println("⚠️ Error during tap seek: ${e.message}")
                    }
                })
            }

            // 🖱 Handle drag gestures to seek
            .pointerInput(videoLength) {
                if (!enabled) return@pointerInput

                detectDragGestures(
                    onDragStart = {
                        onUserInteracted?.invoke()
                        isDragging = true
                    },
                    onDragEnd = {
                        isDragging = false
                        try {
                            onSeekFinished(sliderPosition.toLong())
                        } catch (e: Exception) {
                            println("⚠️ Error finishing drag: ${e.message}")
                        }
                    },
                    onDragCancel = {
                        isDragging = false
                    },
                    onDrag = { change, dragAmount ->
                        try {
                            val width = size.width
                            val currentX = (sliderPosition / videoLength.toFloat()) * width
                            val newX = (currentX + dragAmount.x).coerceIn(0f, width.toFloat())
                            val newPos = ((newX / width) * videoLength).coerceIn(0f, videoLength.toFloat())

                            sliderPosition = newPos
                            onSeekChanged(newPos.toLong())

                            onUserInteracted?.invoke()
                            change.consume()
                        } catch (e: Exception) {
                            println("⚠️ Error during drag seek: ${e.message}")
                        }
                    }
                )
            }

            .focusable()
    ) {

        val animatedThumbRadius by animateFloatAsState(
            targetValue = if (isFocused.value) thumbRadius.value * 1.5f else thumbRadius.value,
            animationSpec = tween(durationMillis = 150),
            label = "Slider Focus Thumb Animation"
        )

        // ─────────────────────────────────────────────────────────
        // 🎨 Draw slider: background, progress, thumb
        // ─────────────────────────────────────────────────────────

        Canvas(modifier = Modifier.fillMaxSize()) {
            val canvasWidth = size.width
            val canvasHeight = size.height

            val thumbPx = with(density) { animatedThumbRadius.dp.toPx() }


            val trackY = canvasHeight / 2
            val progressFraction = sliderPosition / videoLength.toFloat()
            val thumbX = progressFraction * canvasWidth

            // Background line (inactive track)
            drawLine(
                color = finalInactiveColor,
                start = Offset(0f, trackY),
                end = Offset(canvasWidth, trackY),
                strokeWidth = with(density) { trackHeight.toPx() }
            )

            // Foreground line (active progress)
            drawLine(
                color = finalActiveColor,
                start = Offset(0f, trackY),
                end = Offset(thumbX, trackY),
                strokeWidth = with(density) { trackHeight.toPx() }
            )

            // Thumb (circular progress handle)
            drawCircle(
                color = thumbColor,
                radius = thumbPx,
                center = Offset(thumbX, trackY)
            )
        }
    }
}
