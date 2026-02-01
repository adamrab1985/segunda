package com.segunda.xreport.ui.theme

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

private val DarkColorScheme = darkColorScheme(primary = Color(0xFF4CAF50), onPrimary = Color.White, surface = Color(0xFF121212), onSurface = Color(0xFFE0E0E0))
private val LightColorScheme = lightColorScheme(primary = Color(0xFF2E7D32), onPrimary = Color.White, surface = Color.White, onSurface = Color(0xFF1C1C1C))

@Composable
fun XReportTheme(darkTheme: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view).setAppearanceLightStatusBars(false)
        }
    }
    MaterialTheme(colorScheme = colorScheme, content = content)
}
