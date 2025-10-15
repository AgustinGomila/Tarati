package com.agustin.tarati.game.ai.tournament

import com.agustin.tarati.game.ai.Difficulty
import org.junit.Test
import kotlin.system.measureTimeMillis

class TournamentTest {

    private val runner = TournamentRunner()

    // ==================== Tests Básicos ====================

    @Test
    fun testSingleMatch() {
        println("\n========== TEST: Single Match ==========")

        val baseline = ConfigBuilder.baseline()
        val aggressive = ConfigBuilder.aggressive()

        val result = runner.runSingleMatch(
            configA = baseline,
            configB = aggressive,
            tournamentConfig = TournamentConfig(
                gamesPerMatch = 20,
                alternateColors = true,
                verbose = false,
                showProgress = true
            ),
        )

        result.printSummary(baseline.name, aggressive.name)

        // Verificaciones básicas
        assert(result.totalGames == 20) { "Total games should be 20" }
        assert(result.winsA + result.winsB + result.draws == 20) { "Results should sum to total games" }
        assert(result.averageMoves > 0) { "Average moves should be positive" }

        println("✓ Test passed: Single match completed successfully")
    }

    @Test
    fun testRoundRobin() {
        println("\n========== TEST: Round Robin ==========")

        val baseline = ConfigBuilder.baseline()
        val aggressive = ConfigBuilder.aggressive()
        val defensive = ConfigBuilder.defensive()

        val configs = mapOf(
            baseline.name to baseline,
            aggressive.name to aggressive,
            defensive.name to defensive
        )

        val results = runner.runRoundRobin(
            configs = configs,
            tournamentConfig = TournamentConfig(
                gamesPerMatch = 10,
                alternateColors = true,
                verbose = false,
                showProgress = true
            )
        )

        // Verificaciones
        assert(results.size == 3) { "Should have 3 competitors" }
        assert(results.all { it.totalGames == 20 }) { "Each should play 20 games (10 vs each opponent)" }
        assert(results[0].score >= results[1].score) { "Results should be sorted by score" }

        println("✓ Test passed: Round robin completed successfully")
    }

    // ==================== Tests de Profundidad ====================

    @Test
    fun testDepthComparison() {
        println("\n========== TEST: Depth Comparison ==========")

        val depths = Difficulty.ALL
        val baseConfig = ConfigBuilder.baseline()

        println("\nTesting different search depths against baseline (depth ${baseConfig.difficulty}):")
        println("-".repeat(60))

        for (depth in depths) {
            if (depth == baseConfig.difficulty) continue // Skip baseline vs baseline

            measureTimeMillis {
                val result = runner.runSingleMatch(
                    configA = baseConfig.copy(name = "Depth: ${baseConfig.difficulty}"),
                    configB = baseConfig.copy(name = "Depth: $depth", difficulty = depth),
                    tournamentConfig = TournamentConfig(
                        gamesPerMatch = 10,
                        alternateColors = true,
                        verbose = false,
                        showProgress = false
                    ),
                )

                println("Depth $depth: ${result.winsA} wins, ${result.draws} draws, ${result.winsB} losses")
            }
        }

        println("✓ Test passed: Depth comparison completed")
    }

    // ==================== Tests de Configuraciones ====================

    @Test
    fun testAllPresetConfigs() {
        println("\n========== TEST: All Preset Configurations ==========")

        val baseline = ConfigBuilder.baseline()
        val aggressive = ConfigBuilder.aggressive()
        val defensive = ConfigBuilder.defensive()
        val material = ConfigBuilder.materialFocused()
        val positional = ConfigBuilder.positional()
        val balanced = ConfigBuilder.balanced()

        val configs = mapOf(
            baseline.name to baseline,
            aggressive.name to aggressive,
            defensive.name to defensive,
            material.name to material,
            positional.name to positional,
            balanced.name to balanced,
        )

        val totalTime = measureTimeMillis {
            runner.runRoundRobin(
                configs = configs,
                tournamentConfig = TournamentConfig(
                    gamesPerMatch = 20,
                    alternateColors = true,
                    verbose = false,
                    showProgress = true
                )
            )
        }

        println("\nTotal tournament time: ${totalTime / 1000.0}s")
        println("✓ Test passed: All preset configs tested")
    }

