package com.agustin.tarati.game.core

import com.agustin.tarati.game.logic.GameStateBuilder

data class GameState(
    val cobs: Map<String, Cob>,
    val currentTurn: CobColor
)

fun initialGameState(currentTurn: CobColor = CobColor.WHITE): GameState {
    val map = mapOf(
        "C1" to Cob(CobColor.WHITE, false),
        "C2" to Cob(CobColor.WHITE, false),
        "D1" to Cob(CobColor.WHITE, false),
        "D2" to Cob(CobColor.WHITE, false),
        "C7" to Cob(CobColor.BLACK, false),
        "C8" to Cob(CobColor.BLACK, false),
        "D3" to Cob(CobColor.BLACK, false),
        "D4" to Cob(CobColor.BLACK, false)
    )
    return GameState(cobs = map, currentTurn = currentTurn)
}

fun createGameState(block: GameStateBuilder.() -> Unit): GameState {
    return GameStateBuilder().apply(block).build()
}

fun cleanGameState(currentTurn: CobColor = CobColor.WHITE): GameState {
    return GameState(cobs = mapOf(), currentTurn = currentTurn)
}