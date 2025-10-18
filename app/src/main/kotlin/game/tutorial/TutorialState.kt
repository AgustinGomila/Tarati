package com.agustin.tarati.game.tutorial

sealed class TutorialState {
    object Idle : TutorialState()
    object WaitingForInteraction : TutorialState()
    data class ShowingStep(
        val step: TutorialStep,
        val progress: Float = 0f
    ) : TutorialState()

    data class Completed(val skipped: Boolean) : TutorialState()
}

data class TutorialProgress(
    val currentStepIndex: Int = 0,
    val totalSteps: Int = 0,
    val completed: Boolean = false
)