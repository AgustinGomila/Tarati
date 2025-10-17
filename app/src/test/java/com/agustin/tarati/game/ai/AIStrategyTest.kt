package com.agustin.tarati.game.ai

import com.agustin.tarati.game.ai.TaratiAI.applyMoveToBoard
import com.agustin.tarati.game.ai.TaratiAI.clearAIHistory
import com.agustin.tarati.game.ai.TaratiAI.evalConfig
import com.agustin.tarati.game.ai.TaratiAI.evaluateBoard
import com.agustin.tarati.game.ai.TaratiAI.getNextBestMove
import com.agustin.tarati.game.ai.TaratiAI.getWinner
import com.agustin.tarati.game.ai.TaratiAI.isGameOver
import com.agustin.tarati.game.ai.TaratiAI.quickEvaluate
import com.agustin.tarati.game.ai.TaratiAI.recordRealMove
import com.agustin.tarati.game.ai.TaratiAI.setEvaluationConfig
import com.agustin.tarati.game.core.Color.BLACK
import com.agustin.tarati.game.core.Color.WHITE
import com.agustin.tarati.game.core.GameBoard
import com.agustin.tarati.game.core.GameBoard.adjacencyMap
import com.agustin.tarati.game.core.GameBoard.centerVertices
import com.agustin.tarati.game.core.GameBoard.getAllPossibleMoves
import com.agustin.tarati.game.core.GameBoard.homeBases
import com.agustin.tarati.game.core.GameBoard.isForwardMove
import com.agustin.tarati.game.core.GameState
import com.agustin.tarati.game.core.Move
import com.agustin.tarati.game.core.initialGameState
import com.agustin.tarati.game.core.opponent
import org.junit.Before
import org.junit.Test

class TaratiChampionDiagnosticTest {

    @Before
    fun setup() {
        clearAIHistory()
        setEvaluationConfig(EvaluationConfig.CHAMPION)
    }

    /**
     * Test principal: reproduce la secuencia completa y detecta dónde la IA toma malas decisiones
     */
    @Test
    fun diagnoseLosingSequence() {
        println("=" * 80)
        println("DIAGNOSTIC: CHAMPION Losing Sequence Analysis")
        println("=" * 80)

        val moves = listOf(
            Move("C2", "C3"),  // 1. Blancas
            Move("C7", "C6"),  // 1. Negras
            Move("C1", "B1"),  // 2. Blancas
            Move("C8", "B4"),  // 2. Negras - CRITICAL MOVE
            Move("D2", "C2"),  // 3. Blancas
            Move("D3", "C7"),  // 3. Negras
            Move("D1", "C1"),  // 4. Blancas
            Move("B4", "A1"),  // 4. Negras - CRITICAL MOVE
            Move("C3", "B2"),  // 5. Blancas
            Move("C6", "B3"),  // 5. Negras
            Move("C2", "C3"),  // 6. Blancas
            Move("A1", "B6"),  // 6. Negras
            Move("B2", "A1"),  // 7. Blancas - CAPTURE CENTER
            Move("D4", "C8"),  // 7. Negras
            Move("A1", "B4")   // 8. Blancas - MIT
        )

        var gameState = initialGameState()
        var moveNumber = 1
        var criticalMovesFound = 0

        for (i in moves.indices) {
            val move = moves[i]
            val currentPlayer = gameState.currentTurn
            val isAIMove = currentPlayer == BLACK

            println("\n" + "-" * 80)
            println("Move $moveNumber: ${currentPlayer.name} (${move.from} -> ${move.to})")
            println("-" * 80)

            if (!GameBoard.isValidMove(gameState, move.from, move.to)) {
                println("ERROR: Invalid move in sequence!")
                printBoardState(gameState)
                return
            }

            if (isAIMove) {
                val divergence = analyzeAIDecision(gameState, move)
                if (divergence) criticalMovesFound++
            }

            // Apply move
            gameState = applyMoveToBoard(gameState, move.from, move.to)
            gameState = gameState.copy(currentTurn = currentPlayer.opponent())
            recordRealMove(gameState, currentPlayer)

            printBoardState(gameState)
            printEvaluation(gameState)

            if (isGameOver(gameState)) {
                println("\nGAME OVER")
                val winner = getWinner(gameState)
                println("Winner: ${winner?.name ?: "DRAW"}")
                break
            }

            if (currentPlayer == BLACK) moveNumber++
        }

        println("\n" + "=" * 80)
        println("DIAGNOSIS COMPLETE")
        println("Critical divergences found: $criticalMovesFound")
        println("=" * 80)
    }

