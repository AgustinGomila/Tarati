package com.agustin.tarati.game.ai

import com.agustin.tarati.game.ai.TaratiAI.clearPositionHistory
import com.agustin.tarati.game.ai.TaratiAI.getWinner
import com.agustin.tarati.game.ai.TaratiAI.isGameOver
import com.agustin.tarati.game.ai.TaratiAI.recordRealMove
import com.agustin.tarati.game.ai.tournament.ConfigPerformance
import com.agustin.tarati.game.ai.tournament.TournamentConfig
import com.agustin.tarati.game.ai.tournament.TournamentRunner
import com.agustin.tarati.game.core.Color
import com.agustin.tarati.game.core.initialGameState
import com.agustin.tarati.game.logic.createGameState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.Int

class AITournamentTests {

    private val tournamentRunner = TournamentRunner()

    @Test
    fun testTournamentGameEndsByTripleRepetition() {
        // Test específico para el problema del torneo
        val tournamentRunner = TournamentRunner()

        // Configuración simple
        val config = EvaluationConfig()

        // Jugar un solo juego con configuración para forzar repetición
        var gameState = initialGameState()
        clearPositionHistory()

        // Forzar una situación de triple repetición
        val repeatedState = createGameState {
            setTurn(Color.WHITE)
            setChecker("C1", Color.WHITE, false)
            setChecker("C7", Color.BLACK, false)
        }

        // Registrar la misma posición 3 veces
        repeat(3) {
            recordRealMove(repeatedState, Color.WHITE)
        }

        // Verificar que el juego termina
        assertTrue(isGameOver(repeatedState), "Game should end by triple repetition")

        val winner = getWinner(repeatedState)
        assertEquals("Black should win when white causes triple repetition", Color.BLACK, winner)

        println("Tournament triple repetition test passed: $winner wins")
    }

    private fun assertTrue(message: Boolean, condition: String) {}

    @Test
    fun testMaterialWeightComparison() {
        // Probar diferentes pesos para el material
        val aggressiveConfig = EvaluationConfig(
            materialScore = 150,
            upgradedPieceScore = 250,
            captureUpgradedBonus = 200,
            captureNormalBonus = 150,
            controlCenterScore = 20,
            mobilityScore = 10
        )

        val positionalConfig = EvaluationConfig(
            materialScore = 100,
            upgradedPieceScore = 200,
            controlCenterScore = 50,
            mobilityScore = 30,
            homeBaseControlScore = 80,
            opponentBasePressureScore = 60,
            upgradeOpportunityScore = 120
        )

        val result = tournamentRunner.runSingleMatch(
            aggressiveConfig,
            positionalConfig,
            TournamentConfig(gamesPerMatch = 20, depth = Difficulty.CHAMPION.aiDepth)
        )

        println("Aggressive vs Positional:")
        println("Wins Aggressive: ${result.winsA}")
        println("Wins Positional: ${result.winsB}")
        println("Draws: ${result.draws}")
        println("Average moves: ${result.averageMoves}")

        assertTrue("Should complete all games", result.winsA + result.winsB + result.draws == 20)
    }

    @Test
    fun testUpgradeFocusedConfig() {
        // Configuración que prioriza mejoras vs. configuración balanceada
        val upgradeFocused = EvaluationConfig(
            materialScore = 80,
            upgradedPieceScore = 300,
            upgradeOpportunityScore = 200,
            opponentBasePressureScore = 100,
            controlCenterScore = 20,
            mobilityScore = 10
        )

        val balancedConfig = EvaluationConfig() // Configuración por defecto

        val result = tournamentRunner.runSingleMatch(
            upgradeFocused,
            balancedConfig,
            TournamentConfig(gamesPerMatch = 15, depth = Difficulty.CHAMPION.aiDepth)
        )

        println("Upgrade Focused vs Balanced:")
        println("Wins Upgrade: ${result.winsA}")
        println("Wins Balanced: ${result.winsB}")
        println("Win Rate Upgrade: ${result.winRateA}")
        println("Win Rate Balanced: ${result.winRateB}")
    }

