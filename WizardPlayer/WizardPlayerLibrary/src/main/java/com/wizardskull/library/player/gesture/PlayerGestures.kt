package com.wizardskull.library.player.gesture

import android.view.KeyEvent
import androidx.activity.compose.BackHandler
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.foundation.focusable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import com.wizardskull.library.player.utils.AppLogger
import com.wizardskull.library.viewmodel.VideoViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.videolan.libvlc.MediaPlayer

fun Modifier.handlePlayerGestures(
    isPlaying: Boolean,
    videoUrl: String,
    showControls: Boolean,
    viewModel: VideoViewModel,
    isPlayButtonEnabled: Boolean,
    mediaPlayer: MediaPlayer?
): Modifier {
    return this
        .pointerInput(isPlaying) {
            detectTapGestures(
                onTap = {
                    viewModel.toggleControls(true)
                    viewModel.setUserInteracting(true)
                    viewModel.startUserInteractionTimeout()

                    if (!isPlayButtonEnabled) return@detectTapGestures

                    if (showControls && videoUrl.isNotEmpty() && mediaPlayer != null) {
                        CoroutineScope(Dispatchers.IO).launch {
                            try {
                                if (isPlaying) {
                                    mediaPlayer.pause()
                                    viewModel.setIsPlaying(false)
                                } else {
                                    mediaPlayer.play()
                                    viewModel.setIsPlaying(true)
                                }
                            } catch (e: Exception) {
                                AppLogger.error("PlayerGestures", "❌ Error toggling play/pause: ${e.message}")
                            }
                        }
                    }
                }
            )
        }
        .onKeyEvent { keyEvent ->
            val isBack = keyEvent.nativeKeyEvent.keyCode == KeyEvent.KEYCODE_BACK
            val isDown = keyEvent.nativeKeyEvent.action == KeyEvent.ACTION_DOWN

            when {
                isDown && isBack -> {
                    if (viewModel.showControls.value) {
                        viewModel.toggleControls(false)
                    } else {
                        viewModel.requestExit()
                    }
                    true
                }

                isDown -> {
                    viewModel.toggleControls(true)
                    viewModel.setUserInteracting(true)
                    viewModel.startUserInteractionTimeout()
                    false
                }

                else -> false
            }
        }
        .focusable()
}


@Composable
fun HandleBackPressVisibleControls(viewModel: VideoViewModel) {
    BackHandler {
        if (viewModel.showControls.value) {
            viewModel.toggleControls(false)
        } else {
            viewModel.requestExit()
        }
    }
}