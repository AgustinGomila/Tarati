package com.agustin.tarati.game.core

import com.agustin.tarati.game.logic.GameStateBuilder

data class GameState(
    val cobs: Map<String, Cob>,
    val currentTurn: Color
)

fun initialGameState(currentTurn: Color = Color.WHITE): GameState {
    val map = mapOf(
        "C1" to Cob(Color.WHITE, false),
        "C2" to Cob(Color.WHITE, false),
        "D1" to Cob(Color.WHITE, false),
        "D2" to Cob(Color.WHITE, false),
        "C7" to Cob(Color.BLACK, false),
        "C8" to Cob(Color.BLACK, false),
        "D3" to Cob(Color.BLACK, false),
        "D4" to Cob(Color.BLACK, false)
    )
    return GameState(cobs = map, currentTurn = currentTurn)
}

fun createGameState(block: GameStateBuilder.() -> Unit): GameState {
    return GameStateBuilder().apply(block).build()
}

fun cleanGameState(currentTurn: Color = Color.WHITE): GameState {
    return GameState(cobs = mapOf(), currentTurn = currentTurn)
}