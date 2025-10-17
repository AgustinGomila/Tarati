package com.agustin.tarati.ui.helpers

import androidx.compose.material3.DrawerValue
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.agustin.tarati.game.core.Color
import com.agustin.tarati.game.core.Color.BLACK
import com.agustin.tarati.game.core.Color.WHITE
import com.agustin.tarati.game.core.GameState
import com.agustin.tarati.game.core.createGameState
import com.agustin.tarati.game.core.initialGameState
import com.agustin.tarati.game.logic.BoardOrientation
import com.agustin.tarati.game.logic.modifyCob
import com.agustin.tarati.game.logic.withTurn
import com.agustin.tarati.ui.components.board.BoardState

/**
 * Estados de juego predefinidos para previews
 */
fun initialGameStateWithUpgrades(): GameState {
    return initialGameState()
        .modifyCob("C2", WHITE, true)
        .modifyCob("C8", BLACK, true)
}

fun midGameState(): GameState {
    return createGameState {
        setTurn(BLACK)
        // Mover algunas piezas
        moveCob("C1", "B1")
        moveCob("C7", "B4")
        // Crear piezas mejoradas
        setCob("C2", WHITE, true)
        setCob("C8", BLACK, true)
    }
}

fun endGameState(turn: Color): GameState {
    return createGameState {
        setTurn(turn)
        // Pocas piezas restantes
        setCob("A1", WHITE, true)
        setCob("B3", WHITE, false)
        setCob("C10", BLACK, true)
    }
}

fun customGameState(): GameState {
    return initialGameState().modifyCob("C2", WHITE, true)
        .modifyCob("C8", BLACK, true)
        .modifyCob("B1", WHITE, false)
        .modifyCob("B4", BLACK, false)
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

data class PreviewConfig(
    val darkTheme: Boolean = false,
    val drawerStateValue: DrawerValue = DrawerValue.Closed,
    val playerSide: Color = WHITE,
    val landScape: Boolean = false,
    val isEditing: Boolean = false,
    val labelsVisible: Boolean = true,
    val debug: Boolean = false
)