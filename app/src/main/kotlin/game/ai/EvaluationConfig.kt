package com.agustin.tarati.game.ai

/**
 * Configurable evaluation parameters for Tarati AI.
 * Añadir más campos aquí para exponer más parámetros (p. ej. tablas de valores por vértice).
 *  */
data class EvaluationConfig(
    val name: String = "Default",
    val winningScore: Double = 1_000_000.0,
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
    val quickThreatWeight: Int = 15,
    val winningThreshold: Float = 0.9f,
    val winningPositionThreshold: Float = 0.5f,

    // Decisions factors
    val immediateWinBonusMultiplier: Float = 2.0f,
    val repetitionPenaltyMultiplier: Float = 10.0f,
)