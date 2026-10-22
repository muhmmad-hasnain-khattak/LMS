package com.raju.edutrack.ui.theme

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

private val Indigo80 = Color(0xFF818CF8)

private val DarkColorScheme = darkColorScheme(
    primary = RoyalBlue80,
    onPrimary = Color(0xFF002D6C),
    primaryContainer = Color(0xFF1E40AF),
    onPrimaryContainer = RoyalBlue90,
    secondary = Teal80,
    onSecondary = Color(0xFF003733),
    secondaryContainer = Color(0xFF115E59),
    onSecondaryContainer = Teal90,
    tertiary = Indigo80,
    onTertiary = Color(0xFF1E1B4B),
    background = DarkBackground,
    onBackground = Color(0xFFF1F5F9),
    surface = DarkSurface,
    onSurface = Color(0xFFF1F5F9),
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = Color(0xFFCBD5E1)
)

private val LightColorScheme = lightColorScheme(
    primary = RoyalBlue60,
    onPrimary = Color.White,
    primaryContainer = RoyalBlue90,
    onPrimaryContainer = Color(0xFF1E3A8A),
    secondary = Teal60,
    onSecondary = Color.White,
    secondaryContainer = Teal90,
    onSecondaryContainer = Color(0xFF115E59),
    tertiary = Indigo60,
    onTertiary = Color.White,
    background = LightBackground,
    onBackground = Color(0xFF0F172A),
    surface = LightSurface,
    onSurface = Color(0xFF0F172A),
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = Color(0xFF475569)
)

@Composable
fun EduTrackTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
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
