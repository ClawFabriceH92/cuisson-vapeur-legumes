package com.trucdecomptable.cuissonvapeur.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import com.trucdecomptable.cuissonvapeur.data.local.entity.ThemeMode

private val LightColors = lightColorScheme(
    primary = SteamGreen40,
    secondary = Teal40,
    tertiary = Amber40,
    background = BackgroundLight,
    surface = SurfaceLight,
)

private val DarkColors = darkColorScheme(
    primary = SteamGreen80,
    secondary = Teal80,
    tertiary = Amber80,
    background = BackgroundDark,
    surface = SurfaceDark,
)

/**
 * EF-27: light / dark / system theme. [themeMode] comes from
 * [com.trucdecomptable.cuissonvapeur.data.repository.SettingsRepository]
 * (D8: system by default). Dynamic color (Android 12+) is intentionally
 * left off — the spec calls for a consistent brand palette, not per-device
 * wallpaper-derived colors.
 */
@Composable
fun CuissonVapeurTheme(
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    val useDarkTheme = when (themeMode) {
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
    }

    val context = LocalContext.current
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S ->
            if (useDarkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)

        useDarkTheme -> DarkColors
        else -> LightColors
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = CuissonVapeurTypography,
        content = content,
    )
}
