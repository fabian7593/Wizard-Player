package com.wizardskull.library.player

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.vlc.utils.LanguageMatcher
import com.example.vlc.utils.NetworkMonitor
import com.wizardskull.library.player.composable.ToastAnimatedVisibility
import com.wizardskull.library.player.composable.WatermarkBranding
import com.wizardskull.library.player.config.Config.AspectRatioOptions
import com.wizardskull.library.player.config.Config.applyAspectRatio
import com.wizardskull.library.player.config.PlayerConfig
import com.wizardskull.library.player.config.PlayerLabels
import com.wizardskull.library.player.config.VideoItem
import com.wizardskull.library.player.gesture.HandleBackPressVisibleControls
import com.wizardskull.library.player.gesture.handlePlayerGestures
import com.wizardskull.library.player.handler.HandleConnectionLoss
import com.wizardskull.library.player.handler.HandleExitRequest
import com.wizardskull.library.player.handler.HandleInitialFocus
import com.wizardskull.library.player.handler.HandleMediaPlayerReinit
import com.wizardskull.library.player.handler.HandleNetworkMonitor
import com.wizardskull.library.player.handler.HandlePlayerCleanup
import com.wizardskull.library.player.handler.HandleResumePlayback
import com.wizardskull.library.player.handler.ReportCurrentPlaybackStatus
import com.wizardskull.library.player.surface.WizardPlayerView
import com.wizardskull.library.player.utils.AppLogger
import com.wizardskull.library.player.utils.SetupFullscreenLandscape
import com.wizardskull.library.player.utils.playNextOrExit
import com.wizardskull.library.utils.GeneralUtils
import com.wizardskull.library.viewmodel.VideoViewModel
import com.wizardskull.library.widgets.AdaptiveNextButton
import com.wizardskull.library.widgets.ContinueWatchingDialog
import com.wizardskull.library.widgets.CustomVideoSlider
import com.wizardskull.library.widgets.ScrollableDialogList
import com.wizardskull.library.widgets.TvIconButton
import org.videolan.libvlc.LibVLC
import org.videolan.libvlc.MediaPlayer

/**
 * WizardVideoPlayer composable
 * Displays fullscreen VLC-based video playback with customizable controls and multilingual labels.
 */
