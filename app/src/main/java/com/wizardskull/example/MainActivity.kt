package com.wizardskull.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.wizardskull.example.ui.theme.WizardPlayerTheme
import com.wizardskull.library.player.WizardVideoPlayer
import com.wizardskull.library.player.config.BorderType
import com.wizardskull.library.player.config.FontSize
import com.wizardskull.library.player.config.PlayerConfig
import com.wizardskull.library.player.config.PlayerLabels
import com.wizardskull.library.player.config.VideoItem
import com.wizardskull.library.player.config.VideoSizePreference
import com.wizardskull.library.player.utils.SetupFullscreenLandscape

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)



        setContent {
            var showPlayer by remember { mutableStateOf(false) }
            var config by remember { mutableStateOf<PlayerConfig?>(null) }
            val labels = PlayerLabels()

            if (showPlayer && config != null) {
                WizardPlayerTheme{
                    WizardVideoPlayer(
                        config = config!!,
                        labels = labels,
                        onAudioChanged = { println("New Audio changed: $it") },
                        onSubtitleChanged = { println("New Subtitle changed: $it") },
                        onAspectRatioChanged = { println("New Aspect Ratio: $it") },
                        onGetCurrentTime = { println("Current time: $it") },
                        onGetCurrentItem = { println("Current item: $it") },
                        onExit = {
                            showPlayer = false
                        }
                    )
                }
            } else {
                WizardPlayerTheme {
                    val focusManager = LocalFocusManager.current
                    MainScreen(
                        onStartPlayer = {
                            focusManager.clearFocus()
                            config = it
                            showPlayer = true
                        }
                    )
                }
            }
        }
    }
}


@Composable
fun MainScreen(onStartPlayer: (PlayerConfig) -> Unit) {

    val context = LocalContext.current
    SetupFullscreenLandscape(context)

    var currentId by remember { mutableStateOf("") }
    val ids = remember { mutableStateListOf<String>() }
    var isPoorCpuMode by remember { mutableStateOf(true) }
    var showBrand by remember { mutableStateOf(true) } // ← NUEVO SWITCH

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        contentAlignment = Alignment.Center
    ) {

        val scrollState = rememberScrollState()

        Column(
            modifier = Modifier
                .verticalScroll(scrollState)
                .widthIn(max = 450.dp)
                .fillMaxHeight()
                .padding(top = 32.dp, bottom = 48.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            androidx.compose.foundation.Image(
                painter = painterResource(id = R.drawable.fulllogo_nobuffer),
                contentDescription = "Wizard Player Logo",
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 200.dp)
                    .padding(bottom = 16.dp)
                    .padding(bottom = 16.dp)
            )

            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = currentId,
                    onValueChange = { currentId = it },
                    label = { Text(stringResource(R.string.label_id_numeric)) },
                    keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.secondary,
                        cursorColor = MaterialTheme.colorScheme.primary,
                        focusedLabelColor = MaterialTheme.colorScheme.primary
                    )
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    if (ids.isNotEmpty()) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            ids.forEach { id ->
                                Text(
                                    text = id,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    } else {
                        Spacer(modifier = Modifier.weight(1f))
                    }

                    Button(
                        onClick = {
                            if (currentId.isNotBlank()) {
                                ids.add(currentId)
                                currentId = ""
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        )
                    ) {
                        Text(stringResource(R.string.button_add))
                    }
                }

                // 🔥 SWITCH 1: POOR CPU
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = stringResource(R.string.text_poor_cpu),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Switch(
                        checked = isPoorCpuMode,
                        onCheckedChange = { isPoorCpuMode = it }
                    )
                }

                // ⚡ SWITCH 2: SHOW BRAND
                Row(
                    modifier = Modifier
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = stringResource(R.string.text_show_brand),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Switch(
                        checked = showBrand,
                        onCheckedChange = { showBrand = it }
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = {
                    if (ids.isNotEmpty()) {
                        val videoItems = ids.map { id ->
                            VideoItem(
                                title = "Video $id",
                                subtitle = "Subtítulo $id",
                                url = "http://161.97.128.152:80/movie/test777/test777/$id.mkv",
                                season = 1,
                                episodeNumber = id.toInt(),
                                lastSecondView = if (id.toInt() == 63) 2000 else 0
                            )
                        }

                        val config = PlayerConfig(
                            videoItems = videoItems,
                            primaryColor = 0xFF2C7414.toInt(),
                            focusColor = 0xFFFFFFFF.toInt(),
                            inactiveColor = 0xFF888888.toInt(),
                            diameterButtonCircleDp = 48,
                            iconSizeDp = 32,
                            showSubtitleButton = true,
                            showAudioButton = true,
                            showAspectRatioButton = true,
                            autoPlay = true,
                            startEpisodeNumber = null,
                            preferenceLanguage = "es",
                            preferenceSubtitle = null,
                            preferenceVideoSize = VideoSizePreference.AUTOFIT,
                            watermarkResId = R.drawable.icononly_transparent_nobuffer,
                            showWatermark = showBrand,
                            brandingSize = 64,
                            playbackProgress = 180_000,
                            fontSize = if (isPoorCpuMode) FontSize.XSMALL else FontSize.MEDIUM,
                            borderType = if (isPoorCpuMode) BorderType.NONE else BorderType.NORMAL,
                            hasShadowText = !isPoorCpuMode,
                            textColor = 0xffffff
                        )

                        onStartPlayer(config)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Text(stringResource(R.string.button_play_all))
            }
        }
    }
}
