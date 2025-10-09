package com.agustin.tarati.ui.screens.main

import androidx.lifecycle.ViewModel
import com.agustin.tarati.game.Checker
import com.agustin.tarati.game.Color
import com.agustin.tarati.game.Difficulty
import com.agustin.tarati.game.GameState
import com.agustin.tarati.game.Move
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class ScreenViewModel : ViewModel() {

    private val _gameState = MutableStateFlow(initialGameState())
    private val _history = MutableStateFlow(listOf<Pair<Move, GameState>>())
    private val _difficulty = MutableStateFlow(Difficulty.DEFAULT)
    private val _moveIndex = MutableStateFlow(-1)
    private val _aIEnabled = MutableStateFlow(false)

    val gameState: StateFlow<GameState> = _gameState.asStateFlow()
    val history: StateFlow<List<Pair<Move, GameState>>> = _history.asStateFlow()
    val difficulty: StateFlow<Difficulty> = _difficulty.asStateFlow()
    val moveIndex: StateFlow<Int> = _moveIndex.asStateFlow()
    val aIEnabled: StateFlow<Boolean> = _aIEnabled.asStateFlow()

    fun updateGameState(newState: GameState) {
        _gameState.value = newState
    }

    fun updateHistory(newHistory: List<Pair<Move, GameState>>) {
        _history.value = newHistory
    }

    fun updateDifficulty(newDifficulty: Difficulty) {
        _difficulty.value = newDifficulty
    }

    fun updateMoveIndex(newMoveIndex: Int) {
        _moveIndex.value = newMoveIndex
    }

    fun decrementMoveIndex() {
        _moveIndex.value--
    }

    fun incrementMoveIndex() {
        _moveIndex.value++
    }

    fun updateAIEnabled(newAIEnabled: Boolean) {
        _aIEnabled.value = newAIEnabled
    }

    companion object {
        fun initialGameState(): GameState {
            val map = mapOf(
                "C1" to Checker(Color.WHITE, false),
                "C2" to Checker(Color.WHITE, false),
                "D1" to Checker(Color.WHITE, false),
                "D2" to Checker(Color.WHITE, false),
                "C7" to Checker(Color.BLACK, false),
                "C8" to Checker(Color.BLACK, false),
                "D3" to Checker(Color.BLACK, false),
                "D4" to Checker(Color.BLACK, false)
            )
            return GameState(checkers = map, currentTurn = Color.WHITE)
        }
    }
}