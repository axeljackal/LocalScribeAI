# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.kts.

# Mantener clases de Sherpa ONNX
-keep class com.k2fsa.sherpa.onnx.** { *; }
-keepclassmembers class com.k2fsa.sherpa.onnx.** { *; }

# Audio conversion usa APIs nativas de Android (MediaCodec/MediaExtractor)
# No requiere reglas ProGuard adicionales

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

# Mantener JNI
-keepclasseswithmembernames class * {
    native <methods>;
}
