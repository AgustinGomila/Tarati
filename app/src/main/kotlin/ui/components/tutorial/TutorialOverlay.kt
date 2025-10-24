package com.agustin.tarati.ui.components.tutorial

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.agustin.tarati.R
import com.agustin.tarati.game.core.GameState
import com.agustin.tarati.game.logic.BoardOrientation
import com.agustin.tarati.game.tutorial.BasicMovesStep
import com.agustin.tarati.game.tutorial.BridgeStep
import com.agustin.tarati.game.tutorial.CapturesStep
import com.agustin.tarati.game.tutorial.CastlingStep
import com.agustin.tarati.game.tutorial.CenterStep
import com.agustin.tarati.game.tutorial.CircumferenceStep
import com.agustin.tarati.game.tutorial.CobsStep
import com.agustin.tarati.game.tutorial.CompletedStep
import com.agustin.tarati.game.tutorial.DomesticBasesStep
import com.agustin.tarati.game.tutorial.IntroductionStep
import com.agustin.tarati.game.tutorial.TutorialState
import com.agustin.tarati.game.tutorial.TutorialStep
import com.agustin.tarati.game.tutorial.UpgradeStep
import com.agustin.tarati.ui.components.board.animation.HighlightAnimation
import com.agustin.tarati.ui.localization.localizedString
import com.agustin.tarati.ui.screens.main.TutorialEvents
import kotlinx.coroutines.delay

@Composable
fun TutorialOverlay(
    viewModel: TutorialViewModel,
    tutorialEvents: TutorialEvents,
    updateGameState: (GameState) -> Unit,
    boardWidth: Float,
    boardHeight: Float,
    boardOrientation: BoardOrientation,
    modifier: Modifier = Modifier
) {
    val coordinateMapper = remember(boardWidth, boardHeight, boardOrientation) {
        TutorialCoordinateMapper(boardWidth, boardHeight, boardOrientation)
    }

    val state by viewModel.tutorialState.collectAsState()

    LaunchedEffect(state) {
        val state = state
        val newGameState = viewModel.getCurrentGameState()
        newGameState?.let { updateGameState(it) }

        when (state) {
            is TutorialState.ShowingStep -> {
                if (viewModel.shouldAutoAdvance()) {
                    delay(viewModel.getCurrentStepDuration())
                    viewModel.nextStep()
                }
            }

            else -> {
                // No auto-advance para otros estados
            }
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        when (state) {
            is TutorialState.ShowingStep,
            is TutorialState.WaitingForMove -> {
                val step = when (state) {
                    is TutorialState.ShowingStep -> (state as TutorialState.ShowingStep).step
                    is TutorialState.WaitingForMove -> (state as TutorialState.WaitingForMove).step
                    else -> null
                }

                if (step != null) {
                    val progress = viewModel.progress

                    // Extraer vertex objetivo de las animaciones para posicionar burbuja
                    val targetVertex = step.animations
                        .filterIsInstance<HighlightAnimation.Vertex>()
                        .firstOrNull()
                        ?.highlight
                        ?.vertexId

                    val bubbleConfig = if (targetVertex != null) {
                        coordinateMapper.getBubblePositionForVertex(targetVertex)
                    } else {
                        getDefaultBubbleConfigForStep(step)
                    }

                    // Determinar si estamos esperando interacción del usuario
                    val isWaitingForMove = state is TutorialState.WaitingForMove

                    TutorialBubble(
                        title = localizedString(step.titleResId),
                        bubbleState = TutorialBubbleState(
                            contentState = TutorialBubbleContentState(
                                description = if (isWaitingForMove) {
                                    stringResource(
                                        R.string.perform_the_indicated_move,
                                        localizedString(step.descriptionResId)
                                    )
                                } else {
                                    localizedString(step.descriptionResId)
                                },
                                canGoBack = progress.currentStepIndex > 1,
                                canGoForward = !isWaitingForMove,
                                currentStep = progress.currentStepIndex,
                                totalSteps = progress.totalSteps,
                            ),
                            config = bubbleConfig
                        ),
                        bubbleEvents = tutorialBubbleEvents(
                            viewModel = viewModel,
                            tutorialEvents = tutorialEvents,
                            updateGameState = updateGameState
                        ),
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }

            is TutorialState.Idle -> {}
            is TutorialState.Completed -> tutorialEvents.onFinishTutorial()
        }
    }
}

fun tutorialBubbleEvents(
    viewModel: TutorialViewModel,
    tutorialEvents: TutorialEvents,
    updateGameState: (GameState) -> Unit
): TutorialBubbleEvents = object : TutorialBubbleEvents {
    override fun onNext() {
        viewModel.nextStep()
        if (viewModel.isCompleted()) {
            tutorialEvents.onFinishTutorial()
        }
    }

    override fun onPrevious() = viewModel.previousStep()
    override fun onSkip() = tutorialEvents.onSkipTutorial()

    override fun onRepeat() {
        viewModel.repeatCurrentStep()
        val tutorialState = viewModel.getCurrentGameState()
        if (tutorialState != null) {
            updateGameState(tutorialState)
        }
    }
}

fun getDefaultBubbleConfigForStep(step: TutorialStep): BubbleConfig = when (step) {
    is IntroductionStep -> BubbleConfig(BubblePosition.CENTER_CENTER)
    is CompletedStep -> BubbleConfig(BubblePosition.CENTER_CENTER)
    is CenterStep -> BubbleConfig(BubblePosition.BOTTOM_CENTER)
    is BridgeStep -> BubbleConfig(BubblePosition.BOTTOM_CENTER)
    is CircumferenceStep -> BubbleConfig(BubblePosition.BOTTOM_CENTER)
    is DomesticBasesStep -> BubbleConfig(BubblePosition.CENTER_CENTER)
    is CobsStep -> BubbleConfig(BubblePosition.BOTTOM_CENTER)
    is BasicMovesStep -> BubbleConfig(BubblePosition.BOTTOM_CENTER)
    is CapturesStep -> BubbleConfig(BubblePosition.BOTTOM_CENTER)
    is UpgradeStep -> BubbleConfig(BubblePosition.BOTTOM_CENTER)
    is CastlingStep -> BubbleConfig(BubblePosition.TOP_CENTER)
    else -> BubbleConfig(BubblePosition.TOP_CENTER)
}