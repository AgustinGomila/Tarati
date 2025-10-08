package com.agustin.tarati.game

data class Checker(val color: Color, val isUpgraded: Boolean = false)

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
    val whiteBase = GameBoard.homeBases[Color.WHITE] ?: emptyList()
    val blackBase = GameBoard.homeBases[Color.BLACK] ?: emptyList()
    if (whiteBase.contains(to) && movedChecker.color == Color.BLACK) {
        placedChecker = movedChecker.copy(isUpgraded = true)
    } else if (blackBase.contains(to) && movedChecker.color == Color.WHITE) {
        placedChecker = movedChecker.copy(isUpgraded = true)
    }
    mutable[to] = placedChecker

    // Flip adjacent checkers (for each edge containing 'to', flip the other vertex if opponent)
    for (edge in GameBoard.edges) {
        val (a, b) = edge
        if (a == to || b == to) {
            val adjacent = if (a == to) b else a
            val adjChecker = mutable[adjacent]
            if (adjChecker != null && adjChecker.color != placedChecker.color) {
                var newAdj = adjChecker.copy(color = placedChecker.color)
                // Check for upgrades for flipped piece
                if (whiteBase.contains(adjacent) && newAdj.color == Color.BLACK) {
                    newAdj = newAdj.copy(isUpgraded = true)
                } else if (blackBase.contains(adjacent) && newAdj.color == Color.WHITE) {
                    newAdj = newAdj.copy(isUpgraded = true)
                }
                mutable[adjacent] = newAdj
            }
        }
    }

    // IMPORTANT: applyMoveToBoard DOES NOT toggle currentTurn; App does.
    return GameState(mutable.toMap(), prevState.currentTurn)
}