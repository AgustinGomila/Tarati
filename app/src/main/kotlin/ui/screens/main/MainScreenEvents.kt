package com.agustin.tarati.ui.screens.main

import androidx.compose.material3.DrawerState
import com.agustin.tarati.game.ai.TaratiAI.applyMoveToBoard
import com.agustin.tarati.game.ai.TaratiAI.clearAIHistory
import com.agustin.tarati.game.ai.TaratiAI.recordRealMove
import com.agustin.tarati.game.core.CobColor
import com.agustin.tarati.game.core.GameState
import com.agustin.tarati.game.core.Move
import com.agustin.tarati.game.core.initialGameState
import com.agustin.tarati.game.core.opponent
import com.agustin.tarati.game.logic.isGameOver
import com.agustin.tarati.ui.components.board.animation.AnimationCoordinator
import com.agustin.tarati.ui.components.board.animation.AnimationEvent
import com.agustin.tarati.ui.components.board.helpers.HighlightService
import com.agustin.tarati.ui.components.tutorial.TutorialViewModel
import kotlinx.coroutines.CoroutineScope

class MainScreenEvents(
    private val scope: CoroutineScope,
    private val drawerState: DrawerState,
    private val animationCoordinator: AnimationCoordinator,
    private val highlightService: HighlightService,
    private val viewModel: MainViewModel,
    private val tutorialViewModel: TutorialViewModel,
    private val onShowNewGameDialog: (color: CobColor) -> Unit,
    private val onShowAboutDialog: () -> Unit,
) {

    fun applyMove(from: String, to: String, gameState: GameState) {
        val move = Move(from, to)
        val newBoardState = applyMoveToBoard(gameState, from, to)
        val nextState = newBoardState.copy(currentTurn = gameState.currentTurn.opponent())

        animationCoordinator.handleEvent(AnimationEvent.MoveEvent(move, gameState, nextState))

        recordRealMove(nextState, gameState.currentTurn)

        viewModel.gameManager.addMove(move, nextState)
        viewModel.gameManager.updateGameState(nextState)

        if (nextState.isGameOver()) {
            viewModel.gameOver()
        }
    }

    fun clearBoard() {
        stopGame()
        clearAIHistory()
        viewModel.setGame(initialGameState())
    }

    fun stopGame() {
        viewModel.stopGame()
    }

    fun startNewGame(playerSide: CobColor) {
        clearBoard()
        tutorialViewModel.closeTutorial()

        highlightService.stopHighlights()
        animationCoordinator.handleEvent(AnimationEvent.SyncState)

        drawerState.closeIfOpen(scope)

        viewModel.startGame(playerSide)
    }

    fun startTutorial() {
        viewModel.endEditing()
        stopGame()

        highlightService.stopHighlights()
        animationCoordinator.handleEvent(AnimationEvent.SyncState)

        drawerState.closeIfOpen(scope)

        tutorialViewModel.startTutorial()
    }

    fun resetTutorial() {
        tutorialViewModel.resetTutorial()
        clearBoard()
    }

    fun endTutorial() {
        tutorialViewModel.endTutorial()
        clearBoard()
    }

    fun showNewGameDialog(color: CobColor) {
        onShowNewGameDialog(color)
    }

    fun showAboutDialog() {
        onShowAboutDialog()
    }
}