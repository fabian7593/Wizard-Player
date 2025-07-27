package com.wizardskull.library.player.composable

import android.graphics.BitmapFactory
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
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
            val context = LocalContext.current
            val bitmap = BitmapFactory.decodeResource(context.resources, config.watermarkResId)
            val imageBitmap = bitmap.asImageBitmap()

            Image(
                bitmap = imageBitmap,
                contentDescription = "App branding",
                modifier = Modifier
                    .size((config.brandingSize ?: 48).dp)
                    .graphicsLayer(alpha = 0.8f),
                contentScale = ContentScale.Fit,
                filterQuality = FilterQuality.High
            )
        }
    }
}