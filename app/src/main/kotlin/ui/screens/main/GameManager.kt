package com.agustin.tarati.ui.screens.main

import com.agustin.tarati.game.core.GameState
import com.agustin.tarati.game.core.GameStatus
import com.agustin.tarati.game.core.Move
import com.agustin.tarati.game.core.initialGameState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class GameManager {
    private val _gameStatus = MutableStateFlow(GameStatus.NO_PLAYING)
    val gameStatus: StateFlow<GameStatus> = _gameStatus.asStateFlow()
    fun updateGameStatus(newStatus: GameStatus) {
        _gameStatus.value = newStatus
    }

    private val _gameState = MutableStateFlow(initialGameState())
    val gameState: StateFlow<GameState> = _gameState.asStateFlow()
    fun updateGameState(newState: GameState) {
        _gameState.value = newState
    }

    private val _history = MutableStateFlow(listOf<Pair<Move, GameState>>())
    val history: StateFlow<List<Pair<Move, GameState>>> = _history.asStateFlow()

    private val _moveIndex = MutableStateFlow(-1)
    val moveIndex: StateFlow<Int> = _moveIndex.asStateFlow()

    fun addMove(move: Move, nextState: GameState) {
        val newEntry = Pair(move, nextState)
        val currentHistory = _history.value
        val currentMoveIndex = _moveIndex.value

        // Truncar el historial si no estamos al final
        val newHistory = if (currentMoveIndex < currentHistory.size - 1) {
            currentHistory.take(currentMoveIndex + 1)
        } else {
            currentHistory
        }

        _history.value = newHistory + newEntry
        _moveIndex.value = newHistory.size // Índice apunta al nuevo estado

        updateGameState(nextState)
    }

    fun undoMove() {
        if (!canUndo()) return

        // Retroceder DOS movimientos (jugada adversario + propia)
        val targetIndex = _moveIndex.value - 2

        if (targetIndex >= 0) {
            _moveIndex.value = targetIndex
            val undoState = _history.value[targetIndex].second
            updateGameState(undoState)
        } else {
            // Si no hay suficientes movimientos, volver al estado inicial
            _moveIndex.value = -1
            updateGameState(initialGameState())
        }
    }

    fun redoMove() {
        if (!canRedo()) return

        // Avanzar DOS movimientos (jugada del adversario + propia)
        val targetIndex = _moveIndex.value + 2

        // Verificar que podemos avanzar
        if (targetIndex < _history.value.size) {
            _moveIndex.value = targetIndex
            val redoState = _history.value[targetIndex].second
            updateGameState(redoState)
        }
    }

    fun moveToCurrentState() {
        if (_history.value.isEmpty()) return

        _moveIndex.value = _history.value.lastIndex
        updateGameState(_history.value.last().second)
    }

    private fun canUndo(): Boolean {
        // Podemos deshacer si tenemos al menos 2 movimientos para retroceder
        // o si estamos en el estado inicial pero hay historial
        return _moveIndex.value >= 2 ||
                (_moveIndex.value > -1 && _history.value.isNotEmpty())
    }

    private fun canRedo(): Boolean {
        // Podemos rehacer si tenemos al menos 2 movimientos por delante
        return _moveIndex.value + 2 < _history.value.size
    }

    // Reiniciar el historial
    fun clearHistory(gameState: GameState = initialGameState()) {
        _history.value = emptyList()
        _moveIndex.value = -1
        updateGameState(gameState)
    }
}