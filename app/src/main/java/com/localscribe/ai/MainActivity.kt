package com.localscribe.ai

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import com.localscribe.ai.model.TranscriptionState
import com.localscribe.ai.ui.screens.*
import com.localscribe.ai.ui.theme.LocalScribeAITheme
import com.localscribe.ai.viewmodel.TranscriptionViewModel

/**
 * Activity principal de LocalScribeAI
 * 
 * Maneja la recepción de archivos de audio compartidos desde otras apps
 * y coordina la UI de transcripción
 */
class MainActivity : ComponentActivity() {

    private val viewModel: TranscriptionViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            LocalScribeAITheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainScreen(viewModel = viewModel)
                }
            }
        }

        // Procesar intent inicial si viene de compartir
        handleIntent(intent)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent?) {
        if (intent?.action == Intent.ACTION_SEND || intent?.action == Intent.ACTION_VIEW) {
            viewModel.processIncomingIntent(intent)
        }
    }
}

/**
 * Pantalla principal que maneja las transiciones entre estados
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(viewModel: TranscriptionViewModel) {
    val state by viewModel.state.collectAsState()
    val selectedMode by viewModel.selectedMode.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "LocalScribe AI",
                        fontWeight = FontWeight.Bold
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Transición animada entre estados
            AnimatedContent(
                targetState = state,
                transitionSpec = {
                    fadeIn(animationSpec = tween(300)) togetherWith
                            fadeOut(animationSpec = tween(300))
                },
                label = "StateTransition"
            ) { currentState ->
                when (currentState) {
                    is TranscriptionState.Idle -> {
                        IdleScreen(
                            selectedMode = selectedMode,
                            onModeChange = { viewModel.setTranscriptionMode(it) }
                        )
                    }
                    is TranscriptionState.ReceivingFile,
                    is TranscriptionState.ConvertingAudio,
                    is TranscriptionState.LoadingModel,
                    is TranscriptionState.Transcribing -> {
                        ProcessingScreen(state = currentState)
                    }
                    is TranscriptionState.Completed -> {
                        ResultScreen(
                            text = currentState.text,
                            durationMs = currentState.durationMs,
                            onCopy = { text ->
                                viewModel.copyToClipboard(text)
                            },
                            onNewTranscription = { viewModel.resetState() }
                        )
                    }
                    is TranscriptionState.Error -> {
                        ErrorScreen(
                            message = currentState.message,
                            onRetry = { viewModel.retry() }
                        )
                    }
                }
            }
        }
    }
}
