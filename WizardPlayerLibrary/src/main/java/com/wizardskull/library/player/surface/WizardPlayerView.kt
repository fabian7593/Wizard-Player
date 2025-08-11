package com.wizardskull.library.player.surface

import android.view.SurfaceHolder
import android.view.SurfaceView
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.example.vlc.utils.LanguageMatcher
import com.example.vlc.utils.LanguageMatcher.isSubtitleAllowed
import org.videolan.libvlc.Media
import org.videolan.libvlc.MediaPlayer
import java.util.concurrent.ExecutorService
import androidx.core.net.toUri
import com.wizardskull.library.player.config.Config.applyAspectRatio
import com.wizardskull.library.player.config.PlayerConfig
import com.wizardskull.library.player.utils.AppLogger
import com.wizardskull.library.utils.GeneralUtils.shouldForceHWDecoding

@Composable
fun WizardPlayerView(
    modifier: Modifier = Modifier,
    config: PlayerConfig,
    mediaPlayer: MediaPlayer,
    videoUrl: String,
    executor: ExecutorService,
    onTracksLoaded: (List<Pair<Int, String>>) -> Unit,
    onSubtitleLoaded: (List<Pair<Int, String>>) -> Unit,
    onPlaybackStateChanged: (Boolean) -> Unit,
    onEndReached: () -> Unit,
    onBufferingChanged: (Boolean) -> Unit,
    onDurationChanged: (Long) -> Unit,
    onStart: () -> Unit,
    onAspectRatioChanged: ((String) -> Unit)? = null,
    onAudioChanged: ((String) -> Unit)? = null,
    onSubtitleChanged: ((String) -> Unit)? = null,
) {
    var surfaceHolder by remember { mutableStateOf<SurfaceHolder?>(null) }
    var surfaceViewRef by remember { mutableStateOf<SurfaceView?>(null) }
    var tracksLoaded by remember(videoUrl) { mutableStateOf(false) }

    AndroidView(
        modifier = modifier,
        factory = { context ->
            val surfaceView = SurfaceView(context)
            surfaceViewRef = surfaceView

            surfaceView.holder.addCallback(object : SurfaceHolder.Callback {
                override fun surfaceCreated(holder: SurfaceHolder) {
                    surfaceHolder = holder
                }

                override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {}
                override fun surfaceDestroyed(holder: SurfaceHolder) {
                    surfaceHolder = null
                    try {
                        if (mediaPlayer.isReleased.not()) {
                            mediaPlayer.stop()
                            mediaPlayer.vlcVout.detachViews()
                        }
                    } catch (e: Exception) {
                        AppLogger.error("WizardPlayerView", "❌  Error during surfaceDestroyed: ${e.message}")
                    }
                }
            })

            surfaceView
        },
        onRelease = {
            try {
                mediaPlayer.stop()
                mediaPlayer.vlcVout.detachViews()
                mediaPlayer.setEventListener(null)
            } catch (e: Exception) {
                AppLogger.error("WizardPlayerView", "❌  Error during VLC player release: ${e.message}")
            }
        }
    )

    LaunchedEffect(surfaceHolder, videoUrl) {
        if (surfaceHolder == null || surfaceViewRef == null || videoUrl.isBlank()) return@LaunchedEffect

        executor.execute {
            try {
                val context = surfaceViewRef!!.context
                val displayMetrics = context.resources.displayMetrics
                val width = displayMetrics.widthPixels
                val height = displayMetrics.heightPixels

                applyAspectRatio(mediaPlayer, config.preferenceVideoSize.value) {
                    onAspectRatioChanged?.invoke(it)
                }

                val vout = mediaPlayer.vlcVout
                vout.detachViews()
                vout.setVideoView(surfaceViewRef)
                vout.setWindowSize(width, height)
                vout.attachViews(null)

                val media = Media(mediaPlayer.libVLC, videoUrl.toUri())

                val preferHW: Boolean = config.preferHardwareDecoding ?: shouldForceHWDecoding()
                val forceStrict: Boolean = config.forceHardwareStrict ?: false

                media.setHWDecoderEnabled(preferHW, forceStrict)
                mediaPlayer.media = media
                media.release()
                mediaPlayer.play()

                mediaPlayer.setEventListener { event ->
                    try {
                        when (event.type) {
                            MediaPlayer.Event.Vout -> onStart()
                            MediaPlayer.Event.Playing -> onPlaybackStateChanged(true)
                            MediaPlayer.Event.EndReached -> {
                                onPlaybackStateChanged(false)
                                onEndReached()
                            }
                            MediaPlayer.Event.Paused,
                            MediaPlayer.Event.Stopped -> onPlaybackStateChanged(false)
                            MediaPlayer.Event.Buffering -> onBufferingChanged(event.buffering != 100f)
                            MediaPlayer.Event.LengthChanged -> onDurationChanged(event.lengthChanged)

                            MediaPlayer.Event.ESAdded -> {

                                if (!tracksLoaded) {
                                    tracksLoaded = true
                                    try {

                                        // -- AUDIO TRACKS --
                                        val audioTracks = mediaPlayer.audioTracks?.map {
                                            it.id to (it.name ?: "Audio ${it.id}")
                                        } ?: emptyList()
                                        onTracksLoaded(audioTracks)

                                        val preferredAudio = audioTracks.find {
                                            LanguageMatcher.matchesLanguage(
                                                it.second,
                                                config.preferenceLanguage
                                            )
                                        }
                                        preferredAudio?.let {
                                            mediaPlayer.setAudioTrack(it.first)
                                            onAudioChanged?.invoke(it.second)
                                        }


                                        // -- SUBTITLES TRACKS --
                                        val subtitleTracks = mediaPlayer.spuTracks
                                            ?.mapNotNull { track ->
                                                val name = track.name ?: return@mapNotNull null
                                                if (isSubtitleAllowed(name)) {
                                                    track.id to name
                                                } else null
                                            } ?: emptyList()
                                        onSubtitleLoaded(subtitleTracks)

                                        config.preferenceSubtitle?.let { preferredCode ->
                                            val preferredSub = subtitleTracks.find {
                                                LanguageMatcher.matchesLanguage(
                                                    it.second,
                                                    preferredCode
                                                )
                                            }
                                            preferredSub?.let {
                                                mediaPlayer.spuTrack = it.first
                                                val code =
                                                    LanguageMatcher.detectSubtitleCode(it.second)
                                                onSubtitleChanged?.invoke(code ?: preferredCode)
                                            }
                                        }

                                    } catch (e: Exception) {
                                        tracksLoaded = false
                                        AppLogger.warning(
                                            "WizardPlayerView",
                                            "⚠️ Error loading media tracks: ${e.message}"
                                        )
                                    }
                                }

                            }

                            MediaPlayer.Event.EncounteredError -> {
                                AppLogger.error("WizardPlayerView", "❌ Playback error encountered")
                                onBufferingChanged(false)
                            }
                        }
                    } catch (e: Exception) {
                        AppLogger.error("WizardPlayerView", "❌ Error inside VLC event listener: ${e.message}")
                    }
                }
            } catch (e: Exception) {
                AppLogger.error("WizardPlayerView", "❌ Error initializing VLC player: ${e.message}")
            }
        }
    }
}
