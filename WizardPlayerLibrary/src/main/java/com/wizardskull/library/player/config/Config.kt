package com.wizardskull.library.player.config

import com.wizardskull.library.player.utils.AppLogger
import org.videolan.libvlc.MediaPlayer

object Config {

    val SHOW_LOGS = true
    fun createLibVlcConfig(config: PlayerConfig): ArrayList<String> {

        val codec =  if (config.preferHardwareDecoding == false) "avcodec" else "mediacodec_ndk"

        val vlcOptions = arrayListOf(
            // -- DECODING ENGINE OPTIONS --
            "--codec=$codec",               // Use modern Android NDK hardware decoder (Android 9+)

            // -- CACHING (BUFFERING) SETTINGS --
            "--file-caching=3500",                  // Local file buffering time in ms
            "--network-caching=${config.networkCachingMs ?: 7777}",               // Network stream buffering time in ms

            if (config.forceNoDropLateFrames == true) "--no-drop-late-frames" else "--drop-late-frames",  // Drop late frames to maintain sync
            if (config.forceNoSkipFrames == true) "--no-skip-frames" else "--skip-frames",                // Skip frames if decoding is too slow

            // -- CLOCK & TIMING OPTIONS --
            "--clock-jitter=500",                   // Synchronization clock jitter tolerance in ms

            // -- UI & DISPLAY CLEANUP --
            "--no-osd",                             // Disable on-screen display (e.g. volume indicator)
            "--no-video-title-show",                // Prevent video title from displaying on playback start

            // -- SUBTITLE RENDERING ENGINE --
            "--sub-filter=marq",                    // Use lightweight subtitle rendering engine
            "--sub-fps=3",                          // Subtitle rendering frame rate to reduce CPU usage


            // -- COLOR RENDERING OPTIMIZATIONS --
            "--dither-algo=-1",                     // Disable dithering to improve color rendering performance
            //"--tone-mapping=0",                     // Disable tone mapping (no HDR adjustments)

            // -- SUBTITLE OVERLAP & TIMING CONTROL --
            "--subsdelay-mode=0",                   // Use absolute delay mode for subtitles (simpler logic)
            "--subsdelay-factor=0.0",               // No additional delay factor
            "--subsdelay-overlap=1",                // Max 1 subtitle displayed at a time
            "--subsdelay-min-alpha=255",            // Force full alpha (visibility) for first subtitle in overlap

            // -- PREFETCH BUFFERING --
            "--prefetch-buffer-size=64",            // Prefetch buffer size in KiB (reduce lag on slow I/O)
            "--prefetch-read-size=65536",           // Prefetch read block size (64 KB)
            "--prefetch-seek-threshold=1048576",    // Prefetch seek threshold (1MB)

            // -- AUDIO OPTIMIZATIONS --
            "--no-audio-time-stretch",              // Avoid pitch correction when changing speed (less CPU)

            // -- UNUSED OR EXPERIMENTAL OPTIONS (currently disabled due to compatibility issues) --
            // "--avcodec-fast",                    // Try fast decoding mode (may crash on some devices)
            // "--avcodec-hw=mediacodec",           // Explicitly use MediaCodec HW acceleration (conflicts in some setups)
            // "--codec=avcodec",                   // Use FFmpeg-based decoder instead of NDK (less efficient on Android)
            // "--freetype-spacing=0",              // Adjust inter-character spacing (not used)
            // "--freetype-monospaced",             // Force monospace font (not necessary)
            // "--effect-list=dummy",               // Disable audio visualizations (spectrum, etc.)
            // "--no-equalizer",                    // Disable audio equalizer (only needed if EQ was active)

            // -- FREETYPE FONT CONFIGURATION FOR SUBTITLES --
           /* "--freetype-rel-fontsize=20",           // Relative font size (based on video height)
            "--freetype-outline-thickness=1",       // Subtitle font outline thickness for readability
            "--freetype-color=0xffffff",            // Subtitle font color (white)
            "--freetype-background-opacity=0",      // Background box opacity (0 = none)
            "--freetype-shadow-opacity=0",          // Shadow opacity (0 = none)
            "--freetype-opacity=225",               // Subtitle font opacity (0-255, higher = more visible)
            "--freetype-font=Arial",                // Use Arial as subtitle font (fallback to default if unavailable)
            */
        )

        val subtitleStyleOptions = buildSubtitleRenderingOptions(config)
        vlcOptions.addAll(subtitleStyleOptions)

        return vlcOptions
    }