    @Test
    fun testMaterialWeightVariations() {
        println("\n========== TEST: Material Weight Variations ==========")

        val baseline = ConfigBuilder.baseline()
        val material20plus = baseline.copy(name = "material +20%", upgradedPieceScore = 240, materialScore = 120)
        val material20less = baseline.copy(name = "material -20%", upgradedPieceScore = 160, materialScore = 80)
        val captures50plus = baseline.copy(name = "captures +50%", captureUpgradedBonus = 225, captureNormalBonus = 75)
        val mobilityX2 = baseline.copy(name = "mobility x2", mobilityScore = 10)

        val configs = mapOf(
            baseline.name to baseline,
            material20plus.name to material20plus,
            material20less.name to material20less,
            captures50plus.name to captures50plus,
            mobilityX2.name to mobilityX2
        )

        runner.runRoundRobin(
            configs = configs,
            tournamentConfig = TournamentConfig(
                gamesPerMatch = 20,
                alternateColors = true,
                verbose = false,
                showProgress = true
            )
        )

        println("✓ Test passed: Material weight variations tested")
    }

    // ==================== Tests de Optimización ====================

    @Test
    fun testFindBestMobilityWeight() {
        println("\n========== TEST: Find Best Mobility Weight ==========")

        val mobilityWeights = listOf(2, 4, 8, 16, 32)
        val baseline = ConfigBuilder.baseline()

        println("\nTesting mobility weights:")
        println("-".repeat(60))

        val results = mutableListOf<Pair<Int, TournamentResult>>()

        for (weight in mobilityWeights) {
            val config = baseline.copy(name = "Mobility $weight", mobilityScore = weight)

            val result = runner.runSingleMatch(
                configA = baseline,
                configB = config,
                tournamentConfig = TournamentConfig(
                    gamesPerMatch = 30,
                    alternateColors = true,
                    verbose = false,
                    showProgress = false
                ),
            )

            results.add(weight to result)

            val performance = result.winsB - result.winsA
            println(
                "Mobility $weight: ${result.winsB}W ${result.draws}D ${result.winsA}L " +
                        "(Performance: ${if (performance > 0) "+" else ""}$performance)"
            )
        }

        val best = results.maxByOrNull { it.second.winsB }
        println("\nBest mobility weight: ${best?.first} with ${best?.second?.winsB} wins")
        println("✓ Test passed: Mobility weight optimization completed")
    }

    @Test
    fun testFindBestCaptureBonus() {
        println("\n========== TEST: Find Best Capture Bonus ==========")

        val baseline = ConfigBuilder.baseline()
        val low = baseline.copy(
            name = "Low",
            captureUpgradedBonus = (baseline.captureUpgradedBonus * 0.9).toInt(),
            captureNormalBonus = (baseline.captureNormalBonus * 0.9).toInt()
        )
        val high = baseline.copy(
            name = "High",
            captureUpgradedBonus = (baseline.captureUpgradedBonus * 1.1).toInt(),
            captureNormalBonus = (baseline.captureNormalBonus * 1.1).toInt()
        )
        val veryHigh = baseline.copy(
            name = "VeryHigh",
            captureUpgradedBonus = (baseline.captureUpgradedBonus * 1.3).toInt(),
            captureNormalBonus = (baseline.captureNormalBonus * 1.3).toInt()
        )

        val variations = listOf(
            low.name to low,
            baseline.name to baseline,
            high.name to high,
            veryHigh.name to veryHigh
        )

        val configs = variations.toMap()

        runner.runRoundRobin(
            configs = configs,
            tournamentConfig = TournamentConfig(
                gamesPerMatch = 25,
                alternateColors = true,
                verbose = false,
                showProgress = true
            )
        )

        println("✓ Test passed: Capture bonus optimization completed")
    }

