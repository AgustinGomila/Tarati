package com.agustin.tarati.ui.components.tutorial

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.agustin.tarati.R
import com.agustin.tarati.ui.localization.LocalizedText

enum class BubblePosition {
    TOP_LEFT, TOP_CENTER, TOP_RIGHT,
    CENTER_LEFT, CENTER_CENTER, CENTER_RIGHT,
    BOTTOM_LEFT, BOTTOM_CENTER, BOTTOM_RIGHT,
    VERTEX_SPECIFIC
}

data class BubbleConfig(
    val position: BubblePosition,
    val targetVertex: String? = null,
    val width: Dp = 320.dp,
    val height: Dp = 280.dp
)

data class TutorialBubbleContentState(
    val description: String,
    val canGoBack: Boolean,
    val canGoForward: Boolean,
    val currentStep: Int,
    val totalSteps: Int,
)

data class TutorialBubbleState(
    val contentState: TutorialBubbleContentState,
    val config: BubbleConfig,
)

interface TutorialBubbleEvents {
    fun onNext()
    fun onPrevious()
    fun onSkip()
    fun onRepeat()
}

@Composable
fun TutorialBubble(
    title: String,
    bubbleState: TutorialBubbleState,
    bubbleEvents: TutorialBubbleEvents,
    modifier: Modifier = Modifier
) {
    val config = bubbleState.config

    Box(
        modifier = modifier
            .zIndex(1000f)
            .padding(16.dp)
    ) {
        val alignment = when (config.position) {
            BubblePosition.TOP_LEFT -> Alignment.TopStart
            BubblePosition.TOP_CENTER -> Alignment.TopCenter
            BubblePosition.TOP_RIGHT -> Alignment.TopEnd
            BubblePosition.CENTER_LEFT -> Alignment.CenterStart
            BubblePosition.CENTER_CENTER -> Alignment.Center
            BubblePosition.CENTER_RIGHT -> Alignment.CenterEnd
            BubblePosition.BOTTOM_LEFT -> Alignment.BottomStart
            BubblePosition.BOTTOM_CENTER -> Alignment.BottomCenter
            BubblePosition.BOTTOM_RIGHT -> Alignment.BottomEnd
            BubblePosition.VERTEX_SPECIFIC -> Alignment.TopStart
        }

        Box(
            modifier = Modifier
                .align(alignment)
                .width(config.width)
                .height(config.height)
        ) {
            BubbleContent(
                title = title,
                bubbleState = bubbleState.contentState,
                bubbleEvents = bubbleEvents,
            )
        }
    }
}

@Composable
private fun BubbleContent(
    title: String,
    bubbleState: TutorialBubbleContentState,
    bubbleEvents: TutorialBubbleEvents,
    modifier: Modifier = Modifier
) {
    val currentStep = bubbleState.currentStep
    val totalSteps = bubbleState.totalSteps
    val description = bubbleState.description
    val canGoBack = bubbleState.canGoBack
    val canGoForward = bubbleState.canGoForward
    val lastStep = currentStep == totalSteps

    Surface(
        modifier = modifier
            .shadow(8.dp, RoundedCornerShape(12.dp))
            .clip(RoundedCornerShape(12.dp)),
        color = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )

                Row {
                    // Botón de repetir
                    IconButton(
                        onClick = bubbleEvents::onRepeat,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            Icons.Default.Replay,
                            contentDescription = stringResource(R.string.repeat_explanation),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }

                    // Botón de saltar
                    IconButton(
                        onClick = bubbleEvents::onSkip,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = stringResource(R.string.skip_tutorial),
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Barra de progreso
            LinearProgressIndicator(
                progress = { currentStep.toFloat() / totalSteps },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp),
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Indicador de paso
            Text(
                text = "$currentStep/$totalSteps",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.align(Alignment.End)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Descripción
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Start,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Botones de navegación
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                if (currentStep > 1) {
                    Button(
                        onClick = bubbleEvents::onPrevious,
                        enabled = canGoBack,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer
                        ),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                            contentDescription = stringResource(R.string.back)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(stringResource(R.string.back))
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                Button(
                    onClick = bubbleEvents::onNext,
                    enabled = canGoForward,
                    modifier = Modifier.weight(1f)
                ) {
                    if (lastStep) {
                        LocalizedText(R.string.start)
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            Icons.Default.PlayArrow,
                            contentDescription = stringResource(R.string.start)
                        )
                    } else {
                        LocalizedText(R.string.next)
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            Icons.AutoMirrored.Filled.KeyboardArrowRight,
                            contentDescription = stringResource(R.string.next)
                        )
                    }
                }
            }
        }
    }
}