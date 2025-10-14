package com.agustin.tarati.game.ai.tournament

import com.agustin.tarati.game.ai.Difficulty
import com.agustin.tarati.game.ai.EvaluationConfig
import com.agustin.tarati.game.ai.TaratiAI
import com.agustin.tarati.game.ai.TaratiAI.applyMoveToBoard
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
import kotlin.math.max

data class TournamentConfig(
    val gamesPerMatch: Int = 50,
    val maxMovesPerGame: Int = 200,
    val depth: Int = Difficulty.CHAMPION.aiDepth,
    val alternateColors: Boolean = true
)

data class TournamentResult(
    val configA: EvaluationConfig,
    val configB: EvaluationConfig,
    val winsA: Int,
    val winsB: Int,
    val draws: Int,
    val averageMoves: Double,
    val winRateA: Double,
    val winRateB: Double
)

data class ConfigPerformance(
    val config: EvaluationConfig,
    val wins: Int = 0,
    val losses: Int = 0,
    val draws: Int = 0,
    val totalGames: Int = 0,
    val winRate: Double = 0.0,
    val averageMoves: Double = 0.0
) {
    val score: Double get() = wins + (draws * 0.5)
}

class TournamentRunner {

    fun runSingleMatch(
        configA: EvaluationConfig,
        configB: EvaluationConfig,
        tournamentConfig: TournamentConfig = TournamentConfig()
    ): TournamentResult {
        var winsA = 0
        var winsB = 0
        var draws = 0
        var totalMoves = 0

        repeat(tournamentConfig.gamesPerMatch) { gameIndex ->
            val whiteConfig = if (tournamentConfig.alternateColors && gameIndex % 2 == 0) configA else configB
            val blackConfig = if (whiteConfig == configA) configB else configA

            val gameResult = playGame(whiteConfig, blackConfig, tournamentConfig)
            val color = gameResult.winner

            when (color) {
                WHITE if whiteConfig == configA -> winsA++
                WHITE -> winsB++
                BLACK if blackConfig == configA -> winsA++
                BLACK -> winsB++
                null -> draws++
            }

            totalMoves += gameResult.moves
        }

        val totalGames = tournamentConfig.gamesPerMatch
        return TournamentResult(
            configA = configA,
            configB = configB,
            winsA = winsA,
            winsB = winsB,
            draws = draws,
            averageMoves = totalMoves.toDouble() / totalGames,
            winRateA = winsA.toDouble() / totalGames,
            winRateB = winsB.toDouble() / totalGames
        )
    }

    fun runRoundRobin(
        configs: List<EvaluationConfig>,
        tournamentConfig: TournamentConfig = TournamentConfig()
    ): Map<EvaluationConfig, ConfigPerformance> {
        val results = mutableMapOf<EvaluationConfig, ConfigPerformance>()

        // Inicializar resultados
        configs.forEach { config ->
            results[config] = ConfigPerformance(config = config)
        }

        // Jugar todos contra todos
        for (i in configs.indices) {
            for (j in i + 1 until configs.size) {
                val configA = configs[i]
                val configB = configs[j]

                val matchResult = runSingleMatch(configA, configB, tournamentConfig)

                // Actualizar estadísticas para configA
                val perfA = results[configA]!!
                results[configA] = perfA.copy(
                    wins = perfA.wins + matchResult.winsA,
                    losses = perfA.losses + matchResult.winsB,
                    draws = perfA.draws + matchResult.draws,
                    totalGames = perfA.totalGames + tournamentConfig.gamesPerMatch,
                    averageMoves = (perfA.averageMoves * perfA.totalGames + matchResult.averageMoves * tournamentConfig.gamesPerMatch) /
                            (perfA.totalGames + tournamentConfig.gamesPerMatch)
                )

                // Actualizar estadísticas para configB
                val perfB = results[configB]!!
                results[configB] = perfB.copy(
                    wins = perfB.wins + matchResult.winsB,
                    losses = perfB.losses + matchResult.winsA,
                    draws = perfB.draws + matchResult.draws,
                    totalGames = perfB.totalGames + tournamentConfig.gamesPerMatch,
                    averageMoves = (perfB.averageMoves * perfB.totalGames + matchResult.averageMoves * tournamentConfig.gamesPerMatch) /
                            (perfB.totalGames + tournamentConfig.gamesPerMatch)
                )
            }
        }

        // Calcular win rates finales
        return results.mapValues { (_, perf) ->
            perf.copy(winRate = perf.wins.toDouble() / max(1, perf.totalGames))
        }
    }

    private fun playGame(
        whiteConfig: EvaluationConfig,
        blackConfig: EvaluationConfig,
        tournamentConfig: TournamentConfig
    ): GameResult {
        TaratiAI.clearPositionHistory()
        var gameState = initialGameState()
        var moves = 0
        val positionHistory = mutableMapOf<String, Int>() // Historial local por juego

        while (moves < tournamentConfig.maxMovesPerGame && !isGameOver(gameState)) {
            val currentConfig = if (gameState.currentTurn == WHITE) whiteConfig else blackConfig
            setEvaluationConfig(currentConfig)

            val result = getNextBestMove(gameState, tournamentConfig.depth)
            if (result.move == null) break

            val newState = applyMoveToBoard(gameState, result.move.from, result.move.to)
            val nextState = newState.copy(currentTurn = gameState.currentTurn.opponent())

            // Registrar en historial local y global
            val hash = newState.hashBoard()
            positionHistory[hash] = (positionHistory[hash] ?: 0) + 1

            // Forzar detección de triple repetición si ocurre
            if (positionHistory[hash]!! >= 3) {
                val loser = gameState.currentTurn
                return GameResult(loser.opponent(), moves + 1)
            }

            // También registrar en el historial global
            recordRealMove(nextState, gameState.currentTurn)

            gameState = nextState
            moves++
        }

        val winner = getWinner(gameState)
        return GameResult(winner, moves)
    }

    private data class GameResult(val winner: Color?, val moves: Int)
}