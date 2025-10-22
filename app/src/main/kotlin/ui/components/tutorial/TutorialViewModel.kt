package com.agustin.tarati.ui.components.tutorial

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.agustin.tarati.game.core.GameState
import com.agustin.tarati.game.core.Move
import com.agustin.tarati.game.tutorial.TutorialManager
import com.agustin.tarati.game.tutorial.TutorialState
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class TutorialViewModel(val tutorialManager: TutorialManager) : ViewModel() {

    val tutorialState: StateFlow<TutorialState> = tutorialManager.tutorialState
    val tutorialGameState get() = tutorialManager.getCurrentGameState()
    val progress get() = tutorialManager.progress

    fun onMoveAttempted(
        from: String,
        to: String,
        onMoveAccepted: (gameState: GameState) -> Unit,
        onMoveRejected: (move: List<Move>) -> Unit
    ) {
        if (tutorialManager.isWaitingForUserInteraction()) {
            val move = Move(from, to)
            val moveAccepted = tutorialManager.onUserMove(move)

            if (moveAccepted) {
                val currentGameState = tutorialManager.getCurrentGameState()
                if (currentGameState != null) {
                    onMoveAccepted(currentGameState)
                }
            } else {
                onMoveRejected(tutorialManager.getExpectedMoves())
            }
        }
    }

    // Delegar todos los métodos al tutorialManager
    fun nextStep() = tutorialManager.nextStep()
    fun previousStep() = tutorialManager.previousStep()
    fun skipTutorial() = tutorialManager.skipTutorial()
    fun repeatCurrentStep() = tutorialManager.repeatCurrentStep()
    fun getCurrentGameState(): GameState? = tutorialManager.getCurrentGameState()
    fun shouldAutoAdvance(): Boolean = tutorialManager.shouldAutoAdvance()
    fun getCurrentStepDuration(): Long = tutorialManager.getCurrentStepDuration()
    fun requestUserInteraction(moves: List<Move>) = tutorialManager.requestUserInteraction(moves)
    fun endTutorial() = tutorialManager.reset()

    fun startTutorial() {
        viewModelScope.launch {
            tutorialManager.loadRulesTutorial()
            tutorialManager.getCurrentGameState()
        }
    }
}