package com.wizardskull.library.player.utils

import androidx.compose.runtime.MutableState
import com.wizardskull.library.player.config.PlayerConfig

fun playNextOrExit(
    config: PlayerConfig,
    currentIndex: MutableState<Int>,
    currentTime: Long,
    onGetCurrentTime: (Long) -> Unit,
    onExit: () -> Unit
) {
    if (config.autoPlay) {
        if (currentIndex.value < config.videoItems.lastIndex) {
            currentIndex.value += 1
        } else {
            onGetCurrentTime(currentTime)
            onExit()
        }
    } else {
        onGetCurrentTime(currentTime)
        onExit()
    }
}