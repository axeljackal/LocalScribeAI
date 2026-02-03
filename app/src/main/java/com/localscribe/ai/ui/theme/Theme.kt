package com.localscribe.ai.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

/**
 * Color Scheme para Light Theme
 * Paleta: Verde Agua (Primary) + Rosa Pastel (Secondary)
 */
private val LightColorScheme = lightColorScheme(
    // Primary - Verde Agua
    primary = TealPrimary,
    onPrimary = TealOnPrimary,
    primaryContainer = TealPrimaryContainer,
    onPrimaryContainer = TealOnPrimaryContainer,
    
    // Secondary - Rosa Pastel
    secondary = PinkSecondaryDark,
    onSecondary = PinkOnSecondary,
    secondaryContainer = PinkSecondaryContainer,
    onSecondaryContainer = PinkOnSecondaryContainer,
    
    // Tertiary - Púrpura complementario
    tertiary = TertiaryLight,
    onTertiary = TertiaryOnLight,
    tertiaryContainer = TertiaryContainerLight,
    onTertiaryContainer = TertiaryOnContainerLight,
    
    // Error
    error = ErrorLight,
    onError = ErrorOnLight,
    errorContainer = ErrorContainerLight,
    onErrorContainer = ErrorOnContainerLight,
    
    // Background & Surface
    background = BackgroundLight,
    onBackground = OnBackgroundLight,
    surface = SurfaceLight,
    onSurface = OnSurfaceLight,
    surfaceVariant = SurfaceVariantLight,
    onSurfaceVariant = OnSurfaceVariantLight,
    
    // Outline
    outline = OutlineLight,
    outlineVariant = OutlineVariantLight,
    
    // Inverse
    inverseSurface = SurfaceDark,
    inverseOnSurface = OnSurfaceDark,
    inversePrimary = TealPrimaryDarkTheme
)

/**
 * Color Scheme para Dark Theme
 * Paleta adaptada para modo oscuro
 */
private val DarkColorScheme = darkColorScheme(
    // Primary - Verde Agua (más claro para dark)
    primary = TealPrimaryDarkTheme,
    onPrimary = TealOnPrimaryDarkTheme,
    primaryContainer = TealPrimaryContainerDark,
    onPrimaryContainer = TealOnPrimaryContainerDark,
    
    // Secondary - Rosa Pastel (adaptado para dark)
    secondary = PinkSecondaryDarkTheme,
    onSecondary = PinkOnSecondaryDarkTheme,
    secondaryContainer = PinkSecondaryContainerDark,
    onSecondaryContainer = PinkOnSecondaryContainerDark,
    
    // Tertiary
    tertiary = TertiaryDark,
    onTertiary = TertiaryOnDark,
    tertiaryContainer = TertiaryContainerDark,
    onTertiaryContainer = TertiaryOnContainerDark,
    
    // Error
    error = ErrorDark,
    onError = ErrorOnDark,
    errorContainer = ErrorContainerDark,
    onErrorContainer = ErrorOnContainerDark,
    
    // Background & Surface
    background = BackgroundDark,
    onBackground = OnBackgroundDark,
    surface = SurfaceDark,
    onSurface = OnSurfaceDark,
    surfaceVariant = SurfaceVariantDark,
    onSurfaceVariant = OnSurfaceVariantDark,
    
    // Outline
    outline = OutlineDark,
    outlineVariant = OutlineVariantDark,
    
    // Inverse
    inverseSurface = SurfaceLight,
    inverseOnSurface = OnSurfaceLight,
    inversePrimary = TealPrimary
)

/**
 * Tema principal de LocalScribeAI
 * 
 * @param darkTheme Si es true, usa el tema oscuro. Por defecto sigue el sistema.
 * @param dynamicColor Si es true en Android 12+, usa colores dinámicos del wallpaper.
 *                     Deshabilitado por defecto para mantener la identidad visual.
 * @param content Contenido composable a renderizar con el tema.
 */
@Composable
fun LocalScribeAITheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Deshabilitamos dynamic color para mantener nuestra paleta verde agua + rosa
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = androidx.compose.ui.platform.LocalContext.current
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
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
