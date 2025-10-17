package com.agustin.tarati.game.ai

/**
 * Configurable evaluation parameters for Tarati AI.
 */
data class EvaluationConfig(
    val name: String = Difficulty.DEFAULT.name,
    val difficulty: Difficulty = Difficulty.DEFAULT,

    // Material
    val materialScore: Int = 144,
    val upgradedPieceScore: Int = 300,

    // Capture
    val captureNormalBonus: Int = 70,
    val captureUpgradedBonus: Int = 200,

    // Strategic
    val opponentBasePressureScore: Int = 40,
    val controlCenterScore: Int = 42,
    val homeBaseControlScore: Int = 30,

    // Tactic
    val mobilityScore: Int = 10,
    val upgradeScore: Int = 80,

    // Extras
    val winningScore: Double = 1_000_000.0,
    val quickThreatWeight: Int = 15,
    val winningThreshold: Float = 0.9f,
    val winningPositionThreshold: Float = 0.5f,

    // Decisions factors
    val immediateWinBonusMultiplier: Float = 2.0f,
    val repetitionPenaltyMultiplier: Float = 10.0f,

    // Parallelization
    val parallelSearch: Boolean = true,
    val maxConcurrentThreads: Int = maxOf(2, Runtime.getRuntime().availableProcessors() - 1),
    val parallelizationThreshold: Int = 6
) {
    companion object {
        val DEFAULT = EvaluationConfig()

        val EASY = EvaluationConfig(
            name = Difficulty.EASY.name,
            difficulty = Difficulty.EASY
        )

        val MEDIUM = EvaluationConfig(
            name = Difficulty.MEDIUM.name,
            difficulty = Difficulty.MEDIUM,
            materialScore = 180,
            upgradedPieceScore = 345,
            captureNormalBonus = 77,
            controlCenterScore = 33,
            mobilityScore = 9
        )

        val HARD = EvaluationConfig(
            name = Difficulty.HARD.name,
            difficulty = Difficulty.HARD,
            materialScore = 180,
            upgradedPieceScore = 379,
            captureNormalBonus = 77,
            captureUpgradedBonus = 220,
            controlCenterScore = 39,
            mobilityScore = 13
        )

        val CHAMPION = EvaluationConfig(
            name = Difficulty.CHAMPION.name,
            difficulty = Difficulty.CHAMPION,
            materialScore = 216,
            upgradedPieceScore = 454,
            captureNormalBonus = 77,
            captureUpgradedBonus = 220,
            controlCenterScore = 39,
            mobilityScore = 13
        )

        fun getByDifficulty(difficulty: Difficulty): EvaluationConfig = when (difficulty) {
            Difficulty.EASY -> EASY
            Difficulty.MEDIUM -> MEDIUM
            Difficulty.HARD -> HARD
            Difficulty.CHAMPION -> CHAMPION
        }
    }
}