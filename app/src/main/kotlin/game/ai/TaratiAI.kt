package com.agustin.tarati.game.ai

import com.agustin.tarati.game.core.Color
import com.agustin.tarati.game.core.Color.BLACK
import com.agustin.tarati.game.core.Color.WHITE
import com.agustin.tarati.game.core.GameBoard.adjacencyMap
import com.agustin.tarati.game.core.GameBoard.centerVertices
import com.agustin.tarati.game.core.GameBoard.edges
import com.agustin.tarati.game.core.GameBoard.getAllPossibleMoves
import com.agustin.tarati.game.core.GameBoard.homeBases
import com.agustin.tarati.game.core.GameBoard.isForwardMove
import com.agustin.tarati.game.core.GameBoard.isValidMove
import com.agustin.tarati.game.core.GameState
import com.agustin.tarati.game.core.Move
import com.agustin.tarati.game.core.hashBoard
import com.agustin.tarati.game.core.opponent
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

object TaratiAI {
    const val WINNING_SCORE = 1_000_000.0

    // Pesos ajustados según prioridades del juego
    private const val UPGRADED_PIECE_SCORE = 200  // Piezas mejoradas valen el doble
    private const val CAPTURE_UPGRADED_BONUS = 150  // Bonus por voltear mejoradas
    private const val MATERIAL_SCORE = 100
    private const val UPGRADE_OPPORTUNITY_SCORE = 80  // Estar cerca de base enemiga
    private const val CAPTURE_NORMAL_BONUS = 50  // Bonus por voltear normales
    private const val OPPONENT_BASE_PRESSURE_SCORE = 40
    private const val CONTROL_CENTER_SCORE = 30
    private const val HOME_BASE_CONTROL_SCORE = 25
    private const val MOBILITY_SCORE = 5

    private const val MAX_TABLE_SIZE = 10000
    private const val DEFAULT_TIME_LIMIT_MS = 5000L  // 5 segundos por defecto

    private data class TranspositionEntry(val depth: Int, val result: Result)

    private val transpositionTable: LinkedHashMap<String, TranspositionEntry> =
        object : LinkedHashMap<String, TranspositionEntry>(MAX_TABLE_SIZE, 0.75f, true) {
            override fun removeEldestEntry(eldest: MutableMap.MutableEntry<String, TranspositionEntry>): Boolean {
                return size > MAX_TABLE_SIZE
            }
        }

    fun getNextBestMove(
        gameState: GameState,
        depth: Int = Difficulty.MEDIUM.aiDepth,
        debug: Boolean = false
    ): Result {
        // Determinar automáticamente si el jugador actual es maximizador
        val isMaximizingPlayer = (gameState.currentTurn == WHITE)
        return getNextBestMoveInternal(
            gameState = gameState,
            depth = depth,
            isMaximizingPlayer = isMaximizingPlayer,
            alphaInit = Double.NEGATIVE_INFINITY,
            betaInit = Double.POSITIVE_INFINITY,
            debug = debug
        )
    }

