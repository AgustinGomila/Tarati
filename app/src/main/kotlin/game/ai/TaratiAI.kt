package com.agustin.tarati.game.ai

import com.agustin.tarati.game.core.Color
import com.agustin.tarati.game.core.Color.BLACK
import com.agustin.tarati.game.core.Color.WHITE
import com.agustin.tarati.game.core.GameBoard.adjacencyMap
import com.agustin.tarati.game.core.GameBoard.edges
import com.agustin.tarati.game.core.GameBoard.getAllPossibleMoves
import com.agustin.tarati.game.core.GameBoard.homeBases
import com.agustin.tarati.game.core.GameBoard.isForwardMove
import com.agustin.tarati.game.core.GameState
import com.agustin.tarati.game.core.Move
import com.agustin.tarati.game.core.hashBoard
import com.agustin.tarati.game.core.opponent
import kotlin.math.max
import kotlin.math.min

object TaratiAI {
    const val WINNING_SCORE = 1_000_000.0
    private const val MATERIAL_SCORE = 100
    private const val UPGRADE_SCORE = 50
    private const val OPPONENT_BASE_PRESSURE_SCORE = 40
    private const val CONTROL_CENTER_SCORE = 30
    private const val HOME_BASE_CONTROL_SCORE = 25
    private const val MOBILITY_SCORE = 5

    private const val MAX_TABLE_SIZE = 10000

    private data class TranspositionEntry(val depth: Int, val result: Result)

    private val transpositionTable: LinkedHashMap<String, TranspositionEntry> =
        object : LinkedHashMap<String, TranspositionEntry>(MAX_TABLE_SIZE, 0.75f, true) {
            override fun removeEldestEntry(eldest: MutableMap.MutableEntry<String, TranspositionEntry>): Boolean {
                return size > MAX_TABLE_SIZE
            }
        }

    fun clearTranspositionTable() {
        transpositionTable.clear()
    }

    fun getNextBestMove(
        gameState: GameState,
        depth: Int = Difficulty.MEDIUM.aiDepth,
        isMaximizingPlayer: Boolean = true,
        alphaInit: Double = Double.NEGATIVE_INFINITY,
        betaInit: Double = Double.POSITIVE_INFINITY
    ): Result {
        // Limitar profundidad máxima
        val safeDepth = min(depth, Difficulty.CHAMPION.aiDepth)

        val boardHash = gameState.hashBoard()
        transpositionTable[boardHash]?.let { entry ->
            if (entry.depth >= safeDepth) return entry.result
        }

        val gameOver = isGameOver(gameState)
        if (safeDepth == 0 || gameOver) {
            val score = evaluateBoard(gameState)
            val finalScore = if (gameOver) {
                if (score < 0) WINNING_SCORE else -WINNING_SCORE
            } else score
            return Result(finalScore, null)
        }

        var bestMove: Move? = null
        var bestScore = if (isMaximizingPlayer) Double.NEGATIVE_INFINITY else Double.POSITIVE_INFINITY
        var alpha = alphaInit
        var beta = betaInit

        val possibleMoves = getAllPossibleMoves(gameState)
        sortMoves(possibleMoves, gameState, isMaximizingPlayer)

        for (move in possibleMoves) {
            val newGameState = applyMoveAI(gameState, move.from, move.to)
            val childResult = getNextBestMove(
                gameState = newGameState,
                depth = safeDepth - 1,
                isMaximizingPlayer = !isMaximizingPlayer,
                alphaInit = alpha,
                betaInit = beta
            )
            val score = childResult.score

            if (isMaximizingPlayer) {
                if (score > bestScore) {
                    bestScore = score
                    bestMove = move
                }
                alpha = max(alpha, bestScore)
            } else {
                if (score < bestScore) {
                    bestScore = score
                    bestMove = move
                }
                beta = min(beta, bestScore)
            }

            if (beta <= alpha) break
        }

        println("AI best score: $bestScore, best move: $bestMove, alpha: $alpha, beta: $beta")

        transpositionTable[boardHash] = TranspositionEntry(depth, Result(bestScore, bestMove))
        return Result(bestScore, bestMove)
    }

    fun sortMoves(moves: MutableList<Move>, gameState: GameState, isMaximizingPlayer: Boolean) {
        val moveScores = moves.associateWith { move ->
            quickEvaluate(applyMoveAI(gameState, move.from, move.to))
        }

        moves.sortWith { a, b ->
            val scoreA = moveScores[a]!!
            val scoreB = moveScores[b]!!
            if (isMaximizingPlayer) scoreB.compareTo(scoreA)
            else scoreA.compareTo(scoreB)
        }
    }

    fun quickEvaluate(gameState: GameState): Double {
        var score = 0.0
        var whitePieces = 0
        var blackPieces = 0

        // Solo evaluación básica de material
        for ((_, checker) in gameState.checkers) {
            if (checker.color == WHITE) {
                whitePieces += if (checker.isUpgraded) 2 else 1
            } else {
                blackPieces += if (checker.isUpgraded) 2 else 1
            }
        }

        score += (whitePieces - blackPieces) * MATERIAL_SCORE
        return score
    }

