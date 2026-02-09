// Example ui/theme/Theme.kt
package com.example.pennypilot.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat

// In ui/theme/Theme.kt
private val LightColorScheme = lightColorScheme(
    primary = AppAccentColor,
    onPrimary = AppPrimaryText, // Or Color.Black if AppAccentColor is light
    secondary = AppMutedColor,
    onSecondary = AppPrimaryText,
    background = AppBackgroundEnd, // Or Color.Transparent if you always use a gradient
    onBackground = AppPrimaryText,
    surface = Color.Transparent, // To let gradients show through
    onSurface = AppPrimaryText,
    error = Color.Red,
    onError = Color.White,
    surfaceVariant = AppCardBackground, // Example mapping
    onSurfaceVariant = AppPrimaryText
    // ...
)
// Define a DarkColorScheme similarly if it differs. Given your home screen is already dark,
// your LightColorScheme might effectively be your "only" scheme for this custom style.

// Define your DarkColorScheme similarly if you support dark theme
private val DarkColorScheme = darkColorScheme(
    primary = PennyPilotPrimary, // Or a dark theme adjusted primary
    onPrimary = PennyPilotOnPrimary,
    // ... fill out all colors for your dark theme
    secondary = PennyPilotSecondary,
    // ...
    background = Color(0xFF1A1A1A), // Example dark background
    surface = Color(0xFF2C2C2C),    // Example dark surface
    onBackground = Color(0xFFE0E0E0),
    onSurface = Color(0xFFE0E0E0),
    surfaceVariant = Color(0xFF3A3A3A),
    onSurfaceVariant = Color(0xFFC0C0C0)

)

@Composable
fun PennyPilotTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true, // Dynamic color is available on Android 12+
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
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.primary.toArgb() // Or your desired status bar color
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }
    val Shapes = Shapes(
        extraSmall = RoundedCornerShape(4.dp),
        small = androidx.compose.foundation.shape.RoundedCornerShape(8.dp),
        medium = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
        large = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
        extraLarge = androidx.compose.foundation.shape.RoundedCornerShape(28.dp)
    )
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography, // Make sure you have Typography.kt defined
        shapes = Shapes,         // Make sure you have Shapes.kt defined
        content = content
    )
}

