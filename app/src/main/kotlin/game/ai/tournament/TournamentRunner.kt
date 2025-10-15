package com.agustin.tarati.game.ai.tournament

import com.agustin.tarati.game.ai.EvaluationConfig
import com.agustin.tarati.game.ai.TaratiAI.applyMoveToBoard
import com.agustin.tarati.game.ai.TaratiAI.clearAIHistory
import com.agustin.tarati.game.ai.TaratiAI.getNextBestMove
import com.agustin.tarati.game.ai.TaratiAI.getWinner
import com.agustin.tarati.game.ai.TaratiAI.isGameOver
import com.agustin.tarati.game.ai.TaratiAI.recordRealMove
import com.agustin.tarati.game.ai.TaratiAI.setEvaluationConfig
import com.agustin.tarati.game.core.Color
import com.agustin.tarati.game.core.Color.BLACK
import com.agustin.tarati.game.core.Color.WHITE
import com.agustin.tarati.game.core.hashBoard
import com.agustin.tarati.game.core.initialGameState
import com.agustin.tarati.game.core.opponent
import kotlin.math.roundToInt

infix fun <A, B> A.pairOf(that: B): Pair<A, B> = Pair(this, that)

// ==================== Configuración ====================

data class TournamentConfig(
    val gamesPerMatch: Int = 50,
    val maxMovesPerGame: Int = 200,
    val alternateColors: Boolean = true,
    val verbose: Boolean = false,
    val showProgress: Boolean = true
)

// ==================== Resultados ====================

data class TournamentResult(
    val configA: EvaluationConfig,
    val configB: EvaluationConfig,
    val winsA: Int,
    val winsB: Int,
    val draws: Int,
    val totalGames: Int,
    val averageMoves: Double,
    val averageMovesA: Double,
    val averageMovesB: Double,
    val timeoutsA: Int,
    val timeoutsB: Int
) {
    val winRateA: Double get() = winsA.toDouble() / totalGames
    val winRateB: Double get() = winsB.toDouble() / totalGames
    val scoreA: Double get() = winsA + (draws * 0.5)
    val scoreB: Double get() = winsB + (draws * 0.5)

    fun printSummary(nameA: String = "Config A", nameB: String = "Config B") {
        println("\n" + "=".repeat(60))
        println("TOURNAMENT RESULTS")
        println("=".repeat(60))
        println("$nameA vs $nameB")
        println("-".repeat(60))
        println("Games played: $totalGames")
        println()
        println("$nameA:")
        println("  Wins: $winsA (${(winRateA * 100).roundToInt()}%)")
        println("  Losses: $winsB")
        println("  Draws: $draws")
        println("  Score: ${"%.1f".format(scoreA)} / $totalGames")
        println("  Avg moves (wins): ${"%.1f".format(averageMovesA)}")
        println("  Timeouts: $timeoutsA")
        println()
        println("$nameB:")
        println("  Wins: $winsB (${(winRateB * 100).roundToInt()}%)")
        println("  Losses: $winsA")
        println("  Draws: $draws")
        println("  Score: ${"%.1f".format(scoreB)} / $totalGames")
        println("  Avg moves (wins): ${"%.1f".format(averageMovesB)}")
        println("  Timeouts: $timeoutsB")
        println()
        println("Overall avg moves: ${"%.1f".format(averageMoves)}")
        println("=".repeat(60))
    }
}