    /**
     * Análisis profundo del movimiento crítico #2 de Negras: C8 -> B4
     */
    @Test
    fun analyzeCriticalMove2() {
        println("=" * 80)
        println("DEEP ANALYSIS: Move 2 Black (C8 -> B4)")
        println("=" * 80)

        val gameState = buildStateBeforeMove2Black()

        println("\nBoard state before C8 -> B4:")
        printDetailedState(gameState)

        // Analizar con diferentes profundidades
        println("\n" + "-" * 80)
        println("DEPTH ANALYSIS:")
        println("-" * 80)

        for (depth in 2..8 step 2) {
            val tempDifficulty = Difficulty.entries.firstOrNull { it.aiDepth == depth } ?: Difficulty.CHAMPION
            val result = getNextBestMove(gameState, tempDifficulty, debug = false)

            println("\nDepth $depth:")
            println("  Best move: ${result.move?.from} -> ${result.move?.to}")
            println("  Score: ${result.score}")

            if (result.move?.from == "C8" && result.move.to == "B4") {
                println("  Status: AI chose the losing move")
            } else {
                println("  Status: AI found different move")
            }
        }

        // Evaluar todas las alternativas
        println("\n" + "-" * 80)
        println("ALL POSSIBLE MOVES EVALUATION:")
        println("-" * 80)

        val allMoves = getAllPossibleMoves(gameState)
        val evaluations = allMoves.map { move ->
            val newState = applyMoveToBoard(gameState, move.from, move.to)
                .copy(currentTurn = gameState.currentTurn.opponent())

            val eval = evaluateBoard(newState)
            val whiteResponse = getNextBestMove(newState, Difficulty.CHAMPION, debug = false)

            MoveEvaluation(
                move = move,
                immediateEval = eval,
                whiteResponseMove = whiteResponse.move,
                whiteResponseScore = whiteResponse.score
            )
        }.sortedByDescending { it.immediateEval }

        evaluations.forEachIndexed { index, eval ->
            val marker = if (eval.move.from == "C8" && eval.move.to == "B4") ">>>" else "   "
            println("$marker ${index + 1}. ${eval.move.from} -> ${eval.move.to}")
            println("       Immediate eval: ${eval.immediateEval}")
            println("       White response: ${eval.whiteResponseMove?.from} -> ${eval.whiteResponseMove?.to}")
            println("       After response: ${eval.whiteResponseScore}")
        }
    }

    /**
     * Análisis profundo del movimiento crítico #4 de Negras: B4 -> A1
     */
    @Test
    fun analyzeCriticalMove4() {
        println("=" * 80)
        println("DEEP ANALYSIS: Move 4 Black (B4 -> A1)")
        println("=" * 80)

        val gameState = buildStateBeforeMove4Black()

        println("\nBoard state before B4 -> A1:")
        printDetailedState(gameState)

        // Evaluar todas las alternativas con simulación de respuesta
        println("\n" + "-" * 80)
        println("MOVE COMPARISON WITH WHITE RESPONSES:")
        println("-" * 80)

        val allMoves = getAllPossibleMoves(gameState)

        allMoves.forEach { move ->
            println("\n${move.from} -> ${move.to}:")

            val afterMove = applyMoveToBoard(gameState, move.from, move.to)
                .copy(currentTurn = gameState.currentTurn.opponent())

            printMoveDetails(gameState, move)

            val evalAfterMove = evaluateBoard(afterMove)
            println("  Eval after move: $evalAfterMove")

            // Simular respuesta de blancas
            val whiteResponse = getNextBestMove(afterMove, Difficulty.CHAMPION, debug = false)
            if (whiteResponse.move != null) {
                println("  White responds: ${whiteResponse.move.from} -> ${whiteResponse.move.to}")

                val afterWhiteResponse = applyMoveToBoard(
                    afterMove,
                    whiteResponse.move.from,
                    whiteResponse.move.to
                ).copy(currentTurn = afterMove.currentTurn.opponent())

                val finalEval = evaluateBoard(afterWhiteResponse)
                println("  Eval after response: $finalEval")
                println("  Net change: ${finalEval - evalAfterMove}")

                if (isGameOver(afterWhiteResponse)) {
                    val winner = getWinner(afterWhiteResponse)
                    println("  WARNING: Game ends! Winner: ${winner?.name}")
                }
            }
        }
    }