    fun isGameOver(gameState: GameState): Boolean {
        val whitePieces = gameState.checkers.values.count { it.color == WHITE }
        val blackPieces = gameState.checkers.values.count { it.color == BLACK }

        // Victoria por eliminación
        if (whitePieces == 0 || blackPieces == 0) return true

        // Victoria por bloqueo total
        return getAllPossibleMoves(gameState).isEmpty()
    }

    fun getWinner(gameState: GameState): Color? {
        if (!isGameOver(gameState)) return null

        val whitePieces = gameState.checkers.values.count { it.color == WHITE }
        val blackPieces = gameState.checkers.values.count { it.color == BLACK }

        // Si un jugador se quedó sin piezas
        if (whitePieces == 0) return BLACK
        if (blackPieces == 0) return WHITE

        // Si ambos tienen piezas, gana quien tenga más
        return when {
            whitePieces > blackPieces -> WHITE
            blackPieces > whitePieces -> BLACK
            else -> null // Empate
        }
    }

    fun evaluateBoard(gameState: GameState): Double {
        var score = 0.0
        var whitePieces = 0
        var blackPieces = 0
        var whiteUpgrades = 0
        var blackUpgrades = 0
        var whiteCenterControl = 0
        var blackCenterControl = 0
        var whiteMobility = 0
        var blackMobility = 0

        var whiteHomeControl = 0
        var blackHomeControl = 0
        var whiteOpponentPressure = 0
        var blackOpponentPressure = 0

        // Definir vértices centrales (posición estratégica)
        val centerVertices = listOf("A1", "B1", "B2", "B3", "B4", "B5", "B6")

        // Primera pasada: contar piezas, mejoras y control del centro
        for ((vertex, checker) in gameState.checkers) {
            if (checker.color == WHITE) {
                whitePieces++
                if (checker.isUpgraded) whiteUpgrades++
                if (vertex in centerVertices) whiteCenterControl++
            } else {
                blackPieces++
                if (checker.isUpgraded) blackUpgrades++
                if (vertex in centerVertices) blackCenterControl++
            }
        }

        // Segunda pasada: calcular movilidad
        for ((vertex, checker) in gameState.checkers) {
            val adjacentVertices = adjacencyMap[vertex] ?: emptyList()
            val possibleMoves = adjacentVertices.count { to ->
                !gameState.checkers.containsKey(to) &&
                        (checker.isUpgraded || isForwardMove(checker.color, vertex, to))
            }

            if (checker.color == WHITE) {
                whiteMobility += possibleMoves
            } else {
                blackMobility += possibleMoves
            }
        }

        // Calcular control de base propia y presión en base oponente
        for ((vertex, checker) in gameState.checkers) {
            when (checker.color) {
                WHITE -> {
                    if (vertex in homeBases[WHITE]!!) whiteHomeControl++
                    if (vertex in homeBases[BLACK]!!) whiteOpponentPressure++
                }

                BLACK -> {
                    if (vertex in homeBases[BLACK]!!) blackHomeControl++
                    if (vertex in homeBases[WHITE]!!) blackOpponentPressure++
                }
            }
        }

        val perspective = if (gameState.currentTurn == WHITE) 1 else -1

        score += (whitePieces - blackPieces) * MATERIAL_SCORE * perspective
        score += (whiteUpgrades - blackUpgrades) * UPGRADE_SCORE * perspective
        score += (whiteCenterControl - blackCenterControl) * CONTROL_CENTER_SCORE * perspective
        score += (whiteMobility - blackMobility) * MOBILITY_SCORE * perspective
        score += (whiteHomeControl - blackHomeControl) * HOME_BASE_CONTROL_SCORE * perspective
        score += (whiteOpponentPressure - blackOpponentPressure) * OPPONENT_BASE_PRESSURE_SCORE * perspective

        return score
    }

    private fun applyMoveAI(boardState: GameState, from: String, to: String): GameState {
        // Apply move using the board mutator (does NOT toggle turn)
        val newState = applyMoveToBoard(boardState, from, to)
        // toggle turn (Does this outside applyMoveToBoard; AI.ApplyMoveAI must toggle)
        val nextTurn = boardState.currentTurn.opponent()
        return newState.copy(currentTurn = nextTurn)
    }

    fun applyMoveToBoard(prevState: GameState, from: String, to: String): GameState {
        val mutable = prevState.checkers.toMutableMap()
        val movedChecker = mutable[from] ?: return prevState
        // remove from
        mutable.remove(from)
        // place moved checker (copy to allow mutability of isUpgraded)
        var placedChecker = movedChecker

        // Check for upgrades when moved into opponent home base
        val whiteBase = homeBases[WHITE] ?: emptyList()
        val blackBase = homeBases[BLACK] ?: emptyList()
        if (whiteBase.contains(to) && movedChecker.color == BLACK) {
            placedChecker = movedChecker.copy(isUpgraded = true)
        } else if (blackBase.contains(to) && movedChecker.color == WHITE) {
            placedChecker = movedChecker.copy(isUpgraded = true)
        }
        mutable[to] = placedChecker

        // Flip adjacent checkers (for each edge containing 'to', flip the other vertex if opponent)
        for (edge in edges) {
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

        return GameState(mutable.toMap(), prevState.currentTurn)
    }
}