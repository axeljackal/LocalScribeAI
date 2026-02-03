# LocalScribe AI

<!-- markdownlint-disable MD033 -->
<p align="center">
  <img src="IMG/logo.png" alt="LocalScribe AI Logo" width="150"/>
</p>
<!-- markdownlint-enable MD033 -->

> Aplicación Android de transcripción de audio 100% offline usando IA local

[![Android Build CI](https://github.com/axeljackal/LocalScribeAI/actions/workflows/android_build.yml/badge.svg)](https://github.com/axeljackal/LocalScribeAI/actions/workflows/android_build.yml)

## 🎯 Características

- ✅ **100% Offline** - Tus datos nunca salen del dispositivo
- ✅ **Privacidad Total** - Sin servidores externos, sin telemetría
- ✅ **Integración WhatsApp** - Comparte audios directamente desde WhatsApp
- ✅ **Dos Modos de Potencia**:
  - ⚡ **Rápido (Tiny)** - Para notas de voz cortas
  - 🎯 **Preciso (Base)** - Para audios largos o complejos
- ✅ **Material Design 3** - Interfaz moderna y profesional
- ✅ **Soporte NPU** - Aprovecha aceleradores de hardware cuando estén disponibles

## 📱 Requisitos del Sistema

- Android 8.0 (API 26) o superior
- Mínimo 4GB RAM recomendado
- ~500MB de espacio para modelos

## 🚀 Instalación

### Opción 1: Descargar APK (Recomendado)

1. Ve a la sección [Releases](https://github.com/axeljackal/LocalScribeAI/releases)
2. Descarga el archivo `LocalScribeAI-vX.X.X.apk`
3. Instala en tu dispositivo Android

### Opción 2: Compilar desde código fuente

#### Requisitos de Desarrollo

- **JDK 17** (Java Development Kit, NO solo JRE)
  - Windows: `winget install EclipseAdoptium.Temurin.17.JDK`
  - macOS: `brew install --cask temurin17`
  - Linux: `sudo apt install openjdk-17-jdk`
- **Android SDK** (incluido con Android Studio)
- **~4GB RAM** disponible para Gradle

> ⚠️ **Importante**: Necesitás el **JDK** (incluye compilador `javac`), no el JRE.
> Verificá con: `javac -version`

#### Configurar JAVA_HOME (Windows)

```powershell
# Configurar variable de entorno (ejecutar como administrador)
[System.Environment]::SetEnvironmentVariable("JAVA_HOME", "C:\Program Files\Eclipse Adoptium\jdk-17.0.17.10-hotspot", "Machine")
```

#### Compilar

```bash
# Clonar repositorio
git clone https://github.com/axeljackal/LocalScribeAI.git
cd LocalScribeAI

# Compilar APK debug
./gradlew assembleDebug

# El APK estará en: app/build/outputs/apk/debug/app-debug.apk
```

## 📦 Configuración de Modelos de IA

Antes de ejecutar la app, necesitas descargar los modelos de Whisper cuantizados:

### Modelo Tiny (Modo Rápido) ~ 40MB

Descarga desde HuggingFace:

- **Repositorio**: <https://huggingface.co/csukuangfj/sherpa-onnx-whisper-tiny.en>
- **Archivos necesarios**:
  - `tiny.en-encoder.int8.onnx` → renombrar a `encoder.int8.onnx`
  - `tiny.en-decoder.int8.onnx` → renombrar a `decoder.int8.onnx`
  - `tiny.en-tokens.txt` → renombrar a `tokens.txt`

Para español, usa el modelo multilingüe:

- **Repositorio**: <https://huggingface.co/csukuangfj/sherpa-onnx-whisper-tiny>
- **Archivos**:
  - `tiny-encoder.int8.onnx` → renombrar a `encoder.int8.onnx`
  - `tiny-decoder.int8.onnx` → renombrar a `decoder.int8.onnx`
  - `tiny-tokens.txt` → renombrar a `tokens.txt`

### Modelo Base (Modo Preciso) ~ 150MB

Descarga desde HuggingFace:

- **Repositorio**: <https://huggingface.co/csukuangfj/sherpa-onnx-whisper-base>
- **Archivos necesarios**:
  - `base-encoder.int8.onnx` → renombrar a `encoder.int8.onnx`
  - `base-decoder.int8.onnx` → renombrar a `decoder.int8.onnx`
  - `base-tokens.txt` → renombrar a `tokens.txt`

### Ubicación de los Modelos

Coloca los archivos en la estructura de assets:

```text
app/src/main/assets/
├── model_tiny/
│   ├── encoder.int8.onnx
│   ├── decoder.int8.onnx
│   └── tokens.txt
└── model_base/
    ├── encoder.int8.onnx
    ├── decoder.int8.onnx
    └── tokens.txt
```

## 🎨 Personalización de Iconos

El proyecto incluye iconos vectoriales por defecto. Para personalizar:

### Archivos de iconos requeridos

| Archivo | Ubicación | Descripción |
| ------- | --------- | ----------- |
| `ic_app_logo.xml` | `res/drawable/` | Logo principal de la app (vector) |
| `ic_mode_fast.xml` | `res/drawable/` | Icono modo rápido (vector) |
| `ic_mode_accurate.xml` | `res/drawable/` | Icono modo preciso (vector) |

### Para usar imágenes PNG en lugar de vectores

1. Crea versiones PNG en las densidades requeridas:
   - `res/drawable-mdpi/` (48x48px)
   - `res/drawable-hdpi/` (72x72px)
   - `res/drawable-xhdpi/` (96x96px)
   - `res/drawable-xxhdpi/` (144x144px)
   - `res/drawable-xxxhdpi/` (192x192px)

2. Nombres de archivos:
   - `ic_app_logo.png`
   - `ic_mode_fast.png`
   - `ic_mode_accurate.png`

## 🔧 Arquitectura del Proyecto

```text
LocalScribeAI/
├── app/
│   ├── src/main/
│   │   ├── java/com/localscribe/ai/
│   │   │   ├── MainActivity.kt           # Activity principal + UI Compose
│   │   │   ├── LocalScribeApplication.kt # Clase Application
│   │   │   ├── model/
│   │   │   │   └── TranscriptionState.kt # Estados y modelos de datos
│   │   │   ├── service/
│   │   │   │   ├── AudioConverterService.kt  # Conversión FFmpeg
│   │   │   │   └── TranscriptionService.kt   # Inferencia Sherpa
│   │   │   ├── viewmodel/
│   │   │   │   └── TranscriptionViewModel.kt # Lógica de negocio
│   │   │   └── ui/theme/
│   │   │       ├── Theme.kt              # Tema Material 3
│   │   │       └── Type.kt               # Tipografía
│   │   ├── res/
│   │   │   ├── drawable/                 # Iconos vectoriales
│   │   │   ├── values/                   # Strings, colores, temas
│   │   │   └── xml/                      # Configuraciones
│   │   ├── assets/                       # Modelos de IA (añadir manualmente)
│   │   └── AndroidManifest.xml
│   └── build.gradle.kts                  # Config app-level
├── gradle/
│   ├── libs.versions.toml                # Catálogo de dependencias
│   └── wrapper/
├── .github/workflows/
│   └── android_build.yml                 # CI/CD GitHub Actions
├── build.gradle.kts                      # Config project-level
├── settings.gradle.kts
└── README.md
```

## 🔄 Flujo de Trabajo CI/CD

El workflow de GitHub Actions:

1. **Trigger**: Push a `main` o Pull Request
2. **Build Debug**: Compila APK de debug
3. **Build Release**: Compila APK release (solo en push a main)
4. **Artifacts**: Sube APKs como artifacts descargables

### Descargar APK compilado

1. Ve a **Actions** en GitHub
2. Selecciona el último workflow exitoso
3. Descarga el artifact `LocalScribeAI-Debug-xxx`

## 📋 Uso de la Aplicación

1. **Abre WhatsApp** (u otra app de mensajería)
2. **Mantén presionado** sobre una nota de voz
3. **Selecciona "Compartir"**
4. **Elige "LocalScribe AI"**
5. **Espera** la transcripción
6. **Copia** el texto con el botón de copiar

## 🛠️ Tecnologías Utilizadas

- **Kotlin** - Lenguaje principal
- **Jetpack Compose** - UI declarativa
- **Material Design 3** - Sistema de diseño
- **Sherpa ONNX** - Motor de transcripción offline
- **FFmpeg Kit** - Conversión de audio
- **Coroutines** - Programación asíncrona
- **GitHub Actions** - CI/CD automatizado

## 📄 Licencia

**Software Propietario - Todos los derechos reservados.**

Copyright © 2026 LocalScribe AI

- ✅ Uso personal y comercial permitido (adquisición legítima)
- ❌ Distribución prohibida fuera de canales oficiales
- ❌ Modificación y obras derivadas prohibidas
- ❌ Ingeniería inversa prohibida

Ver [LICENSE](LICENSE) para los términos completos.

## 🤝 Contribuciones

¡Las contribuciones son bienvenidas! Por favor:

1. Fork el repositorio
2. Crea una rama feature (`git checkout -b feature/AmazingFeature`)
3. Commit tus cambios (`git commit -m 'Add some AmazingFeature'`)
4. Push a la rama (`git push origin feature/AmazingFeature`)
5. Abre un Pull Request

---

<!-- markdownlint-disable MD033 -->
<p align="center">
  Hecho con ❤️ para la privacidad del usuario
</p>
<!-- markdownlint-enable MD033 -->