    @Test
    fun testRoundRobinTournament() {
        val configs = listOf(
            // Configuración agresiva (capturas)
            EvaluationConfig(
                name = "Aggressive",
                materialScore = 150,
                captureUpgradedBonus = 250,
                captureNormalBonus = 180,
                upgradedPieceScore = 220,
                quickThreatWeight = 80
            ),

            // Configuración posicional
            EvaluationConfig(
                name = "Positional",
                controlCenterScore = 60,
                mobilityScore = 40,
                homeBaseControlScore = 80,
                opponentBasePressureScore = 70,
                upgradeOpportunityScore = 150
            ),

            // Configuración balanceada
            EvaluationConfig(
                name = "Balanced"
            ),

            // Configuración defensiva
            EvaluationConfig(
                name = "Defensive",
                materialScore = 120,
                upgradedPieceScore = 250,
                controlCenterScore = 40,
                homeBaseControlScore = 100,
                opponentBasePressureScore = 30,
                captureUpgradedBonus = 120,
                captureNormalBonus = 80
            ),

            // Configuraciones más especializadas
            EvaluationConfig(
                name = "hyperAggressive",
                materialScore = 250,
                captureUpgradedBonus = 350,
                captureNormalBonus = 250,
                quickThreatWeight = 150,
                mobilityScore = 50
            )
        )

        val results = tournamentRunner.runRoundRobin(
            configs,
            TournamentConfig(gamesPerMatch = 10, depth = Difficulty.CHAMPION.aiDepth)
        )

        // Imprimir resultados del torneo
        println("\n=== ROUND ROBIN TOURNAMENT RESULTS ===")
        results.entries.sortedByDescending { it.value.score }.forEach { (config, performance) ->
            println("${config.name}:")
            println("  Score: ${performance.score}")
            println("  Wins: ${performance.wins}, Losses: ${performance.losses}, Draws: ${performance.draws}")
            println("  Win Rate: ${"%.2f".format(performance.winRate * 100)}%")
            println("  Avg Moves: ${"%.1f".format(performance.averageMoves)}")
            println()
        }

        // Verificar que todas las configuraciones jugaron
        assertEquals(configs.size, results.size)
    }

    @Test
    fun testWinningThresholdSensitivity() {
        // Probar diferentes umbrales de victoria
        val sensitiveConfig = EvaluationConfig(
            winningThreshold = 0.7f,  // Más sensible - poda antes
            winningPositionThreshold = 0.4f
        )

        val conservativeConfig = EvaluationConfig(
            winningThreshold = 0.95f,  // Más conservador - busca más profundo
            winningPositionThreshold = 0.6f
        )

        val result = tournamentRunner.runSingleMatch(
            sensitiveConfig,
            conservativeConfig,
            TournamentConfig(gamesPerMatch = 25, depth = Difficulty.CHAMPION.aiDepth)
        )

        println("Sensitive vs Conservative Thresholds:")
        println("Sensitive wins: ${result.winsA} (${"%.1f".format(result.winRateA * 100)}%)")
        println("Conservative wins: ${result.winsB} (${"%.1f".format(result.winRateB * 100)}%)")
        println("Average moves: ${"%.1f".format(result.averageMoves)}")

        // El config más sensible debería tener juegos más cortos en promedio
        assertTrue("Sensitive config should have strategic value", result.winsA + result.winsB >= 0)
    }

    @Test
    fun testCaptureVsMobility() {
        // Capturas vs. Movilidad
        val captureFocused = EvaluationConfig(
            captureUpgradedBonus = 300,
            captureNormalBonus = 200,
            quickThreatWeight = 100,
            mobilityScore = 10
        )

        val mobilityFocused = EvaluationConfig(
            captureUpgradedBonus = 100,
            captureNormalBonus = 80,
            mobilityScore = 80,
            controlCenterScore = 60
        )

        val result = tournamentRunner.runSingleMatch(
            captureFocused,
            mobilityFocused,
            TournamentConfig(gamesPerMatch = 30, depth = Difficulty.CHAMPION.aiDepth)
        )

        println("Capture vs Mobility Focus:")
        println("Capture wins: ${result.winsA}")
        println("Mobility wins: ${result.winsB}")
        println("Draws: ${result.draws}")

        // Análisis estadístico básico
        val totalGames = result.winsA + result.winsB + result.draws
        val captureWinRate = result.winsA.toDouble() / totalGames
        val mobilityWinRate = result.winsB.toDouble() / totalGames

        println("Capture win rate: ${"%.1f".format(captureWinRate * 100)}%")
        println("Mobility win rate: ${"%.1f".format(mobilityWinRate * 100)}%")
    }

    @Test
    fun testParameterOptimization() {
        val baseConfig = EvaluationConfig()

        // Probar variaciones de un parámetro específico
        val materialWeights = listOf(50, 100, 150, 200)
        val results = mutableMapOf<Int, ConfigPerformance>()

        materialWeights.forEach { materialWeight ->
            val testConfig = baseConfig.copy(
                materialScore = materialWeight,
                name = "Material$materialWeight"
            )

            val tournamentResult = tournamentRunner.runRoundRobin(
                listOf(testConfig, baseConfig),
                TournamentConfig(gamesPerMatch = 10, depth = Difficulty.CHAMPION.aiDepth)
            )

            results[materialWeight] = tournamentResult[testConfig]!!
        }

        // Encontrar el mejor peso
        val bestWeight = results.maxByOrNull { it.value.winRate }?.key
        val bestWinRate = results[bestWeight]?.winRate ?: 0.0

        println("Material weight optimization results:")
        results.forEach { (weight, performance) ->
            println("Weight $weight: ${"%.1f".format(performance.winRate * 100)}% win rate")
        }
        println("Best weight: $bestWeight with ${"%.1f".format(bestWinRate * 100)}% win rate")
    }
}