data class ConfigPerformance(
    val config: EvaluationConfig,
    val name: String,
    val wins: Int = 0,
    val losses: Int = 0,
    val draws: Int = 0,
    val totalGames: Int = 0,
    val totalMoves: Int = 0,
    val timeouts: Int = 0
) {
    val winRate: Double get() = if (totalGames > 0) wins.toDouble() / totalGames else 0.0
    val score: Double get() = wins + (draws * 0.5)
    val averageMoves: Double get() = if (totalGames > 0) totalMoves.toDouble() / totalGames else 0.0

    fun withResults(
        additionalWins: Int = 0,
        additionalLosses: Int = 0,
        additionalDraws: Int = 0,
        additionalMoves: Int = 0,
        additionalTimeouts: Int = 0
    ): ConfigPerformance {
        return copy(
            wins = wins + additionalWins,
            losses = losses + additionalLosses,
            draws = draws + additionalDraws,
            totalGames = totalGames + additionalWins + additionalLosses + additionalDraws,
            totalMoves = totalMoves + additionalMoves,
            timeouts = timeouts + additionalTimeouts
        )
    }
}

// ==================== Tournament Runner ====================

class TournamentRunner {

    /**
     * Ejecuta un match entre dos configuraciones
     */
    fun runSingleMatch(
        configA: EvaluationConfig,
        configB: EvaluationConfig,
        tournamentConfig: TournamentConfig = TournamentConfig(),
    ): TournamentResult {
        var winsA = 0
        var winsB = 0
        var draws = 0
        var totalMoves = 0
        var movesWhenAWins = 0
        var movesWhenBWins = 0
        var aWinCount = 0
        var bWinCount = 0
        var timeoutsA = 0
        var timeoutsB = 0

        if (tournamentConfig.verbose) {
            println("\n${"=".repeat(60)}")
            println("Starting match: ${configA.name} (${configA.difficulty.aiDepth}) vs ${configB.name} (${configB.difficulty.aiDepth})")
            println("Games: ${tournamentConfig.gamesPerMatch}")
            println("=".repeat(60))
        }

        repeat(tournamentConfig.gamesPerMatch) { gameIndex ->
            if (tournamentConfig.showProgress && gameIndex % 10 == 0) {
                print(".")
                if (gameIndex % 50 == 0 && gameIndex > 0) {
                    println(" [$gameIndex/${tournamentConfig.gamesPerMatch}]")
                }
            }

            val whiteConfig = if (tournamentConfig.alternateColors && gameIndex % 2 == 0) configA else configB
            val blackConfig = if (whiteConfig == configA) configB else configA

            val gameResult = playGame(
                whiteConfig = whiteConfig,
                blackConfig = blackConfig,
                tournamentConfig = tournamentConfig,
                gameNumber = gameIndex + 1
            )

            when (gameResult.winner) {
                WHITE if whiteConfig == configA -> {
                    winsA++
                    movesWhenAWins += gameResult.moves
                    aWinCount++
                }

                WHITE if whiteConfig == configB -> {
                    winsB++
                    movesWhenBWins += gameResult.moves
                    bWinCount++
                }

                BLACK if blackConfig == configA -> {
                    winsA++
                    movesWhenAWins += gameResult.moves
                    aWinCount++
                }

                BLACK if blackConfig == configB -> {
                    winsB++
                    movesWhenBWins += gameResult.moves
                    bWinCount++
                }

                else -> draws++
            }

            totalMoves += gameResult.moves

            if (gameResult.timeout) {
                if ((gameResult.winner == WHITE && whiteConfig == configA) ||
                    (gameResult.winner == BLACK && blackConfig == configA)
                ) {
                    timeoutsB++
                } else if ((gameResult.winner == WHITE && whiteConfig == configB) ||
                    (gameResult.winner == BLACK && blackConfig == configB)
                ) {
                    timeoutsA++
                }
            }
        }

        if (tournamentConfig.showProgress) {
            println(" [${tournamentConfig.gamesPerMatch}/${tournamentConfig.gamesPerMatch}]")
        }

        val avgMovesA = if (aWinCount > 0) movesWhenAWins.toDouble() / aWinCount else 0.0
        val avgMovesB = if (bWinCount > 0) movesWhenBWins.toDouble() / bWinCount else 0.0

        return TournamentResult(
            configA = configA,
            configB = configB,
            winsA = winsA,
            winsB = winsB,
            draws = draws,
            totalGames = tournamentConfig.gamesPerMatch,
            averageMoves = totalMoves.toDouble() / tournamentConfig.gamesPerMatch,
            averageMovesA = avgMovesA,
            averageMovesB = avgMovesB,
            timeoutsA = timeoutsA,
            timeoutsB = timeoutsB
        )
    }

