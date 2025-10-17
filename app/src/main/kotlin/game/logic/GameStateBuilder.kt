package com.agustin.tarati.game.logic

import com.agustin.tarati.game.core.Color
import com.agustin.tarati.game.core.GameState
import com.agustin.tarati.game.core.cleanGameState

// Builder para crear estados de juego complejos
class GameStateBuilder(initialState: GameState = cleanGameState()) {
    private var state = initialState

    fun setTurn(turn: Color): GameStateBuilder {
        state = state.copy(currentTurn = turn)
        return this
    }

    fun setCob(position: String, color: Color, isUpgraded: Boolean = false): GameStateBuilder {
        state = state.modifyCob(position, color, isUpgraded)
        return this
    }

    fun removeCob(position: String): GameStateBuilder {
        state = state.modifyCob(position)
        return this
    }

    fun moveCob(from: String, to: String): GameStateBuilder {
        state = state.moveCob(from, to)
        return this
    }

    fun build(): GameState = state
}