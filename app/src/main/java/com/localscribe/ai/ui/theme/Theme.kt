package com.localscribe.ai.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// Colores Light Theme
private val LightPrimary = Color(0xFF1976D2)
private val LightOnPrimary = Color(0xFFFFFFFF)
private val LightPrimaryContainer = Color(0xFFD1E4FF)
private val LightOnPrimaryContainer = Color(0xFF001D36)

private val LightSecondary = Color(0xFF00BFA5)
private val LightOnSecondary = Color(0xFFFFFFFF)
private val LightSecondaryContainer = Color(0xFFCDFFF7)
private val LightOnSecondaryContainer = Color(0xFF00201B)

private val LightTertiary = Color(0xFF7C4DFF)
private val LightOnTertiary = Color(0xFFFFFFFF)
private val LightTertiaryContainer = Color(0xFFE8DDFF)
private val LightOnTertiaryContainer = Color(0xFF22005D)

private val LightError = Color(0xFFBA1A1A)
private val LightOnError = Color(0xFFFFFFFF)
private val LightErrorContainer = Color(0xFFFFDAD6)
private val LightOnErrorContainer = Color(0xFF410002)

private val LightBackground = Color(0xFFFDFCFF)
private val LightOnBackground = Color(0xFF1A1C1E)
private val LightSurface = Color(0xFFFDFCFF)
private val LightOnSurface = Color(0xFF1A1C1E)
private val LightSurfaceVariant = Color(0xFFE1E2EC)
private val LightOnSurfaceVariant = Color(0xFF44474F)
private val LightOutline = Color(0xFF74777F)
private val LightOutlineVariant = Color(0xFFC4C6D0)

// Colores Dark Theme
private val DarkPrimary = Color(0xFF9ECAFF)
private val DarkOnPrimary = Color(0xFF003258)
private val DarkPrimaryContainer = Color(0xFF00497D)
private val DarkOnPrimaryContainer = Color(0xFFD1E4FF)

private val DarkSecondary = Color(0xFF4FDBCA)
private val DarkOnSecondary = Color(0xFF003730)
private val DarkSecondaryContainer = Color(0xFF005047)
private val DarkOnSecondaryContainer = Color(0xFF70F7E5)

private val DarkTertiary = Color(0xFFCFBDFE)
private val DarkOnTertiary = Color(0xFF3A1D84)
private val DarkTertiaryContainer = Color(0xFF5336A4)
private val DarkOnTertiaryContainer = Color(0xFFE8DDFF)

private val DarkError = Color(0xFFFFB4AB)
private val DarkOnError = Color(0xFF690005)
private val DarkErrorContainer = Color(0xFF93000A)
private val DarkOnErrorContainer = Color(0xFFFFDAD6)

private val DarkBackground = Color(0xFF1A1C1E)
private val DarkOnBackground = Color(0xFFE2E2E6)
private val DarkSurface = Color(0xFF1A1C1E)
private val DarkOnSurface = Color(0xFFE2E2E6)
private val DarkSurfaceVariant = Color(0xFF44474F)
private val DarkOnSurfaceVariant = Color(0xFFC4C6D0)
private val DarkOutline = Color(0xFF8E9099)
private val DarkOutlineVariant = Color(0xFF44474F)

private val LightColorScheme = lightColorScheme(
    primary = LightPrimary,
    onPrimary = LightOnPrimary,
    primaryContainer = LightPrimaryContainer,
    onPrimaryContainer = LightOnPrimaryContainer,
    secondary = LightSecondary,
    onSecondary = LightOnSecondary,
    secondaryContainer = LightSecondaryContainer,
    onSecondaryContainer = LightOnSecondaryContainer,
    tertiary = LightTertiary,
    onTertiary = LightOnTertiary,
    tertiaryContainer = LightTertiaryContainer,
    onTertiaryContainer = LightOnTertiaryContainer,
    error = LightError,
    onError = LightOnError,
    errorContainer = LightErrorContainer,
    onErrorContainer = LightOnErrorContainer,
    background = LightBackground,
    onBackground = LightOnBackground,
    surface = LightSurface,
    onSurface = LightOnSurface,
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = LightOnSurfaceVariant,
    outline = LightOutline,
    outlineVariant = LightOutlineVariant
)

private val DarkColorScheme = darkColorScheme(
    primary = DarkPrimary,
    onPrimary = DarkOnPrimary,
    primaryContainer = DarkPrimaryContainer,
    onPrimaryContainer = DarkOnPrimaryContainer,
    secondary = DarkSecondary,
    onSecondary = DarkOnSecondary,
    secondaryContainer = DarkSecondaryContainer,
    onSecondaryContainer = DarkOnSecondaryContainer,
    tertiary = DarkTertiary,
    onTertiary = DarkOnTertiary,
    tertiaryContainer = DarkTertiaryContainer,
    onTertiaryContainer = DarkOnTertiaryContainer,
    error = DarkError,
    onError = DarkOnError,
    errorContainer = DarkErrorContainer,
    onErrorContainer = DarkOnErrorContainer,
    background = DarkBackground,
    onBackground = DarkOnBackground,
    surface = DarkSurface,
    onSurface = DarkOnSurface,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = DarkOnSurfaceVariant,
    outline = DarkOutline,
    outlineVariant = DarkOutlineVariant
)

@Composable
fun LocalScribeAITheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color está disponible en Android 12+
    dynamicColor: Boolean = true,
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
            window.statusBarColor = colorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
