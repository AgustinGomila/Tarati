package com.agustin.tarati.game.ai

import com.agustin.tarati.game.core.Cob
import com.agustin.tarati.game.core.Color
import com.agustin.tarati.game.core.Color.BLACK
import com.agustin.tarati.game.core.Color.WHITE
import com.agustin.tarati.game.core.GameBoard.BLACK_CASTLING_VERTEX
import com.agustin.tarati.game.core.GameBoard.WHITE_CASTLING_VERTEX
import com.agustin.tarati.game.core.GameBoard.adjacencyMap
import com.agustin.tarati.game.core.GameBoard.centerVertices
import com.agustin.tarati.game.core.GameBoard.edges
import com.agustin.tarati.game.core.GameBoard.homeBases
import com.agustin.tarati.game.core.GameBoard.isForwardMove
import com.agustin.tarati.game.core.GameState
import com.agustin.tarati.game.core.Move
import com.agustin.tarati.game.core.countFlipsByType
import com.agustin.tarati.game.core.isCastling
import com.agustin.tarati.game.core.opponent
import com.agustin.tarati.game.logic.checkIfWouldCauseRepetition
import com.agustin.tarati.game.logic.getAllMovesForTurn
import com.agustin.tarati.game.logic.getWinner
import com.agustin.tarati.game.logic.hasTripleRepetition
import com.agustin.tarati.game.logic.hashBoard
import com.agustin.tarati.game.logic.isGameOver
import java.util.concurrent.atomic.AtomicReference
import kotlin.math.max
import kotlin.math.min

object TaratiAI {
    private const val MAX_TABLE_SIZE = 10000

    // Configuración global (thread-safe)
    private val globalConfigRef: AtomicReference<EvaluationConfig> = AtomicReference(EvaluationConfig())
    val evalConfig: EvaluationConfig get() = globalConfigRef.get()

    // Historial de posiciones reales
    val realGameHistory = mutableMapOf<String, Int>()

    // Tabla de transposición con LRU
    private val transpositionTable: LinkedHashMap<String, TranspositionEntry> =
        object : LinkedHashMap<String, TranspositionEntry>(MAX_TABLE_SIZE, 0.75f, true) {
            override fun removeEldestEntry(eldest: MutableMap.MutableEntry<String, TranspositionEntry>): Boolean {
                return size > MAX_TABLE_SIZE
            }
        }

    private fun clearTranspositionTable() {
        transpositionTable.clear()
    }

    private data class TranspositionEntry(val depth: Int, val result: Result)

    // ==================== API Pública ====================

    fun setEvaluationConfig(config: EvaluationConfig) {
        globalConfigRef.set(config)
    }

    fun clearAIHistory() {
        clearTranspositionTable()
        clearPositionHistory()
    }

    private fun clearPositionHistory() {
        realGameHistory.clear()
    }

    fun recordRealMove(gameState: GameState, moveBy: Color): Color? {
        val hash = gameState.hashBoard()
        val count = (realGameHistory[hash] ?: 0) + 1
        realGameHistory[hash] = count
        return if (count >= 3) moveBy else null
    }

    fun getRepetitionCount(gameState: GameState): Int {
        val hash = gameState.hashBoard()
        return realGameHistory[hash] ?: 0
    }

    fun getNextBestMove(
        gameState: GameState,
        debug: Boolean = false
    ): Result {
        return getNextBestMove(gameState, evalConfig.difficulty, debug)
    }

    fun getNextBestMove(
        gameState: GameState,
        difficulty: Difficulty,
        debug: Boolean = false
    ): Result {
        return minimax(
            gameState = gameState,
            depth = min(difficulty.aiDepth, Difficulty.MAX.aiDepth),
            alpha = Double.NEGATIVE_INFINITY,
            beta = Double.POSITIVE_INFINITY,
            isMaximizing = (gameState.currentTurn == WHITE),
            debug = debug
        )
    }

    // ==================== Minimax con Alpha-Beta ====================

