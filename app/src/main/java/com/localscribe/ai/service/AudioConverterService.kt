package com.localscribe.ai.service

import android.content.Context
import android.media.MediaCodec
import android.media.MediaExtractor
import android.media.MediaFormat
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.io.RandomAccessFile
import java.nio.ByteBuffer
import java.nio.ByteOrder

/**
 * Servicio para conversión de audio usando APIs nativas de Android
 * Convierte cualquier formato de audio a WAV 16kHz mono PCM
 * Compatible 100% con AAB (App Bundle) para Play Store
 * Sin dependencias externas - usa MediaCodec y MediaExtractor
 */
class AudioConverterService(private val context: Context) {

    companion object {
        private const val TAG = "AudioConverter"
        private const val TARGET_SAMPLE_RATE = 16000
        private const val TARGET_CHANNELS = 1
        private const val BITS_PER_SAMPLE = 16
        private const val TIMEOUT_US = 10000L
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
            
            // Ejecutar conversión nativa
            val result = executeNativeConversion(inputFile.absolutePath, outputFile.absolutePath)
            
            // Limpiar archivo temporal de entrada
            inputFile.delete()
            
            if (result) {
                Log.d(TAG, "Conversion successful: ${outputFile.absolutePath}")
                Result.success(outputFile.absolutePath)
            } else {
                outputFile.delete()
                Result.failure(Exception("Audio conversion failed"))
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
        return File(outputDir, "audio_${System.currentTimeMillis()}.wav")
    }

    /**
     * Ejecuta la conversión usando APIs nativas de Android (MediaCodec/MediaExtractor)
     */
    private fun executeNativeConversion(inputPath: String, outputPath: String): Boolean {
        var extractor: MediaExtractor? = null
        var decoder: MediaCodec? = null
        var outputStream: RandomAccessFile? = null
        
        try {
            Log.d(TAG, "Starting native audio conversion")
            
            // Configurar extractor
            extractor = MediaExtractor()
            extractor.setDataSource(inputPath)
            
            // Buscar track de audio
            val audioTrackIndex = findAudioTrack(extractor)
            if (audioTrackIndex < 0) {
                Log.e(TAG, "No audio track found in file")
                return false
            }
            
            extractor.selectTrack(audioTrackIndex)
            val inputFormat = extractor.getTrackFormat(audioTrackIndex)
            val mimeType = inputFormat.getString(MediaFormat.KEY_MIME) ?: return false
            
            Log.d(TAG, "Input format: $inputFormat")
            Log.d(TAG, "MIME type: $mimeType")
            
            // Obtener parámetros del audio original
            val inputSampleRate = inputFormat.getInteger(MediaFormat.KEY_SAMPLE_RATE)
            val inputChannels = inputFormat.getInteger(MediaFormat.KEY_CHANNEL_COUNT)
            
            Log.d(TAG, "Input: ${inputSampleRate}Hz, $inputChannels channels")
            
            // Configurar decoder
            decoder = MediaCodec.createDecoderByType(mimeType)
            decoder.configure(inputFormat, null, null, 0)
            decoder.start()
            
            // Preparar archivo de salida (dejar espacio para header WAV)
            outputStream = RandomAccessFile(outputPath, "rw")
            outputStream.seek(44) // Skip WAV header, lo escribimos al final
            
            // Buffers
            val bufferInfo = MediaCodec.BufferInfo()
            val pcmDataList = mutableListOf<ByteArray>()
            var totalPcmBytes = 0
            var isEndOfStream = false
            
            // Loop de decodificación
            while (!isEndOfStream) {
                // Feed input
                val inputBufferIndex = decoder.dequeueInputBuffer(TIMEOUT_US)
                if (inputBufferIndex >= 0) {
                    val inputBuffer = decoder.getInputBuffer(inputBufferIndex) ?: continue
                    val sampleSize = extractor.readSampleData(inputBuffer, 0)
                    
                    if (sampleSize < 0) {
                        decoder.queueInputBuffer(
                            inputBufferIndex, 0, 0, 0,
                            MediaCodec.BUFFER_FLAG_END_OF_STREAM
                        )
                        isEndOfStream = true
                    } else {
                        decoder.queueInputBuffer(
                            inputBufferIndex, 0, sampleSize,
                            extractor.sampleTime, 0
                        )
                        extractor.advance()
                    }
                }
                
                // Get output
                var outputBufferIndex = decoder.dequeueOutputBuffer(bufferInfo, TIMEOUT_US)
                while (outputBufferIndex >= 0) {
                    if (bufferInfo.flags and MediaCodec.BUFFER_FLAG_END_OF_STREAM != 0) {
                        isEndOfStream = true
                    }
                    
                    if (bufferInfo.size > 0) {
                        val outputBuffer = decoder.getOutputBuffer(outputBufferIndex)
                        if (outputBuffer != null) {
                            val pcmData = ByteArray(bufferInfo.size)
                            outputBuffer.get(pcmData)
                            outputBuffer.clear()
                            
                            // Resample y convertir a mono si es necesario
                            val processedData = processAudio(
                                pcmData, 
                                inputSampleRate, 
                                inputChannels,
                                TARGET_SAMPLE_RATE,
                                TARGET_CHANNELS
                            )
                            
                            pcmDataList.add(processedData)
                            totalPcmBytes += processedData.size
                        }
                    }
                    
                    decoder.releaseOutputBuffer(outputBufferIndex, false)
                    outputBufferIndex = decoder.dequeueOutputBuffer(bufferInfo, TIMEOUT_US)
                }
            }
            
            // Escribir datos PCM
            for (chunk in pcmDataList) {
                outputStream.write(chunk)
            }
            
            // Escribir WAV header al inicio
            outputStream.seek(0)
            writeWavHeader(outputStream, totalPcmBytes, TARGET_SAMPLE_RATE, TARGET_CHANNELS)
            
            Log.d(TAG, "Conversion complete. Output size: ${totalPcmBytes + 44} bytes")
            return true
            
        } catch (e: Exception) {
            Log.e(TAG, "Native conversion error", e)
            return false
        } finally {
            try { decoder?.stop() } catch (_: Exception) { }
            try { decoder?.release() } catch (_: Exception) { }
            try { extractor?.release() } catch (_: Exception) { }
            try { outputStream?.close() } catch (_: Exception) { }
        }
    }

    /**
     * Busca el track de audio en el extractor
     */
    private fun findAudioTrack(extractor: MediaExtractor): Int {
        for (i in 0 until extractor.trackCount) {
            val format = extractor.getTrackFormat(i)
            val mime = format.getString(MediaFormat.KEY_MIME)
            if (mime?.startsWith("audio/") == true) {
                return i
            }
        }
        return -1
    }

    /**
     * Procesa el audio: resample y conversión a mono
     */
    private fun processAudio(
        input: ByteArray,
        inputSampleRate: Int,
        inputChannels: Int,
        targetSampleRate: Int,
        targetChannels: Int
    ): ByteArray {
        // Convertir bytes a samples de 16-bit
        val inputSamples = ShortArray(input.size / 2)
        val inputBuffer = ByteBuffer.wrap(input).order(ByteOrder.LITTLE_ENDIAN)
        for (i in inputSamples.indices) {
            inputSamples[i] = inputBuffer.short
        }
        
        // Convertir a mono si es necesario
        val monoSamples = if (inputChannels > 1) {
            convertToMono(inputSamples, inputChannels)
        } else {
            inputSamples
        }
        
        // Resample si es necesario
        val resampledSamples = if (inputSampleRate != targetSampleRate) {
            resample(monoSamples, inputSampleRate, targetSampleRate)
        } else {
            monoSamples
        }
        
        // Convertir samples a bytes
        val output = ByteArray(resampledSamples.size * 2)
        val outputBuffer = ByteBuffer.wrap(output).order(ByteOrder.LITTLE_ENDIAN)
        for (sample in resampledSamples) {
            outputBuffer.putShort(sample)
        }
        
        return output
    }

    /**
     * Convierte audio multicanal a mono (promedio de canales)
     */
    private fun convertToMono(samples: ShortArray, channels: Int): ShortArray {
        val monoLength = samples.size / channels
        val mono = ShortArray(monoLength)
        
        for (i in 0 until monoLength) {
            var sum = 0L
            for (ch in 0 until channels) {
                sum += samples[i * channels + ch]
            }
            mono[i] = (sum / channels).toInt().coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
        }
        
        return mono
    }

    /**
     * Resample lineal simple (funciona bien para speech)
     */
    private fun resample(samples: ShortArray, fromRate: Int, toRate: Int): ShortArray {
        if (fromRate == toRate) return samples
        
        val ratio = fromRate.toDouble() / toRate.toDouble()
        val newLength = (samples.size / ratio).toInt()
        val resampled = ShortArray(newLength)
        
        for (i in 0 until newLength) {
            val srcIndex = (i * ratio)
            val srcIndexInt = srcIndex.toInt()
            val frac = srcIndex - srcIndexInt
            
            if (srcIndexInt + 1 < samples.size) {
                // Interpolación lineal
                val sample1 = samples[srcIndexInt]
                val sample2 = samples[srcIndexInt + 1]
                resampled[i] = (sample1 + (sample2 - sample1) * frac).toInt()
                    .coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
            } else if (srcIndexInt < samples.size) {
                resampled[i] = samples[srcIndexInt]
            }
        }
        
        return resampled
    }

    /**
     * Escribe el header WAV estándar
     */
    private fun writeWavHeader(
        output: RandomAccessFile,
        dataSize: Int,
        sampleRate: Int,
        channels: Int
    ) {
        val byteRate = sampleRate * channels * BITS_PER_SAMPLE / 8
        val blockAlign = channels * BITS_PER_SAMPLE / 8
        
        output.writeBytes("RIFF")
        output.writeIntLE(36 + dataSize)  // Chunk size
        output.writeBytes("WAVE")
        output.writeBytes("fmt ")
        output.writeIntLE(16)              // Subchunk1Size (PCM)
        output.writeShortLE(1)             // AudioFormat (PCM = 1)
        output.writeShortLE(channels)      // NumChannels
        output.writeIntLE(sampleRate)      // SampleRate
        output.writeIntLE(byteRate)        // ByteRate
        output.writeShortLE(blockAlign)    // BlockAlign
        output.writeShortLE(BITS_PER_SAMPLE) // BitsPerSample
        output.writeBytes("data")
        output.writeIntLE(dataSize)        // Subchunk2Size
    }

    // Extension functions para escribir little-endian
    private fun RandomAccessFile.writeIntLE(value: Int) {
        write(value and 0xFF)
        write((value shr 8) and 0xFF)
        write((value shr 16) and 0xFF)
        write((value shr 24) and 0xFF)
    }

    private fun RandomAccessFile.writeShortLE(value: Int) {
        write(value and 0xFF)
        write((value shr 8) and 0xFF)
    }

    /**
     * Obtiene la duración de un archivo de audio en segundos
     */
    suspend fun getAudioDuration(filePath: String): Float = withContext(Dispatchers.IO) {
        var extractor: MediaExtractor? = null
        try {
            extractor = MediaExtractor()
            extractor.setDataSource(filePath)
            
            val audioTrackIndex = findAudioTrack(extractor)
            if (audioTrackIndex >= 0) {
                val format = extractor.getTrackFormat(audioTrackIndex)
                val durationUs = format.getLong(MediaFormat.KEY_DURATION)
                (durationUs / 1_000_000f)
            } else {
                0f
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting audio duration", e)
            0f
        } finally {
            extractor?.release()
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
                    Log.d(TAG, "Cleaned up temp file: ${file.name}")
                }
            }
            
            convertedDir.listFiles()?.forEach { file ->
                if (file.isFile && file.lastModified() < oneHourAgo) {
                    file.delete()
                    Log.d(TAG, "Cleaned up converted file: ${file.name}")
                }
            }
            
            Log.d(TAG, "Temp files cleanup completed")
        } catch (e: Exception) {
            Log.e(TAG, "Error cleaning temp files", e)
        }
    }

    /**
     * Verifica si un archivo de audio está en formato WAV 16kHz mono
     * (ya listo para transcripción, no necesita conversión)
     */
    suspend fun isAlreadyWav16kMono(filePath: String): Boolean = withContext(Dispatchers.IO) {
        var extractor: MediaExtractor? = null
        try {
            extractor = MediaExtractor()
            extractor.setDataSource(filePath)
            
            val audioTrackIndex = findAudioTrack(extractor)
            if (audioTrackIndex < 0) return@withContext false
            
            val format = extractor.getTrackFormat(audioTrackIndex)
            val mime = format.getString(MediaFormat.KEY_MIME)
            val sampleRate = format.getInteger(MediaFormat.KEY_SAMPLE_RATE)
            val channels = format.getInteger(MediaFormat.KEY_CHANNEL_COUNT)
            
            // WAV PCM tiene mime "audio/raw"
            mime == "audio/raw" && sampleRate == TARGET_SAMPLE_RATE && channels == TARGET_CHANNELS
        } catch (e: Exception) {
            false
        } finally {
            extractor?.release()
        }
    }
}
