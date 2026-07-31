package com.medhaup.time.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColorScheme = lightColorScheme(
    primary = NavyBlue,
    onPrimary = PureWhite,
    secondary = BurntOrange,
    onSecondary = PureWhite,
    tertiary = BurntOrangeLight,
    background = OffWhite,
    onBackground = TextPrimary,
    surface = PureWhite,
    onSurface = TextPrimary,
    onSurfaceVariant = TextSecondary,
    error = ErrorRed,
    onError = PureWhite
)

@Composable
fun TimeTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = LightColorScheme,
        content = content
    )
}