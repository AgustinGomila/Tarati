package com.agustin.tarati.game.logic

import com.agustin.tarati.game.core.Color
import com.agustin.tarati.game.core.GameState
import com.agustin.tarati.ui.screens.main.MainViewModel.Companion.initialGameState

// Builder para crear estados de juego complejos
class GameStateBuilder(initialState: GameState = initialGameState()) {
    private var state = initialState

    fun setTurn(turn: Color): GameStateBuilder {
        state = state.copy(currentTurn = turn)
        return this
    }

    fun setChecker(position: String, color: Color, isUpgraded: Boolean = false): GameStateBuilder {
        state = state.modifyChecker(position, color, isUpgraded)
        return this
    }

    fun removeChecker(position: String): GameStateBuilder {
        state = state.modifyChecker(position)
        return this
    }

    fun moveChecker(from: String, to: String): GameStateBuilder {
        state = state.moveChecker(from, to)
        return this
    }

    fun build(): GameState = state
}