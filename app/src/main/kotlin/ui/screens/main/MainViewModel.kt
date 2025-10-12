package com.agustin.tarati.ui.screens.main

import androidx.lifecycle.ViewModel
import com.agustin.tarati.game.ai.Difficulty
import com.agustin.tarati.game.core.Checker
import com.agustin.tarati.game.core.Color
import com.agustin.tarati.game.core.GameState
import com.agustin.tarati.game.core.Move
import com.agustin.tarati.game.logic.BoardOrientation
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class MainViewModel() : ViewModel() {
    private val _gameState = MutableStateFlow(initialGameState())
    val gameState: StateFlow<GameState> = _gameState.asStateFlow()
    fun updateGameState(newState: GameState) {
        _gameState.value = newState
    }

    private val _history = MutableStateFlow(listOf<Pair<Move, GameState>>())
    val history: StateFlow<List<Pair<Move, GameState>>> = _history.asStateFlow()
    fun updateHistory(newHistory: List<Pair<Move, GameState>>) {
        _history.value = newHistory
    }

    private val _difficulty = MutableStateFlow(Difficulty.DEFAULT)
    val difficulty: StateFlow<Difficulty> = _difficulty.asStateFlow()
    fun updateDifficulty(newDifficulty: Difficulty) {
        _difficulty.value = newDifficulty
    }

    private val _moveIndex = MutableStateFlow(-1)
    val moveIndex: StateFlow<Int> = _moveIndex.asStateFlow()
    fun updateMoveIndex(newMoveIndex: Int) {
        _moveIndex.value = newMoveIndex
    }

    fun decrementMoveIndex() {
        _moveIndex.value--
    }

    fun incrementMoveIndex() {
        _moveIndex.value++
    }

    private val _aIEnabled = MutableStateFlow(true)
    val aIEnabled: StateFlow<Boolean> = _aIEnabled.asStateFlow()
    fun updateAIEnabled(newAIEnabled: Boolean) {
        _aIEnabled.value = newAIEnabled
    }

    private val _playerSide = MutableStateFlow(Color.WHITE)
    val playerSide: StateFlow<Color> = _playerSide.asStateFlow()
    fun updatePlayerSide(newSide: Color) {
        _playerSide.value = newSide
    }

    private val _isEditing = MutableStateFlow(false)
    val isEditing: StateFlow<Boolean> = _isEditing.asStateFlow()

    private val _editColor = MutableStateFlow(Color.WHITE)
    val editColor: StateFlow<Color> = _editColor.asStateFlow()

    private val _editTurn = MutableStateFlow(Color.WHITE)
    val editTurn: StateFlow<Color> = _editTurn.asStateFlow()

    private val _editBoardOrientation = MutableStateFlow(BoardOrientation.PORTRAIT_WHITE)
    val editBoardOrientation: StateFlow<BoardOrientation> = _editBoardOrientation.asStateFlow()

    // Métodos para edición
    fun endEditing() {
        _isEditing.value = false
    }

    fun toggleEditing() {
        _isEditing.value = !_isEditing.value
        if (_isEditing.value) {
            // Al entrar en edición, actualizar el turno de edición al turno actual del juego
            _editTurn.value = _gameState.value.currentTurn
            // Resetear color de edición a blanco
            _editColor.value = Color.WHITE
        }
    }

    fun toggleEditColor() {
        _editColor.value = if (_editColor.value == Color.WHITE) Color.BLACK else Color.WHITE
    }

    fun toggleEditTurn() {
        _editTurn.value = if (_editTurn.value == Color.WHITE) Color.BLACK else Color.WHITE
    }

    fun rotateEditBoard() {
        _editBoardOrientation.value = when (_editBoardOrientation.value) {
            BoardOrientation.PORTRAIT_WHITE -> BoardOrientation.LANDSCAPE_BLACK
            BoardOrientation.LANDSCAPE_BLACK -> BoardOrientation.PORTRAIT_BLACK
            BoardOrientation.PORTRAIT_BLACK -> BoardOrientation.LANDSCAPE_WHITE
            BoardOrientation.LANDSCAPE_WHITE -> BoardOrientation.PORTRAIT_WHITE
        }
    }

    fun togglePlayerSide() {
        _playerSide.value = if (_playerSide.value == Color.WHITE) Color.BLACK else Color.WHITE
    }

    fun clearBoard() {
        _gameState.value = cleanGameState(_editTurn.value)
    }

    fun editPiece(vertexId: String) {
        val currentState = _gameState.value
        val currentChecker = currentState.checkers[vertexId]
        val mutableCheckers = currentState.checkers.toMutableMap()

        val pieceCounts = getPieceCounts(currentState)

        when {
            // Caso 1: No hay pieza - colocar nueva (si no excedemos 8 piezas y la distribución es válida)
            currentChecker == null -> {
                if (canPlacePiece(_editColor.value, pieceCounts)) {
                    mutableCheckers[vertexId] = Checker(_editColor.value, false)
                }
            }
            // Caso 2: Pieza del color seleccionado - mejorar
            currentChecker.color == _editColor.value && !currentChecker.isUpgraded -> {
                mutableCheckers[vertexId] = currentChecker.copy(isUpgraded = true)
            }
            // Caso 3: Pieza mejorada del color seleccionado - quitar
            currentChecker.color == _editColor.value && currentChecker.isUpgraded -> {
                mutableCheckers.remove(vertexId)
            }
            // Caso 4: Pieza del color opuesto - reemplazar solo si la distribución lo permite
            else -> {
                if (canReplacePiece(
                        newColor = _editColor.value,
                        oldColor = currentChecker.color,
                        currentCounts = pieceCounts
                    )
                ) {
                    mutableCheckers[vertexId] = Checker(_editColor.value, false)
                }
            }
        }

        _gameState.value = currentState.copy(checkers = mutableCheckers.toMap())
    }

    private fun getPieceCounts(state: GameState): PieceCounts {
        val whiteCount = state.checkers.values.count { it.color == Color.WHITE }
        val blackCount = state.checkers.values.count { it.color == Color.BLACK }
        return PieceCounts(whiteCount, blackCount)
    }

    private fun canPlacePiece(color: Color, currentCounts: PieceCounts): Boolean {
        val totalPieces = currentCounts.white + currentCounts.black
        if (totalPieces >= 8) return false

        val newWhiteCount = if (color == Color.WHITE) currentCounts.white + 1 else currentCounts.white
        val newBlackCount = if (color == Color.BLACK) currentCounts.black + 1 else currentCounts.black

        return isValidDistribution(newWhiteCount, newBlackCount)
    }

    private fun canReplacePiece(newColor: Color, oldColor: Color, currentCounts: PieceCounts): Boolean {
        // Si reemplazamos una pieza del color opuesto, los conteos no cambian
        // Solo verificamos que la distribución sea válida
        return isValidDistribution(currentCounts.white, currentCounts.black)
    }

    private fun isValidDistribution(white: Int, black: Int): Boolean {
        val total = white + black
        if (total > 8) return false

        // Distribuciones permitidas: 7-1, 6-2, 5-3, 4-4
        return when {
            white == 7 && black == 1 -> true
            white == 6 && black == 2 -> true
            white == 5 && black == 3 -> true
            white == 4 && black == 4 -> true
            white == 1 && black == 7 -> true
            white == 2 && black == 6 -> true
            white == 3 && black == 5 -> true
            total < 8 -> true // Durante construcción, mientras no lleguemos a 8
            else -> false
        }
    }

    fun startGameFromEditedState() {
        val currentState = _gameState.value
        // Validar que la distribución final sea válida
        val pieceCounts = getPieceCounts(currentState)
        if (!isValidDistribution(pieceCounts.white, pieceCounts.black)) {
            // Si no es válida, no salir del modo edición
            return
        }

        _gameState.value = currentState.copy(currentTurn = _editTurn.value)
        _isEditing.value = false
        _history.value = emptyList()
        _moveIndex.value = -1
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

        fun cleanGameState(currentTurn: Color = Color.WHITE): GameState {
            return GameState(checkers = mapOf(), currentTurn = currentTurn)
        }
    }
}