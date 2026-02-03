package com.localscribe.ai.service

import android.content.Context
import android.util.Log
import com.k2fsa.sherpa.onnx.OfflineModelConfig
import com.k2fsa.sherpa.onnx.OfflineRecognizer
import com.k2fsa.sherpa.onnx.OfflineRecognizerConfig
import com.k2fsa.sherpa.onnx.OfflineWhisperModelConfig
import com.k2fsa.sherpa.onnx.getFeatureConfig
import com.localscribe.ai.model.TranscriptionMode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

/**
 * Servicio de transcripción usando Sherpa ONNX
 * Ejecuta inferencia local con modelos Whisper cuantizados
 */
class TranscriptionService(private val context: Context) {

    companion object {
        private const val TAG = "TranscriptionService"
        private const val SAMPLE_RATE = 16000
    }

    private var currentRecognizer: OfflineRecognizer? = null
    private var currentMode: TranscriptionMode? = null

    /**
     * Inicializa el reconocedor con el modelo especificado
     */
    suspend fun initializeModel(mode: TranscriptionMode): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            // Si ya tenemos el modelo correcto cargado, no hacer nada
            if (currentMode == mode && currentRecognizer != null) {
                Log.d(TAG, "Model already loaded: ${mode.modelFolder}")
                return@withContext Result.success(Unit)
            }

            // Liberar modelo anterior si existe
            releaseModel()

            Log.d(TAG, "Initializing model: ${mode.modelFolder}")
            
            // Extraer archivos del modelo desde assets al almacenamiento interno
            val modelDir = extractModelAssets(mode.modelFolder)
            
            // Crear configuración del reconocedor
            val config = createRecognizerConfig(modelDir)
            
            // Crear el reconocedor desde archivos (no desde assets porque ya extrajimos)
            currentRecognizer = OfflineRecognizer(
                assetManager = null,
                config = config
            )
            currentMode = mode
            
