package com.localscribe.ai

import android.app.Application
import android.util.Log

/**
 * Application class para LocalScribe AI
 * Inicializa configuraciones globales de la app
 */
class LocalScribeApplication : Application() {

    companion object {
        private const val TAG = "LocalScribeApp"
        
        @Volatile
        private var instance: LocalScribeApplication? = null
        
        fun getInstance(): LocalScribeApplication {
            return instance ?: throw IllegalStateException("Application not initialized")
        }
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
        Log.d(TAG, "LocalScribe AI Application initialized")
        
        // Pre-calentar el cache de archivos temporales
        cacheDir.mkdirs()
        
        // Configurar el directorio para archivos procesados
        getExternalFilesDir(null)?.mkdirs()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        Log.w(TAG, "Low memory warning received")
    }

    override fun onTrimMemory(level: Int) {
        super.onTrimMemory(level)
        if (level >= TRIM_MEMORY_MODERATE) {
            Log.w(TAG, "Memory trim requested at level: $level")
        }
    }
}
