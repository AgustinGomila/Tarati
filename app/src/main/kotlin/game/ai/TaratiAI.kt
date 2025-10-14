package com.agustin.tarati.game.ai

import com.agustin.tarati.game.core.Color
import com.agustin.tarati.game.core.Color.BLACK
import com.agustin.tarati.game.core.Color.WHITE
import com.agustin.tarati.game.core.GameBoard.adjacencyMap
import com.agustin.tarati.game.core.GameBoard.centerVertices
import com.agustin.tarati.game.core.GameBoard.edges
import com.agustin.tarati.game.core.GameBoard.getAllPossibleMoves
import com.agustin.tarati.game.core.GameBoard.homeBases
import com.agustin.tarati.game.core.GameState
import com.agustin.tarati.game.core.Move
import com.agustin.tarati.game.core.hashBoard
import com.agustin.tarati.game.core.opponent
import java.util.concurrent.atomic.AtomicReference
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

object TaratiAI {
    private const val MAX_TABLE_SIZE = 10000
    private const val DEFAULT_TIME_LIMIT_MS = 5000L

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

    private data class TranspositionEntry(val depth: Int, val result: Result)

    // ==================== API Pública ====================

    @Suppress("unused")
    fun setEvaluationConfig(config: EvaluationConfig) {
        globalConfigRef.set(config)
    }

    fun clearPositionHistory() {
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
        depth: Int = Difficulty.MEDIUM.aiDepth,
        debug: Boolean = false
    ): Result {
        return minimax(
            gameState = gameState,
            depth = min(depth, Difficulty.CHAMPION.aiDepth),
            alpha = Double.NEGATIVE_INFINITY,
            beta = Double.POSITIVE_INFINITY,
            isMaximizing = (gameState.currentTurn == WHITE),
            debug = debug
        )
    }

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
            if (System.currentTimeMillis() - startTime > timeLimitMs) {
                if (debug) println("Time limit reached at depth ${depth - 1}")
                break
            }

            val result = getNextBestMove(gameState, depth, debug)
            if (result.move != null) bestResult = result

            if (debug) println("Completed depth $depth: score=${result.score}, move=${result.move}")