    private fun minimax(
        gameState: GameState,
        depth: Int,
        alpha: Double,
        beta: Double,
        isMaximizing: Boolean,
        debug: Boolean = false
    ): Result {
        // Verificar triple repetición
        if (gameState.hasTripleRepetition()) {
            val losingPlayer = gameState.currentTurn.opponent()
            val score = if (losingPlayer == WHITE) -evalConfig.winningScore else evalConfig.winningScore
            if (debug) println("[TRIPLE REPETITION] Player $losingPlayer loses")
            return Result(score, null)
        }

        // Verificar cache
        val boardHash = gameState.hashBoard()
        transpositionTable[boardHash]?.let { entry ->
            if (entry.depth >= depth) {
                if (debug) println("[CACHE HIT] depth=$depth, score=${entry.result.score}")
                return entry.result
            }
        }

        // Verificar condiciones terminales
        val terminalResult = checkTerminalState(gameState, depth, debug)
        if (terminalResult != null) return terminalResult

        // Generar y ordenar movimientos
        val moves = gameState.getAllMovesForTurn()
        if (moves.isEmpty()) {
            return Result(evaluateBoard(gameState), null)
        }

        sortMoves(moves, gameState, isMaximizing)

        // Verificar victoria inmediata
        val immediateWin = checkImmediateWin(moves.first(), gameState, isMaximizing, debug)
        if (immediateWin != null) return immediateWin

        // Búsqueda minimax
        return searchBestMove(gameState, moves, depth, alpha, beta, isMaximizing, debug)
            .also { result ->
                transpositionTable[boardHash] = TranspositionEntry(depth, result)
                if (debug) println("[RESULT] depth=$depth, bestScore=${result.score}, bestMove=${result.move}")
            }
    }

    private fun checkTerminalState(gameState: GameState, depth: Int, debug: Boolean): Result? {
        if (depth == 0 || gameState.isGameOver()) {
            val score = if (gameState.isGameOver()) {
                val winner = gameState.getWinner()
                if (debug) println("[TERMINAL] winner=$winner, currentTurn=${gameState.currentTurn}")
                when (winner) {
                    WHITE -> evalConfig.winningScore
                    BLACK -> -evalConfig.winningScore
                    else -> evaluateBoard(gameState)
                }
            } else {
                if (debug) println("[LEAF] depth=0, score=${evaluateBoard(gameState)}")
                evaluateBoard(gameState)
            }
            return Result(score, null)
        }
        return null
    }

    private fun checkImmediateWin(move: Move, gameState: GameState, isMaximizing: Boolean, debug: Boolean): Result? {
        val newState = applyMove(gameState, move)
        if (newState.isGameOver() && newState.getWinner() == gameState.currentTurn) {
            if (debug) println("[IMMEDIATE WIN] Found winning move: ${move.from}->${move.to}")
            return Result(
                if (isMaximizing) evalConfig.winningScore else -evalConfig.winningScore,
                move
            )
        }
        return null
    }

    private fun searchBestMove(
        gameState: GameState,
        moves: List<Move>,
        depth: Int,
        alphaInit: Double,
        betaInit: Double,
        isMaximizing: Boolean,
        debug: Boolean
    ): Result {
        var bestMove: Move? = null
        var bestScore = if (isMaximizing) Double.NEGATIVE_INFINITY else Double.POSITIVE_INFINITY
        var alpha = alphaInit
        var beta = betaInit

        if (debug) {
            println("[SEARCH] depth=$depth, isMax=$isMaximizing, turn=${gameState.currentTurn}")
            println("  Moves: ${moves.take(3).joinToString { "${it.from}->${it.to}" }}...")
        }

        for ((index, move) in moves.withIndex()) {
            // Verificar triple repetición
            val newState = applyMove(gameState, move)
            if (newState.checkIfWouldCauseRepetition()) {
                if (debug) println("  [MOVE $index] ${move.from}->${move.to}: WOULD CAUSE TRIPLE REPETITION")
                continue
            }

            val result = minimax(newState, depth - 1, alpha, beta, !isMaximizing, debug)
            val score = result.score

            if (debug) println("  [MOVE $index] ${move.from}->${move.to}: score=$score")

            val isNewBest = if (isMaximizing) score > bestScore else score < bestScore
            if (isNewBest) {
                bestScore = score
                bestMove = move
                if (debug) println("    -> NEW BEST")
            }

            // Actualizar alpha/beta
            if (isMaximizing) {
                alpha = max(alpha, bestScore)
            } else {
                beta = min(beta, bestScore)
            }

            // Poda
            if (shouldPrune(bestScore, alpha, beta, isMaximizing, debug)) break
        }

        // Fallback si todos los movimientos causan repetición
        if (bestMove == null && moves.isNotEmpty()) {
            bestMove = moves.first()
            bestScore = if (isMaximizing) -evalConfig.winningScore else evalConfig.winningScore
            if (debug) println("[FALLBACK] All moves cause repetition, choosing first")
        }

        return Result(bestScore, bestMove)
    }