            Log.d(TAG, "Model initialized successfully: ${mode.modelFolder}")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize model", e)
            Result.failure(e)
        }
    }

    /**
     * Transcribe un archivo de audio WAV
     */
    suspend fun transcribe(wavFilePath: String): Result<String> = withContext(Dispatchers.IO) {
        try {
            val recognizer = currentRecognizer 
                ?: return@withContext Result.failure(Exception("Model not initialized"))

            Log.d(TAG, "Starting transcription: $wavFilePath")
            val startTime = System.currentTimeMillis()

            // Leer el archivo WAV
            val samples = readWavFile(wavFilePath)
            if (samples.isEmpty()) {
                return@withContext Result.failure(Exception("Failed to read audio samples"))
            }

            Log.d(TAG, "Read ${samples.size} samples from WAV file")

            // Crear stream y procesar
            val stream = recognizer.createStream()
            stream.acceptWaveform(samples, sampleRate = SAMPLE_RATE)
            
            // Decodificar
            recognizer.decode(stream)
            
            // Obtener resultado
            val result = recognizer.getResult(stream)
            val text = result.text.trim()
            
            val duration = System.currentTimeMillis() - startTime
            Log.d(TAG, "Transcription completed in ${duration}ms: ${text.take(100)}...")

            stream.release()
            
            Result.success(text)
        } catch (e: Exception) {
            Log.e(TAG, "Transcription failed", e)
            Result.failure(e)
        }
    }

    /**
     * Lee un archivo WAV y retorna las muestras como FloatArray
     */
    private fun readWavFile(filePath: String): FloatArray {
        return try {
            val file = File(filePath)
            if (!file.exists()) {
                Log.e(TAG, "WAV file not found: $filePath")
                return floatArrayOf()
            }

            val bytes = file.readBytes()
            
            // Verificar header WAV mínimo
            if (bytes.size < 44) {
                Log.e(TAG, "File too small to be valid WAV")
                return floatArrayOf()
            }

            // Encontrar el chunk "data"
            var dataOffset = 12
            while (dataOffset < bytes.size - 8) {
                val chunkId = String(bytes.sliceArray(dataOffset until dataOffset + 4), Charsets.US_ASCII)
                val chunkSize = bytesToInt(bytes, dataOffset + 4)
                
                if (chunkId == "data") {
                    dataOffset += 8
                    break
                }
                dataOffset += 8 + chunkSize
            }

            if (dataOffset >= bytes.size) {
                Log.e(TAG, "Could not find data chunk in WAV file")
                return floatArrayOf()
            }

            // Convertir bytes a float samples (PCM 16-bit little endian)
            val numSamples = (bytes.size - dataOffset) / 2
            val samples = FloatArray(numSamples)
            
            for (i in 0 until numSamples) {
                val byteIndex = dataOffset + i * 2
                if (byteIndex + 1 < bytes.size) {
                    val low = bytes[byteIndex].toInt() and 0xFF
                    val high = bytes[byteIndex + 1].toInt()
                    val sample = (high shl 8) or low
                    // Convertir de Int16 a Float normalizado [-1, 1]
                    samples[i] = sample.toShort() / 32768f
                }
            }
            
            Log.d(TAG, "Successfully read $numSamples samples from WAV")
            samples
        } catch (e: Exception) {
            Log.e(TAG, "Error reading WAV file", e)
            floatArrayOf()
        }
    }

    private fun bytesToInt(bytes: ByteArray, offset: Int): Int {
        return (bytes[offset].toInt() and 0xFF) or
               ((bytes[offset + 1].toInt() and 0xFF) shl 8) or
               ((bytes[offset + 2].toInt() and 0xFF) shl 16) or
               ((bytes[offset + 3].toInt() and 0xFF) shl 24)
    }

    /**
     * Extrae los archivos del modelo desde assets al almacenamiento interno
     */
    private fun extractModelAssets(modelFolder: String): File {
        val modelDir = File(context.filesDir, modelFolder)
        
        // Si el directorio ya existe y tiene los archivos necesarios, asumimos que está listo
        val requiredFiles = listOf("encoder.int8.onnx", "decoder.int8.onnx", "tokens.txt")
        if (modelDir.exists() && requiredFiles.all { File(modelDir, it).exists() }) {
            Log.d(TAG, "Model directory already exists with all files: ${modelDir.absolutePath}")
            return modelDir
        }
        
        modelDir.mkdirs()
        
        // Listar y copiar archivos del modelo desde assets
        val assetManager = context.assets
        try {
            val files = assetManager.list(modelFolder) ?: emptyArray()
            
            if (files.isEmpty()) {
                throw Exception("No files found in assets/$modelFolder. Please add the model files.")
            }
            
            Log.d(TAG, "Found ${files.size} files in assets/$modelFolder: ${files.joinToString()}")
            
            for (fileName in files) {
                // Saltar archivos README
                if (fileName.lowercase().contains("readme")) {
                    continue
                }
                
                val assetPath = "$modelFolder/$fileName"
                val outputFile = File(modelDir, fileName)
                
                assetManager.open(assetPath).use { input ->
                    FileOutputStream(outputFile).use { output ->
                        input.copyTo(output, bufferSize = 8192)
                    }
                }
                Log.d(TAG, "Extracted: $fileName (${outputFile.length()} bytes)")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error extracting model assets from $modelFolder", e)
            throw e
        }
        
        return modelDir
    }

    /**
     * Crea la configuración del reconocedor para modelos Whisper
     */
    private fun createRecognizerConfig(modelDir: File): OfflineRecognizerConfig {
        val encoder = File(modelDir, "encoder.int8.onnx").absolutePath
        val decoder = File(modelDir, "decoder.int8.onnx").absolutePath
        val tokens = File(modelDir, "tokens.txt").absolutePath
        
        // Verificar que los archivos existen
        require(File(encoder).exists()) { "Encoder model not found: $encoder" }
        require(File(decoder).exists()) { "Decoder model not found: $decoder" }
        require(File(tokens).exists()) { "Tokens file not found: $tokens" }
        
        Log.d(TAG, "Creating config with encoder=$encoder, decoder=$decoder, tokens=$tokens")
        
        val whisperConfig = OfflineWhisperModelConfig(
            encoder = encoder,
            decoder = decoder,
            language = "es",  // Español por defecto
            task = "transcribe",
            tailPaddings = 1000
        )
        
        val modelConfig = OfflineModelConfig(
            whisper = whisperConfig,
            tokens = tokens,
            numThreads = Runtime.getRuntime().availableProcessors().coerceIn(2, 4),
            debug = false,
            provider = "cpu",
            modelType = "whisper"
        )
        
        return OfflineRecognizerConfig(
            featConfig = getFeatureConfig(sampleRate = SAMPLE_RATE, featureDim = 80),
            modelConfig = modelConfig,
            decodingMethod = "greedy_search"
        )
    }

    /**
     * Libera los recursos del modelo actual
     */
    fun releaseModel() {
        try {
            currentRecognizer?.release()
            currentRecognizer = null
            currentMode = null
            Log.d(TAG, "Model released")
        } catch (e: Exception) {
            Log.e(TAG, "Error releasing model", e)
        }
    }

    /**
     * Verifica si hay un modelo cargado
     */
    fun isModelLoaded(): Boolean = currentRecognizer != null

    /**
     * Obtiene el modo actual cargado
     */
    fun getCurrentMode(): TranscriptionMode? = currentMode
}
