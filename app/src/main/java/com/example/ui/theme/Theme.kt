package com.example.ui.theme

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
import com.example.model.AccentColor
import com.example.model.AppTheme

fun createDarkColorScheme(accentColor: AccentColor) = darkColorScheme(
    primary = accentColor.darkColor,
    onPrimary = Color.White,
    primaryContainer = accentColor.darkSoft,
    onPrimaryContainer = accentColor.darkColor,
    secondary = DarkUtilityKeyBg,
    onSecondary = DarkUtilityKeyText,
    background = DarkBg,
    onBackground = DarkTextPrimary,
    surface = DarkSurface,
    onSurface = DarkTextPrimary,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = DarkTextSecondary,
    outline = DarkCardBorder,
    error = CleanRedDark
)

fun createLightColorScheme(accentColor: AccentColor) = lightColorScheme(
    primary = accentColor.color,
    onPrimary = Color.White,
    primaryContainer = accentColor.lightSoft,
    onPrimaryContainer = accentColor.color,
    secondary = LightUtilityKeyBg,
    onSecondary = LightUtilityKeyText,
    background = LightBg,
    onBackground = LightTextPrimary,
    surface = LightSurface,
    onSurface = LightTextPrimary,
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = LightTextSecondary,
    outline = LightCardBorder,
    error = CleanRed
)

@Composable
fun CalculatorTheme(
    appTheme: AppTheme = AppTheme.SYSTEM,
    accentColor: AccentColor = AccentColor.BLUE,
    content: @Composable () -> Unit
) {
    val darkTheme = when (appTheme) {
        AppTheme.SYSTEM -> isSystemInDarkTheme()
        AppTheme.LIGHT -> false
        AppTheme.DARK -> true
    }

    val colorScheme = if (darkTheme) createDarkColorScheme(accentColor) else createLightColorScheme(accentColor)

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                window.statusBarColor = colorScheme.background.toArgb()
                window.navigationBarColor = if (darkTheme) DarkSurface.toArgb() else LightSurface.toArgb()
                val insetsController = WindowCompat.getInsetsController(window, view)
                insetsController.isAppearanceLightStatusBars = !darkTheme
                insetsController.isAppearanceLightNavigationBars = !darkTheme
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
