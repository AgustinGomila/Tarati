package com.agustin.tarati.game.logic

import com.agustin.tarati.game.ai.TaratiAI.realGameHistory
import com.agustin.tarati.game.core.Cob
import com.agustin.tarati.game.core.Color
import com.agustin.tarati.game.core.Color.BLACK
import com.agustin.tarati.game.core.Color.WHITE
import com.agustin.tarati.game.core.GameBoard.adjacencyMap
import com.agustin.tarati.game.core.GameBoard.isValidMove
import com.agustin.tarati.game.core.GameResult
import com.agustin.tarati.game.core.GameState
import com.agustin.tarati.game.core.MatchState
import com.agustin.tarati.game.core.Move
import com.agustin.tarati.game.core.getPosibleCastling
import com.agustin.tarati.game.core.opponent
import com.agustin.tarati.ui.screens.main.PieceCounts

// Función de extensión para modificar piezas
fun GameState.modifyCob(position: String, cob: Cob?): GameState {
    return this.modifyCob(position, cob?.color, cob?.isUpgraded)
}

fun GameState.modifyCob(position: String, color: Color? = null, isUpgraded: Boolean? = null): GameState {
    val newCobs = cobs.toMutableMap()

    if (color == null && isUpgraded == null) {
        // Si ambos son null, eliminar la pieza
        newCobs.remove(position)
    } else {
        val currentCob = newCobs[position]
        val newColor = color ?: currentCob?.color ?: WHITE
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

// ==================== Estado del Juego ====================

fun GameState.isGameOver(): Boolean {
    val whiteCobs = this.cobs.values.count { it.color == WHITE }
    val blackCobs = this.cobs.values.count { it.color == BLACK }

    return whiteCobs == 0 || blackCobs == 0 ||
            this.getAllMovesForTurn().isEmpty() ||
            this.hasTripleRepetition()
}

fun GameState.getWinner(): Color? {
    return getMatchState().winner
}

fun GameState.getMatchState(): MatchState {
    val whiteCobs = this.cobs.values.count { it.color == WHITE }
    val blackCobs = this.cobs.values.count { it.color == BLACK }

    var matchState = MatchState(this, GameResult.PLAYING, null, realGameHistory)

    if (!this.isGameOver()) return matchState

    if (this.hasTripleRepetition()) {
        matchState = matchState.copy(winner = this.currentTurn.opponent(), gameResult = GameResult.TRIPLE)
        return matchState
    }

    matchState = when {
        whiteCobs == 0 -> {
            matchState.copy(winner = BLACK, gameResult = GameResult.MIT)
        }

        blackCobs == 0 -> {
            matchState.copy(winner = WHITE, gameResult = GameResult.MIT)
        }

        this.getAllMovesForTurn().isEmpty() -> {
            matchState.copy(winner = this.currentTurn.opponent(), gameResult = GameResult.STALEMIT)
        }

        else -> {
            matchState.copy(winner = this.currentTurn, gameResult = GameResult.STALEMIT)
        }
    }

    return matchState
}

fun GameState.hasTripleRepetition(): Boolean {
    val hash = this.hashBoard()
    return (realGameHistory[hash] ?: 0) >= 3
}

fun GameState.checkIfWouldCauseRepetition(): Boolean {
    val hash = this.hashBoard()
    val currentCount = realGameHistory[hash] ?: 0
    return (currentCount + 1) >= 3
}

fun GameState.getAllMovesForTurn(): MutableList<Move> {
    val possibleMoves = mutableListOf<Move>()

    for ((from, cob) in this.cobs) {
        if (cob.color != this.currentTurn) continue

        // Obtener movimientos especiales de captura
        val castlingMove = this.getPosibleCastling(from, cob)
        if (castlingMove != null)
            possibleMoves.add(castlingMove)

        // Movimientos normales (adyacentes)
        val connectedVertices = adjacencyMap[from] ?: emptyList()
        for (to in connectedVertices) {
            if (isValidMove(this, from, to)) {
                possibleMoves.add(Move(from, to))
            }
        }
    }

    return possibleMoves
}

fun GameState.getPieceCounts(): PieceCounts {
    val whiteCount = this.cobs.values.count { it.color == WHITE }
    val blackCount = this.cobs.values.count { it.color == BLACK }
    return PieceCounts(whiteCount, blackCount)
}

fun GameState.shouldAnimateMove(newState: GameState, move: Move): Boolean {
    return this.cobs.containsKey(move.from) &&
            !this.cobs.containsKey(move.to) &&
            newState.cobs.containsKey(move.to) &&
            !newState.cobs.containsKey(move.from)
}

fun GameState.detectConversions(move: Move, newState: GameState): List<Pair<String, Cob>> {
    val adjacentVertices = adjacencyMap[move.to] ?: emptyList()
    val conversions = mutableListOf<Pair<String, Cob>>()

    adjacentVertices.forEach { vertexId ->
        val oldCob = this.cobs[vertexId]
        val newCob = newState.cobs[vertexId]

        if (oldCob != null && newCob != null && oldCob.color != newCob.color) {
            conversions.add(vertexId to newCob)
        }
    }

    return conversions
}

fun GameState.detectUpgrades(newState: GameState): List<Pair<String, Cob>> {
    val upgrades = mutableListOf<Pair<String, Cob>>()

    newState.cobs.forEach { (vertexId, newCob) ->
        val oldCob = this.cobs[vertexId]

        val wasUpgraded = when {
            oldCob != null && !oldCob.isUpgraded && newCob.isUpgraded -> true
            oldCob == null && newCob.isUpgraded -> true
            else -> false
        }

        if (wasUpgraded) {
            upgrades.add(vertexId to newCob)
        }
    }

    return upgrades
}