package com.agustin.tarati.ui.preview

import com.agustin.tarati.game.core.Color
import com.agustin.tarati.game.core.Color.BLACK
import com.agustin.tarati.game.core.Color.WHITE
import com.agustin.tarati.game.core.GameState
import com.agustin.tarati.game.core.initialGameState
import com.agustin.tarati.game.logic.createGameState
import com.agustin.tarati.game.logic.modifyChecker
import com.agustin.tarati.game.logic.withTurn

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