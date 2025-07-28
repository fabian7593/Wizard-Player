package com.wizardskull.library.player.config

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

// -- ENUMS FOR SAFETY AND AUTOCOMPLETION --
@Parcelize
enum class FontSize : Parcelable {
    XSMALL, SMALL, MEDIUM, HIGH
}

@Parcelize
enum class BorderType : Parcelable {
    NONE, BASIC, NORMAL
}

@Parcelize
enum class VideoSizePreference(val value: String) : Parcelable {
    AUTOFIT("autofit"),
    FILL("fill"),
    CINEMATIC("cinematic"),
    RATIO_16_9("16:9"),
    RATIO_4_3("4:3");

    override fun toString(): String = value
}