    private fun getNextBestMoveInternal(
        gameState: GameState,
        depth: Int,
        isMaximizingPlayer: Boolean,
        alphaInit: Double,
        betaInit: Double,
        debug: Boolean = false
    ): Result {
        val safeDepth = min(depth, Difficulty.CHAMPION.aiDepth)

        val boardHash = gameState.hashBoard()
        transpositionTable[boardHash]?.let { entry ->
            if (entry.depth >= safeDepth) {
                if (debug) println("[CACHE HIT] depth=$safeDepth, score=${entry.result.score}")
                return entry.result
            }
        }

        val gameOver = isGameOver(gameState)
        if (safeDepth == 0 || gameOver) {
            val score = evaluateBoard(gameState)
            val finalScore = if (gameOver) {
                // Si el juego terminó, determinar quién ganó
                val winner = getWinner(gameState)
                if (debug) println("[TERMINAL] gameOver=${true}, winner=$winner, currentTurn=${gameState.currentTurn}")
                when (winner) {
                    WHITE -> WINNING_SCORE
                    BLACK -> -WINNING_SCORE
                    else -> score // Empate (no debería pasar según reglas)
                }
            } else {
                if (debug) println("[LEAF] depth=0, score=$score")
                score
            }
            return Result(finalScore, null)
        }

        var bestMove: Move? = null
        var bestScore = if (isMaximizingPlayer) Double.NEGATIVE_INFINITY else Double.POSITIVE_INFINITY
        var alpha = alphaInit
        var beta = betaInit

        val possibleMoves = getAllPossibleMoves(gameState)

        // Verificar que todos los movimientos sean legales
        if (debug) {
            println("[SEARCH] depth=$safeDepth, isMax=$isMaximizingPlayer, turn=${gameState.currentTurn}, moves=${possibleMoves.size}")
            possibleMoves.forEach { move ->
                val isLegal = isValidMove(gameState, move.from, move.to)
                if (!isLegal) {
                    println("  [WARNING] ILLEGAL MOVE DETECTED: ${move.from} -> ${move.to}")
                }
            }
        }

        sortMoves(possibleMoves, gameState, isMaximizingPlayer)

        for ((index, move) in possibleMoves.withIndex()) {
            val newGameState = applyMoveAI(gameState, move.from, move.to)
            val childResult = getNextBestMoveInternal(
                gameState = newGameState,
                depth = safeDepth - 1,
                isMaximizingPlayer = !isMaximizingPlayer,
                alphaInit = alpha,
                betaInit = beta,
                debug = debug
            )
            val score = childResult.score

            if (debug) println("  [MOVE $index] ${move.from}->${move.to}: score=$score")

            if (isMaximizingPlayer) {
                if (score > bestScore) {
                    bestScore = score
                    bestMove = move
                    if (debug) println("    -> NEW BEST (max)")
                }
                alpha = max(alpha, bestScore)
            } else {
                if (score < bestScore) {
                    bestScore = score
                    bestMove = move
                    if (debug) println("    -> NEW BEST (min)")
                }
                beta = min(beta, bestScore)
            }

            if (beta <= alpha) {
                if (debug) println("    -> PRUNED (beta=$beta <= alpha=$alpha)")
                break
            }
        }

        if (debug) println("[RESULT] depth=$safeDepth, bestScore=$bestScore, bestMove=$bestMove")

        transpositionTable[boardHash] = TranspositionEntry(depth, Result(bestScore, bestMove))
        return Result(bestScore, bestMove)
    }

    // Ordenamiento considerando capturas de piezas mejoradas
    fun sortMoves(moves: MutableList<Move>, gameState: GameState, isMaximizingPlayer: Boolean) {
        data class MoveEval(
            val move: Move,
            val score: Double,
            val capturesUpgraded: Int,
            val capturesNormal: Int,
            val leadsToUpgrade: Boolean,
            val isWinningMove: Boolean // Nuevo campo
        )

        val moveEvals = moves.map { move ->
            val newState = applyMoveAI(gameState, move.from, move.to)
            val (upgradedCaptures, normalCaptures) = countCapturesByType(gameState, newState, move.to)
            val quickScore = quickEvaluate(newState)

            // Bonus si el movimiento lleva a mejora
            val leadsToUpgrade = move.to in homeBases[gameState.currentTurn.opponent()]!!
            val upgradeBonus = if (leadsToUpgrade) UPGRADE_OPPORTUNITY_SCORE.toDouble() else 0.0

            // Verificar si este movimiento gana el juego
            val isWinningMove = isGameOver(newState) && getWinner(newState) == gameState.currentTurn

            val totalScore = quickScore + upgradeBonus +
                    upgradedCaptures * CAPTURE_UPGRADED_BONUS +
                    normalCaptures * CAPTURE_NORMAL_BONUS

            MoveEval(move, totalScore, upgradedCaptures, normalCaptures, leadsToUpgrade, isWinningMove)
        }

        val sorted = if (isMaximizingPlayer) {
            moveEvals.sortedWith(
                compareByDescending<MoveEval> { it.isWinningMove }
                    .thenByDescending { it.capturesUpgraded }
                    .thenByDescending { it.capturesNormal }
                    .thenByDescending { it.leadsToUpgrade }
                    .thenByDescending { it.score }
            )
        } else {
            moveEvals.sortedWith(
                compareByDescending<MoveEval> { it.isWinningMove } // Priorizar movimientos ganadores para minimizador
                    .thenBy { it.capturesUpgraded }
                    .thenBy { it.capturesNormal }
                    .thenBy { it.leadsToUpgrade }
                    .thenBy { it.score }
            )
        }

        moves.clear()
        moves.addAll(sorted.map { it.move })
    }

