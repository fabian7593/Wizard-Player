package com.wizardskull.library.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wizardskull.library.player.utils.AppLogger
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import org.videolan.libvlc.Media
import org.videolan.libvlc.MediaPlayer
import java.util.concurrent.Executors

class VideoViewModel : ViewModel() {

    // ───────────────────────────────────────────────────────────────
    // ▶ Playback State
    // ───────────────────────────────────────────────────────────────
    // Indicates whether the video is currently playing
    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying

    // Indicates whether the video is buffering
    private val _isBuffering = MutableStateFlow(true)
    val isBuffering: StateFlow<Boolean> = _isBuffering.asStateFlow()

    // Current playback time in seconds
    private val _currentTime = MutableStateFlow(0L)
    val currentTime: StateFlow<Long> = _currentTime.asStateFlow()

    // Total duration of the video in seconds
    private val _videoLength = MutableStateFlow(0L)
    val videoLength: StateFlow<Long> = _videoLength.asStateFlow()

    // Flag to indicate seeking in progress
    private var seeking: Boolean = false

    // Last known playback position in milliseconds
    private var lastKnownPosition: Long = 0
    private var recoveringFromFreeze = false

    // ───────────────────────────────────────────────────────────────
    // 👤 User Interaction State
    // ───────────────────────────────────────────────────────────────
    // Indicates whether the user is currently interacting with controls
    private val _isUserInteracting = MutableStateFlow(false)
    val isUserInteracting: StateFlow<Boolean> = _isUserInteracting.asStateFlow()

    // ───────────────────────────────────────────────────────────────
    // 🎛 UI Controls Visibility
    // ───────────────────────────────────────────────────────────────
    // Whether the video controls are currently shown on screen
    private val _showControls = MutableStateFlow(true)
    val showControls: StateFlow<Boolean> = _showControls.asStateFlow()

    // ───────────────────────────────────────────────────────────────
    // 📺 Media Player & Video Source
    // ───────────────────────────────────────────────────────────────

    // Video URL to be played
    private val _videoUrl = MutableStateFlow("")
    val videoUrl: StateFlow<String> = _videoUrl.asStateFlow()

    // VLC media player instance (must be initialized externally)
    lateinit var mediaPlayer: MediaPlayer

    // ───────────────────────────────────────────────────────────────
    // 🚪 App Exit Management (Double back press)
    // ───────────────────────────────────────────────────────────────
    // Timestamp of the last back press
    private var lastBackPressTime: Long = 0

    // Show toast/prompt to press back again to exit
    private val _showExitPrompt = MutableStateFlow(false)
    val showExitPrompt: StateFlow<Boolean> = _showExitPrompt.asStateFlow()

    // Flag that triggers actual app exit
    private val _shouldExitApp = MutableStateFlow(false)
    val shouldExitApp: StateFlow<Boolean> = _shouldExitApp.asStateFlow()

    // ───────────────────────────────────────────────────────────────
    // 🔁 INIT block - Setup Observers
    // ───────────────────────────────────────────────────────────────
    init {
        observeVideoPosition()
        detectFreeze()
        autoHideControlsWhenPlaying()
    }

    // ───────────────────────────────────────────────────────────────
    // 🎞 Load & Play Video
    // ───────────────────────────────────────────────────────────────
    fun prepareVideoUrl(url: String) {
        _videoUrl.value = url
    }

    // ───────────────────────────────────────────────────────────────
    // 🕓 Track Playback Time
    // ───────────────────────────────────────────────────────────────
    private var observeJob: Job? = null

    fun observeVideoPosition() {
        observeJob?.cancel()
        observeJob = viewModelScope.launch {
            while (isActive) {
                delay(1000)
                try {
                    if (::mediaPlayer.isInitialized && _isPlaying.value && !seeking) {
                        _currentTime.value = mediaPlayer.time / 1000
                    }
                } catch (e: Exception) {
                    AppLogger.error("VideoViewModel", "❌ Error getting playback time: ${e.message}")
                    cancel()
                }
            }
        }
    }

