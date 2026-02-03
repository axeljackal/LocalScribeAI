package com.localscribe.ai.viewmodel

import android.app.Application
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.localscribe.ai.model.TranscriptionMode
import com.localscribe.ai.model.TranscriptionState
import com.localscribe.ai.service.AudioConverterService
import com.localscribe.ai.service.TranscriptionService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.InputStream

/**
 * ViewModel principal para gestionar el estado de la transcripción
 */
class TranscriptionViewModel(application: Application) : AndroidViewModel(application) {

    companion object {
        private const val TAG = "TranscriptionVM"
        private const val PREFS_NAME = "localscribe_prefs"
        private const val KEY_SELECTED_MODE = "selected_mode"
    }

    private val context: Context = application.applicationContext
    private val audioConverter = AudioConverterService(context)
    private val transcriptionService = TranscriptionService(context)
    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    // Estado de la UI
    private val _state = MutableStateFlow<TranscriptionState>(TranscriptionState.Idle)
    val state: StateFlow<TranscriptionState> = _state.asStateFlow()

    // Modo de transcripción seleccionado
    private val _selectedMode = MutableStateFlow(loadSavedMode())
    val selectedMode: StateFlow<TranscriptionMode> = _selectedMode.asStateFlow()

    // Último resultado de transcripción
    private val _lastResult = MutableStateFlow<String?>(null)
    val lastResult: StateFlow<String?> = _lastResult.asStateFlow()

    init {
        Log.d(TAG, "ViewModel initialized with mode: ${_selectedMode.value}")
    }

    /**
     * Carga el modo guardado o retorna el valor por defecto
     */
    private fun loadSavedMode(): TranscriptionMode {
        val savedMode = prefs.getString(KEY_SELECTED_MODE, TranscriptionMode.FAST.name)
        return try {
            TranscriptionMode.valueOf(savedMode ?: TranscriptionMode.FAST.name)
        } catch (e: Exception) {
            TranscriptionMode.FAST
        }
    }

    /**
     * Cambia el modo de transcripción
     */
    fun setTranscriptionMode(mode: TranscriptionMode) {
        _selectedMode.value = mode
        prefs.edit().putString(KEY_SELECTED_MODE, mode.name).apply()
        Log.d(TAG, "Mode changed to: ${mode.name}")
    }

    /**
     * Procesa un Intent recibido con un archivo de audio
     */
    fun processIncomingIntent(intent: Intent?) {
        if (intent == null) {
            Log.d(TAG, "Intent is null, ignoring")
            return
        }

        viewModelScope.launch {
            try {
                when (intent.action) {
                    Intent.ACTION_SEND -> {
                        val uri = intent.getParcelableExtra<Uri>(Intent.EXTRA_STREAM)
                        if (uri != null) {
                            processAudioUri(uri)
                        } else {
                            _state.value = TranscriptionState.Error("No se encontró archivo de audio")
                        }
                    }
                    Intent.ACTION_VIEW -> {
                        intent.data?.let { uri ->
                            processAudioUri(uri)
                        } ?: run {
                            _state.value = TranscriptionState.Error("No se encontró archivo de audio")
                        }
                    }
                    else -> {
                        Log.d(TAG, "Unhandled intent action: ${intent.action}")
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error processing intent", e)
                _state.value = TranscriptionState.Error("Error al procesar el archivo: ${e.message}", e)
            }
        }
    }

    /**
     * Procesa un URI de archivo de audio
     */
    private suspend fun processAudioUri(uri: Uri) {
        Log.d(TAG, "Processing audio URI: $uri")
        
        _state.value = TranscriptionState.ReceivingFile
        
        try {
            // Obtener el stream del archivo
            val inputStream = context.contentResolver.openInputStream(uri)
                ?: throw Exception("No se pudo abrir el archivo")
            
            // Obtener nombre del archivo
            val fileName = getFileName(uri) ?: "audio_${System.currentTimeMillis()}"
            
            // Procesar el audio
            processAudioStream(inputStream, fileName)
            
        } catch (e: Exception) {
            Log.e(TAG, "Error processing audio URI", e)
            _state.value = TranscriptionState.Error("Error al acceder al archivo: ${e.message}", e)
        }
    }

    /**
     * Procesa un stream de audio completo
     */
    private suspend fun processAudioStream(inputStream: InputStream, fileName: String) {
        val startTime = System.currentTimeMillis()
        
        try {
            // Paso 1: Convertir a WAV
            _state.value = TranscriptionState.ConvertingAudio(0)
            Log.d(TAG, "Converting audio to WAV...")
            
            val wavResult = audioConverter.convertToWav(inputStream, fileName)
            val wavPath = wavResult.getOrElse { error ->
                _state.value = TranscriptionState.Error("Error al convertir audio: ${error.message}", error)
                return
            }
            
            // Paso 2: Cargar modelo
            val mode = _selectedMode.value
            _state.value = TranscriptionState.LoadingModel(mode.displayName)
            Log.d(TAG, "Loading model: ${mode.modelFolder}")
            
            val modelResult = transcriptionService.initializeModel(mode)
            modelResult.getOrElse { error ->
                _state.value = TranscriptionState.Error("Error al cargar modelo: ${error.message}", error)
                return
            }
            
            // Paso 3: Transcribir
            _state.value = TranscriptionState.Transcribing(0)
            Log.d(TAG, "Transcribing audio...")
            
            val transcriptionResult = transcriptionService.transcribe(wavPath)
            val transcribedText = transcriptionResult.getOrElse { error ->
                _state.value = TranscriptionState.Error("Error en transcripción: ${error.message}", error)
                return
            }
            
            // Completado
            val totalDuration = System.currentTimeMillis() - startTime
            _lastResult.value = transcribedText
            _state.value = TranscriptionState.Completed(transcribedText, totalDuration)
            
            Log.d(TAG, "Transcription completed in ${totalDuration}ms")
            
            // Limpiar archivos temporales
            audioConverter.cleanupTempFiles()
            
        } catch (e: Exception) {
            Log.e(TAG, "Error in audio processing pipeline", e)
            _state.value = TranscriptionState.Error("Error durante el procesamiento: ${e.message}", e)
        }
    }

    /**
     * Obtiene el nombre del archivo desde un URI
     */
    private fun getFileName(uri: Uri): String? {
        return try {
            context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                val nameIndex = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                if (cursor.moveToFirst() && nameIndex >= 0) {
                    cursor.getString(nameIndex)
                } else null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting file name", e)
            null
        }
    }

    /**
     * Copia el texto transcrito al portapapeles
     */
    fun copyToClipboard(text: String): Boolean {
        return try {
            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("Transcripción", text)
            clipboard.setPrimaryClip(clip)
            Log.d(TAG, "Text copied to clipboard")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error copying to clipboard", e)
            false
        }
    }

    /**
     * Reinicia el estado a Idle
     */
    fun resetState() {
        _state.value = TranscriptionState.Idle
        _lastResult.value = null
    }

    /**
     * Reintentar la última operación (si hay error)
     */
    fun retry() {
        _state.value = TranscriptionState.Idle
    }

    override fun onCleared() {
        super.onCleared()
        transcriptionService.releaseModel()
        audioConverter.cleanupTempFiles()
        Log.d(TAG, "ViewModel cleared")
    }
}
