package com.agustin.tarati.ui.screens.main

import androidx.compose.material3.DrawerState
import com.agustin.tarati.game.ai.TaratiAI.applyMoveToBoard
import com.agustin.tarati.game.ai.TaratiAI.clearAIHistory
import com.agustin.tarati.game.core.Color
import com.agustin.tarati.game.core.GameState
import com.agustin.tarati.game.core.Move
import com.agustin.tarati.game.core.initialGameState
import com.agustin.tarati.game.core.opponent
import com.agustin.tarati.game.logic.isGameOver
import com.agustin.tarati.ui.components.board.animation.AnimationCoordinator
import com.agustin.tarati.ui.components.board.animation.AnimationEvent
import com.agustin.tarati.ui.components.board.helpers.HighlightService
import com.agustin.tarati.ui.components.tutorial.TutorialViewModel
import com.agustin.tarati.ui.screens.settings.SettingsViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

class MainScreenEvents(
    private val scope: CoroutineScope,
    private val drawerState: DrawerState,
    private val animationCoordinator: AnimationCoordinator,
    private val highlightService: HighlightService,
    private val viewModel: MainViewModel,
    private val tutorialViewModel: TutorialViewModel,
    private val settingsViewModel: SettingsViewModel,
    private val onShowNewGameDialog: () -> Unit,
    private val onShowAboutDialog: () -> Unit,
) {

    fun applyMove(from: String, to: String, gameState: GameState) {
        val move = Move(from, to)
        val newBoardState = applyMoveToBoard(gameState, from, to)
        val nextState = newBoardState.copy(currentTurn = gameState.currentTurn.opponent())

        // Actualizar historial
        val newEntry = Pair(move, nextState)
        val currentHistory = viewModel.history.value
        val currentMoveIndex = viewModel.moveIndex.value

        val truncated = if (currentMoveIndex < currentHistory.size - 1) {
            currentHistory.take(currentMoveIndex + 1)
        } else currentHistory

        val newMoveHistory = truncated + newEntry
        viewModel.updateMoveIndex(newMoveHistory.size - 1)
        viewModel.updateHistory(newMoveHistory)

        // Animar highlights
        if (settingsViewModel.settingsState.value.boardState.animateEffects) {
            highlightService.animateHighlights(highlightService.createMoveHighlights(move))
        }

        animationCoordinator.handleEvent(AnimationEvent.MoveEvent(move, gameState, nextState))
        viewModel.updateGameState(nextState)

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

    fun startNewGame(playerSide: Color) {
        clearBoard()
        tutorialViewModel.skipTutorial()

        highlightService.stopHighlights()
        animationCoordinator.handleEvent(AnimationEvent.SyncState)

        scope.launch { drawerState.close() }

        viewModel.startGame(playerSide)
    }

    fun startTutorial() {
        viewModel.endEditing()
        stopGame()

        highlightService.stopHighlights()
        animationCoordinator.handleEvent(AnimationEvent.SyncState)

        scope.launch { drawerState.close() }

        tutorialViewModel.startTutorial()
    }

    fun endTutorial() {
        tutorialViewModel.endTutorial()
        clearBoard()
    }

    fun skipTutorial() {
        tutorialViewModel.skipTutorial()
        clearBoard()
    }

    fun undoMove(history: List<Pair<Move, GameState>>, moveIndex: Int) {
        if (moveIndex >= 0) {
            viewModel.decrementMoveIndex()
            val newState = if (moveIndex < history.size) history[moveIndex].second else initialGameState()
            viewModel.updateGameState(newState)
        }
    }

    fun redoMove(history: List<Pair<Move, GameState>>, moveIndex: Int) {
        if (moveIndex < history.size - 1) {
            viewModel.incrementMoveIndex()
            val newState = history[moveIndex].second
            viewModel.updateGameState(newState)
        }
    }

    fun moveToCurrentState(history: List<Pair<Move, GameState>>) {
        if (history.isNotEmpty()) {
            viewModel.updateMoveIndex(history.size - 1)
            viewModel.updateGameState(history.last().second)
        }
    }

    fun showNewGameDialog(color: Color) {
        viewModel.updatePlayerSide(color)
        onShowNewGameDialog()
    }

    fun showAboutDialog() {
        onShowAboutDialog()
    }
}