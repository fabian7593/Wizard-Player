package com.wizardskull.library.player.handler

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.ui.focus.FocusRequester

import com.example.vlc.utils.NetworkMonitor
import com.wizardskull.library.player.config.Config.createLibVlcConfig
import com.wizardskull.library.player.config.PlayerConfig
import com.wizardskull.library.player.config.VideoItem
import com.wizardskull.library.player.utils.AppLogger
import com.wizardskull.library.viewmodel.VideoViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.videolan.libvlc.LibVLC
import org.videolan.libvlc.MediaPlayer

@Composable
fun HandlePlayerCleanup(
    viewModel: VideoViewModel,
    libVLC: LibVLC?
) {
    DisposableEffect(Unit) {
        onDispose {
            try {
                viewModel.disposePlayer()
                libVLC?.release()
            } catch (e: Exception) {
                AppLogger.error("HandlePlayerCleanup", "Cleanup error: ${e.message}")
            }
        }
    }
}

@Composable
fun HandleConnectionLoss(
    isConnected: Boolean,
    coroutineScope: CoroutineScope,
    mediaPlayer: MediaPlayer?,
    viewModel: VideoViewModel,
    showConnectionWarning: MutableState<Boolean>
) {
    LaunchedEffect(isConnected) {
        if (!isConnected) {
            coroutineScope.launch(Dispatchers.IO) {
                mediaPlayer?.pause()
                viewModel.setIsPlaying(false)
            }
            showConnectionWarning.value = true
            delay(3000)
            showConnectionWarning.value = false
        }
    }
}

@Composable
fun HandleNetworkMonitor(context: Context) {
    LaunchedEffect(Unit) {
        NetworkMonitor.start(context)
        AppLogger.debug("HandleNetworkMonitor", "Started monitoring network")
    }
}

@Composable
fun HandleMediaPlayerReinit(
    context: Context,
    config: PlayerConfig,
    currentItem: VideoItem?,
    mediaPlayer: MediaPlayer?,
    libVLC: LibVLC?,
    setMediaPlayer: (MediaPlayer?) -> Unit,
    setLibVLC: (LibVLC?) -> Unit,
    viewModel: VideoViewModel
) {
    LaunchedEffect(currentItem) {
        currentItem?.let { item ->
            try {
                mediaPlayer?.stop()
                mediaPlayer?.release()
                libVLC?.release()

                delay(100)

                val options = createLibVlcConfig(config)
                val newLib = LibVLC(context, options)
                val newPlayer = MediaPlayer(newLib)

                setLibVLC(newLib)
                setMediaPlayer(newPlayer)
                viewModel.mediaPlayer = newPlayer
                viewModel.observeVideoPosition()
                viewModel.prepareVideoUrl(item.url)
                viewModel.resetPlaybackTime()
                viewModel.mediaPlayer.position = 0f

                AppLogger.info("HandleMediaPlayerReinit", "Media player reinitialized for ${item.title}")
            } catch (e: Exception) {
                AppLogger.error("HandleMediaPlayerReinit", "⚠️ Error switching media player: ${e.message}")
            }
        }
    }
}

@Composable
fun HandleResumePlayback(
    isConnected: Boolean,
    currentItem: VideoItem?,
    mediaPlayer: MediaPlayer?,
    viewModel: VideoViewModel,
    coroutineScope: CoroutineScope
) {
    LaunchedEffect(isConnected, currentItem) {
        if (isConnected && !viewModel.isPlaying.value) {
            coroutineScope.launch(Dispatchers.IO) {
                try {
                    mediaPlayer?.play()
                    viewModel.setIsPlaying(true)
                    AppLogger.debug("HandleResumePlayback", "Playback resumed")
                } catch (e: Exception) {
                    AppLogger.error("HandleResumePlayback", "❌ Resume playback error: ${e.message}")
                }
            }
        }
    }
}

@Composable
fun HandleExitRequest(
    shouldExitApp: Boolean,
    currentTime: Long,
    onExit: () -> Unit,
    onGetCurrentTime: (Long) -> Unit
) {
    LaunchedEffect(shouldExitApp) {
        if (shouldExitApp) {
            onGetCurrentTime(currentTime)
            onExit()
            AppLogger.info("HandleExitRequest", "Exit requested at $currentTime")
        }
    }
}

@Composable
fun ReportCurrentPlaybackStatus(
    currentItem: VideoItem?,
    currentTime: Long,
    onGetCurrentTime: (Long) -> Unit,
    onGetCurrentItem: (VideoItem?) -> Unit,
    intervalMillis: Long = 180_000L
) {
    LaunchedEffect(currentItem) {
        while (true) {
            onGetCurrentTime(currentTime)
            onGetCurrentItem(currentItem)
            AppLogger.debug("ReportPlayback", "Time: $currentTime | Item: ${currentItem?.title}")
            delay(intervalMillis)
        }
    }
}


@Composable
fun HandleInitialFocus(
    showControls: Boolean,
    playFocusRequester: FocusRequester
) {
    LaunchedEffect(showControls) {
        if (showControls) {
            delay(200)
            playFocusRequester.requestFocus()
        }
    }
}