    /**
     * Round-robin: todos contra todos
     */
    fun runRoundRobin(
        configs: Map<String, EvaluationConfig>,
        tournamentConfig: TournamentConfig = TournamentConfig()
    ): List<ConfigPerformance> {
        val results = mutableMapOf<String, ConfigPerformance>()

        // Inicializar resultados
        configs.forEach { (name, config) ->
            results[name] = ConfigPerformance(config = config, name = name)
        }

        val configList = configs.toList()
        val totalMatches = configList.size * (configList.size - 1) / 2
        var completedMatches = 0

        println("\n${"=".repeat(60)}")
        println("ROUND ROBIN TOURNAMENT")
        println("Competitors: ${configList.size}")
        println("Total matches: $totalMatches")
        println("Games per match: ${tournamentConfig.gamesPerMatch}")
        println("=".repeat(60))

        // Jugar todos contra todos
        for (i in configList.indices) {
            for (j in i + 1 until configList.size) {
                val (nameA, configA) = configList[i]
                val (nameB, configB) = configList[j]

                completedMatches++
                println("\n[Match $completedMatches/$totalMatches] $nameA vs $nameB")

                val matchResult = runSingleMatch(
                    configA = configA,
                    configB = configB,
                    tournamentConfig = tournamentConfig,
                )

                // Actualizar estadísticas
                results[nameA] = results[nameA]!!.withResults(
                    additionalWins = matchResult.winsA,
                    additionalLosses = matchResult.winsB,
                    additionalDraws = matchResult.draws,
                    additionalMoves = (matchResult.averageMovesA * matchResult.winsA).toInt(),
                    additionalTimeouts = matchResult.timeoutsA
                )

                results[nameB] = results[nameB]!!.withResults(
                    additionalWins = matchResult.winsB,
                    additionalLosses = matchResult.winsA,
                    additionalDraws = matchResult.draws,
                    additionalMoves = (matchResult.averageMovesB * matchResult.winsB).toInt(),
                    additionalTimeouts = matchResult.timeoutsB
                )

                println("Result: $nameA ${matchResult.winsA}-${matchResult.winsB} $nameB (${matchResult.draws} draws)")
            }
        }

        // Ordenar por score (wins + 0.5*draws)
        val sortedResults = results.values.sortedByDescending { it.score }

        // Imprimir tabla final
        printLeaderboard(sortedResults)

        return sortedResults
    }

    /**
     * Juega una partida individual
     */
    private fun playGame(
        whiteConfig: EvaluationConfig,
        blackConfig: EvaluationConfig,
        tournamentConfig: TournamentConfig,
        gameNumber: Int
    ): GameResult {
        clearAIHistory()

        var gameState = initialGameState()
        var moves = 0
        val localHistory = mutableMapOf<String, Int>()

        while (moves < tournamentConfig.maxMovesPerGame && !isGameOver(gameState)) {
            val currentConfig = when (gameState.currentTurn) {
                WHITE -> whiteConfig
                else -> blackConfig
            }
            setEvaluationConfig(currentConfig)
            val result = getNextBestMove(gameState, currentConfig.difficulty.aiDepth, debug = false)
            if (result.move == null) break

            if (tournamentConfig.verbose && moves < 10) {
                println("  Move $moves: ${gameState.currentTurn} ${result.move.from}->${result.move.to}")
            }

            // Aplicar movimiento
            val newState = applyMoveToBoard(gameState, result.move.from, result.move.to)
            val nextState = newState.copy(currentTurn = gameState.currentTurn.opponent())

            // Verificar triple repetición ANTES de registrar
            val hash = nextState.hashBoard()
            val count = localHistory[hash] ?: 0

            if (count >= 2) {
                // Triple repetición: el jugador actual pierde
                val winner = nextState.currentTurn
                if (tournamentConfig.verbose) {
                    println("  Game $gameNumber ended by triple repetition at move $moves")
                    println("  Winner: $winner")
                }
                return GameResult(winner = winner, moves = moves + 1, timeout = false)
            }

            localHistory[hash] = count + 1
            recordRealMove(nextState, gameState.currentTurn)

            gameState = nextState
            moves++
        }

        val winner = getWinner(gameState)
        val timeout = moves >= tournamentConfig.maxMovesPerGame

        if (tournamentConfig.verbose) {
            println("  Game $gameNumber ended in $moves moves")
            println("  Winner: ${winner ?: "DRAW"} ${if (timeout) "(timeout)" else ""}")
        }

        return GameResult(winner = winner, moves = moves, timeout = timeout)
    }

