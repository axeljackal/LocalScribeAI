# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.kts.

# Mantener clases de Sherpa ONNX
-keep class com.k2fsa.sherpa.onnx.** { *; }
-keepclassmembers class com.k2fsa.sherpa.onnx.** { *; }

# Mantener clases de FFmpeg Kit
-keep class com.arthenica.ffmpegkit.** { *; }
-keepclassmembers class com.arthenica.ffmpegkit.** { *; }

# Mantener nuestras clases de modelo
-keep class com.localscribe.ai.** { *; }

# Reglas generales para Kotlin
-keepattributes *Annotation*
-keepattributes Signature
-keepattributes InnerClasses

# Reglas para Coroutines
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-keepclassmembers class kotlinx.coroutines.** {
    volatile <fields>;
}

# Reglas para Compose
-keep class androidx.compose.** { *; }

# Reglas para Bytedeco/JavaCPP y FFmpeg
-keep class org.bytedeco.** { *; }
-keepclassmembers class org.bytedeco.** { *; }
-dontwarn org.bytedeco.**

# Ignorar clases de OSGI que no existen en Android
-dontwarn org.osgi.**
-dontwarn org.osgi.annotation.versioning.**
-dontwarn aQute.bnd.annotation.**
-dontwarn org.slf4j.**

# Mantener JNI
-keepclasseswithmembernames class * {
    native <methods>;
}
