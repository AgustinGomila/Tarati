package com.agustin.tarati.game.logic

import com.agustin.tarati.game.core.Checker
import com.agustin.tarati.game.core.Color
import com.agustin.tarati.game.core.GameState

// Función de extensión para modificar piezas
fun GameState.modifyChecker(position: String, color: Color? = null, isUpgraded: Boolean? = null): GameState {
    val newCheckers = checkers.toMutableMap()

    if (color == null && isUpgraded == null) {
        // Si ambos son null, eliminar la pieza
        newCheckers.remove(position)
    } else {
        val currentChecker = newCheckers[position]
        val newColor = color ?: currentChecker?.color ?: Color.WHITE
        val newUpgraded = isUpgraded ?: currentChecker?.isUpgraded ?: false

        newCheckers[position] = Checker(newColor, newUpgraded)
    }

    return this.copy(checkers = newCheckers)
}

// Función para mover piezas
fun GameState.moveChecker(from: String, to: String): GameState {
    val newCheckers = checkers.toMutableMap()
    val checker = newCheckers[from] ?: return this

    newCheckers.remove(from)
    newCheckers[to] = checker

    return this.copy(checkers = newCheckers)
}

// Función para cambiar el turno
fun GameState.withTurn(newTurn: Color): GameState {
    return this.copy(currentTurn = newTurn)
}

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