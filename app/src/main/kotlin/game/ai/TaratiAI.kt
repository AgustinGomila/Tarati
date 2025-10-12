package com.agustin.tarati.game.ai

import com.agustin.tarati.game.core.Color
import com.agustin.tarati.game.core.Color.BLACK
import com.agustin.tarati.game.core.Color.WHITE
import com.agustin.tarati.game.core.GameBoard.adjacencyMap
import com.agustin.tarati.game.core.GameBoard.edges
import com.agustin.tarati.game.core.GameBoard.homeBases
import com.agustin.tarati.game.core.GameBoard.vertices
import com.agustin.tarati.game.core.GameState
import com.agustin.tarati.game.core.Move
import com.agustin.tarati.game.core.hashBoard
import com.agustin.tarati.game.core.switchColor
import com.agustin.tarati.game.logic.NormalizedBoard
import com.agustin.tarati.game.logic.PositionHelper.getPosition
import kotlin.math.max
import kotlin.math.min

object TaratiAI {
    private const val WINNING_SCORE = 1_000_000.0
    private const val MAX_TABLE_SIZE = 10000

    private data class TranspositionEntry(val depth: Int, val result: Result)

    private val transpositionTable: LinkedHashMap<String, TranspositionEntry> =
        object : LinkedHashMap<String, TranspositionEntry>(MAX_TABLE_SIZE, 0.75f, true) {
            override fun removeEldestEntry(eldest: MutableMap.MutableEntry<String, TranspositionEntry>): Boolean {
                return size > MAX_TABLE_SIZE
            }
        }

    fun getNextBestMove(
        gameState: GameState,
        depth: Int = 8,
        isMaximizingPlayer: Boolean = true,
        alphaInit: Double = Double.NEGATIVE_INFINITY,
        betaInit: Double = Double.POSITIVE_INFINITY
    ): Result {
        val boardHash = gameState.hashBoard()
        transpositionTable[boardHash]?.let { entry ->
            if (entry.depth >= depth) return entry.result
        }

        val gameOver = isGameOver(gameState)
        if (depth == 0 || gameOver) {
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
                depth = depth - 1,
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
        moves.sortWith { a, b ->
            val newStateA = applyMoveAI(gameState, a.from, a.to)
            val newStateB = applyMoveAI(gameState, b.from, b.to)
            val scoreA = if (isGameOver(newStateA)) {
                if (isMaximizingPlayer) WINNING_SCORE else -WINNING_SCORE
            } else quickEvaluate(newStateA)
            val scoreB = if (isGameOver(newStateB)) {
                if (isMaximizingPlayer) WINNING_SCORE else -WINNING_SCORE
            } else quickEvaluate(newStateB)
            if (isMaximizingPlayer) scoreB.compareTo(scoreA) else scoreA.compareTo(scoreB)
        }
    }

    fun quickEvaluate(gameState: GameState): Double {
        var score = 0.0
        for ((_, checker) in gameState.checkers) {
            score += if (checker.color == BLACK) 1.0 else -1.0
            if (checker.isUpgraded) score += if (checker.color == BLACK) 0.5 else -0.5
        }
        return score
    }

    fun isGameOver(gameState: GameState): Boolean {
        val whitePieces = gameState.checkers.values.count { it.color == WHITE }
        val blackPieces = gameState.checkers.values.count { it.color == BLACK }
        if (whitePieces == 0 || blackPieces == 0) return true

        val possibleMoves = getAllPossibleMoves(gameState)
        return possibleMoves.isEmpty()
    }

    fun evaluateBoard(gameState: GameState): Double {
        var score = 0.0
        var whitePieces = 0.0
        var blackPieces = 0.0
        var whiteUpgrades = 0
        var blackUpgrades = 0

        for ((_, checker) in gameState.checkers) {
            val pieceValue = if (checker.isUpgraded) 1.5 else 1.0
            if (checker.color == WHITE) {
                whitePieces += pieceValue
                if (checker.isUpgraded) whiteUpgrades++
            } else {
                blackPieces += pieceValue
                if (checker.isUpgraded) blackUpgrades++
            }
        }

        score += (whitePieces - blackPieces) * 97.0
        score += (whiteUpgrades - blackUpgrades) * 117.0
        return score
    }

    fun getAllPossibleMoves(gameState: GameState): MutableList<Move> {
        val possibleMoves = mutableListOf<Move>()

        for ((from, checker) in gameState.checkers) {
            if (checker.color != gameState.currentTurn) continue

            // Usar el mapa de adyacencia para obtener solo vértices conectados
            val connectedVertices = adjacencyMap[from] ?: emptyList()
            for (to in connectedVertices) {
                if (isValidMove(gameState, from, to)) {
                    possibleMoves.add(Move(from, to))
                }
            }
        }
        return possibleMoves
    }

    private fun applyMoveAI(boardState: GameState, from: String, to: String): GameState {
        // Apply move using the board mutator (does NOT toggle turn)
        val newState = applyMoveToBoard(boardState, from, to)
        // toggle turn (Does this outside applyMoveToBoard; AI.ApplyMoveAI must toggle)
        val nextTurn = boardState.currentTurn.switchColor()
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

    val normalizedPositions: Map<String, NormalizedBoard> by lazy {
        val tempMap = mutableMapOf<String, NormalizedBoard>()
        val referenceSize = 1100f to 1100f // Tamaño de referencia para normalizar

        vertices.forEach { vertexId ->
            val position = getPosition(vertexId, referenceSize, 250f)
            // Normalizar las coordenadas (0-1)
            val normalizedX = position.x / referenceSize.first
            val normalizedY = position.y / referenceSize.second
            tempMap[vertexId] = NormalizedBoard(normalizedX, normalizedY)
        }

        tempMap.toMap()
    }

    fun isForwardMove(color: Color, from: String, to: String): Boolean {
        val boardCenter = 250f to 250f
        val vWidth = 250f
        val fromPos = getPosition(from, boardCenter, vWidth)
        val toPos = getPosition(to, boardCenter, vWidth)

        return if (color == WHITE) {
            fromPos.y - toPos.y > 10
        } else {
            toPos.y - fromPos.y > 10
        }
    }

    fun isValidMove(gs: GameState, from: String, to: String): Boolean {
        // Verificar que from y to sean adyacentes
        val isAdjacent = adjacencyMap[from]?.contains(to) ?: false
        if (!isAdjacent) return false

        if (from == to) return false

        val checker = gs.checkers[from] ?: return false
        if (gs.checkers.containsKey(to)) return false

        if (checker.color != gs.currentTurn) return false

        if (!checker.isUpgraded) {
            val boardCenter = 250f to 250f
            val vWidth = 250f

            val fromPos = getPosition(from, boardCenter, vWidth)
            val toPos = getPosition(to, boardCenter, vWidth)

            return if (checker.color == WHITE) {
                fromPos.y - toPos.y > 10
            } else {
                toPos.y - fromPos.y > 10
            }
        }

        return true
    }
}