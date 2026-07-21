package com.sgbread.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val SgbColorScheme = lightColorScheme(
    primary = RiceGreenDark,
    onPrimary = CreamWhite,
    secondary = SunOrange,
    onSecondary = TextBrown,
    tertiary = WaterBlue,
    background = SkyBlueLight,
    onBackground = TextBrown,
    surface = CreamWhite,
    onSurface = TextBrown,
    error = IncorrectRed,
    onError = CreamWhite
)

@Composable
fun SgbReadTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = SgbColorScheme,
        typography = SgbTypography,
        content = content
    )
}
