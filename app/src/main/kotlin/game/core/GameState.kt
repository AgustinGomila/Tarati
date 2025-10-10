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