            if (abs(result.score) >= evalConfig.winningScore * evalConfig.winningThreshold) {
                if (debug) println("Found winning/losing line at depth $depth")
                break
            }
        }

        return bestResult
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
        if (hasTripleRepetition(gameState)) {
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
        val moves = getAllPossibleMoves(gameState)
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
        if (depth == 0 || isGameOver(gameState)) {
            val score = if (isGameOver(gameState)) {
                val winner = getWinner(gameState)
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
        if (isGameOver(newState) && getWinner(newState) == gameState.currentTurn) {
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
            if (checkIfWouldCauseRepetition(newState)) {
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
                    .thenByDescending { it.capturesUpgraded }
                    .thenByDescending { it.capturesNormal }
                    .thenByDescending { it.leadsToUpgrade }
                    .thenByDescending { it.score }
            )
        } else {
            evaluatedMoves.sortedWith(
                compareByDescending<MoveEvaluation> { it.isImmediateWin }
                    .thenByDescending { !it.isImmediateLoss }
                    .thenByDescending { it.isWinningMove }
                    .thenBy { it.capturesUpgraded }
                    .thenBy { it.capturesNormal }
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
        val capturesUpgraded: Int,
        val capturesNormal: Int,
        val leadsToUpgrade: Boolean,
        val isWinningMove: Boolean,
        val isImmediateWin: Boolean,
        val isImmediateLoss: Boolean
    )

    private fun evaluateMove(move: Move, gameState: GameState): MoveEvaluation {
        val newState = applyMove(gameState, move)
        val wouldCauseRepetition = checkIfWouldCauseRepetition(newState)

        val (upgradedCaptures, normalCaptures) = countCapturesByType(gameState, newState, move.to)
        val quickScore = quickEvaluate(newState)

        val leadsToUpgrade = move.to in homeBases[gameState.currentTurn.opponent()]!!
        val upgradeBonus = if (leadsToUpgrade) evalConfig.upgradeOpportunityScore.toDouble() else 0.0

        val isImmediateWin = isGameOver(newState) && getWinner(newState) == gameState.currentTurn
        val isWinningMove = !isImmediateWin && leadsToWinningPosition(newState, gameState.currentTurn)

        val repetitionPenalty =
            if (wouldCauseRepetition) -evalConfig.winningScore * evalConfig.repetitionPenaltyMultiplier else 0.0
        val immediateWinBonus =
            if (isImmediateWin) evalConfig.winningScore * evalConfig.immediateWinBonusMultiplier else 0.0

        val totalScore = quickScore + upgradeBonus + repetitionPenalty + immediateWinBonus +
                upgradedCaptures * evalConfig.captureUpgradedBonus +
                normalCaptures * evalConfig.captureNormalBonus

        return MoveEvaluation(
            move, totalScore, upgradedCaptures, normalCaptures,
            leadsToUpgrade, isWinningMove, isImmediateWin, wouldCauseRepetition
        )
    }

    private fun leadsToWinningPosition(gameState: GameState, movingPlayer: Color): Boolean {
        if (getAllPossibleMoves(gameState).isEmpty()) {
            return getWinner(gameState) == movingPlayer
        }

        val score = quickEvaluate(gameState)
        val threshold = evalConfig.winningScore * evalConfig.winningPositionThreshold

        return if (movingPlayer == WHITE) score > threshold else score < -threshold
    }

    // ==================== Evaluación ====================

    fun evaluateBoard(gameState: GameState): Double {
        val metrics = calculateBoardMetrics(gameState)

        return with(evalConfig) {
            // Material (ya incluye el peso de upgradedPieceScore y materialScore)
            (metrics.whiteMaterial - metrics.blackMaterial) +
                    // Factores posicionales y tácticos
                    (metrics.whiteCenterControl - metrics.blackCenterControl) * controlCenterScore +
                    (metrics.whiteMobility - metrics.blackMobility) * mobilityScore +
                    (metrics.whiteHomeControl - metrics.blackHomeControl) * homeBaseControlScore +
                    (metrics.whiteOpponentPressure - metrics.blackOpponentPressure) * opponentBasePressureScore +
                    (metrics.whiteUpgradeOpportunities - metrics.blackUpgradeOpportunities) * upgradeOpportunityScore
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

        for ((vertex, checker) in gameState.checkers) {
            val materialValue = if (checker.isUpgraded) evalConfig.upgradedPieceScore else evalConfig.materialScore

            // Material y control del centro
            if (checker.color == WHITE) {
                whiteMaterial += materialValue
                if (vertex in centerVertices) whiteCenterControl++
            } else {
                blackMaterial += materialValue
                if (vertex in centerVertices) blackCenterControl++
            }

            // Movilidad
            val possibleMoves = countValidMoves(gameState, vertex, checker)
            if (checker.color == WHITE) whiteMobility += possibleMoves else blackMobility += possibleMoves

            // Control de bases y presión
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

            // Oportunidades de mejora
            if (!checker.isUpgraded) {
                val adjacent = adjacencyMap[vertex] ?: emptyList()
                val enemyBase = homeBases[checker.color.opponent()]!!
                if (adjacent.any { it in enemyBase }) {
                    if (checker.color == WHITE) whiteUpgradeOpportunities++ else blackUpgradeOpportunities++
                }
            }
        }

        return BoardMetrics(
            whiteMaterial, blackMaterial, whiteCenterControl, blackCenterControl,
            whiteMobility, blackMobility, whiteHomeControl, blackHomeControl,
            whiteOpponentPressure, blackOpponentPressure,
            whiteUpgradeOpportunities, blackUpgradeOpportunities
        )
    }

    private fun countValidMoves(
        gameState: GameState,
        vertex: String,
        checker: com.agustin.tarati.game.core.Checker
    ): Int {
        val adjacentVertices = adjacencyMap[vertex] ?: emptyList()
        return adjacentVertices.count { to ->
            !gameState.checkers.containsKey(to) &&
                    (checker.isUpgraded || com.agustin.tarati.game.core.GameBoard.isForwardMove(
                        checker.color,
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

            for ((vertex, checker) in gameState.checkers) {
                val materialValue = if (checker.isUpgraded) evalConfig.upgradedPieceScore else evalConfig.materialScore

                if (checker.color == WHITE) {
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
            val enemy = gameState.checkers[it]
            enemy?.color == enemyColor && enemy.isUpgraded
        }
    }

    private fun countCapturesByType(oldState: GameState, newState: GameState, targetVertex: String): Pair<Int, Int> {
        var upgradedCaptures = 0
        var normalCaptures = 0

        for (vertex in adjacencyMap[targetVertex] ?: emptyList()) {
            val oldChecker = oldState.checkers[vertex]
            val newChecker = newState.checkers[vertex]

            if (oldChecker != null && newChecker != null && oldChecker.color != newChecker.color) {
                if (oldChecker.isUpgraded) upgradedCaptures++ else normalCaptures++
            }
        }

        return Pair(upgradedCaptures, normalCaptures)
    }

    // ==================== Estado del Juego ====================

    fun isGameOver(gameState: GameState): Boolean {
        val whitePieces = gameState.checkers.values.count { it.color == WHITE }
        val blackPieces = gameState.checkers.values.count { it.color == BLACK }

        return whitePieces == 0 || blackPieces == 0 ||
                getAllPossibleMoves(gameState).isEmpty() ||
                hasTripleRepetition(gameState)
    }

    fun getWinner(gameState: GameState): Color? {
        if (!isGameOver(gameState)) return null

        if (hasTripleRepetition(gameState)) {
            return gameState.currentTurn.opponent()
        }

        val whitePieces = gameState.checkers.values.count { it.color == WHITE }
        val blackPieces = gameState.checkers.values.count { it.color == BLACK }

        return when {
            whitePieces == 0 -> BLACK
            blackPieces == 0 -> WHITE
            whitePieces > blackPieces -> WHITE
            blackPieces > whitePieces -> BLACK
            else -> {
                // Empate por igualdad de material - considerar movilidad
                val whiteMoves = getAllPossibleMoves(gameState.copy(currentTurn = WHITE)).size
                val blackMoves = getAllPossibleMoves(gameState.copy(currentTurn = BLACK)).size
                return when {
                    whiteMoves > blackMoves -> WHITE
                    blackMoves > whiteMoves -> BLACK
                    else -> null // Empate verdadero
                }
            }
        }
    }

    private fun hasTripleRepetition(gameState: GameState): Boolean {
        val hash = gameState.hashBoard()
        return (realGameHistory[hash] ?: 0) >= 3
    }

    fun checkIfWouldCauseRepetition(gameState: GameState): Boolean {
        val hash = gameState.hashBoard()
        val currentCount = realGameHistory[hash] ?: 0
        return (currentCount + 1) >= 3
    }

    // ==================== Aplicar Movimiento ====================

    private fun applyMove(gameState: GameState, move: Move): GameState {
        val newState = applyMoveToBoard(gameState, move.from, move.to)
        return newState.copy(currentTurn = gameState.currentTurn.opponent())
    }

    fun applyMoveToBoard(prevState: GameState, from: String, to: String): GameState {
        val mutable = prevState.checkers.toMutableMap()
        val movedChecker = mutable[from] ?: return prevState

        mutable.remove(from)

        // Aplicar mejora si llega a base enemiga
        val placedChecker = upgradeIfInEnemyBase(movedChecker, to)
        mutable[to] = placedChecker

        // Voltear fichas adyacentes
        flipAdjacentCheckers(mutable, to, placedChecker.color)

        return GameState(mutable.toMap(), prevState.currentTurn)
    }

    private fun upgradeIfInEnemyBase(
        checker: com.agustin.tarati.game.core.Checker,
        vertex: String
    ): com.agustin.tarati.game.core.Checker {
        val enemyBase = homeBases[checker.color.opponent()] ?: emptyList()
        return if (vertex in enemyBase) {
            checker.copy(isUpgraded = true)
        } else {
            checker
        }
    }

    private fun flipAdjacentCheckers(
        mutable: MutableMap<String, com.agustin.tarati.game.core.Checker>,
        vertex: String,
        color: Color
    ) {
        for (edge in edges) {
            val (a, b) = edge
            if (a != vertex && b != vertex) continue

            val adjacent = if (a == vertex) b else a
            val adjChecker = mutable[adjacent]
            if (adjChecker == null || adjChecker.color == color) continue

            val flipped = adjChecker.copy(color = color)
            mutable[adjacent] = upgradeIfInEnemyBase(flipped, adjacent)
        }
    }
}