    private fun printLeaderboard(results: List<ConfigPerformance>) {
        println("\n${"=".repeat(80)}")
        println("FINAL LEADERBOARD")
        println("=".repeat(80))
        println(
            "%-3s %-20s %6s %6s %6s %6s %8s %8s %8s".format(
                "Pos", "Name", "Wins", "Loss", "Draw", "Games", "Score", "Win%", "AvgMvs"
            )
        )
        println("-".repeat(80))

        results.forEachIndexed { index, perf ->
            println(
                "%-3d %-20s %6d %6d %6d %6d %8.1f %7.1f%% %8.1f".format(
                    index + 1,
                    perf.name.take(20),
                    perf.wins,
                    perf.losses,
                    perf.draws,
                    perf.totalGames,
                    perf.score,
                    perf.winRate * 100,
                    perf.averageMoves
                )
            )
        }
        println("=".repeat(80))
    }

    private data class GameResult(
        val winner: Color?,
        val moves: Int,
        val timeout: Boolean
    )
}

// ==================== Config Builder Helper ====================

/**
 * Helper para crear variaciones de configuración fácilmente
 */
object ConfigBuilder {
    fun baseline() = EvaluationConfig(name = "Default")

    fun aggressive() = EvaluationConfig(
        name = "Aggressive",
        // Capture
        captureNormalBonus = 60,
        captureUpgradedBonus = 180,
        // Strategic
        opponentBasePressureScore = 50,
        // Tactic
        mobilityScore = 3
    )

    fun defensive() = EvaluationConfig(
        name = "Defensive",
        // Material
        materialScore = 40,
        // Strategic       
        opponentBasePressureScore = 75,
        controlCenterScore = 60,
        // Tactic
        upgradeScore = 240,
    )

    fun materialFocused() = EvaluationConfig(
        name = "MaterialFocused",
        // Material
        materialScore = 120,
        upgradedPieceScore = 250,
        // Capture
        captureNormalBonus = 70,
        captureUpgradedBonus = 200,
    )

    fun positional() = EvaluationConfig(
        name = "Positional",
        // Strategic
        controlCenterScore = 50,
        homeBaseControlScore = 40,
        opponentBasePressureScore = 60,
        // Tactic
        mobilityScore = 10,
        upgradeScore = 100,
    )

    fun balanced() = EvaluationConfig(
        name = "Balanced",
        // Material
        materialScore = 100,
        upgradedPieceScore = 200,
        // Capture
        captureUpgradedBonus = 150,
        // Strategic      
        opponentBasePressureScore = 40,
        controlCenterScore = 35,
        homeBaseControlScore = 30,
        // Tactic
        mobilityScore = 7,
        upgradeScore = 80,
    )

    @Suppress("unused")
    fun custom(block: EvaluationConfig.() -> EvaluationConfig): EvaluationConfig {
        return baseline().block()
    }
}