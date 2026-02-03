package com.localscribe.ai.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.localscribe.ai.R
import com.localscribe.ai.model.TranscriptionState

/**
 * Pantalla de procesamiento que muestra el progreso de la transcripción
 */
@Composable
fun ProcessingScreen(state: TranscriptionState) {
    val infiniteTransition = rememberInfiniteTransition(label = "loading")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = EaseInOutCubic),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    val statusMessage = when (state) {
        is TranscriptionState.ReceivingFile -> stringResource(R.string.status_receiving)
        is TranscriptionState.ConvertingAudio -> stringResource(R.string.status_converting)
        is TranscriptionState.LoadingModel -> stringResource(R.string.status_loading_model)
        is TranscriptionState.Transcribing -> stringResource(R.string.status_transcribing)
        else -> ""
    }

    val statusIcon = when (state) {
        is TranscriptionState.ReceivingFile -> Icons.Default.CloudDownload
        is TranscriptionState.ConvertingAudio -> Icons.Default.Transform
        is TranscriptionState.LoadingModel -> Icons.Default.Memory
        is TranscriptionState.Transcribing -> Icons.Default.RecordVoiceOver
        else -> Icons.Default.HourglassTop
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Indicador de progreso animado
        Box(
            modifier = Modifier.size(160.dp),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(
                modifier = Modifier
                    .size(140.dp * scale),
                strokeWidth = 6.dp,
                color = MaterialTheme.colorScheme.primary
            )

            Surface(
                modifier = Modifier.size(100.dp),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = statusIcon,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(48.dp))

        // Mensaje de estado
        Text(
            text = statusMessage,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Indicador de pasos
        ProcessingSteps(currentState = state)

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = "Por favor, espera mientras procesamos tu audio...",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

/**
 * Indicador visual de los pasos del procesamiento
 */
@Composable
fun ProcessingSteps(currentState: TranscriptionState) {
    val steps = listOf(
        "Recibir" to (currentState !is TranscriptionState.ReceivingFile),
        "Convertir" to (currentState is TranscriptionState.LoadingModel || 
                        currentState is TranscriptionState.Transcribing),
        "Cargar IA" to (currentState is TranscriptionState.Transcribing),
        "Transcribir" to false
    )

    Row(
        modifier = Modifier.padding(horizontal = 32.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        steps.forEachIndexed { index, (name, completed) ->
            val isActive = when (index) {
                0 -> currentState is TranscriptionState.ReceivingFile
                1 -> currentState is TranscriptionState.ConvertingAudio
                2 -> currentState is TranscriptionState.LoadingModel
                3 -> currentState is TranscriptionState.Transcribing
                else -> false
            }

            StepIndicator(
                name = name,
                completed = completed,
                isActive = isActive,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

/**
 * Indicador individual de un paso del proceso
 */
@Composable
fun StepIndicator(
    name: String,
    completed: Boolean,
    isActive: Boolean,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(24.dp)
                .clip(CircleShape)
                .background(
                    when {
                        completed -> MaterialTheme.colorScheme.primary
                        isActive -> MaterialTheme.colorScheme.primaryContainer
                        else -> MaterialTheme.colorScheme.surfaceVariant
                    }
                ),
            contentAlignment = Alignment.Center
        ) {
            if (completed) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(16.dp)
                )
            } else if (isActive) {
                CircularProgressIndicator(
                    modifier = Modifier.size(16.dp),
                    strokeWidth = 2.dp,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = name,
            style = MaterialTheme.typography.labelSmall,
            color = if (completed || isActive) 
                MaterialTheme.colorScheme.primary 
            else 
                MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
