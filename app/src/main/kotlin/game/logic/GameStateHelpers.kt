package com.agustin.tarati.game.logic

import com.agustin.tarati.game.core.Cob
import com.agustin.tarati.game.core.Color
import com.agustin.tarati.game.core.GameState

// Función de extensión para modificar piezas
fun GameState.modifyCob(position: String, color: Color? = null, isUpgraded: Boolean? = null): GameState {
    val newCobs = cobs.toMutableMap()

    if (color == null && isUpgraded == null) {
        // Si ambos son null, eliminar la pieza
        newCobs.remove(position)
    } else {
        val currentCob = newCobs[position]
        val newColor = color ?: currentCob?.color ?: Color.WHITE
        val newUpgraded = isUpgraded ?: currentCob?.isUpgraded ?: false

        newCobs[position] = Cob(newColor, newUpgraded)
    }

    return this.copy(cobs = newCobs)
}

// Función para mover piezas
fun GameState.moveCob(from: String, to: String): GameState {
    val newCobs = cobs.toMutableMap()
    val cob = newCobs[from] ?: return this

    newCobs.remove(from)
    newCobs[to] = cob

    return this.copy(cobs = newCobs)
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
        val sortedEntries = cobs.entries.sortedBy { it.key }
        sortedEntries.forEach { (pos, cob) ->
            append("$pos:${cob.color}:${cob.isUpgraded},")
        }
    }
}