    // Contar capturas separando mejoradas de normales
    private fun countCapturesByType(
        oldState: GameState,
        newState: GameState,
        targetVertex: String
    ): Pair<Int, Int> {
        var upgradedCaptures = 0
        var normalCaptures = 0

        for (vertex in adjacencyMap[targetVertex] ?: emptyList()) {
            val oldChecker = oldState.checkers[vertex]
            val newChecker = newState.checkers[vertex]

            if (oldChecker != null && newChecker != null &&
                oldChecker.color != newChecker.color
            ) {
                if (oldChecker.isUpgraded) {
                    upgradedCaptures++
                } else {
                    normalCaptures++
                }
            }
        }

        return Pair(upgradedCaptures, normalCaptures)
    }

    // Evaluación rápida con amenazas a piezas mejoradas
    fun quickEvaluate(gameState: GameState): Double {
        var score = 0.0
        var whiteMaterial = 0.0
        var blackMaterial = 0.0
        var whiteThreatsToUpgraded = 0
        var blackThreatsToUpgraded = 0

        for ((vertex, checker) in gameState.checkers) {
            val materialValue = if (checker.isUpgraded) UPGRADED_PIECE_SCORE else MATERIAL_SCORE

            if (checker.color == WHITE) {
                whiteMaterial += materialValue

                // Contar amenazas a piezas mejoradas enemigas
                val adjacent = adjacencyMap[vertex] ?: emptyList()
                whiteThreatsToUpgraded += adjacent.count {
                    val enemy = gameState.checkers[it]
                    enemy?.color == BLACK && enemy.isUpgraded
                }
            } else {
                blackMaterial += materialValue

                val adjacent = adjacencyMap[vertex] ?: emptyList()
                blackThreatsToUpgraded += adjacent.count {
                    val enemy = gameState.checkers[it]
                    enemy?.color == WHITE && enemy.isUpgraded
                }
            }
        }

        score += (whiteMaterial - blackMaterial)
        score += (whiteThreatsToUpgraded - blackThreatsToUpgraded) * 15 // Bonus por amenazar mejoradas
        return score
    }

