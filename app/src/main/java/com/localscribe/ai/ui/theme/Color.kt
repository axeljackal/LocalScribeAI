package com.localscribe.ai.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * Paleta de colores de LocalScribeAI
 * 
 * Colores principales:
 * - Verde Agua (Teal): Representa tecnología, confianza y claridad
 * - Rosa Pastel: Representa suavidad, accesibilidad y calidez
 */

// ============================================
// COLORES PRIMARIOS - Verde Agua (Teal)
// ============================================

// Light Theme - Primary
val TealPrimary = Color(0xFF26A69A)           // Verde agua principal
val TealPrimaryDark = Color(0xFF00897B)       // Verde agua oscuro (pressed)
val TealPrimaryLight = Color(0xFF4DB6AC)      // Verde agua claro
val TealOnPrimary = Color(0xFFFFFFFF)         // Texto sobre primary
val TealPrimaryContainer = Color(0xFFB2DFDB)  // Container suave
val TealOnPrimaryContainer = Color(0xFF00352F) // Texto sobre container

// Dark Theme - Primary
val TealPrimaryDarkTheme = Color(0xFF80CBC4)      // Verde agua para dark theme
val TealOnPrimaryDarkTheme = Color(0xFF003731)    // Texto sobre primary dark
val TealPrimaryContainerDark = Color(0xFF004D47)  // Container dark
val TealOnPrimaryContainerDark = Color(0xFFA7F3EC) // Texto sobre container dark

// ============================================
// COLORES SECUNDARIOS - Rosa Pastel
// ============================================

// Light Theme - Secondary
val PinkSecondary = Color(0xFFF8BBD9)         // Rosa pastel principal
val PinkSecondaryDark = Color(0xFFF48FB1)     // Rosa más intenso
val PinkSecondaryLight = Color(0xFFFCE4EC)    // Rosa muy suave
val PinkOnSecondary = Color(0xFF4A0E31)       // Texto sobre secondary
val PinkSecondaryContainer = Color(0xFFFCE4EC) // Container suave
val PinkOnSecondaryContainer = Color(0xFF31111D) // Texto sobre container

// Dark Theme - Secondary  
val PinkSecondaryDarkTheme = Color(0xFFF48FB1)    // Rosa para dark theme
val PinkOnSecondaryDarkTheme = Color(0xFF4A0E31)  // Texto sobre secondary dark
val PinkSecondaryContainerDark = Color(0xFF5D1A3B) // Container dark
val PinkOnSecondaryContainerDark = Color(0xFFFFD9E7) // Texto sobre container dark

// ============================================
// COLORES TERCIARIOS - Combinación complementaria
// ============================================

// Light Theme - Tertiary (acento complementario)
val TertiaryLight = Color(0xFF7E57C2)         // Púrpura suave
val TertiaryOnLight = Color(0xFFFFFFFF)
val TertiaryContainerLight = Color(0xFFEDE7F6)
val TertiaryOnContainerLight = Color(0xFF1A0E2E)

// Dark Theme - Tertiary
val TertiaryDark = Color(0xFFB39DDB)
val TertiaryOnDark = Color(0xFF1A0E2E)
val TertiaryContainerDark = Color(0xFF4A2D73)
val TertiaryOnContainerDark = Color(0xFFEDE7F6)

// ============================================
// COLORES DE ESTADO
// ============================================

// Success (Verde éxito)
val SuccessLight = Color(0xFF4CAF50)
val SuccessContainerLight = Color(0xFFC8E6C9)
val SuccessDark = Color(0xFF81C784)
val SuccessContainerDark = Color(0xFF1B5E20)

// Error (Rojo error)
val ErrorLight = Color(0xFFE53935)
val ErrorOnLight = Color(0xFFFFFFFF)
val ErrorContainerLight = Color(0xFFFFCDD2)
val ErrorOnContainerLight = Color(0xFF410002)

val ErrorDark = Color(0xFFEF9A9A)
val ErrorOnDark = Color(0xFF690005)
val ErrorContainerDark = Color(0xFF93000A)
val ErrorOnContainerDark = Color(0xFFFFDAD6)

// Warning (Naranja advertencia)
val WarningLight = Color(0xFFFF9800)
val WarningContainerLight = Color(0xFFFFE0B2)
val WarningDark = Color(0xFFFFB74D)
val WarningContainerDark = Color(0xFF5D4037)

// ============================================
// COLORES NEUTROS - Light Theme
// ============================================

val BackgroundLight = Color(0xFFFAFAFA)       // Fondo principal
val OnBackgroundLight = Color(0xFF1C1B1F)     // Texto sobre fondo
val SurfaceLight = Color(0xFFFFFFFF)          // Superficies (cards)
val OnSurfaceLight = Color(0xFF1C1B1F)        // Texto sobre surface
val SurfaceVariantLight = Color(0xFFE7E0EC)   // Surface alternativo
val OnSurfaceVariantLight = Color(0xFF49454F) // Texto sobre surface variant
val OutlineLight = Color(0xFF79747E)          // Bordes
val OutlineVariantLight = Color(0xFFCAC4D0)   // Bordes sutiles

// ============================================
// COLORES NEUTROS - Dark Theme
// ============================================

val BackgroundDark = Color(0xFF121212)        // Fondo principal dark
val OnBackgroundDark = Color(0xFFE6E1E5)      // Texto sobre fondo dark
val SurfaceDark = Color(0xFF1E1E1E)           // Superficies dark
val OnSurfaceDark = Color(0xFFE6E1E5)         // Texto sobre surface dark
val SurfaceVariantDark = Color(0xFF2D2D2D)    // Surface alternativo dark
val OnSurfaceVariantDark = Color(0xFFCAC4D0)  // Texto sobre surface variant dark
val OutlineDark = Color(0xFF938F99)           // Bordes dark
val OutlineVariantDark = Color(0xFF49454F)    // Bordes sutiles dark

// ============================================
// COLORES ESPECIALES DE LA APP
// ============================================

// Modo Rápido (Fast)
val FastModeColor = Color(0xFFFF9800)         // Naranja - velocidad
val FastModeContainerLight = Color(0xFFFFF3E0)
val FastModeContainerDark = Color(0xFF5D4037)

// Modo Preciso (Accurate)  
val AccurateModeColor = Color(0xFF4CAF50)     // Verde - precisión
val AccurateModeContainerLight = Color(0xFFE8F5E9)
val AccurateModeContainerDark = Color(0xFF1B5E20)

// Gradiente del logo
val GradientStart = TealPrimary               // Verde agua
val GradientEnd = PinkSecondaryDark           // Rosa pastel intenso

// Toast de confirmación
val ToastBackground = Color(0xFF323232)
val ToastContent = Color.White
