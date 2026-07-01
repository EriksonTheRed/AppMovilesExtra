package com.example.appmoviles.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

enum class AppTheme { GUINDA, AZUL }

private val GuindaLightScheme = lightColorScheme(
    primary = GuindaPrimaryLight,
    secondary = GuindaSecondaryLight,
    background = GuindaBackgroundLight,
    surface = GuindaSurfaceLight
)

private val GuindaDarkScheme = darkColorScheme(
    primary = GuindaPrimaryDark,
    secondary = GuindaSecondaryDark,
    background = GuindaBackgroundDark,
    surface = GuindaSurfaceDark
)

private val AzulLightScheme = lightColorScheme(
    primary = AzulPrimaryLight,
    secondary = AzulSecondaryLight,
    background = AzulBackgroundLight,
    surface = AzulSurfaceLight
)

private val AzulDarkScheme = darkColorScheme(
    primary = AzulPrimaryDark,
    secondary = AzulSecondaryDark,
    background = AzulBackgroundDark,
    surface = AzulSurfaceDark
)

/**
 * @param appTheme Guinda (IPN) o Azul (ESCOM).
 * @param forceDarkMode null = sigue el modo del sistema; true/false = override manual.
 */
@Composable
fun AppMovilesTheme(
    appTheme: AppTheme,
    forceDarkMode: Boolean? = null,
    content: @Composable () -> Unit
) {
    val systemDark = isSystemInDarkTheme()
    val useDark = forceDarkMode ?: systemDark

    val colorScheme = when (appTheme) {
        AppTheme.GUINDA -> if (useDark) GuindaDarkScheme else GuindaLightScheme
        AppTheme.AZUL -> if (useDark) AzulDarkScheme else AzulLightScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}