    fun isGameOver(gameState: GameState): Boolean {
        val whitePieces = gameState.checkers.values.count { it.color == WHITE }
        val blackPieces = gameState.checkers.values.count { it.color == BLACK }

        // Victoria por eliminación
        if (whitePieces == 0 || blackPieces == 0) return true

        // Victoria por bloqueo total (ya no hay movimientos)
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
            else -> null // No debería pasar según las reglas
        }
    }

    fun evaluateBoard(gameState: GameState): Double {
        var whiteMaterial = 0.0
        var blackMaterial = 0.0
        var whiteCenterControl = 0
        var blackCenterControl = 0
        var whiteMobility = 0
        var blackMobility = 0
        var whiteHomeControl = 0
        var blackHomeControl = 0
        var whiteOpponentPressure = 0
        var blackOpponentPressure = 0
        var whiteUpgradeOpportunities = 0
        var blackUpgradeOpportunities = 0

        // Primera pasada: material, mejoras y control del centro
        for ((vertex, checker) in gameState.checkers) {
            val materialValue = if (checker.isUpgraded) UPGRADED_PIECE_SCORE else MATERIAL_SCORE

            if (checker.color == WHITE) {
                whiteMaterial += materialValue
                if (vertex in centerVertices) whiteCenterControl++

                // Oportunidades de mejora (fichas normales cerca de base enemiga)
                if (!checker.isUpgraded) {
                    val adjacent = adjacencyMap[vertex] ?: emptyList()
                    if (adjacent.any { it in homeBases[BLACK]!! }) {
                        whiteUpgradeOpportunities++
                    }
                }
            } else {
                blackMaterial += materialValue
                if (vertex in centerVertices) blackCenterControl++

                if (!checker.isUpgraded) {
                    val adjacent = adjacencyMap[vertex] ?: emptyList()
                    if (adjacent.any { it in homeBases[WHITE]!! }) {
                        blackUpgradeOpportunities++
                    }
                }
            }
        }

        // Segunda pasada: movilidad
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

        // Tercera pasada: control de bases y presión
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

        var score = 0.0

        score += (whiteMaterial - blackMaterial) * MATERIAL_SCORE
        score += (whiteCenterControl - blackCenterControl) * CONTROL_CENTER_SCORE
        score += (whiteMobility - blackMobility) * MOBILITY_SCORE
        score += (whiteHomeControl - blackHomeControl) * HOME_BASE_CONTROL_SCORE
        score += (whiteOpponentPressure - blackOpponentPressure) * OPPONENT_BASE_PRESSURE_SCORE
        score += (whiteUpgradeOpportunities - blackUpgradeOpportunities) * UPGRADE_OPPORTUNITY_SCORE

        return score
    }

    private fun applyMoveAI(boardState: GameState, from: String, to: String): GameState {
        val newState = applyMoveToBoard(boardState, from, to)
        val nextTurn = boardState.currentTurn.opponent()
        return newState.copy(currentTurn = nextTurn)
    }

    fun applyMoveToBoard(prevState: GameState, from: String, to: String): GameState {
        val mutable = prevState.checkers.toMutableMap()
        val movedChecker = mutable[from] ?: return prevState
        mutable.remove(from)
        var placedChecker = movedChecker

        val whiteBase = homeBases[WHITE] ?: emptyList()
        val blackBase = homeBases[BLACK] ?: emptyList()
        if (whiteBase.contains(to) && movedChecker.color == BLACK) {
            placedChecker = movedChecker.copy(isUpgraded = true)
        } else if (blackBase.contains(to) && movedChecker.color == WHITE) {
            placedChecker = movedChecker.copy(isUpgraded = true)
        }
        mutable[to] = placedChecker

        // Voltear fichas adyacentes
        for (edge in edges) {
            val (a, b) = edge
            if (a != to && b != to) continue

            val adjacent = if (a == to) b else a
            val adjChecker = mutable[adjacent]
            if (adjChecker == null || adjChecker.color == placedChecker.color) continue

            var newAdj = adjChecker.copy(color = placedChecker.color)
            if (whiteBase.contains(adjacent) && newAdj.color == BLACK) {
                newAdj = newAdj.copy(isUpgraded = true)
            } else if (blackBase.contains(adjacent) && newAdj.color == WHITE) {
                newAdj = newAdj.copy(isUpgraded = true)
            }
            mutable[adjacent] = newAdj
        }

        return GameState(mutable.toMap(), prevState.currentTurn)
    }

    // Historial para detectar repetición triple
    private val positionHistory = mutableMapOf<String, Int>()

    /**
     * Limpia el historial de posiciones
     */
    @Suppress("unused")
    fun clearPositionHistory() {
        positionHistory.clear()
    }

    /**
     * Registrar posición en el historial
     */
    @Suppress("unused")
    fun recordPosition(gameState: GameState) {
        val hash = gameState.hashBoard()
        positionHistory[hash] = (positionHistory[hash] ?: 0) + 1
    }

    /**
     * Verificar si hay triple repetición
     */
    @Suppress("unused")
    fun hasTripleRepetition(gameState: GameState): Boolean {
        val hash = gameState.hashBoard()
        return (positionHistory[hash] ?: 0) >= 3
    }

    /**
     * Búsqueda iterativa con límite de tiempo
     */
    @Suppress("unused")
    fun getNextBestMoveWithTimeLimit(
        gameState: GameState,
        maxDepth: Int = Difficulty.MEDIUM.aiDepth,
        timeLimitMs: Long = DEFAULT_TIME_LIMIT_MS,
        debug: Boolean = false
    ): Result {
        val startTime = System.currentTimeMillis()
        var bestResult = Result(0.0, null)

        for (depth in 1..maxDepth) {
            val elapsed = System.currentTimeMillis() - startTime
            if (elapsed > timeLimitMs) {
                if (debug) println("Time limit reached at depth ${depth - 1}")
                break
            }

            val result = getNextBestMove(gameState, depth, debug)
            if (result.move != null) {
                bestResult = result
                if (debug) println("Completed depth $depth: score=${result.score}, move=${result.move}")
            }

            // Si encontramos victoria garantizada, no buscar más profundo
            if (abs(result.score) >= WINNING_SCORE * 0.9) {
                if (debug) println("Found winning/losing line at depth $depth")
                break
            }
        }

        return bestResult
    }
}