@Composable
fun WizardVideoPlayer(
    config: PlayerConfig,
    labels: PlayerLabels,
    onAspectRatioChanged: (String) -> Unit,
    onAudioChanged: (String) -> Unit,
    onSubtitleChanged: (String) -> Unit,
    onGetCurrentTime: (Long) -> Unit,
    onGetCurrentItem: (VideoItem?) -> Unit,
    onExit: () -> Unit
) {
    val context = LocalContext.current
    SetupFullscreenLandscape(context)

    // ───────────────────────────────────────────────────────
    // 🎞️ VLC engine and media player setup
    // ───────────────────────────────────────────────────────
    var libVLC by remember { mutableStateOf<LibVLC?>(null) }
    var mediaPlayer by remember { mutableStateOf<MediaPlayer?>(null) }

    // ───────────────────────────────────────────────────────
    // 🔁 State and UI holders
    // ───────────────────────────────────────────────────────
    // Used to request focus for the play/pause button (especially important on Android TV).
    val playFocusRequester = remember { FocusRequester() }

    // Used to request focus for the video slider (for keyboard/remote navigation).
    val sliderFocusRequester = remember { FocusRequester() }

    // The main ViewModel handling player state and logic.
    val viewModel = remember { VideoViewModel() }

    // Indicates whether the video is currently playing.
    val isPlaying by viewModel.isPlaying.collectAsState()

    // Indicates whether the video is buffering/loading.
    val isBuffering by viewModel.isBuffering.collectAsState()

    // The current playback position of the video in seconds.
    val currentTime by viewModel.currentTime.collectAsState()

    // The total duration of the video in seconds.
    val videoLength by viewModel.videoLength.collectAsState()

    // Whether the playback controls should be visible on screen.
    val showControls by viewModel.showControls.collectAsState()

    // The current URL of the video being played.
    val videoUrl by viewModel.videoUrl.collectAsState()

    // Whether the player should trigger an exit action (e.g. user requested exit).
    val shouldExitApp by viewModel.shouldExitApp.collectAsState()

    // Whether to display a prompt asking the user to confirm exiting.
    val showExitPrompt by viewModel.showExitPrompt.collectAsState()

    // Controls the visibility of the audio track selection dialog.
    val showAudioDialog = remember { mutableStateOf(false) }

    // Controls the visibility of the subtitle track selection dialog.
    var showSubtitlesDialog by remember { mutableStateOf(false) }

    // Controls the visibility of the aspect ratio selection dialog.
    var showAspectRatioDialog by remember { mutableStateOf(false) }

    // A list of available audio tracks (index and display name).
    val audioTracks = remember { mutableStateListOf<Pair<Int, String>>() }

    // A list of available subtitle tracks (index and display name).
    val subtitleTracks = remember { mutableStateListOf<Pair<Int, String>>() }

    // Whether the play/pause button is currently focused.
    val isPlayFocused = remember { mutableStateOf(false) }

    // Whether the subtitles button is currently focused.
    val isSubFocused = remember { mutableStateOf(false) }

    // Whether the audio button is currently focused.
    val isAudioFocused = remember { mutableStateOf(false) }

    // Whether the aspect ratio button is currently focused.
    val isAspectFocused = remember { mutableStateOf(false) }

    // Whether the "Next" button is currently focused.
    val nextFocused = remember { mutableStateOf(false) }

    //connection
    val isConnected by NetworkMonitor.isConnected.collectAsState()
    val showConnectionWarning = remember { mutableStateOf(false) }

    // Determines the starting index of the video to be played, based on the episode number.
    // If the episode number is not found, defaults to index 0.
    val initialIndex = remember(config.startEpisodeNumber, config.videoItems) {
        config.videoItems.indexOfFirst { item ->
            item.episodeNumber?.toInt() == config.startEpisodeNumber
        }.takeIf { it >= 0 } ?: 0
    }

    // Holds the currently selected video index.
    val currentIndex = remember { mutableStateOf(initialIndex) }

    // The actual video item (title, URL, subtitle, etc.) based on the current index.
    val currentItem = remember(currentIndex.value) {
        config.videoItems.getOrNull(currentIndex.value)
    }

    //Show the dialog of continue episode or reset
    val showContinueDialog = remember { mutableStateOf(false) }
    val TAG = "WizardVideoPlayer"

    // ───────────────────────────────────────────────────────
    // Validate Internet Connection
    // ───────────────────────────────────────────────────────
    val coroutineScope = rememberCoroutineScope()

    //change the player to new video
    HandleMediaPlayerReinit(
        currentItem = currentItem,
        context = context,
        mediaPlayer = mediaPlayer,
        libVLC = libVLC,
        setMediaPlayer = { mediaPlayer = it },
        setLibVLC = { libVLC = it },
        viewModel = viewModel,
        config = config
    )
    //get current item and get current time
    ReportCurrentPlaybackStatus(
        currentItem = currentItem,
        currentTime = currentTime,
        onGetCurrentTime = onGetCurrentTime,
        onGetCurrentItem = onGetCurrentItem,
        intervalMillis = config.playbackProgress
    )

    //Network Monitor
    HandleNetworkMonitor(context)
    //Reconnect when get internet
    HandleResumePlayback(
        isConnected = isConnected,
        currentItem = currentItem,
        mediaPlayer = mediaPlayer,
        viewModel = viewModel,
        coroutineScope = coroutineScope
    )
    //Show toast when the connection is loss
    HandleConnectionLoss(
        isConnected = isConnected,
        coroutineScope = coroutineScope,
        mediaPlayer = mediaPlayer,
        viewModel = viewModel,
        showConnectionWarning = showConnectionWarning
    )

    //Press back button
    //get the time and close
    HandleExitRequest(
        shouldExitApp = shouldExitApp,
        currentTime = currentTime,
        onExit = onExit,
        onGetCurrentTime = onGetCurrentTime
    )
    // Handle back button behavior
    HandleBackPressVisibleControls(viewModel)

    //focus when visible buttons are visible
    HandleInitialFocus(
        showControls = showControls,
        playFocusRequester = playFocusRequester
    )


    // Similar to Garbage Collector
    HandlePlayerCleanup(viewModel, libVLC)

    // ───────────────────────────────────────────────────────
    // 🧱 Main player container with interactions
    // ───────────────────────────────────────────────────────
    Box(

        modifier = Modifier
            .fillMaxSize()
            .handlePlayerGestures(
                isPlaying = isPlaying,
                videoUrl = videoUrl,
                showControls = showControls,
                viewModel = viewModel,
                mediaPlayer = mediaPlayer,
                isPlayButtonEnabled = videoUrl.isNotEmpty() && videoLength > 0 && isConnected,
            )
    ) {
        // ───── Video Background ─────
        Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
            if (videoUrl.isNotEmpty() && mediaPlayer != null) {

                key(videoUrl + mediaPlayer.hashCode()) {
                    val executor = viewModel.getExecutor()

                    WizardPlayerView(
                        modifier = Modifier.fillMaxSize(),
                        config = config,
                        mediaPlayer = mediaPlayer!!,
                        videoUrl = videoUrl,
                        executor = executor,
                        onAspectRatioChanged = {
                            onAspectRatioChanged(it)
                        },
                        onAudioChanged = {
                            onAudioChanged(it)
                        },
                        onTracksLoaded = {
                            audioTracks.clear()
                            audioTracks.addAll(it)
                        },
                        onSubtitleLoaded = {
                            subtitleTracks.clear()
                            subtitleTracks.addAll(it)
                        },
                        onPlaybackStateChanged = { viewModel.onPlaybackChanged(it) },
                        onBufferingChanged = {
                            val effectiveBuffering = it || !isConnected
                            viewModel.onBufferingChanged(effectiveBuffering)
                        },
                        onEndReached = {
                            playNextOrExit(
                                config = config,
                                currentIndex = currentIndex,
                                currentTime = currentTime,
                                onGetCurrentTime = onGetCurrentTime,
                                onExit = onExit
                            )
                        },
                        onDurationChanged = { viewModel.onDurationChanged(it) },
                        onStart = {
                            currentItem?.lastSecondView?.toLong()?.takeIf { it > 0 }?.let {
                                showContinueDialog.value = true
                            }
                        }
                    )
                }
            }
        }

        // ───── Buffer Indicator ─────
        if (isBuffering) {
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center),
                color = Color.White
            )
        }

        // ───── UI Controls Overlay ─────
        AnimatedVisibility(
            visible = showControls,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Column(modifier = Modifier.fillMaxSize().padding(bottom = 16.dp)) {

                // ─── Header Section (Titles + Next) ───
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    Column(modifier = Modifier.align(Alignment.CenterStart)) {
                        Text(currentItem?.title ?: "", color = Color.White, style = MaterialTheme.typography.titleLarge)
                        Text(currentItem?.subtitle ?: "", color = Color.LightGray,  style = MaterialTheme.typography.bodyMedium)
                    }

                    if (config.videoItems.size > 1 && currentIndex.value < config.videoItems.lastIndex) {
                        AdaptiveNextButton(
                            label = labels.nextLabel,
                            isFocused = nextFocused,
                            enabled = isConnected,
                            onClick = {
                                try {
                                    currentIndex.value += 1
                                    isPlayFocused.value = true
                                    playFocusRequester.requestFocus()
                                } catch (e: Exception) {
                                    AppLogger.warning(TAG, "⚠️ Failed to switch video: ${e.message}")
                                }
                            },
                            activeColor = Color(config.primaryColor),
                            onUserInteracted = {
                                viewModel.setUserInteracting(true)
                                viewModel.startUserInteractionTimeout()
                            },
                            modifier = Modifier.align(Alignment.CenterEnd)
                        )
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                // ─── Progress & Timestamp ───
                Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp)) {
                    Text(
                        text = "${GeneralUtils.formatTime(currentTime)} / ${GeneralUtils.formatTime(videoLength)}",
                        modifier = Modifier.align(Alignment.End),
                        color = Color.White
                    )

                    CustomVideoSlider(
                        enabled = isConnected,
                        currentTime = currentTime,
                        videoLength = videoLength,
                        onSeekChanged = {
                            viewModel.onSeekStart()
                            viewModel.onSeekUpdate(it)
                        },
                        onSeekFinished = { viewModel.onSeekFinished() },
                        onFocusDown = { playFocusRequester.requestFocus() },
                        onUserInteracted = {
                            viewModel.setUserInteracting(true)
                            viewModel.startUserInteractionTimeout()
                        },
                        activeColor =  Color(config.primaryColor),
                        focusColor = Color(config.focusColor),
                        inactiveColor = Color(config.inactiveColor),
                        modifier = Modifier.focusRequester(sliderFocusRequester)
                    )
                }

                // ─── Playback Buttons ───
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TvIconButton(
                        onClick = {
                            if (videoUrl.isEmpty() || videoLength <= 0 || !isConnected) return@TvIconButton
                            try {
                                if (isPlaying) mediaPlayer?.pause() else mediaPlayer?.play()
                                viewModel.setIsPlaying(!isPlaying)
                                viewModel.toggleControls(true)
                            } catch (e: Exception) {
                                AppLogger.error(TAG, "❌ Playback toggle failed: ${e.message}")
                            }
                        },
                        focusRequester = playFocusRequester,
                        isFocused = isPlayFocused,
                        icon = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        description = labels.nextLabel,
                        activeColor =  Color(config.primaryColor),
                        focusColor = Color(config.focusColor),
                        enabled = videoUrl.isNotEmpty() && videoLength > 0 && isConnected,
                        onUserInteracted = {
                            viewModel.setUserInteracting(true)
                            viewModel.startUserInteractionTimeout()
                        },
                        diameterButtonCircleDp = config.diameterButtonCircleDp.dp,
                        iconSizeDp = config.iconSizeDp.dp
                    )

                    // ─── Extra Buttons Row ───
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        if (config.showSubtitleButton) {
                            TvIconButton(
                                onClick = { if (subtitleTracks.isNotEmpty()) showSubtitlesDialog = true },
                                isFocused = isSubFocused,
                                icon = Icons.Default.Subtitles,
                                description = labels.subtitleLabel,
                                activeColor =  Color(config.primaryColor),
                                focusColor = Color(config.focusColor),
                                enabled = subtitleTracks.isNotEmpty() && isConnected,
                                onUserInteracted = {
                                    viewModel.setUserInteracting(true)
                                    viewModel.startUserInteractionTimeout()
                                },
                                diameterButtonCircleDp = config.diameterButtonCircleDp.dp,
                                iconSizeDp = config.iconSizeDp.dp
                            )
                        }

                        if (config.showAudioButton) {
                            TvIconButton(
                                onClick = { if (audioTracks.isNotEmpty()) showAudioDialog.value = true },
                                isFocused = isAudioFocused,
                                icon = Icons.Default.VolumeUp,
                                description = labels.audioLabel,
                                activeColor =  Color(config.primaryColor),
                                focusColor = Color(config.focusColor),
                                enabled = audioTracks.isNotEmpty() && isConnected,
                                onUserInteracted = {
                                    viewModel.setUserInteracting(true)
                                    viewModel.startUserInteractionTimeout()
                                },
                                diameterButtonCircleDp = config.diameterButtonCircleDp.dp,
                                iconSizeDp = config.iconSizeDp.dp
                            )
                        }

                        if (config.showAspectRatioButton) {
                            TvIconButton(
                                onClick = {  if (audioTracks.isNotEmpty()) showAspectRatioDialog = true },
                                isFocused = isAspectFocused,
                                icon = Icons.Default.AspectRatio,
                                description = labels.aspectRatioLabel,
                                activeColor =  Color(config.primaryColor),
                                focusColor = Color(config.focusColor),
                                enabled = audioTracks.isNotEmpty() && isConnected,
                                onUserInteracted = {
                                    viewModel.setUserInteracting(true)
                                    viewModel.startUserInteractionTimeout()
                                },
                                diameterButtonCircleDp = config.diameterButtonCircleDp.dp,
                                iconSizeDp = config.iconSizeDp.dp
                            )
                        }
                    }
                }
            }
        }

        // ───── Audio / Subtitle / Aspect Dialogs ─────
        if (showAudioDialog.value) {
            ScrollableDialogList(
                title = labels.selectAudioTitle,
                items = audioTracks,
                onItemSelected = { id, name ->
                    mediaPlayer?.setAudioTrack(id)
                    val langCode = LanguageMatcher.detectLanguageCode(name.toString())
                    onAudioChanged.invoke(langCode.toString())
                                 },
                onDismiss = { showAudioDialog.value = false },
                onUserInteracted = {
                    viewModel.setUserInteracting(true)
                    viewModel.startUserInteractionTimeout()
                }
            )
        }

        if (showSubtitlesDialog) {
            ScrollableDialogList(
                title = labels.selectSubtitleTitle,
                items = subtitleTracks,
                onItemSelected = { id, name ->
                    mediaPlayer?.setSpuTrack(id)
                    val langCode = LanguageMatcher.detectSubtitleCode(name.toString())
                    onSubtitleChanged.invoke(langCode.toString())
                                 },
                onDismiss = { showSubtitlesDialog = false },
                onUserInteracted = {
                    viewModel.setUserInteracting(true)
                    viewModel.startUserInteractionTimeout()
                }
            )
        }

        if (showAspectRatioDialog) {
            ScrollableDialogList(
                title = labels.aspectRatioTitle,
                items = AspectRatioOptions.mapIndexed { index, pair -> index to pair.second },
                onItemSelected = { index, name ->
                    val selectedKey = AspectRatioOptions[index].first
                    applyAspectRatio(mediaPlayer, selectedKey) {
                        onAspectRatioChanged.invoke(it)
                    }
                },

                onDismiss = { showAspectRatioDialog = false },
                onUserInteracted = {
                    viewModel.setUserInteracting(true)
                    viewModel.startUserInteractionTimeout()
                }
            )
        }

        //Open the dialog of continue watching
        if (showContinueDialog.value) {
            ContinueWatchingDialog(
                labels = labels,
                activeColor = Color(config.primaryColor),
                onContinue = {
                    viewModel.onSeekUpdate(currentItem?.lastSecondView?.toLong() ?: 0L)
                    viewModel.onSeekFinished()
                    showContinueDialog.value = false
                },
                onRestart = {
                    showContinueDialog.value = false
                },
                onDismiss = {
                    showContinueDialog.value = false
                },
                onUserInteracted = {
                    viewModel.setUserInteracting(true)
                    viewModel.startUserInteractionTimeout()
                }
            )
        }

        // ───── Watermark Branding ─────
        WatermarkBranding(
            modifier = Modifier
                .align(Alignment.BottomEnd),
            config = config,
            showControls = showControls
        )

        // ───── Exit Prompt (double back) ─────
        ToastAnimatedVisibility(isVisible = showExitPrompt,
            text = labels.exitPrompt,
            modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 32.dp))

        // ⚡ Internet connection warning
        ToastAnimatedVisibility(isVisible = showConnectionWarning.value,
                                text = labels.errorConnectionMessage,
                                modifier = Modifier
                                            .align(Alignment.BottomCenter)
                                            .padding(bottom = 32.dp))
    }
}
