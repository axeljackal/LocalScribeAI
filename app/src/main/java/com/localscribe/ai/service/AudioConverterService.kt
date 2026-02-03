package com.localscribe.ai.service

import android.content.Context
import android.util.Log
import com.arthenica.ffmpegkit.FFmpegKit
import com.arthenica.ffmpegkit.ReturnCode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

/**
 * Servicio para conversión de audio usando FFmpeg
 * Convierte cualquier formato de audio a WAV 16kHz mono PCM
 */
class AudioConverterService(private val context: Context) {

    companion object {
        private const val TAG = "AudioConverter"
        private const val TARGET_SAMPLE_RATE = 16000
        private const val TARGET_CHANNELS = 1
        private const val TARGET_FORMAT = "wav"
    }

    /**
     * Convierte un archivo de audio a WAV 16kHz mono PCM
     * @param inputStream Stream del archivo de audio original
     * @param originalFileName Nombre original del archivo
     * @return Ruta al archivo WAV convertido
     */
    suspend fun convertToWav(
        inputStream: InputStream,
        originalFileName: String
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Starting audio conversion for: $originalFileName")
            
            // Crear archivo temporal para el input
            val inputFile = createTempInputFile(inputStream, originalFileName)
            
            // Crear archivo de salida
            val outputFile = createOutputFile()
            
            // Ejecutar conversión FFmpeg
            val result = executeConversion(inputFile.absolutePath, outputFile.absolutePath)
            
            // Limpiar archivo temporal de entrada
            inputFile.delete()
            
            if (result) {
                Log.d(TAG, "Conversion successful: ${outputFile.absolutePath}")
                Result.success(outputFile.absolutePath)
            } else {
                outputFile.delete()
                Result.failure(Exception("FFmpeg conversion failed"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Conversion error", e)
            Result.failure(e)
        }
    }

    /**
     * Copia el InputStream a un archivo temporal
     */
    private fun createTempInputFile(inputStream: InputStream, originalFileName: String): File {
        val extension = originalFileName.substringAfterLast('.', "audio")
        val tempFile = File(context.cacheDir, "input_${System.currentTimeMillis()}.$extension")
        
        FileOutputStream(tempFile).use { output ->
            inputStream.copyTo(output, bufferSize = 8192)
        }
        
        Log.d(TAG, "Created temp input file: ${tempFile.absolutePath} (${tempFile.length()} bytes)")
        return tempFile
    }

    /**
     * Crea el archivo de salida WAV
     */
    private fun createOutputFile(): File {
        val outputDir = File(context.cacheDir, "converted")
        outputDir.mkdirs()
        return File(outputDir, "audio_${System.currentTimeMillis()}.$TARGET_FORMAT")
    }

    /**
     * Ejecuta el comando FFmpeg para conversión
     */
    private fun executeConversion(inputPath: String, outputPath: String): Boolean {
        // Comando FFmpeg optimizado para Sherpa ONNX:
        // -y: Sobrescribir sin preguntar
        // -i: Archivo de entrada
        // -ar 16000: Sample rate 16kHz (requerido por Whisper)
        // -ac 1: Mono (requerido por Whisper)
        // -c:a pcm_s16le: Codec PCM 16-bit little endian
        // -f wav: Formato de salida WAV
        val command = "-y -i \"$inputPath\" -ar $TARGET_SAMPLE_RATE -ac $TARGET_CHANNELS -c:a pcm_s16le -f $TARGET_FORMAT \"$outputPath\""
        
        Log.d(TAG, "Executing FFmpeg command: $command")
        
        val session = FFmpegKit.execute(command)
        val returnCode = session.returnCode
        
        return if (ReturnCode.isSuccess(returnCode)) {
            Log.d(TAG, "FFmpeg completed successfully")
            true
        } else {
            Log.e(TAG, "FFmpeg failed with return code: $returnCode")
            Log.e(TAG, "FFmpeg output: ${session.output}")
            false
        }
    }

    /**
     * Obtiene la duración de un archivo de audio en segundos
     */
    suspend fun getAudioDuration(filePath: String): Float = withContext(Dispatchers.IO) {
        try {
            val session = com.arthenica.ffmpegkit.FFprobeKit.getMediaInformation(filePath)
            val mediaInfo = session.mediaInformation
            mediaInfo?.duration?.toFloatOrNull() ?: 0f
        } catch (e: Exception) {
            Log.e(TAG, "Error getting audio duration", e)
            0f
        }
    }

    /**
     * Limpia archivos temporales antiguos
     */
    fun cleanupTempFiles() {
        try {
            val cacheDir = context.cacheDir
            val convertedDir = File(cacheDir, "converted")
            
            // Eliminar archivos más antiguos de 1 hora
            val oneHourAgo = System.currentTimeMillis() - (60 * 60 * 1000)
            
            cacheDir.listFiles()?.forEach { file ->
                if (file.isFile && file.name.startsWith("input_") && file.lastModified() < oneHourAgo) {
                    file.delete()
                }
            }
            
            convertedDir.listFiles()?.forEach { file ->
                if (file.isFile && file.lastModified() < oneHourAgo) {
                    file.delete()
                }
            }
            
            Log.d(TAG, "Temp files cleanup completed")
        } catch (e: Exception) {
            Log.e(TAG, "Error cleaning temp files", e)
        }
    }
}
