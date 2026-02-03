# Plan de Mejoras y Auditorías - LocalScribeAI

> **Versión**: 1.0  
> **Fecha**: Febrero 2026  
> **Estado**: En planificación

Plan integral para optimizar rendimiento, seguridad, UI/UX y arquitectura de la aplicación de transcripción offline.

---

## Índice

1. [Resumen Ejecutivo](#resumen-ejecutivo)
2. [Fase 1: UI/UX - Colores y Diseño](#fase-1-uiux---colores-y-diseño)
3. [Fase 2: Optimización de Tamaño](#fase-2-optimización-de-tamaño)
4. [Fase 3: Soporte de Formatos de Audio](#fase-3-soporte-de-formatos-de-audio)
5. [Fase 4: Auditoría de Seguridad](#fase-4-auditoría-de-seguridad)
6. [Fase 5: Build y CI/CD](#fase-5-build-y-cicd)
7. [Fase 6: Mejoras de Transcripción](#fase-6-mejoras-de-transcripción)
8. [Fase 7: Compatibilidad de Dispositivos](#fase-7-compatibilidad-de-dispositivos)
9. [Mejoras Futuras](#mejoras-futuras)
10. [Prioridades y Roadmap](#prioridades-y-roadmap)

---

## Resumen Ejecutivo

| Fase | Área | Prioridad | Esfuerzo | Estado |
|------|------|-----------|----------|--------|
| 1 | 🎨 UI/UX - Colores y Diseño | Alta | Medio | ⏳ Pendiente |
| 2 | 📦 Optimización de Tamaño | Alta | Alto | ⏳ Pendiente |
| 3 | 🎵 Soporte de Formatos de Audio | Media | Bajo | ⏳ Pendiente |
| 4 | 🔒 Auditoría de Seguridad | Alta | Bajo | ⏳ Pendiente |
| 5 | ⚙️ Build y CI/CD | Media | Medio | ⏳ Pendiente |
| 6 | 🎤 Mejoras de Transcripción | Media | Alto | ⏳ Pendiente |
| 7 | 📱 Compatibilidad de Dispositivos | Alta | Medio | ⏳ Pendiente |

**Dispositivo de referencia**: Moto Edge 40 Pro (Snapdragon 8 Gen 2, 12GB RAM)

---

## Fase 1: UI/UX - Colores y Diseño

### Objetivo

Implementar paleta de colores verde agua + rosa pastel, modularizar UI, mejorar UX de copiado de texto.

### Paleta de Colores

| Elemento | Color Light | Color Dark | Hex |
|----------|-------------|------------|-----|
| Primary (Verde Agua) | Verde agua | Verde agua claro | `#26A69A` / `#4DB6AC` |
| Secondary (Rosa Pastel) | Rosa pastel | Rosa pastel suave | `#F8BBD9` / `#F48FB1` |
| Background | Blanco cálido | Gris oscuro | `#FAFAFA` / `#121212` |
| Surface | Blanco | Gris oscuro | `#FFFFFF` / `#1E1E1E` |
| On Primary | Blanco | Negro | `#FFFFFF` / `#000000` |
| On Secondary | Negro | Negro | `#000000` / `#000000` |

### Tareas

- [ ] **1.1** Crear archivo `Color.kt` con paleta completa (light + dark)
- [ ] **1.2** Actualizar `Theme.kt` con `MaterialTheme` usando nuevos colores
- [ ] **1.3** Implementar modo oscuro con colores adaptados
- [ ] **1.4** Refactorizar `MainActivity.kt` (834 líneas) en componentes modulares:
  - `screens/IdleScreen.kt`
  - `screens/ProcessingScreen.kt`
  - `screens/ResultScreen.kt`
  - `screens/ErrorScreen.kt`
  - `components/ModeSelector.kt`
  - `components/ProcessingSteps.kt`
  - `components/TranscriptionResult.kt`
- [ ] **1.5** Agregar `SelectionContainer` para selección de texto transcrito
- [ ] **1.6** Implementar botón de copiar con feedback visual (Snackbar "Texto copiado")
- [ ] **1.7** Agregar animaciones sutiles con `animateContentSize` en transiciones

### Archivos a Modificar

- `app/src/main/java/com/localscribe/ai/ui/theme/Color.kt` (crear)
- `app/src/main/java/com/localscribe/ai/ui/theme/Theme.kt`
- `app/src/main/java/com/localscribe/ai/MainActivity.kt`
- `app/src/main/java/com/localscribe/ai/ui/screens/` (crear directorio)
- `app/src/main/java/com/localscribe/ai/ui/components/` (crear directorio)

### Decisiones

| Pregunta | Decisión |
|----------|----------|
| ¿Implementar modo oscuro? | ✅ **Sí** - Con colores adaptados |
| ¿Agregar splash screen animado? | ❌ **No** - Priorizar velocidad de interacción |

---

## Fase 2: Optimización de Tamaño

### Objetivo

Reducir tamaño del APK manteniendo todos los modelos incluidos.

### Estado Actual vs Objetivo

| Componente | Tamaño Actual | Tamaño Objetivo |
|------------|---------------|-----------------|
| Modelo Tiny | ~40 MB | ~40 MB |
| Modelo Base | ~150 MB | ~150 MB |
| FFmpeg (bytedeco) | ~100 MB (todas las plataformas) | ~30 MB (solo Android ARM) |
| Sherpa ONNX AAR | ~35 MB | ~35 MB |
| **Total APK** | **~300-400 MB** | **~250 MB** |

### Tareas

- [ ] **2.1** Migrar de APK a **Android App Bundle (AAB)** para optimización por arquitectura
- [ ] **2.2** Reemplazar dependencia FFmpeg:
  ```kotlin
  // Antes (todas las plataformas)
  implementation("org.bytedeco:ffmpeg-platform:7.1-1.5.11")
  
  // Después (solo Android ARM)
  implementation("org.bytedeco:ffmpeg:7.1-1.5.11:android-arm64")
  implementation("org.bytedeco:ffmpeg:7.1-1.5.11:android-arm")
  ```
- [ ] **2.3** Habilitar `isShrinkResources = true` en build de debug para testing
- [ ] **2.4** Revisar y eliminar recursos no utilizados
- [ ] **2.5** Optimizar imágenes y vectores drawable

### Archivos a Modificar

- `app/build.gradle.kts`
- `gradle/libs.versions.toml`

### Decisiones

| Pregunta | Decisión |
|----------|----------|
| ¿Play Asset Delivery o HTTP? | ❌ **Ninguno** - Todos los assets incluidos en APK |
| ¿Versión "lite" solo con Tiny? | ❌ **No** - Una sola versión con ambos modelos |

---

## Fase 3: Soporte de Formatos de Audio

### Objetivo

Verificar y documentar formatos soportados, agregar validación y feedback al usuario.

### Formatos Soportados

| Formato | Extensión | MIME Type | Estado |
|---------|-----------|-----------|--------|
| WAV | `.wav` | `audio/wav` | ✅ Nativo |
| MP3 | `.mp3` | `audio/mpeg` | ✅ Via FFmpeg |
| OGG/Opus | `.ogg`, `.opus` | `audio/ogg`, `audio/opus` | ✅ Via FFmpeg |
| M4A/AAC | `.m4a`, `.aac` | `audio/mp4`, `audio/aac` | ✅ Via FFmpeg |
| FLAC | `.flac` | `audio/flac` | ✅ Via FFmpeg |
| WebM | `.webm` | `audio/webm` | ✅ Via FFmpeg |
| AMR | `.amr` | `audio/amr` | ✅ Via FFmpeg |
| 3GP | `.3gp` | `audio/3gpp` | ✅ Via FFmpeg |

### Tareas

- [ ] **3.1** Agregar validación de formato con mensaje de error específico
- [ ] **3.2** Mostrar formato detectado en UI durante procesamiento
- [ ] **3.3** Agregar intent-filters específicos en `AndroidManifest.xml` para cada MIME type
- [ ] **3.4** Implementar manejo de archivos sin extensión (detección por magic bytes)
- [ ] **3.5** Documentar formatos soportados en README.md

### Archivos a Modificar

- `app/src/main/java/com/localscribe/ai/service/AudioConverterService.kt`
- `app/src/main/AndroidManifest.xml`
- `README.md`

### Decisiones

| Pregunta | Decisión |
|----------|----------|
| ¿Soporte para video (MP4/MOV)? | ❌ **No por ahora** - Documentar como mejora futura |

---

## Fase 4: Auditoría de Seguridad

### Objetivo

Verificar y certificar que la aplicación es 100% offline y segura.

### Checklist de Seguridad

- [ ] **4.1** Verificar: Sin permiso `INTERNET` en AndroidManifest.xml
- [ ] **4.2** Verificar: Sin dependencias que requieran red
- [ ] **4.3** Verificar: Sin API keys, secrets, o tokens hardcodeados
- [ ] **4.4** Verificar: Sin telemetría o analytics
- [ ] **4.5** Verificar: FileProvider configurado correctamente
- [ ] **4.6** Verificar: Datos temporales se eliminan correctamente
- [ ] **4.7** Agregar badge "100% Offline - Sin Telemetría" en UI
- [ ] **4.8** Crear documento `SECURITY.md` con modelo de seguridad

### Modelo de Seguridad

```
┌─────────────────────────────────────────────────────────┐
│                    LocalScribeAI                        │
├─────────────────────────────────────────────────────────┤
│  ✅ Sin conexión a Internet                             │
│  ✅ Sin telemetría ni analytics                         │
│  ✅ Sin API keys ni secrets                             │
│  ✅ Procesamiento 100% local en dispositivo             │
│  ✅ Archivos temporales eliminados automáticamente      │
│  ✅ No se almacenan transcripciones permanentemente     │
└─────────────────────────────────────────────────────────┘
```

### Archivos a Crear/Modificar

- `SECURITY.md` (crear)
- `app/src/main/AndroidManifest.xml` (verificar)

### Resultado Esperado

**Certificación**: App 100% offline, zero network, zero telemetry

---

## Fase 5: Build y CI/CD

### Objetivo

Optimizar pipeline, preparar para futuras releases firmadas.

### Tareas

- [ ] **5.1** Limpiar regla obsoleta `com.arthenica.ffmpegkit` de ProGuard
- [ ] **5.2** Agregar job de tests unitarios en workflow
- [ ] **5.3** Agregar versionado automático basado en tags Git
- [ ] **5.4** Configurar Dependabot para actualizaciones de dependencias
- [ ] **5.5** Agregar badge de build status en README.md
- [ ] **5.6** Documentar proceso de build local

### Configuración de Signing (Futuro)

```kotlin
// build.gradle.kts - Para cuando se configure signing
signingConfigs {
    create("release") {
        storeFile = file(System.getenv("KEYSTORE_PATH") ?: "keystore.jks")
        storePassword = System.getenv("KEYSTORE_PASSWORD") ?: ""
        keyAlias = System.getenv("KEY_ALIAS") ?: ""
        keyPassword = System.getenv("KEY_PASSWORD") ?: ""
    }
}
```

### Archivos a Modificar

- `app/proguard-rules.pro`
- `.github/workflows/android_build.yml`
- `README.md`

### Decisiones

| Pregunta | Decisión |
|----------|----------|
| ¿Publicar en Google Play Store? | ⏳ **Futuro** - Primero probar APK extensivamente |
| ¿GitHub Releases automáticos? | ⏳ **Futuro** - Cuando la app esté probada |

---

## Fase 6: Mejoras de Transcripción

### Objetivo

Mejorar precisión, agregar features avanzados, optimizar rendimiento.

### Tareas

- [ ] **6.1** Implementar **detección automática de idioma**
- [ ] **6.2** Agregar **selector manual de idioma** como fallback
- [ ] **6.3** Agregar opción de **idioma por defecto** en configuración
- [ ] **6.4** Implementar **callback de progreso** durante transcripción
- [ ] **6.5** Agregar opción de **timestamps/subtítulos** (formato SRT)
- [ ] **6.6** Optimizar threads según núcleos del dispositivo
- [ ] **6.7** Implementar **chunking** para audios >5 minutos
- [ ] **6.8** Agregar **post-procesamiento**: puntuación automática, capitalización

### Idiomas Soportados

| Idioma | Código | Detección Auto |
|--------|--------|----------------|
| Español | `es` | ✅ |
| Inglés | `en` | ✅ |
| Portugués | `pt` | ✅ |
| Francés | `fr` | ✅ |
| Alemán | `de` | ✅ |
| Italiano | `it` | ✅ |
| (más según modelo Whisper) | ... | ✅ |

### Configuración de Idioma

```
┌─────────────────────────────────────────┐
│         Configuración de Idioma         │
├─────────────────────────────────────────┤
│  ○ Detección automática (recomendado)   │
│  ○ Español                              │
│  ○ Inglés                               │
│  ○ Portugués                            │
│  ○ Otro...                              │
├─────────────────────────────────────────┤
│  Idioma por defecto: [Español ▼]        │
│  (usado cuando la detección falla)      │
└─────────────────────────────────────────┘
```

### Archivos a Modificar

- `app/src/main/java/com/localscribe/ai/service/TranscriptionService.kt`
- `app/src/main/java/com/localscribe/ai/viewmodel/TranscriptionViewModel.kt`
- `app/src/main/java/com/localscribe/ai/model/TranscriptionState.kt`

### Decisiones

| Pregunta | Decisión |
|----------|----------|
| ¿Modelo "Large" opcional (~300MB)? | ✅ **Sí** - Descarga opcional para máxima precisión |
| ¿Transcripción en tiempo real? | ⏳ **Evaluar** - Ver análisis abajo |

### Análisis: Transcripción en Tiempo Real (Streaming)

| Aspecto | Pros | Contras |
|---------|------|---------|
| **UX** | Ver texto mientras habla | Texto cambia constantemente, puede confundir |
| **Rendimiento** | Feedback inmediato | Mayor uso de CPU/batería continuo |
| **Precisión** | N/A | Menor precisión que batch (sin contexto completo) |
| **Implementación** | Feature atractivo | Complejidad alta, requiere modelo diferente |
| **Casos de uso** | Grabación en vivo | No aplica a audios compartidos (ya grabados) |

**Recomendación**: Para audios compartidos (caso de uso principal), el modo batch es superior. El streaming sería útil solo si se agrega grabación en vivo, lo cual es una feature separada.

**Decisión**: ⏳ **Futuro** - Considerar solo si se implementa grabación en vivo

---

## Fase 7: Compatibilidad de Dispositivos

### Objetivo

Asegurar funcionamiento en dispositivos desde gama media.

### Requisitos Mínimos

| Requisito | Mínimo | Recomendado |
|-----------|--------|-------------|
| Android | 8.0 (API 26) | 11.0+ (API 30) |
| RAM | 4 GB | 6 GB+ |
| Arquitectura | ARM64-v8a | ARM64-v8a |
| Almacenamiento libre | 500 MB | 1 GB |

### Dispositivo de Referencia

**Moto Edge 40 Pro**
- SoC: Snapdragon 8 Gen 2
- RAM: 12 GB
- Android: 13+
- **Resultado esperado**: Rendimiento excelente en ambos modos

### Tareas

- [ ] **7.1** Agregar detección de memoria disponible antes de cargar modelo
- [ ] **7.2** Implementar fallback automático: Si RAM < 4GB → Solo Tiny
- [ ] **7.3** Agregar benchmark inicial para recomendar modo óptimo
- [ ] **7.4** Agregar soporte x86_64 para emuladores de desarrollo
- [ ] **7.5** Crear matriz de pruebas con dispositivos objetivo
- [ ] **7.6** Documentar requisitos mínimos en README.md

### Matriz de Pruebas

| Dispositivo | Gama | RAM | Resultado Esperado |
|-------------|------|-----|-------------------|
| Moto Edge 40 Pro | Alta | 12 GB | ✅ Excelente |
| Samsung Galaxy A54 | Media-Alta | 8 GB | ✅ Muy bueno |
| Pixel 6a | Media-Alta | 6 GB | ✅ Bueno |
| Xiaomi Redmi Note 11 | Media | 4 GB | ⚠️ Solo Tiny recomendado |
| Samsung Galaxy A13 | Baja | 3 GB | ⚠️ Puede tener limitaciones |

### Archivos a Modificar

- `app/build.gradle.kts`
- `app/src/main/java/com/localscribe/ai/viewmodel/TranscriptionViewModel.kt`
- `README.md`

---

## Mejoras Futuras

Funcionalidades consideradas para versiones posteriores, no incluidas en el plan actual.

### Prioridad Media

| Mejora | Descripción | Complejidad |
|--------|-------------|-------------|
| Soporte para video | Extraer audio de MP4, MOV, WebM | Media |
| Historial de transcripciones | Guardar y buscar transcripciones anteriores | Media |
| Exportar a múltiples formatos | TXT, SRT, PDF, DOCX | Media |
| Widget de Android | Transcribir desde pantalla de inicio | Alta |

### Prioridad Baja

| Mejora | Descripción | Complejidad |
|--------|-------------|-------------|
| Transcripción en tiempo real | Streaming de audio en vivo | Alta |
| Grabación de audio integrada | Grabar y transcribir sin salir de la app | Media |
| Traducción automática | Transcribir y traducir en un paso | Alta |
| Resumen con IA | Resumir transcripciones largas | Alta |
| Wear OS companion | App para smartwatch | Alta |

### Distribución

| Mejora | Descripción | Requisitos |
|--------|-------------|------------|
| Google Play Store | Publicación oficial | Signing, políticas, cuenta dev |
| GitHub Releases | APKs firmados automáticos | Keystore, workflow |
| F-Droid | Tienda alternativa FOSS | Build reproducible |

---

## Prioridades y Roadmap

### Sprint 1: Fundamentos (Semanas 1-2)

| # | Tarea | Fase | Prioridad |
|---|-------|------|-----------|
| 1 | Cambiar colores a verde agua + rosa pastel | 1 | 🔴 Alta |
| 2 | Implementar modo oscuro | 1 | 🔴 Alta |
| 3 | Agregar SelectionContainer para copiar texto | 1 | 🔴 Alta |
| 4 | Verificar seguridad y crear SECURITY.md | 4 | 🔴 Alta |

### Sprint 2: Optimización (Semanas 3-4)

| # | Tarea | Fase | Prioridad |
|---|-------|------|-----------|
| 5 | Optimizar dependencia FFmpeg (solo Android ARM) | 2 | 🔴 Alta |
| 6 | Modularizar MainActivity.kt | 1 | 🟡 Media |
| 7 | Limpiar ProGuard rules obsoletas | 5 | 🟡 Media |
| 8 | Verificar formatos de audio soportados | 3 | 🟡 Media |

### Sprint 3: Features (Semanas 5-6)

| # | Tarea | Fase | Prioridad |
|---|-------|------|-----------|
| 9 | Detección automática de idioma | 6 | 🟡 Media |
| 10 | Selector manual de idioma | 6 | 🟡 Media |
| 11 | Detección de memoria y fallback | 7 | 🟡 Media |
| 12 | Agregar soporte x86_64 para emuladores | 7 | 🟢 Baja |

### Sprint 4: Pulido (Semanas 7-8)

| # | Tarea | Fase | Prioridad |
|---|-------|------|-----------|
| 13 | Callback de progreso en transcripción | 6 | 🟡 Media |
| 14 | Tests unitarios básicos | 5 | 🟡 Media |
| 15 | Documentación completa en README | - | 🟡 Media |
| 16 | Pruebas en dispositivos de referencia | 7 | 🔴 Alta |

---

## Métricas de Éxito

| Métrica | Actual | Objetivo |
|---------|--------|----------|
| Tamaño APK | ~300 MB | < 280 MB |
| Tiempo de inicio | ? | < 2 segundos |
| Tiempo transcripción (1 min audio, Tiny) | ? | < 15 segundos |
| Tiempo transcripción (1 min audio, Base) | ? | < 30 segundos |
| Uso de RAM (pico, Tiny) | ? | < 1 GB |
| Uso de RAM (pico, Base) | ? | < 2 GB |
| Crash rate | ? | 0% |
| Dispositivos compatibles | API 26+ ARM | API 26+ ARM + x86_64 |

---

## Notas Adicionales

### Convenciones de Código

- Kotlin con Jetpack Compose
- MVVM architecture
- Coroutines para operaciones asíncronas
- Material Design 3

### Testing

- Dispositivo principal de pruebas: Moto Edge 40 Pro
- Emulador: Pixel 6 API 34 (x86_64)
- Audios de prueba: WhatsApp, grabadora nativa, archivos descargados

### Documentación Relacionada

- `README.md` - Documentación general
- `SECURITY.md` - Modelo de seguridad (a crear)
- `LICENSE` - Licencia propietaria
- `CONTRIBUTING.md` - Guía de contribución (futuro)

---

*Última actualización: Febrero 2026*
