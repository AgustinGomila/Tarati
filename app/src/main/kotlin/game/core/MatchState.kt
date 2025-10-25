package com.agustin.tarati.game.core

data class MatchState(
    val gameState: GameState,
    val gameResult: GameResult,
    val winner: CobColor?,
    val moveHistory: Map<String, Int>
)