    val AspectRatioOptions = listOf(
        VideoSizePreference.AUTOFIT.value to "Auto Fit",
        VideoSizePreference.FILL.value to "Fill",
        VideoSizePreference.CINEMATIC.value to "Cinematic",
        VideoSizePreference.RATIO_16_9.value to "16:9",
        VideoSizePreference.RATIO_4_3.value to "4:3"
    )


    /**
     * Applies the desired aspect ratio setting to the MediaPlayer.
     * @param mediaPlayer The VLC media player instance
     * @param aspectRatio One of: "autofit", "fill", "cinematic", "16:9", "4:3"
     * @param onApplied Optional callback invoked with the applied mode (useful for state sync)
     */
    fun applyAspectRatio(
        mediaPlayer: MediaPlayer?,
        aspectRatio: String,
        onApplied: ((String) -> Unit)? = null
    ) {
        try {
            when (aspectRatio.lowercase()) {
                VideoSizePreference.AUTOFIT.value -> {
                    mediaPlayer?.aspectRatio = ""
                    mediaPlayer?.scale = 0f
                    onApplied?.invoke(VideoSizePreference.AUTOFIT.value)
                }
                VideoSizePreference.FILL.value -> {
                    mediaPlayer?.aspectRatio = "21:9"
                    mediaPlayer?.scale = 1f
                    mediaPlayer?.videoScale = MediaPlayer.ScaleType.SURFACE_FILL
                    onApplied?.invoke(VideoSizePreference.FILL.value)
                }
                VideoSizePreference.CINEMATIC.value -> {
                    mediaPlayer?.aspectRatio = "2:1"
                    mediaPlayer?.scale = 0f
                    onApplied?.invoke(VideoSizePreference.CINEMATIC.value)
                }
                VideoSizePreference.RATIO_16_9.value -> {
                    mediaPlayer?.aspectRatio = "16:9"
                    mediaPlayer?.scale = 0f
                    onApplied?.invoke(VideoSizePreference.RATIO_16_9.value)
                }
                VideoSizePreference.RATIO_4_3.value -> {
                    mediaPlayer?.aspectRatio = "4:3"
                    mediaPlayer?.scale = 0f
                    onApplied?.invoke(VideoSizePreference.RATIO_4_3.value)
                }
                else -> {
                    mediaPlayer?.aspectRatio = ""
                    mediaPlayer?.scale = 0f
                    onApplied?.invoke(VideoSizePreference.AUTOFIT.value)
                }
            }
        } catch (e: Exception) {
            AppLogger.error("Config", "❌ Error applying aspect ratio: ${e.message}")
        }
    }



    fun buildSubtitleRenderingOptions(config: PlayerConfig): List<String> {
        val options = mutableListOf<String>()

        // -- FONT SIZE LOGIC --
        val fontSizeValue = when (config.fontSize) {
            FontSize.XSMALL -> 25
            FontSize.SMALL -> 22
            FontSize.MEDIUM -> 19
            FontSize.HIGH -> 16
        }
        options += "--freetype-rel-fontsize=$fontSizeValue"

        // -- BORDER OUTLINE THICKNESS --
        val outlineThickness = when (config.borderType) {
            BorderType.NONE -> 0
            BorderType.BASIC -> 1
            BorderType.NORMAL -> 2
        }
        options += "--freetype-outline-thickness=$outlineThickness"

        // -- SHADOW OPACITY --
        val shadowOpacity = if (config.hasShadowText) 80 else 0
        options += "--freetype-shadow-opacity=$shadowOpacity"

        // -- FONT COLOR --
        val hexColor = String.format("0x%06x", config.textColor)
        options += "--freetype-color=$hexColor"

        // -- DEFAULTS THAT REMAIN CONSTANT --
        options += "--freetype-background-opacity=0"
        options += "--freetype-opacity=225"
        options += "--freetype-font=Arial"

        return options
    }

}