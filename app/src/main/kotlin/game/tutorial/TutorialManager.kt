package com.agustin.tarati.game.tutorial

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.agustin.tarati.game.core.GameState
import com.agustin.tarati.game.core.Move
import kotlinx.coroutines.Job

class TutorialManager {
    var tutorialState: TutorialState by mutableStateOf(TutorialState.Idle)
        private set

    var progress: TutorialProgress by mutableStateOf(TutorialProgress())
        private set

    private var currentStepIndex = 0
    private var autoAdvanceJob: Job? = null

    // Usamos la interface TutorialStep en la lista
    private val steps: List<TutorialStep> = listOf(
        IntroductionStep,
        BoardLayoutStep,
        BasicMoveStep,
        CaptureStep,
        UpgradeStep,
        CastlingStep
    )

    fun startTutorial() {
        currentStepIndex = 0
        progress = TutorialProgress(
            currentStepIndex = 1, // Empezar en 1
            totalSteps = steps.size,
            completed = false
        )
        showStep(0)
    }

    fun skipTutorial() {
        autoAdvanceJob?.cancel()
        tutorialState = TutorialState.Completed(skipped = true)
        progress = progress.copy(completed = true)
    }

    fun nextStep() {
        autoAdvanceJob?.cancel()
        currentStepIndex++

        if (currentStepIndex >= steps.size) {
            tutorialState = TutorialState.Completed(skipped = false)
            progress = progress.copy(completed = true)
        } else {
            showStep(currentStepIndex)
            progress = progress.copy(currentStepIndex = currentStepIndex + 1)
        }
    }

    fun previousStep() {
        autoAdvanceJob?.cancel()
        if (currentStepIndex > 0) {
            currentStepIndex--
            showStep(currentStepIndex)
            progress = progress.copy(currentStepIndex = currentStepIndex + 1)
        }
    }

    fun repeatCurrentStep() {
        showStep(currentStepIndex)
    }

    fun checkMove(move: Move): Boolean {
        val currentStep = steps.getOrNull(currentStepIndex) ?: return false
        return currentStep.requiredMove?.let { required ->
            move.from == required.from && move.to == required.to
        } ?: false
    }

    private fun showStep(index: Int) {
        val step = steps[index]
        tutorialState = TutorialState.ShowingStep(step)
    }

    fun getCurrentGameState(): GameState? {
        return (tutorialState as? TutorialState.ShowingStep)?.step?.gameState
    }

    fun getCurrentStep(): TutorialStep? {
        return steps.getOrNull(currentStepIndex)
    }

    fun isTutorialActive(): Boolean {
        return tutorialState != TutorialState.Idle &&
                tutorialState !is TutorialState.Completed
    }

    fun shouldAutoAdvance(): Boolean {
        val currentStep = steps.getOrNull(currentStepIndex)
        return currentStep?.autoAdvance == true && currentStep.durationMs > 0
    }

    fun getCurrentStepDuration(): Long {
        return steps.getOrNull(currentStepIndex)?.durationMs ?: 0L
    }

    fun reset() {
        autoAdvanceJob?.cancel()
        tutorialState = TutorialState.Idle
        progress = TutorialProgress()
        currentStepIndex = 0
    }
}