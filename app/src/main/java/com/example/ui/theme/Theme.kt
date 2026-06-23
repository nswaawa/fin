package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = TealOnContainer,
    onPrimary = TealPrimary,
    primaryContainer = TealContainer,
    onPrimaryContainer = TealOnContainer,
    secondary = GreenContainer,
    onSecondary = GreenOnContainer,
    error = RedError,
    errorContainer = RedContainer,
    onError = RedOnPrimary,
    onErrorContainer = RedOnContainer,
    background = Color(0xFF121415),
    surface = Color(0xFF191C1D),
    onBackground = Color(0xFFE1E3E4),
    onSurface = Color(0xFFE1E3E4)
)

private val LightColorScheme = lightColorScheme(
    primary = TealPrimary,
    onPrimary = TealOnPrimary,
    primaryContainer = TealContainer,
    onPrimaryContainer = TealOnContainer,
    secondary = GreenSuccess,
    onSecondary = GreenOnSecondary,
    secondaryContainer = GreenContainer,
    onSecondaryContainer = GreenOnContainer,
    error = RedError,
    errorContainer = RedContainer,
    onError = RedOnPrimary,
    onErrorContainer = RedOnContainer,
    background = NeutralBackground,
    surface = NeutralSurface,
    onBackground = TextPrimary,
    onSurface = TextPrimary,
    surfaceVariant = BorderSoft,
    outline = BorderOutline
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = false,
    // Keep dynamic color disabled to preserve our custom premium brand colors
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
