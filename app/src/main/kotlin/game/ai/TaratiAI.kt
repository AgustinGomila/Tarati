package com.agustin.tarati.game.ai

import com.agustin.tarati.game.core.Color
import com.agustin.tarati.game.core.Color.BLACK
import com.agustin.tarati.game.core.Color.WHITE
import com.agustin.tarati.game.core.GameBoard
import com.agustin.tarati.game.core.GameState
import com.agustin.tarati.game.core.Move
import com.agustin.tarati.game.core.applyMoveToBoard
import com.agustin.tarati.game.logic.PositionHelper.getPosition
import kotlin.math.max
import kotlin.math.min

// --- IA: constantes y tabla de trasposición ---
object TaratiAI {
    private const val WINNING_SCORE = 1_000_000.0
    private const val UPGRADE_SCORE = 50.0
    private const val CONTROL_CENTER_SCORE = 30.0
    private const val MOBILITY_SCORE = 5.0

    private data class TranspositionEntry(val depth: Int, val result: Result)

    private val transpositionTable: MutableMap<String, TranspositionEntry> = mutableMapOf()

    fun getNextBestMove(
        currentGameState: GameState,
        depth: Int = 8,
        isMaximizingPlayer: Boolean = true,
        alphaInit: Double = Double.NEGATIVE_INFINITY,
        betaInit: Double = Double.POSITIVE_INFINITY
    ): Result {
        val boardHash = currentGameState.hashBoard()
        transpositionTable[boardHash]?.let { entry ->
            if (entry.depth >= depth) return entry.result
        }

        if (depth == 0 || isGameOver(currentGameState)) {
            val score = evaluateBoard(currentGameState)
            val finalScore = if (isGameOver(currentGameState)) {
                if (score < 0) WINNING_SCORE else -WINNING_SCORE
            } else score
            return Result(finalScore, null)
        }

        var bestMove: Move? = null
        var bestScore = if (isMaximizingPlayer) Double.NEGATIVE_INFINITY else Double.POSITIVE_INFINITY
        var alpha = alphaInit
        var beta = betaInit

        val possibleMoves = getAllPossibleMoves(currentGameState)
        sortMoves(possibleMoves, currentGameState, isMaximizingPlayer)

        for (move in possibleMoves) {
            val newGameState = applyMoveAI(currentGameState, move.from, move.to)
            val childResult = getNextBestMove(
                currentGameState = newGameState,
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

        transpositionTable[boardHash] = TranspositionEntry(depth, Result(bestScore, bestMove))
        return Result(bestScore, bestMove)
    }

    private fun sortMoves(moves: MutableList<Move>, gameState: GameState, isMaximizingPlayer: Boolean) {
        moves.sortWith { a, b ->
            val scoreA = quickEvaluate(applyMoveAI(gameState, a.from, a.to))
            val scoreB = quickEvaluate(applyMoveAI(gameState, b.from, b.to))
            if (isMaximizingPlayer) scoreB.compareTo(scoreA) else scoreA.compareTo(scoreB)
        }
    }

    private fun quickEvaluate(gameState: GameState): Double {
        var score = 0.0
        for ((_, checker) in gameState.checkers) {
            score += if (checker.color == BLACK) 1.0 else -1.0
            if (checker.isUpgraded) score += if (checker.color == BLACK) 0.5 else -0.5
        }
        return score
    }

    private fun isGameOver(gameState: GameState): Boolean {
        val possible = getAllPossibleMoves(gameState)
        if (possible.isEmpty()) return true
        val colors = gameState.checkers.values.map { it.color }.toSet()
        return colors.size <= 1
    }

    private fun evaluateBoard(gameState: GameState): Double {
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
            val connectedVertices = GameBoard.adjacencyMap[from] ?: emptyList()
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
        val nextTurn = if (boardState.currentTurn == WHITE) BLACK else WHITE
        return newState.copy(currentTurn = nextTurn)
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
        val isAdjacent = GameBoard.adjacencyMap[from]?.contains(to) ?: false
        if (!isAdjacent) {
            return false
        }

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