package com.agustin.tarati.ui.components.tutorial

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.agustin.tarati.game.logic.BoardOrientation
import com.agustin.tarati.game.tutorial.BasicMoveStep
import com.agustin.tarati.game.tutorial.BoardLayoutStep
import com.agustin.tarati.game.tutorial.CaptureStep
import com.agustin.tarati.game.tutorial.CastlingStep
import com.agustin.tarati.game.tutorial.IntroductionStep
import com.agustin.tarati.game.tutorial.TutorialManager
import com.agustin.tarati.game.tutorial.TutorialState
import com.agustin.tarati.game.tutorial.TutorialStep
import com.agustin.tarati.game.tutorial.UpgradeStep
import com.agustin.tarati.ui.helpers.rememberPreviewTutorialState
import com.agustin.tarati.ui.theme.TaratiTheme
import kotlinx.coroutines.delay

@Composable
fun TutorialOverlay(
    tutorialManager: TutorialManager,
    boardWidth: Float,
    boardHeight: Float,
    boardOrientation: BoardOrientation,
    onSkip: () -> Unit,
    onNext: () -> Unit,
    onPrevious: () -> Unit,
    onRepeat: () -> Unit,
    modifier: Modifier = Modifier
) {
    val coordinateMapper = remember(boardWidth, boardHeight, boardOrientation) {
        TutorialCoordinateMapper(boardWidth, boardHeight, boardOrientation)
    }

    // Manejar auto-advance
    LaunchedEffect(tutorialManager.getCurrentStep()) {
        if (tutorialManager.shouldAutoAdvance()) {
            delay(tutorialManager.getCurrentStepDuration())
            onNext()
        }
    }

    Box(
        modifier = modifier.fillMaxSize()
    ) {
        when (val state = tutorialManager.tutorialState) {
            is TutorialState.ShowingStep -> {
                val step = state.step
                val progress = tutorialManager.progress

                val bubbleConfig = if (step.highlights.isNotEmpty()) {
                    // Si hay highlights, posicionar cerca del primer vértice destacado
                    val targetVertex = step.highlights.first().vertexId
                    coordinateMapper.getBubblePositionForVertex(targetVertex)
                } else {
                    // Si no hay vértice específico, usar posición por defecto basada en el paso
                    getDefaultBubbleConfigForStep(step)
                }

                TutorialBubble(
                    title = step.title,
                    bubbleState = TutorialBubbleState(
                        contentState = TutorialBubbleContentState(
                            description = step.description,
                            canGoBack = progress.currentStepIndex > 1,
                            canGoForward = true,
                            currentStep = progress.currentStepIndex,
                            totalSteps = progress.totalSteps,
                        ),
                        config = bubbleConfig
                    ),
                    bubbleEvents = object : TutorialBubbleEvents {
                        override fun onNext() = onNext()
                        override fun onPrevious() = onPrevious()
                        override fun onSkip() = onSkip()
                        override fun onRepeat() = onRepeat()
                    },
                    modifier = Modifier.fillMaxSize()
                )
            }
            // No mostrar nada para otros estados
            is TutorialState.Idle -> {}
            is TutorialState.Completed -> {}
            is TutorialState.WaitingForInteraction -> {}
        }
    }
}

private fun getDefaultBubbleConfigForStep(step: TutorialStep): BubbleConfig {
    // Asignar posiciones por defecto basadas en el tipo de paso
    return when (step) {
        is IntroductionStep -> BubbleConfig(BubblePosition.TOP_CENTER)
        is BoardLayoutStep -> BubbleConfig(BubblePosition.TOP_CENTER)
        is BasicMoveStep -> BubbleConfig(BubblePosition.BOTTOM_LEFT)
        is CaptureStep -> BubbleConfig(BubblePosition.BOTTOM_CENTER)
        is UpgradeStep -> BubbleConfig(BubblePosition.BOTTOM_RIGHT)
        is CastlingStep -> BubbleConfig(BubblePosition.TOP_RIGHT)
        else -> BubbleConfig(BubblePosition.BOTTOM_CENTER)
    }
}

// region Previews

@Preview(name = "Overlay - Primer Paso", group = "Tutorial Overlay")
@Composable
fun TutorialOverlayPreview_FirstStep() {
    TaratiTheme {
        TutorialOverlayPreviewContent(
            step = IntroductionStep,
            stepIndex = 0
        )
    }
}

