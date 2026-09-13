package com.example

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFFE11D48), // Rose 700
    onPrimary = Color.White,
    primaryContainer = Color(0xFFFFE4E6), // Rose 100
    onPrimaryContainer = Color(0xFF881337), // Rose 900
    secondary = Color(0xFF4F46E5), // Indigo 600
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFE0E7FF), // Indigo 100
    onSecondaryContainer = Color(0xFF312E81), // Indigo 900
    tertiary = Color(0xFF0F172A), // Slate 900
    onTertiary = Color.White,
    background = Color(0xFFF8FAFC), // Slate 50
    onBackground = Color(0xFF0F172A), // Slate 900
    surface = Color.White,
    onSurface = Color(0xFF0F172A), // Slate 900
    surfaceVariant = Color(0xFFF1F5F9), // Slate 100
    onSurfaceVariant = Color(0xFF475569) // Slate 600
)

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFFFB7185), // Rose 400
    onPrimary = Color(0xFF4C0519), // Rose 950
    primaryContainer = Color(0xFF881337), // Rose 900
    onPrimaryContainer = Color(0xFFFFE4E6), // Rose 100
    secondary = Color(0xFF818CF8), // Indigo 400
    onSecondary = Color(0xFF1E1B4B), // Indigo 950
    secondaryContainer = Color(0xFF312E81), // Indigo 900
    onSecondaryContainer = Color(0xFFE0E7FF), // Indigo 100
    tertiary = Color(0xFFF8FAFC), // Slate 50
    onTertiary = Color(0xFF0F172A), // Slate 900
    background = Color(0xFF0F172A), // Slate 900
    onBackground = Color(0xFFF8FAFC), // Slate 50
    surface = Color(0xFF1E293B), // Slate 800
    onSurface = Color(0xFFF8FAFC), // Slate 50
    surfaceVariant = Color(0xFF334155), // Slate 700
    onSurfaceVariant = Color(0xFFCBD5E1) // Slate 300
)

@Composable
fun TRYatHOMETheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val view = LocalViewContextOrNull()
    if (view != null) {
        val window = (view.context as? Activity)?.window
        if (window != null) {
            SideEffect {
                window.statusBarColor = Color.Transparent.toArgb()
                window.navigationBarColor = Color.Transparent.toArgb()
                val controller = WindowCompat.getInsetsController(window, view)
                controller.isAppearanceLightStatusBars = !darkTheme
                controller.isAppearanceLightNavigationBars = !darkTheme
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}

@Composable
private fun LocalViewContextOrNull(): android.view.View? {
    return try {
        LocalView.current
    } catch (e: Exception) {
        null
    }
}
