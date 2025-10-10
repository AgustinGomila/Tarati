package com.agustin.tarati.ui.screens.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.agustin.tarati.game.ai.Difficulty
import com.agustin.tarati.game.core.Checker
import com.agustin.tarati.game.core.Color
import com.agustin.tarati.game.core.GameState
import com.agustin.tarati.game.core.Move
import com.agustin.tarati.ui.screens.settings.SettingsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.koin.core.context.GlobalContext.get

class MainViewModel() : ViewModel() {

    val sr: SettingsRepository by lazy { get().get() }

    private val _gameState = MutableStateFlow(initialGameState())
    private val _history = MutableStateFlow(listOf<Pair<Move, GameState>>())
    private val _difficulty = MutableStateFlow(Difficulty.DEFAULT)
    private val _moveIndex = MutableStateFlow(-1)
    private val _aIEnabled = MutableStateFlow(true)
    private val _playerSide = MutableStateFlow(Color.WHITE)

    val gameState: StateFlow<GameState> = _gameState.asStateFlow()
    val history: StateFlow<List<Pair<Move, GameState>>> = _history.asStateFlow()
    val difficulty: StateFlow<Difficulty> = _difficulty.asStateFlow()
    val moveIndex: StateFlow<Int> = _moveIndex.asStateFlow()
    val aIEnabled: StateFlow<Boolean> = _aIEnabled.asStateFlow()
    val playerSide: StateFlow<Color> = _playerSide.asStateFlow()

    fun updateGameState(newState: GameState) {
        _gameState.value = newState
    }

    fun updateHistory(newHistory: List<Pair<Move, GameState>>) {
        _history.value = newHistory
    }

    fun updateDifficulty(newDifficulty: Difficulty) {
        _difficulty.value = newDifficulty
        viewModelScope.launch {
            sr.setDifficulty(newDifficulty)
        }
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

    fun updatePlayerSide(newSide: Color) {
        _playerSide.value = newSide
    }

    companion object {
        fun initialGameState(currentTurn: Color = Color.WHITE): GameState {
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
            return GameState(checkers = map, currentTurn = currentTurn)
        }
    }
}