    /**
     * Compara los componentes de evaluación para diferentes movimientos
     */
    @Test
    fun compareEvaluationComponents() {
        println("=" * 80)
        println("EVALUATION COMPONENTS BREAKDOWN")
        println("=" * 80)

        val gameState = buildStateBeforeMove2Black()

        val testMoves = listOf(
            Move("C8", "B4"),   // El movimiento que hace
            Move("C8", "C7"),   // Alternativa 1
            Move("D4", "C8"),   // Alternativa 2
            Move("C6", "B3"),   // Alternativa 3
        )

        testMoves.forEach { move ->
            println("\n" + "=" * 60)
            println("Move: ${move.from} -> ${move.to}")
            println("=" * 60)

            val newState = applyMoveToBoard(gameState, move.from, move.to)
                .copy(currentTurn = gameState.currentTurn.opponent())

            val metrics = calculateMetrics(newState)
            val config = EvaluationConfig.CHAMPION

            println("\nMaterial:")
            println("  White: ${metrics.whiteMaterial} | Black: ${metrics.blackMaterial}")
            println("  Difference: ${metrics.whiteMaterial - metrics.blackMaterial}")

            println("\nCenter Control:")
            println("  White: ${metrics.whiteCenterControl} | Black: ${metrics.blackCenterControl}")
            val centerScore = (metrics.whiteCenterControl - metrics.blackCenterControl) * config.controlCenterScore
            println("  Score contribution: $centerScore")

            println("\nMobility:")
            println("  White: ${metrics.whiteMobility} | Black: ${metrics.blackMobility}")
            val mobilityScore = (metrics.whiteMobility - metrics.blackMobility) * config.mobilityScore
            println("  Score contribution: $mobilityScore")

            println("\nOpponent Base Pressure:")
            println("  White: ${metrics.whiteOpponentPressure} | Black: ${metrics.blackOpponentPressure}")
            val pressureScore =
                (metrics.whiteOpponentPressure - metrics.blackOpponentPressure) * config.opponentDomesticPressureScore
            println("  Score contribution: $pressureScore")

            println("\nHome Base Control:")
            println("  White: ${metrics.whiteHomeControl} | Black: ${metrics.blackHomeControl}")
            val homeScore = (metrics.whiteHomeControl - metrics.blackHomeControl) * config.domesticControlScore
            println("  Score contribution: $homeScore")

            println("\nUpgrade Opportunities:")
            println("  White: ${metrics.whiteUpgradeOpportunities} | Black: ${metrics.blackUpgradeOpportunities}")
            val upgradeScore =
                (metrics.whiteUpgradeOpportunities - metrics.blackUpgradeOpportunities) * config.upgradeScore
            println("  Score contribution: $upgradeScore")

            val totalEval = evaluateBoard(newState)
            println("\nTOTAL EVALUATION: $totalEval")
        }
    }

    // ==================== Helper Functions ====================

