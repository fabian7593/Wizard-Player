package com.wizardskull.library.player.composable

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.wizardskull.library.player.config.PlayerConfig

@Composable
fun WatermarkBranding(
    modifier: Modifier = Modifier,
    config: PlayerConfig,
    showControls: Boolean
) {
    AnimatedVisibility(
        visible = !showControls && config.showWatermark,
        enter = fadeIn(),
        exit = fadeOut(),
        modifier = modifier
            .padding(end = 12.dp, bottom = 12.dp)
    ) {
        if(config.watermarkResId !== null){
            Icon(
                painter = painterResource(id = config.watermarkResId),
                contentDescription = "App branding",
                tint = Color.Unspecified,
                modifier = Modifier
                    .size((config.brandingSize ?: 48).dp)
                    .graphicsLayer(alpha = 0.8f)
            )
        }
    }
}