@Preview(name = "Overlay - Paso Interactivo", group = "Tutorial Overlay")
@Composable
fun TutorialOverlayPreview_InteractiveStep() {
    TaratiTheme {
        TutorialOverlayPreviewContent(
            step = BasicMoveStep,
            stepIndex = 2
        )
    }
}

@Preview(name = "Overlay - Landscape", group = "Tutorial Overlay")
@Composable
fun TutorialOverlayPreview_Landscape() {
    TaratiTheme {
        TutorialOverlayPreviewContent(
            step = CaptureStep,
            stepIndex = 3,
            boardWidth = 800f,
            boardHeight = 400f,
            orientation = BoardOrientation.LANDSCAPE_WHITE
        )
    }
}

@Composable
private fun TutorialOverlayPreviewContent(
    step: TutorialStep,
    stepIndex: Int,
    boardWidth: Float = 400f,
    boardHeight: Float = 600f,
    orientation: BoardOrientation = BoardOrientation.PORTRAIT_WHITE
) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            // Simular contenido del tablero
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Tablero Simulado\nPaso: ${step.title}",
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }

            val previewState = rememberPreviewTutorialState()
            previewState.currentStepIndex = stepIndex

            val tutorialManager = previewState.getMockTutorialManager(step)

            TutorialOverlay(
                tutorialManager = tutorialManager,
                boardWidth = boardWidth,
                boardHeight = boardHeight,
                boardOrientation = orientation,
                onSkip = { previewState.isActive = false },
                onNext = { previewState.currentStepIndex++ },
                onPrevious = { if (previewState.currentStepIndex > 0) previewState.currentStepIndex-- },
                onRepeat = { /* No-op en preview */ },
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

@Preview(name = "Flujo Completo Tutorial", group = "Tutorial Completo", widthDp = 400, heightDp = 700)
@Composable
fun TutorialFlowPreview() {
    TaratiTheme {
        var currentStep by remember { mutableStateOf(0) }

        val steps = listOf(
            IntroductionStep,
            BoardLayoutStep,
            BasicMoveStep,
            CaptureStep,
            UpgradeStep,
            CastlingStep
        )

        val currentTutorialStep = steps.getOrNull(currentStep)
            ?: IntroductionStep

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Simular tablero
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(400.dp)
                    .align(Alignment.TopCenter)
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Tablero Simulado",
                        style = MaterialTheme.typography.headlineSmall,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    Text(
                        text = "Paso ${currentStep + 1}: ${currentTutorialStep.title}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            // Controles de simulación
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Simulación de Tutorial - Paso ${currentStep + 1}/6",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Text(
                    text = currentTutorialStep.description.take(50) + "...",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                Button(
                    onClick = { if (currentStep > 0) currentStep-- },
                    enabled = currentStep > 0,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Paso Anterior")
                }

                Button(
                    onClick = { if (currentStep < steps.size - 1) currentStep++ },
                    enabled = currentStep < steps.size - 1,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Paso Siguiente")
                }

                Button(
                    onClick = { currentStep = 0 },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Reiniciar Tutorial")
                }
            }

            // Overlay del tutorial
            if (currentStep < steps.size) {
                val previewState = rememberPreviewTutorialState()
                previewState.currentStepIndex = currentStep

                val tutorialManager = previewState.getMockTutorialManager(currentTutorialStep)

                TutorialOverlay(
                    tutorialManager = tutorialManager,
                    boardWidth = 400f,
                    boardHeight = 400f,
                    boardOrientation = BoardOrientation.PORTRAIT_WHITE,
                    onSkip = { currentStep = steps.size },
                    onNext = { if (currentStep < steps.size - 1) currentStep++ },
                    onPrevious = { if (currentStep > 0) currentStep-- },
                    onRepeat = {
                        // En un preview real, esto restauraría el estado del paso
                        // Por simplicidad, solo mostramos un mensaje
                        println("Repetir paso actual")
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(400.dp)
                        .align(Alignment.TopCenter)
                )
            } else {
                // Mensaje de tutorial completado
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(400.dp)
                        .align(Alignment.TopCenter)
                        .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.8f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "¡Tutorial Completado!",
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        }
    }
}

// endregion Previews