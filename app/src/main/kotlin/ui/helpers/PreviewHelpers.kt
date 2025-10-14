package com.agustin.tarati.ui.helpers

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.agustin.tarati.game.core.Color
import com.agustin.tarati.game.core.Color.BLACK
import com.agustin.tarati.game.core.Color.WHITE
import com.agustin.tarati.game.core.GameState
import com.agustin.tarati.game.core.initialGameState
import com.agustin.tarati.game.logic.BoardOrientation
import com.agustin.tarati.game.logic.createGameState
import com.agustin.tarati.game.logic.modifyChecker
import com.agustin.tarati.game.logic.withTurn
import com.agustin.tarati.ui.components.board.BoardState

/**
 * Estados de juego predefinidos para previews
 */
fun initialGameStateWithUpgrades(): GameState {
    return initialGameState()
        .modifyChecker("C2", WHITE, true)
        .modifyChecker("C8", BLACK, true)
}

fun midGameState(): GameState {
    return createGameState {
        setTurn(BLACK)
        // Mover algunas piezas
        moveChecker("C1", "B1")
        moveChecker("C7", "B4")
        // Crear piezas mejoradas
        setChecker("C2", WHITE, true)
        setChecker("C8", BLACK, true)
        // Eliminar piezas capturadas
        removeChecker("D1")
        removeChecker("D3")
    }
}

fun endGameState(turn: Color): GameState {
    return createGameState {
        setTurn(turn)
        // Pocas piezas restantes
        setChecker("A1", WHITE, true)
        setChecker("B3", WHITE, false)
        setChecker("C10", BLACK, true)
        removeChecker("C1")
        removeChecker("C2")
        removeChecker("C7")
        removeChecker("C8")
        removeChecker("D1")
        removeChecker("D2")
        removeChecker("D3")
        removeChecker("D4")
    }
}

fun customGameState(): GameState {
    return initialGameState().modifyChecker("C2", WHITE, true)
        .modifyChecker("C8", BLACK, true)
        .modifyChecker("B1", WHITE, false)
        .modifyChecker("B4", BLACK, false)
        .withTurn(BLACK)
}

// Función helper para crear estados de juego comunes en previews
@Suppress("unused")
@Composable
fun rememberPreviewGameState(
    initialState: GameState = initialGameState(),
    customSetup: (GameState.() -> Unit)? = null
): GameState {
    return remember {
        if (customSetup != null) {
            createGameState { customSetup(this.build()) }
        } else {
            initialState
        }
    }
}

// Función helper para crear BoardState común en previews
@Suppress("unused")
@Composable
fun rememberPreviewBoardState(
    gameState: GameState = initialGameState(),
    boardOrientation: BoardOrientation = BoardOrientation.PORTRAIT_WHITE,
    labelsVisible: Boolean = true,
    isEditing: Boolean = false
): BoardState {
    return remember(gameState, boardOrientation, labelsVisible, isEditing) {
        BoardState(
            gameState = gameState,
            boardOrientation = boardOrientation,
            labelsVisible = labelsVisible,
            isEditing = isEditing
        )
    }
}