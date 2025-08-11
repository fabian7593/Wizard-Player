package com.wizardskull.library.player.config
import kotlinx.parcelize.Parcelize
import android.os.Parcelable


// -- VIDEO ITEM DATA MODEL --
@Parcelize
data class VideoItem(
    val title: String,                       // Video title
    val subtitle: String? = null,            // Optional subtitle text
    val url: String,                         // Video URL
    val season: Number? = null,              // Optional season number
    val episodeNumber: Number? = null,       // Optional episode number
    val lastSecondView: Number? = null,      // Resume playback from this time (in seconds)
) : Parcelable


// -- PLAYER CONFIGURATION (UI, PREFERENCES, BEHAVIOR) --
@Parcelize
data class PlayerConfig(
    // Playlist of videos to play
    val videoItems: List<VideoItem>,

    // -- UI Colors --
    val primaryColor: Int = 0xFF2C7414.toInt(),  // Main accent color
    val focusColor: Int = 0xFFFFFFFF.toInt(),    // Color when a button is focused (white)
    val inactiveColor: Int = 0xFF888888.toInt(), // Color for inactive elements (gray)

    // -- UI Sizes --
    val diameterButtonCircleDp: Int = 48,       // Diameter of circular control buttons
    val iconSizeDp: Int = 24,                   // Icon size inside buttons

    // -- UI Controls Visibility --
    val showSubtitleButton: Boolean = true,     // Show subtitle toggle button
    val showAudioButton: Boolean = true,        // Show audio track selector button
    val showAspectRatioButton: Boolean = true,  // Show aspect ratio button

    // -- Playback Behavior --
    val autoPlay: Boolean = true,               // Autoplay when ready
    val startEpisodeNumber: Int? = null,        // Optional: start from this episode (by number)
    val playbackProgress: Long = 180_000,       // Resume progress (ms) default to 3 mins

    // -- Language Preferences --
    val preferenceLanguage: String = "en",      // Preferred audio language -> es, es-es, es-mx, en, fr, pt, de, it, ja, ko, zh
    val preferenceSubtitle: String? = "es",      // Preferred subtitle language -> es, es-es, es-mx, en, fr, pt, de, it, ja, ko, zh

    // -- Aspect Ratio Preference --
    val preferenceVideoSize: VideoSizePreference = VideoSizePreference.AUTOFIT,

    // -- Watermark / Branding --
    val watermarkResId: Int? = null, // Optional watermark
    val showWatermark: Boolean = true,        // Show watermark
    val brandingSize: Int? = 32,              // Size of watermark in dp

    // -- Subtitle Style Preferences --
    val fontSize: FontSize = FontSize.MEDIUM,      // Subtitle font size: SMALL, MEDIUM, HIGH
    val borderType: BorderType = BorderType.BASIC, // Subtitle border: NONE, BASIC, NORMAL
    val hasShadowText: Boolean = true,             // Whether to render text shadow
    val textColor: Int = 0xffffff,                  // Subtitle text color (e.g. white),

    val forceNoDropLateFrames: Boolean? = false,
    val forceNoSkipFrames: Boolean? = false,
    val networkCachingMs: Int? = 7777,

    // Decoding (HW/SW)
    val preferHardwareDecoding: Boolean? = true, // null = auto (use heuristic shouldForceHWDecoding)
    val forceHardwareStrict: Boolean? = false,    // null = default VLC (false). true = withouth fallback to SW
) : Parcelable


// -- CUSTOMIZABLE UI TEXT LABELS --

@Parcelize
data class PlayerLabels(
    val nextLabel: String = "Next ▶",                       // Next video
    val audioLabel: String = "Audio",                       // Audio track label
    val subtitleLabel: String = "Subtitles",                // Subtitle track label
    val aspectRatioLabel: String = "Aspect",                // Aspect ratio toggle label
    val exitPrompt: String = "Press back again to exit",    // Double back to exit
    val selectAudioTitle: String = "Select Audio",          // Dialog title for audio tracks
    val selectSubtitleTitle: String = "Select Subtitles",   // Dialog title for subtitle tracks
    val aspectRatioTitle: String = "Aspect Ratio",          // Dialog title for aspect ratio
    val titleContinueWatching: String = "Do you want to continue watching?", // Resume question
    val buttonContinueWatching: String = "Continue Watching",               // Button for resume
    val buttonResetVideo: String = "Reset Video",           // Button for restart
    val errorConnectionMessage: String = "No Internet Connection" // Network error message
) : Parcelable
