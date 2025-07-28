package com.wizardskull.library.player.utils

import android.util.Log
import com.wizardskull.library.player.config.Config

enum class LogLevel { DEBUG, INFO, WARN, ERROR }

object AppLogger {

    private fun log(level: LogLevel, tag: String, message: String) {
        if (!Config.SHOW_LOGS) return

        when (level) {
            LogLevel.DEBUG -> Log.d(tag, message)
            LogLevel.INFO  -> Log.i(tag, message)
            LogLevel.WARN  -> Log.w(tag, message)
            LogLevel.ERROR -> Log.e(tag, message)
        }
    }
    // Tag string version
    fun debug(tag: String, message: String) = log(LogLevel.DEBUG, tag, message)
    fun info(tag: String, message: String) = log(LogLevel.INFO, tag, message)
    fun warning(tag: String, message: String) = log(LogLevel.WARN, tag, message)
    fun error(tag: String, message: String) = log(LogLevel.ERROR, tag, message)
}
