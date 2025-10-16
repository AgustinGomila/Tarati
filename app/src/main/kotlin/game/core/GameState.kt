package com.agustin.tarati.game.core

import com.agustin.tarati.game.logic.GameStateBuilder

data class GameState(
    val checkers: Map<String, Checker>,
    val currentTurn: Color
)

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

fun createGameState(block: GameStateBuilder.() -> Unit): GameState {
    return GameStateBuilder().apply(block).build()
}

fun cleanGameState(currentTurn: Color = Color.WHITE): GameState {
    return GameState(checkers = mapOf(), currentTurn = currentTurn)
}