    // ───────────────────────────────────────────────────────────────
    // 🧊 Detect Frozen Playback & Auto-Recover
    // ──────────────────────────────────────────────────────────────
    private fun detectFreeze() {
        viewModelScope.launch {
            while (true) {
                delay(15000)

                if (!::mediaPlayer.isInitialized || !mediaPlayer.vlcVout.areViewsAttached()) continue

                try {
                    val current = mediaPlayer.time
                    val duration = mediaPlayer.length.coerceAtLeast(1)

                    if (recoveringFromFreeze) {
                        lastKnownPosition = current
                        continue
                    }

                    if (current > 0 && current == lastKnownPosition && _isPlaying.value) {

                        val position = (current.toFloat() / duration).coerceIn(0f, 0.99f)

                        recoveringFromFreeze = true
                        mediaPlayer.stop()
                        delay(300)

                        val media = Media(mediaPlayer.libVLC, Uri.parse(videoUrl.value))
                        media.setHWDecoderEnabled(true, false)
                        mediaPlayer.media = media
                        media.release()

                        val vout = mediaPlayer.vlcVout
                        if (!vout.areViewsAttached()) {
                            AppLogger.warning("VideoViewModel", "⚠️ SurfaceView not attached. Cannot play.")
                            recoveringFromFreeze = false
                            continue
                        }

                        mediaPlayer.play()
                        delay(500)

                        mediaPlayer.setPosition(position)
                        _currentTime.value = (position * duration).toLong() / 1000
                        _isPlaying.value = true
                        _isBuffering.value = false

                        // ⏳ Cooldown
                        delay(6000)
                        recoveringFromFreeze = false
                    }

                    lastKnownPosition = current

                } catch (e: Exception) {
                    AppLogger.error("VideoViewModel", "❌ Error in freeze recovery: ${e.message}")
                    recoveringFromFreeze = false
                }
            }
        }
    }

    // ───────────────────────────────────────────────────────────────
    // Delete player
    // ───────────────────────────────────────────────────────────────
    fun disposePlayer() {
        try {
            observeJob?.cancel()
            mediaPlayer.stop()
            mediaPlayer.vlcVout.detachViews()
            mediaPlayer.setEventListener(null)
            mediaPlayer.release()
        } catch (e: Exception) {
            AppLogger.error("VideoViewModel", "Error disposing player: ${e.message}")
        }
    }

    // ───────────────────────────────────────────────────────────────
    // 🙈 Auto-Hide Controls After Inactivity
    // ───────────────────────────────────────────────────────────────
    fun autoHideControlsWhenPlaying() {
        viewModelScope.launch {
            while (true) {
                if (_isPlaying.value && _showControls.value && !_isUserInteracting.value) {
                    delay(10_000)
                    if (!_isUserInteracting.value) {
                        _showControls.value = false
                    }
                }
                delay(1000)
            }
        }
    }

    // ───────────────────────────────────────────────────────────────
    // 🧑 User Interaction Tracking
    // ───────────────────────────────────────────────────────────────
    fun setUserInteracting(interacting: Boolean) {
        _isUserInteracting.value = interacting
    }

    fun startUserInteractionTimeout() {
        viewModelScope.launch {
            delay(10_000)
            _isUserInteracting.value = false
        }
    }

    // ───────────────────────────────────────────────────────────────
    // 🎮 UI Controls State
    // ───────────────────────────────────────────────────────────────
    fun toggleControls(visible: Boolean = true) {
        _showControls.value = visible
    }

    // ───────────────────────────────────────────────────────────────
    // 🔁 Playback Event Callbacks
    // ───────────────────────────────────────────────────────────────
    fun setIsPlaying(value: Boolean) {
        _isPlaying.value = value
    }

    fun onPlaybackChanged(playing: Boolean) {
        _isPlaying.value = playing
    }

    fun onBufferingChanged(buffering: Boolean) {
        _isBuffering.value = buffering
    }

    fun onDurationChanged(durationMs: Long) {
        _videoLength.value = durationMs / 1000
    }

    fun resetPlaybackTime() {
        _currentTime.value = 0L
    }

    // ───────────────────────────────────────────────────────────────
    // ⏩ Seek Operations
    // ───────────────────────────────────────────────────────────────
    fun onSeekStart() {
        seeking = true
    }

    fun onSeekUpdate(newValue: Long) {
        _currentTime.value = newValue
    }

    fun onSeekFinished() {
        try {
            val safeSeek = minOf(_currentTime.value, _videoLength.value - 1)
            val seekFraction = (safeSeek.toFloat() / _videoLength.value).coerceIn(0f, 0.999f)
            mediaPlayer.setPosition(seekFraction)
        } catch (e: Exception) {
            AppLogger.error("VideoViewModel", "❌ Error while seeking: ${e.message}")
        } finally {
            seeking = false
        }
    }

    // ───────────────────────────────────────────────────────────────
    //  Exit App (Double Back Press)
    // ───────────────────────────────────────────────────────────────
    fun requestExit() {
        val now = System.currentTimeMillis()
        if (now - lastBackPressTime < 2000) {
            _shouldExitApp.value = true
        } else {
            _showExitPrompt.value = true
            lastBackPressTime = now
            viewModelScope.launch {
                delay(2000)
                _showExitPrompt.value = false
            }
        }
    }


    private val vlcExecutor = Executors.newSingleThreadExecutor {
        Thread(it, "VLC-Executor").apply { priority = Thread.MAX_PRIORITY }
    }
    fun getExecutor() = vlcExecutor
    override fun onCleared() {
        super.onCleared()
        try {
            vlcExecutor.shutdownNow()
        } catch (e: Exception) {
            AppLogger.warning("VideoViewModel", "⚠️ Error shutting down executor: ${e.message}")
        }
    }
}