    private fun analyzeAIDecision(gameState: GameState, actualMove: Move): Boolean {
        println("\nAI DECISION ANALYSIS:")

        val allMoves = getAllPossibleMoves(gameState)
        println("  Possible moves: ${allMoves.size}")

        val result = getNextBestMove(gameState, Difficulty.CHAMPION, debug = false)
        val aiBestMove = result.move
        val aiScore = result.score

        println("  AI chose: ${aiBestMove?.from} -> ${aiBestMove?.to} (score: $aiScore)")
        println("  Actual was: ${actualMove.from} -> ${actualMove.to}")

        val divergence = aiBestMove?.from != actualMove.from || aiBestMove.to != actualMove.to

        if (divergence) {
            println("  STATUS: DIVERGENCE DETECTED")

            var aiEval = 0.0
            if (aiBestMove != null) {
                val aiNewState = applyMoveToBoard(gameState, aiBestMove.from, aiBestMove.to)
                    .copy(currentTurn = gameState.currentTurn.opponent())
                aiEval = evaluateBoard(aiNewState)
                println("\n  AI preferred move evaluation:")
                println("    Move: ${aiBestMove.from} -> ${aiBestMove.to}")
                println("    Eval: $aiEval")
                printMoveDetails(gameState, aiBestMove)
            }

            val actualNewState = applyMoveToBoard(gameState, actualMove.from, actualMove.to)
                .copy(currentTurn = gameState.currentTurn.opponent())
            val actualEval = evaluateBoard(actualNewState)
            println("\n  Actual move evaluation:")
            println("    Move: ${actualMove.from} -> ${actualMove.to}")
            println("    Eval: $actualEval")
            printMoveDetails(gameState, actualMove)

            println("\n  Evaluation difference: ${if (aiBestMove != null) aiEval - actualEval else "N/A"}")
        } else {
            println("  STATUS: AI chose expected move")
        }

        return divergence
    }

    private fun printMoveDetails(gameState: GameState, move: Move) {
        val newState = applyMoveToBoard(gameState, move.from, move.to)
        val movedCob = gameState.cobs[move.from] ?: return

        var capturedPieces = 0
        var upgradedCaptured = 0
        for (vertex in adjacencyMap[move.to] ?: emptyList()) {
            val cob = gameState.cobs[vertex]
            if (cob != null && cob.color != movedCob.color) {
                capturedPieces++
                if (cob.isUpgraded) upgradedCaptured++
            }
        }

        val enemyBase = homeBases[movedCob.color.opponent()] ?: emptyList()
        val willUpgrade = move.to in enemyBase && !movedCob.isUpgraded

        println("    Captures: $capturedPieces (Upgraded: $upgradedCaptured)")
        println("    Upgrades: ${if (willUpgrade) "YES" else "NO"}")
        println("    Center control: ${if (move.to in centerVertices) "YES" else "NO"}")

        val mobility = getAllPossibleMoves(newState.copy(currentTurn = movedCob.color)).size
        println("    Resulting mobility: $mobility moves")

        if (isGameOver(newState)) {
            println("    WARNING: GAME ENDS - Winner: ${getWinner(newState)?.name}")
        }
    }

    private fun printBoardState(gameState: GameState) {
        println("\nBoard state:")
        val whitePieces = gameState.cobs.values.count { it.color == WHITE }
        val blackPieces = gameState.cobs.values.count { it.color == BLACK }
        val whiteUpgraded = gameState.cobs.values.count { it.color == WHITE && it.isUpgraded }
        val blackUpgraded = gameState.cobs.values.count { it.color == BLACK && it.isUpgraded }

        println("  White: $whitePieces pieces ($whiteUpgraded upgraded)")
        println("  Black: $blackPieces pieces ($blackUpgraded upgraded)")

        val whitesInCenter = gameState.cobs.entries.count {
            it.key in centerVertices && it.value.color == WHITE
        }
        val blacksInCenter = gameState.cobs.entries.count {
            it.key in centerVertices && it.value.color == BLACK
        }
        println("  Center: W:$whitesInCenter B:$blacksInCenter")
    }

