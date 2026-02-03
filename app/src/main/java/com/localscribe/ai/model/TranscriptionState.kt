package com.localscribe.ai.model

/**
 * Representa los diferentes estados de la UI durante el proceso de transcripción
 */
sealed class TranscriptionState {
    
    /**
     * Estado inicial - esperando que el usuario comparta un audio
     */
    data object Idle : TranscriptionState()
    
    /**
     * Recibiendo el archivo de audio desde otra app
     */
    data object ReceivingFile : TranscriptionState()
    
    /**
     * Convirtiendo el audio a formato WAV usando FFmpeg
     */
    data class ConvertingAudio(val progress: Int = 0) : TranscriptionState()
    
    /**
     * Cargando el modelo de IA en memoria
     */
    data class LoadingModel(val modelName: String) : TranscriptionState()
    
    /**
     * Transcribiendo el audio con Sherpa ONNX
     */
    data class Transcribing(val progress: Int = 0) : TranscriptionState()
    
    /**
     * Transcripción completada exitosamente
     */
    data class Completed(val text: String, val durationMs: Long) : TranscriptionState()
    
    /**
     * Error durante el proceso
     */
    data class Error(val message: String, val exception: Throwable? = null) : TranscriptionState()
}

/**
 * Modos de transcripción disponibles
 */
enum class TranscriptionMode(
    val displayName: String,
    val description: String,
    val modelFolder: String
) {
    FAST(
        displayName = "Rápido",
        description = "Procesamiento veloz, ideal para notas de voz cortas",
        modelFolder = "model_tiny"
    ),
    ACCURATE(
        displayName = "Preciso",
        description = "Mayor precisión, recomendado para audios largos",
        modelFolder = "model_base"
    )
}

/**
 * Información del archivo de audio procesado
 */
data class AudioFileInfo(
    val originalUri: String,
    val originalFileName: String,
    val originalFormat: String,
    val convertedPath: String?,
    val durationSeconds: Float,
    val sampleRate: Int,
    val channels: Int
)

/**
 * Resultado de la transcripción
 */
data class TranscriptionResult(
    val text: String,
    val audioInfo: AudioFileInfo,
    val processingTimeMs: Long,
    val modelUsed: TranscriptionMode
)
