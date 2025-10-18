package com.agustin.tarati.ui.components.tutorial

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.agustin.tarati.game.core.GameState
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
import com.agustin.tarati.ui.localization.localizedString
import kotlinx.coroutines.delay

@Composable
fun TutorialOverlay(
    tutorialManager: TutorialManager,
    updateGameState: (GameState) -> Unit,
    boardWidth: Float,
    boardHeight: Float,
    boardOrientation: BoardOrientation,
    modifier: Modifier = Modifier
) {
    val coordinateMapper = remember(boardWidth, boardHeight, boardOrientation) {
        TutorialCoordinateMapper(boardWidth, boardHeight, boardOrientation)
    }

    // Manejar auto-advance
    LaunchedEffect(tutorialManager.getCurrentStep()) {
        if (tutorialManager.shouldAutoAdvance()) {
            delay(tutorialManager.getCurrentStepDuration())
            tutorialManager.nextStep()
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
                    title = localizedString(step.titleResId),
                    bubbleState = TutorialBubbleState(
                        contentState = TutorialBubbleContentState(
                            description = localizedString(step.descriptionResId),
                            canGoBack = progress.currentStepIndex > 1,
                            canGoForward = true,
                            currentStep = progress.currentStepIndex,
                            totalSteps = progress.totalSteps,
                        ),
                        config = bubbleConfig
                    ),
                    bubbleEvents = object : TutorialBubbleEvents {
                        override fun onNext() = tutorialManager.nextStep()
                        override fun onPrevious() = tutorialManager.previousStep()
                        override fun onSkip() = tutorialManager.skipTutorial()
                        override fun onRepeat() {
                            // Repetir: restaurar el estado del paso actual
                            tutorialManager.repeatCurrentStep()
                            val tutorialState = tutorialManager.getCurrentGameState()
                            if (tutorialState != null) {
                                updateGameState(tutorialState)
                            }
                        }
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