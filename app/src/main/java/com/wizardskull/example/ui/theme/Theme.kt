package com.wizardskull.example.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = SkullGreen,
    secondary = DarkWitchGreen,
    tertiary = NecroGreen,
    background = VoidBlack,
    surface = DarkWitchGreen,
    onPrimary = SpiritWhite,
    onSecondary = NecroGreen,
    onTertiary = VoidBlack,
    onBackground = SpiritWhite,
    onSurface = SpiritWhite,
)

private val LightColorScheme = darkColorScheme(
    primary = SkullGreen,
    secondary = DarkWitchGreen,
    tertiary = NecroGreen,
    background = VoidBlack,
    surface = DarkWitchGreen,
    onPrimary = SpiritWhite,
    onSecondary = NecroGreen,
    onTertiary = VoidBlack,
    onBackground = SpiritWhite,
    onSurface = SpiritWhite,
)


@Composable
fun WizardPlayerTheme(
    content: @Composable () -> Unit
) {
    val colorScheme = DarkColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}