    // ==================== Tests de Performance ====================

    @Test
    fun testPerformanceBenchmark() {
        println("\n========== TEST: Performance Benchmark ==========")

        val config = ConfigBuilder.baseline()
        val depths = Difficulty.ALL

        println("\nBenchmarking different depths (10 games each):")
        println("-".repeat(60))

        for (depth in depths) {
            val time = measureTimeMillis {
                runner.runSingleMatch(
                    configA = config.copy(name = "A"),
                    configB = config.copy(name = "B", difficulty = depth),
                    tournamentConfig = TournamentConfig(
                        gamesPerMatch = 10,
                        alternateColors = true,
                        verbose = false,
                        showProgress = false
                    ),
                )
            }

            val avgTimePerGame = time / 10.0
            println("Depth $depth: ${time}ms total, ${avgTimePerGame.toInt()}ms/game")
        }

        println("✓ Test passed: Performance benchmark completed")
    }

    // ==================== Test de Estabilidad ====================

    @Test
    fun testGameStability() {
        println("\n========== TEST: Game Stability ==========")

        val configA = ConfigBuilder.baseline().copy(name = "Player A")
        val configB = ConfigBuilder.baseline().copy(name = "Player B")

        val result = runner.runSingleMatch(
            configA = configA,
            configB = configB,
            tournamentConfig = TournamentConfig(
                gamesPerMatch = 50,
                maxMovesPerGame = 200,
                alternateColors = true,
                verbose = false,
                showProgress = true
            ),
        )

        // Con la misma config, debería haber muchos empates o distribución equitativa
        println("\nStability test (same config vs same config):")
        println("Player A wins: ${result.winsA}")
        println("Player B wins: ${result.winsB}")
        println("Draws: ${result.draws}")
        println("Timeouts A: ${result.timeoutsA}")
        println("Timeouts B: ${result.timeoutsB}")

        // Verificar que no haya una ventaja masiva (debería ser bastante equilibrado)
        val totalDecisive = result.winsA + result.winsB
        val imbalance = kotlin.math.abs(result.winsA - result.winsB).toDouble() / totalDecisive

        println("Imbalance: ${(imbalance * 100).toInt()}%")

        // Con la misma config, no debería haber más de 30% de desequilibrio
        assert(imbalance < 0.3) { "Same config should have less than 30% imbalance" }

        println("✓ Test passed: Game is stable")
    }

    // ==================== Test de Optimización Completa ====================

