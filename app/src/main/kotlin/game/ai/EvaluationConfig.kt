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

    // Parámetros auxiliares
    val quickThreatWeight: Int = 15
)