    private fun shouldPrune(
        bestScore: Double,
        alpha: Double,
        beta: Double,
        isMaximizing: Boolean,
        debug: Boolean
    ): Boolean {
        // Podar si encontramos victoria garantizada
        val winThreshold = evalConfig.winningScore * evalConfig.winningThreshold
        if ((isMaximizing && bestScore >= winThreshold) || (!isMaximizing && bestScore <= -winThreshold)) {
            if (debug) println("    -> PRUNED (winning line found)")
            return true
        }

        // Alpha-beta pruning
        if (beta <= alpha) {
            if (debug) println("    -> PRUNED (beta=$beta <= alpha=$alpha)")
            return true
        }

        return false
    }

    // ==================== Ordenamiento de Movimientos ====================

    fun sortMoves(moves: MutableList<Move>, gameState: GameState, isMaximizing: Boolean) {
        val evaluatedMoves = moves.map { move ->
            evaluateMove(move, gameState)
        }

        val sorted = if (isMaximizing) {
            evaluatedMoves.sortedWith(
                compareByDescending<MoveEvaluation> { it.isImmediateWin }
                    .thenByDescending { !it.isImmediateLoss }
                    .thenByDescending { it.isWinningMove }
                    .thenByDescending { it.flipsRoc }
                    .thenByDescending { it.flipsCob }
                    .thenByDescending { it.leadsToUpgrade }
                    .thenByDescending { it.score }
            )
        } else {
            evaluatedMoves.sortedWith(
                compareByDescending<MoveEvaluation> { it.isImmediateWin }
                    .thenByDescending { !it.isImmediateLoss }
                    .thenByDescending { it.isWinningMove }
                    .thenBy { it.flipsRoc }
                    .thenBy { it.flipsCob }
                    .thenBy { it.leadsToUpgrade }
                    .thenBy { it.score }
            )
        }

        moves.clear()
        moves.addAll(sorted.map { it.move })
    }

    private data class MoveEvaluation(
        val move: Move,
        val score: Double,
        val flipsRoc: Int,
        val flipsCob: Int,
        val leadsToUpgrade: Boolean,
        val isWinningMove: Boolean,
        val isImmediateWin: Boolean,
        val isImmediateLoss: Boolean
    )

    private fun evaluateMove(move: Move, gameState: GameState): MoveEvaluation {
        val newState = applyMove(gameState, move)
        val wouldCauseRepetition = newState.checkIfWouldCauseRepetition()

        val (rocFlips, cobFlips) = move.countFlipsByType(gameState, newState)
        val quickScore = quickEvaluate(newState)

        val leadsToUpgrade = move.to in homeBases[gameState.currentTurn.opponent()]!!
        val upgradeBonus = if (leadsToUpgrade) evalConfig.upgradeScore.toDouble() else 0.0

        val isImmediateWin = newState.isGameOver() && newState.getWinner() == gameState.currentTurn
        val isWinningMove = !isImmediateWin && leadsToWinningPosition(newState, gameState.currentTurn)

        val repetitionPenalty =
            if (wouldCauseRepetition) -evalConfig.winningScore * evalConfig.repetitionPenaltyMultiplier else 0.0
        val immediateWinBonus =
            if (isImmediateWin) evalConfig.winningScore * evalConfig.immediateWinBonusMultiplier else 0.0

        val totalScore = quickScore + upgradeBonus + repetitionPenalty + immediateWinBonus +
                rocFlips * evalConfig.flipRocBonus +
                cobFlips * evalConfig.flipCobBonus

        return MoveEvaluation(
            move = move,
            score = totalScore,
            flipsRoc = rocFlips,
            flipsCob = cobFlips,
            leadsToUpgrade = leadsToUpgrade,
            isWinningMove = isWinningMove,
            isImmediateWin = isImmediateWin,
            isImmediateLoss = wouldCauseRepetition
        )
    }