    @Test
    fun testFullOptimization() {
        println("\n========== TEST: Full Configuration Optimization ==========")
        println("This will take several minutes...")

        // Configurar la profundidad, alta profundidad alta duración del proceso.
        val firstPhaseDepth = Difficulty.EASY
        val secondPhaseDepth = Difficulty.MEDIUM

        val baseline = ConfigBuilder.baseline().copy(difficulty = firstPhaseDepth)
        val aggressive = ConfigBuilder.aggressive().copy(difficulty = firstPhaseDepth)
        val defensive = ConfigBuilder.defensive().copy(difficulty = firstPhaseDepth)
        val material = ConfigBuilder.materialFocused().copy(difficulty = firstPhaseDepth)
        val positional = ConfigBuilder.positional().copy(difficulty = firstPhaseDepth)
        val balanced = ConfigBuilder.balanced().copy(difficulty = firstPhaseDepth)

        // Fase 1: Probar configuraciones base
        val phase1Configs = mapOf(
            baseline.name to baseline,
            aggressive.name to aggressive,
            defensive.name to defensive,
            material.name to material,
            positional.name to positional,
            balanced.name to balanced,
        )

        println("\n=== PHASE 1: Testing base configurations ===")
        val phase1Results = runner.runRoundRobin(
            configs = phase1Configs,
            tournamentConfig = TournamentConfig(
                gamesPerMatch = 10,
                alternateColors = true,
                verbose = false,
                showProgress = true
            )
        )

        val phase1Winner = phase1Results.first()
        println("\nPhase 1 winner: ${phase1Winner.name}")

        // Fase 2: Optimizar el ganador
        println("\n=== PHASE 2: Fine-tuning winner ===")

        val winnerConfig = phase1Winner.config.copy(difficulty = secondPhaseDepth)

        val moreMaterial = winnerConfig.copy(
            name = "MoreMaterial",
            upgradedPieceScore = (winnerConfig.upgradedPieceScore * 1.2).toInt(),
            materialScore = (winnerConfig.materialScore * 1.2).toInt()
        )
        val moreCaptures = winnerConfig.copy(
            name = "MoreCaptures",
            captureUpgradedBonus = (winnerConfig.captureUpgradedBonus * 1.3).toInt(),
            captureNormalBonus = (winnerConfig.captureNormalBonus * 1.3).toInt()
        )
        val morePosition = winnerConfig.copy(
            name = "MorePosition",
            controlCenterScore = (winnerConfig.controlCenterScore * 1.5).toInt(),
            opponentBasePressureScore = (winnerConfig.opponentBasePressureScore * 1.3).toInt()
        )
        val newBalanced = winnerConfig.copy(
            name = "NewBalanced",
            upgradedPieceScore = (winnerConfig.upgradedPieceScore * 1.1).toInt(),
            captureUpgradedBonus = (winnerConfig.captureUpgradedBonus * 1.1).toInt(),
            controlCenterScore = (winnerConfig.controlCenterScore * 1.2).toInt(),
            mobilityScore = (winnerConfig.mobilityScore * 1.5).toInt()
        )

        // Fase 1: Probar configuraciones base
        val phase2Configs = mapOf(
            baseline.name to baseline,
            winnerConfig.name to winnerConfig,
            moreMaterial.name to moreMaterial,
            moreCaptures.name to moreCaptures,
            morePosition.name to morePosition,
            newBalanced.name to newBalanced,
        )

        val phase2Results = runner.runRoundRobin(
            configs = phase2Configs,
            tournamentConfig = TournamentConfig(
                gamesPerMatch = 10,
                alternateColors = true,
                verbose = false,
                showProgress = true
            )
        )

        val finalWinner = phase2Results.first()
        println("\n" + "=".repeat(60))
        println("FINAL OPTIMIZED CONFIGURATION")
        println("=".repeat(60))
        println("Name: ${finalWinner.name}")
        println("Win Rate: ${(finalWinner.winRate * 100).toInt()}%")
        println("Score: ${finalWinner.score} / ${finalWinner.totalGames}")
        println("\nConfiguration:")
        println(finalWinner.config)
        println("=".repeat(60))

        println("✓ Test passed: Full optimization completed")
    }

    // ==================== Test Rápido para CI ====================

    @Test
    fun testQuickSanityCheck() {
        println("\n========== TEST: Quick Sanity Check ==========")

        val result = runner.runSingleMatch(
            configA = ConfigBuilder.baseline().copy(difficulty = Difficulty.EASY),
            configB = ConfigBuilder.aggressive().copy(difficulty = Difficulty.EASY),
            tournamentConfig = TournamentConfig(
                gamesPerMatch = 5,
                alternateColors = true,
                verbose = false,
                showProgress = false
            ),
        )

        assert(result.totalGames == 5) { "Should play 5 games" }
        assert(result.winsA + result.winsB + result.draws == 5) { "Results should sum correctly" }
        assert(result.averageMoves > 0) { "Should have positive average moves" }

        println("✓ Test passed: Quick sanity check OK")
    }
}