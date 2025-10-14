package com.agustin.tarati.game.ai

/**
 * Configurable evaluation parameters for Tarati AI.
 * Añadir más campos aquí para exponer más parámetros (p. ej. tablas de valores por vértice).
 *  */
data class EvaluationConfig(
    val winningScore: Double = 1_000_000.0,

    val upgradedPieceScore: Int = 200,
    val captureUpgradedBonus: Int = 150,
    val materialScore: Int = 100,
    val upgradeOpportunityScore: Int = 80,
    val captureNormalBonus: Int = 50,
    val opponentBasePressureScore: Int = 40,
    val controlCenterScore: Int = 30,
    val homeBaseControlScore: Int = 25,
    val mobilityScore: Int = 5,

    val quickThreatWeight: Int = 15,
    val winningThreshold: Float = 0.9f,
    val winningPositionThreshold: Float = 0.5f,

    val repetitionPenaltyMultiplier: Float = 10.0f,
    val immediateWinBonusMultiplier: Float = 2.0f,

    val name: String = "Default"
)