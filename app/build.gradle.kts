plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.localscribe.ai"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.localscribe.ai"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // Configuración para soportar arquitecturas ARM (mayoría de dispositivos Android)
        ndk {
            abiFilters += listOf("arm64-v8a", "armeabi-v7a")
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        debug {
            isMinifyEnabled = false
            isShrinkResources = false  // Solo release tiene shrink
            applicationIdSuffix = ".debug"
            versionNameSuffix = "-debug"
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
            excludes += "/META-INF/INDEX.LIST"
            excludes += "/META-INF/DEPENDENCIES"
            // Bytedeco/JavaCPP native-image configs (duplicate files)
            excludes += "/META-INF/native-image/**"
            excludes += "/META-INF/proguard/**"
            excludes += "/META-INF/maven/**"
        }
        jniLibs {
            useLegacyPackaging = true
        }
    }

    // Configuración para assets grandes (modelos de IA)
    androidResources {
        noCompress += listOf("bin", "onnx", "param", "model")
    }
}

dependencies {
    // Core Android
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.activity.compose)

    // Jetpack Compose con BOM
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.material.icons.extended)

    // Sherpa ONNX para transcripción offline (AAR local)
    implementation(files("libs/sherpa-onnx-1.12.23.aar"))

    // FFmpeg para conversión de audio (bytedeco - solo Android ARM para reducir tamaño)
    // Optimizado: ~30MB vs ~100MB con ffmpeg-platform
    implementation("org.bytedeco:ffmpeg:7.1-1.5.11:android-arm64")
    implementation("org.bytedeco:ffmpeg:7.1-1.5.11:android-arm")
    // JavaCPP base requerido por ffmpeg
    implementation("org.bytedeco:javacpp:1.5.11:android-arm64")
    implementation("org.bytedeco:javacpp:1.5.11:android-arm")

    // Coroutines para operaciones asíncronas
    implementation(libs.kotlinx.coroutines.android)

    // Debug tools
    debugImplementation(libs.androidx.ui.tooling)
}
