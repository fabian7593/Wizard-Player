package com.wizardskull.library.utils

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build

object GeneralUtils {

    fun formatTime(seconds: Long): String {
        val hours = seconds / 3600
        val minutes = (seconds % 3600) / 60
        val secs = seconds % 60

        return if (hours > 0)
            "%02d:%02d:%02d".format(hours, minutes, secs)
        else
            "%02d:%02d".format(minutes, secs)
    }

    fun Context.isTelevision(): Boolean {
        return packageManager.hasSystemFeature(PackageManager.FEATURE_LEANBACK)
    }

    val isTruchoBox: Boolean
        get() {
            val model = Build.MODEL?.lowercase() ?: ""
            val manufacturer = Build.MANUFACTURER?.lowercase() ?: ""
            val brand = Build.BRAND?.lowercase() ?: ""
            val hardware = Build.HARDWARE?.lowercase() ?: ""
            val product = Build.PRODUCT?.lowercase() ?: ""

            val knownTruchoIndicators = listOf(
                "rk", "rk3229", "rk3328", "x96", "mxq", "t95", "x88", "t9",
                "allwinner", "alps", "sunvell", "bqeel", "magicsee", "meecool", "unknown"
            )

            return knownTruchoIndicators.any {
                model.contains(it) || manufacturer.contains(it) || brand.contains(it) || hardware.contains(it) || product.contains(it)
            }
        }

    fun shouldForceHWDecoding(): Boolean {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.N && !isTruchoBox
    }
}