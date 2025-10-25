package com.agustin.tarati.ui.screens.main

import androidx.lifecycle.ViewModel
import com.agustin.tarati.BuildConfig
import com.agustin.tarati.game.core.Cob
import com.agustin.tarati.game.core.CobColor
import com.agustin.tarati.game.core.GameState
import com.agustin.tarati.game.core.GameStatus
import com.agustin.tarati.game.core.cleanGameState
import com.agustin.tarati.game.core.initialGameState
import com.agustin.tarati.game.core.opponent
import com.agustin.tarati.game.logic.BoardOrientation
import com.agustin.tarati.game.logic.getPieceCounts
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class MainViewModel() : ViewModel() {

    val isDebug: Boolean = BuildConfig.DEBUG

    val gameManager: GameManager by lazy { GameManager() }

    private val _aIEnabled = MutableStateFlow(true)
    val aIEnabled: StateFlow<Boolean> = _aIEnabled.asStateFlow()
    fun updateAIEnabled(newAIEnabled: Boolean) {
        _aIEnabled.value = newAIEnabled
    }

    private val _playerSide = MutableStateFlow(CobColor.WHITE)
    val playerSide: StateFlow<CobColor> = _playerSide.asStateFlow()
    fun updatePlayerSide(newSide: CobColor) {
        _playerSide.value = newSide
    }

    // region EDIT BOARD

    // Edición de tablero
    private val _isEditing = MutableStateFlow(false)
    val isEditing: StateFlow<Boolean> = _isEditing.asStateFlow()

    private val _editColor = MutableStateFlow(CobColor.WHITE)
    val editColor: StateFlow<CobColor> = _editColor.asStateFlow()

    private val _editTurn = MutableStateFlow(CobColor.WHITE)
    val editTurn: StateFlow<CobColor> = _editTurn.asStateFlow()

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
            _editTurn.value = gameManager.gameState.value.currentTurn
            // Resetear color de edición a blanco
            _editColor.value = CobColor.WHITE
        }
    }

    fun toggleEditColor() {
        _editColor.value = _editColor.value.opponent()
    }

    fun toggleEditTurn() {
        _editTurn.value = _editTurn.value.opponent()
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
        _playerSide.value = _playerSide.value.opponent()
    }

    fun clearEditBoard() {
        gameManager.updateGameState(cleanGameState(_editTurn.value))
    }

    fun editPiece(vertexId: String) {
        val currentState = gameManager.gameState.value
        val currentCob = currentState.cobs[vertexId]
        val mutableCobs = currentState.cobs.toMutableMap()

        val pieceCounts = currentState.getPieceCounts()

        when {
            // Caso 1: No hay pieza - colocar nueva (si no excedemos 8 piezas y la distribución es válida)
            currentCob == null -> {
                if (canPlacePiece(_editColor.value, pieceCounts)) {
                    mutableCobs[vertexId] = Cob(_editColor.value, false)
                }
            }
            // Caso 2: Pieza del color seleccionado - mejorar
            currentCob.color == _editColor.value && !currentCob.isUpgraded -> {
                mutableCobs[vertexId] = currentCob.copy(isUpgraded = true)
            }
            // Caso 3: Pieza mejorada del color seleccionado - quitar
            currentCob.color == _editColor.value && currentCob.isUpgraded -> {
                mutableCobs.remove(vertexId)
            }
            // Caso 4: Pieza del color opuesto - reemplazar solo si la distribución lo permite
            else -> {
                if (canReplacePiece(currentCounts = pieceCounts)) {
                    mutableCobs[vertexId] = Cob(_editColor.value, false)
                }
            }
        }

        gameManager.updateGameState(currentState.copy(cobs = mutableCobs.toMap()))
    }

    private fun canPlacePiece(color: CobColor, currentCounts: PieceCounts): Boolean {
        val totalPieces = currentCounts.white + currentCounts.black
        if (totalPieces >= 8) return false

        val newWhiteCount = if (color == CobColor.WHITE) currentCounts.white + 1 else currentCounts.white
        val newBlackCount = if (color == CobColor.BLACK) currentCounts.black + 1 else currentCounts.black

        return isValidDistribution(newWhiteCount, newBlackCount)
    }

    private fun canReplacePiece(currentCounts: PieceCounts): Boolean {
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

    fun clearHistory() {
        gameManager.clearHistory()
    }

    fun setGame(gameState: GameState) {
        clearHistory()
        gameManager.updateGameState(gameState)
    }

    fun gameOver() {
        gameManager.updateGameStatus(GameStatus.GAME_OVER)
    }

    fun stopGame() {
        gameManager.updateGameStatus(GameStatus.NO_PLAYING)
    }

    fun startGame(playerSide: CobColor) {
        endEditing()
        updatePlayerSide(playerSide)
        setGame(initialGameState())
        gameManager.updateGameStatus(GameStatus.PLAYING)
    }

    fun startGameFromEditedState() {
        val currentState = gameManager.gameState.value
        // Validar que la distribución final sea válida
        val pieceCounts = currentState.getPieceCounts()
        if (!isValidDistribution(pieceCounts.white, pieceCounts.black)) {
            // Si no es válida, no salir del modo edición
            return
        }

        endEditing()
        setGame(currentState.copy(currentTurn = _editTurn.value))
        gameManager.updateGameStatus(GameStatus.PLAYING)
    }

    // endregion EDIT BOARD
}