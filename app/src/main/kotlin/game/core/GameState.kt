package com.agustin.tarati.game.core

data class GameState(
    val checkers: Map<String, Checker>,
    val currentTurn: Color
)

fun GameState.hashBoard(): String {
    return buildString {
        // Incluir información del turno actual
        append("turn:${currentTurn},")

        // Ordenar las posiciones para consistencia
        val sortedEntries = checkers.entries.sortedBy { it.key }
        sortedEntries.forEach { (pos, checker) ->
            append("$pos:${checker.color}:${checker.isUpgraded},")
        }
    }
}

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