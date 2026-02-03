package com.localscribe.ai.service

import android.content.Context
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.bytedeco.ffmpeg.global.avcodec
import org.bytedeco.ffmpeg.global.avformat
import org.bytedeco.ffmpeg.global.avutil
import org.bytedeco.ffmpeg.global.swresample
import org.bytedeco.javacpp.Loader
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

/**
 * Servicio para conversión de audio usando FFmpeg (bytedeco)
 * Convierte cualquier formato de audio a WAV 16kHz mono PCM
 */
class AudioConverterService(private val context: Context) {

    companion object {
        private const val TAG = "AudioConverter"
        private const val TARGET_SAMPLE_RATE = 16000
        private const val TARGET_CHANNELS = 1
        private const val TARGET_FORMAT = "wav"
        
        init {
            // Cargar librerías nativas de FFmpeg
            try {
                Loader.load(avutil::class.java)
                Loader.load(avcodec::class.java)
                Loader.load(avformat::class.java)
                Loader.load(swresample::class.java)
            } catch (e: Exception) {
                Log.e(TAG, "Error loading FFmpeg libraries", e)
            }
        }
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
     * Ejecuta la conversión usando FFmpeg vía línea de comandos con bytedeco
     */
    private fun executeConversion(inputPath: String, outputPath: String): Boolean {
        return try {
            // Usar ProcessBuilder para ejecutar ffmpeg
            val ffmpegPath = Loader.load(org.bytedeco.ffmpeg.ffmpeg::class.java)
            
            val command = listOf(
                ffmpegPath,
                "-y",                    // Sobrescribir sin preguntar
                "-i", inputPath,         // Archivo de entrada
                "-ar", TARGET_SAMPLE_RATE.toString(), // Sample rate 16kHz
                "-ac", TARGET_CHANNELS.toString(),    // Mono
                "-c:a", "pcm_s16le",     // Codec PCM 16-bit little endian
                "-f", TARGET_FORMAT,      // Formato WAV
                outputPath
            )
            
            Log.d(TAG, "Executing FFmpeg command: ${command.joinToString(" ")}")
            
            val process = ProcessBuilder(command)
                .redirectErrorStream(true)
                .start()
            
            val output = process.inputStream.bufferedReader().readText()
            val exitCode = process.waitFor()
            
            if (exitCode == 0) {
                Log.d(TAG, "FFmpeg completed successfully")
                true
            } else {
                Log.e(TAG, "FFmpeg failed with exit code: $exitCode")
                Log.e(TAG, "FFmpeg output: $output")
                false
            }
        } catch (e: Exception) {
            Log.e(TAG, "FFmpeg execution error", e)
            false
        }
    }

    /**
     * Obtiene la duración de un archivo de audio en segundos
     */
    suspend fun getAudioDuration(filePath: String): Float = withContext(Dispatchers.IO) {
        try {
            val ffprobePath = Loader.load(org.bytedeco.ffmpeg.ffprobe::class.java)
            
            val command = listOf(
                ffprobePath,
                "-v", "error",
                "-show_entries", "format=duration",
                "-of", "default=noprint_wrappers=1:nokey=1",
                filePath
            )
            
            val process = ProcessBuilder(command)
                .redirectErrorStream(true)
                .start()
            
            val output = process.inputStream.bufferedReader().readText().trim()
            process.waitFor()
            
            output.toFloatOrNull() ?: 0f
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
