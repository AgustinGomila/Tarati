package com.agustin.tarati.game.core

import com.agustin.tarati.game.core.Color.BLACK
import com.agustin.tarati.game.core.Color.WHITE

data class GameState(
    val checkers: Map<String, Checker>,
    val currentTurn: Color
) {
    fun hashBoard(): String {
        val keys = checkers.keys.sorted()
        val sb = StringBuilder()
        for (k in keys) {
            val c = checkers[k]!!
            sb.append("$k:${c.color}:${c.isUpgraded};")
        }
        sb.append("turn:${currentTurn}")
        return sb.toString()
    }
}

// --- applyMoveToBoard: No cambia currentTurn ---
fun applyMoveToBoard(prevState: GameState, from: String, to: String): GameState {
    val mutable = prevState.checkers.toMutableMap()
    val movedChecker = mutable[from] ?: return prevState
    // remove from
    mutable.remove(from)
    // place moved checker (copy to allow mutability of isUpgraded)
    var placedChecker = movedChecker

    // Check for upgrades when moved into opponent home base
    val whiteBase = GameBoard.homeBases[WHITE] ?: emptyList()
    val blackBase = GameBoard.homeBases[BLACK] ?: emptyList()
    if (whiteBase.contains(to) && movedChecker.color == BLACK) {
        placedChecker = movedChecker.copy(isUpgraded = true)
    } else if (blackBase.contains(to) && movedChecker.color == WHITE) {
        placedChecker = movedChecker.copy(isUpgraded = true)
    }
    mutable[to] = placedChecker

    // Flip adjacent checkers (for each edge containing 'to', flip the other vertex if opponent)
    for (edge in GameBoard.edges) {
        val (a, b) = edge
        if (a != to && b != to) continue

        val adjacent = if (a == to) b else a
        val adjChecker = mutable[adjacent]
        if (adjChecker == null || adjChecker.color == placedChecker.color) continue

        var newAdj = adjChecker.copy(color = placedChecker.color)
        // Check for upgrades for flipped piece
        if (whiteBase.contains(adjacent) && newAdj.color == BLACK) {
            newAdj = newAdj.copy(isUpgraded = true)
        } else if (blackBase.contains(adjacent) && newAdj.color == WHITE) {
            newAdj = newAdj.copy(isUpgraded = true)
        }
        mutable[adjacent] = newAdj
    }

    // IMPORTANT: applyMoveToBoard DOES NOT toggle currentTurn; App does.
    return GameState(mutable.toMap(), prevState.currentTurn)
}