    private fun leadsToWinningPosition(gameState: GameState, movingPlayer: Color): Boolean {
        if (gameState.getAllMovesForTurn().isEmpty()) {
            return gameState.getWinner() == movingPlayer
        }

        val score = quickEvaluate(gameState)
        val threshold = evalConfig.winningScore * evalConfig.winningPositionThreshold

        return if (movingPlayer == WHITE) score > threshold else score < -threshold
    }

    // ==================== Evaluación ====================

    fun evaluateBoard(gameState: GameState): Double {
        val metrics = calculateBoardMetrics(gameState)

        return with(evalConfig) {
            // Material (ya incluye el peso de rocScore y cobScore)
            (metrics.whiteMaterial - metrics.blackMaterial) +
                    // Factores posicionales y tácticos
                    (metrics.whiteCenterControl - metrics.blackCenterControl) * controlCenterScore +
                    (metrics.whiteMobility - metrics.blackMobility) * mobilityScore +
                    (metrics.whiteHomeControl - metrics.blackHomeControl) * domesticControlScore +
                    (metrics.whiteOpponentPressure - metrics.blackOpponentPressure) * opponentDomesticPressureScore +
                    (metrics.whiteUpgradeOpportunities - metrics.blackUpgradeOpportunities) * upgradeScore
        }
    }

    private data class BoardMetrics(
        val whiteMaterial: Double,
        val blackMaterial: Double,
        val whiteCenterControl: Int,
        val blackCenterControl: Int,
        val whiteMobility: Int,
        val blackMobility: Int,
        val whiteHomeControl: Int,
        val blackHomeControl: Int,
        val whiteOpponentPressure: Int,
        val blackOpponentPressure: Int,
        val whiteUpgradeOpportunities: Int,
        val blackUpgradeOpportunities: Int
    )

    private fun calculateBoardMetrics(gameState: GameState): BoardMetrics {
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

        for ((vertex, cob) in gameState.cobs) {
            val materialValue = if (cob.isUpgraded) evalConfig.rocScore else evalConfig.cobScore

            // Material y control del centro
            if (cob.color == WHITE) {
                whiteMaterial += materialValue
                if (vertex in centerVertices) whiteCenterControl++
            } else {
                blackMaterial += materialValue
                if (vertex in centerVertices) blackCenterControl++
            }

            // Movilidad
            val possibleMoves = countValidMoves(gameState, vertex, cob)
            if (cob.color == WHITE) whiteMobility += possibleMoves else blackMobility += possibleMoves

            // Control de bases y presión
            when (cob.color) {
                WHITE -> {
                    if (vertex in homeBases[WHITE]!!) whiteHomeControl++
                    if (vertex in homeBases[BLACK]!!) whiteOpponentPressure++
                }

                BLACK -> {
                    if (vertex in homeBases[BLACK]!!) blackHomeControl++
                    if (vertex in homeBases[WHITE]!!) blackOpponentPressure++
                }
            }

            // Oportunidades de mejora
            if (!cob.isUpgraded) {
                val adjacent = adjacencyMap[vertex] ?: emptyList()
                val enemyBase = homeBases[cob.color.opponent()]!!
                if (adjacent.any { it in enemyBase }) {
                    if (cob.color == WHITE) whiteUpgradeOpportunities++ else blackUpgradeOpportunities++
                }
            }
        }

        return BoardMetrics(
            whiteMaterial = whiteMaterial,
            blackMaterial = blackMaterial,
            whiteCenterControl = whiteCenterControl,
            blackCenterControl = blackCenterControl,
            whiteMobility = whiteMobility,
            blackMobility = blackMobility,
            whiteHomeControl = whiteHomeControl,
            blackHomeControl = blackHomeControl,
            whiteOpponentPressure = whiteOpponentPressure,
            blackOpponentPressure = blackOpponentPressure,
            whiteUpgradeOpportunities = whiteUpgradeOpportunities,
            blackUpgradeOpportunities = blackUpgradeOpportunities
        )
    }