    private fun printDetailedState(gameState: GameState) {
        println("Current turn: ${gameState.currentTurn.name}")
        println("Pieces on board:")
        gameState.cobs.entries.sortedBy { it.key }.forEach { (pos, cob) ->
            val upgraded = if (cob.isUpgraded) "[U]" else "   "
            val color = if (cob.color == WHITE) "W" else "B"
            println("  $pos: $color $upgraded")
        }
        printBoardState(gameState)
    }

    private fun printEvaluation(gameState: GameState) {
        val eval = evaluateBoard(gameState)
        val quickEval = quickEvaluate(gameState)
        println("\nFull evaluation: $eval")
        println("Quick evaluation: $quickEval")

        val advantage = when {
            eval > 100 -> "White significant advantage"
            eval > 50 -> "White moderate advantage"
            eval > 10 -> "White slight advantage"
            eval > -10 -> "Balanced position"
            eval > -50 -> "Black slight advantage"
            eval > -100 -> "Black moderate advantage"
            else -> "Black significant advantage"
        }
        println(advantage)
    }

    private fun calculateMetrics(gameState: GameState): BoardMetrics {
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

        val config = evalConfig

        for ((vertex, cob) in gameState.cobs) {
            val materialValue = if (cob.isUpgraded) config.rocScore else config.cobScore

            if (cob.color == WHITE) {
                whiteMaterial += materialValue
                if (vertex in centerVertices) whiteCenterControl++
            } else {
                blackMaterial += materialValue
                if (vertex in centerVertices) blackCenterControl++
            }

            val possibleMoves = adjacencyMap[vertex]?.count { to ->
                !gameState.cobs.containsKey(to) &&
                        (cob.isUpgraded || isForwardMove(cob.color, vertex, to))
            } ?: 0

            if (cob.color == WHITE) whiteMobility += possibleMoves else blackMobility += possibleMoves

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

            if (!cob.isUpgraded) {
                val adjacent = adjacencyMap[vertex] ?: emptyList()
                val enemyBase = homeBases[cob.color.opponent()]!!
                if (adjacent.any { it in enemyBase }) {
                    if (cob.color == WHITE) whiteUpgradeOpportunities++ else blackUpgradeOpportunities++
                }
            }
        }

        return BoardMetrics(
            whiteMaterial, blackMaterial,
            whiteCenterControl, blackCenterControl,
            whiteMobility, blackMobility,
            whiteHomeControl, blackHomeControl,
            whiteOpponentPressure, blackOpponentPressure,
            whiteUpgradeOpportunities, blackUpgradeOpportunities
        )
    }

    private fun buildStateBeforeMove2Black(): GameState {
        var state = initialGameState()

        val setupMoves = listOf(
            Move("C2", "C3"),
            Move("C7", "C6"),
            Move("C1", "B1")
        )

        for (move in setupMoves) {
            val currentPlayer = state.currentTurn
            state = applyMoveToBoard(state, move.from, move.to)
            state = state.copy(currentTurn = currentPlayer.opponent())
            recordRealMove(state, currentPlayer)
        }

        return state
    }

    private fun buildStateBeforeMove4Black(): GameState {
        var state = initialGameState()

        val setupMoves = listOf(
            Move("C2", "C3"),
            Move("C7", "C6"),
            Move("C1", "B1"),
            Move("C8", "B4"),
            Move("D2", "C2"),
            Move("D3", "C7"),
            Move("D1", "C1")
        )

        for (move in setupMoves) {
            val currentPlayer = state.currentTurn
            state = applyMoveToBoard(state, move.from, move.to)
            state = state.copy(currentTurn = currentPlayer.opponent())
            recordRealMove(state, currentPlayer)
        }

        return state
    }

    // ==================== Data Classes ====================

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

    private data class MoveEvaluation(
        val move: Move,
        val immediateEval: Double,
        val whiteResponseMove: Move?,
        val whiteResponseScore: Double
    )

    // ==================== Extensions ====================

    private operator fun String.times(n: Int): String = this.repeat(n)
}