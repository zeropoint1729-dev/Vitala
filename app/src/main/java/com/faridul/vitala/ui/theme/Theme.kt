package com.faridul.vitala.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val VitalaColorScheme = darkColorScheme(
    primary = Amber,
    secondary = Teal,
    background = Background,
    surface = Surface,
    onPrimary = Background,
    onBackground = TextPrimary,
    onSurface = TextPrimary,
    error = Danger
)

@Composable
fun VitalaTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = VitalaColorScheme,
        typography = VitalaTypography,
        content = content
    )
}