    private fun countValidMoves(
        gameState: GameState,
        vertex: String,
        cob: Cob
    ): Int {
        val adjacentVertices = adjacencyMap[vertex] ?: emptyList()
        return adjacentVertices.count { to ->
            !gameState.cobs.containsKey(to) &&
                    (cob.isUpgraded || isForwardMove(
                        cob.color,
                        vertex,
                        to
                    ))
        }
    }

    private val quickEvalCache = mutableMapOf<String, Double>()

    fun quickEvaluate(gameState: GameState): Double {
        val hash = gameState.hashBoard()
        return quickEvalCache.getOrPut(hash) {
            var score = 0.0
            var whiteMaterial = 0.0
            var blackMaterial = 0.0
            var whiteThreatsToUpgraded = 0
            var blackThreatsToUpgraded = 0

            for ((vertex, cob) in gameState.cobs) {
                val materialValue = if (cob.isUpgraded) evalConfig.rocScore else evalConfig.cobScore

                if (cob.color == WHITE) {
                    whiteMaterial += materialValue
                    whiteThreatsToUpgraded += countThreatsToUpgraded(gameState, vertex, BLACK)
                } else {
                    blackMaterial += materialValue
                    blackThreatsToUpgraded += countThreatsToUpgraded(gameState, vertex, WHITE)
                }
            }

            score += (whiteMaterial - blackMaterial)
            score += (whiteThreatsToUpgraded - blackThreatsToUpgraded) * evalConfig.quickThreatWeight
            return score
        }
    }

    private fun countThreatsToUpgraded(gameState: GameState, vertex: String, enemyColor: Color): Int {
        val adjacent = adjacencyMap[vertex] ?: emptyList()
        return adjacent.count {
            val enemy = gameState.cobs[it]
            enemy?.color == enemyColor && enemy.isUpgraded
        }
    }

    // ==================== Aplicar Movimiento ====================

    private fun applyMove(gameState: GameState, move: Move): GameState {
        return applyMoveToBoard(gameState, move.from, move.to)
            .copy(currentTurn = gameState.currentTurn.opponent())
    }

    fun applyMoveToBoard(prevState: GameState, from: String, to: String): GameState {
        val mutableCobs = prevState.cobs.toMutableMap()
        val movedCob = mutableCobs.remove(from) ?: return prevState

        val placedCob = upgradeIfInEnemyBase(movedCob, to)
        mutableCobs[to] = placedCob

        if (Move(from, to).isCastling(movedCob.color)) {
            flipCastlingCobs(mutableCobs, movedCob.color)
        } else {
            flipAdjacentCobs(mutableCobs, to, placedCob.color)
        }

        return GameState(mutableCobs, prevState.currentTurn)
    }

    private fun upgradeIfInEnemyBase(cob: Cob, vertex: String): Cob {
        val enemyBase = homeBases[cob.color.opponent()] ?: emptyList()
        return if (vertex in enemyBase) cob.copy(isUpgraded = true) else cob
    }

    private fun flipCastlingCobs(mutable: MutableMap<String, Cob>, color: Color) {
        val targetVertex = if (color == WHITE) WHITE_CASTLING_VERTEX else BLACK_CASTLING_VERTEX
        mutable[targetVertex]?.let { mutable[targetVertex] = it.copy(color = color) }
    }

    private fun flipAdjacentCobs(mutable: MutableMap<String, Cob>, vertex: String, color: Color) {
        edges.forEach { (a, b) ->
            val adjacent = if (a == vertex) b else if (b == vertex) a else null
            adjacent?.let { adj ->
                mutable[adj]?.takeIf { it.color != color }?.let { adjCob ->
                    mutable[adj] = upgradeIfInEnemyBase(adjCob.copy(color = color), adj)
                }
            }
        }
    }
}