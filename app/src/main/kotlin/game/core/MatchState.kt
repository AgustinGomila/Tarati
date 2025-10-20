package com.agustin.tarati.game.core

data class MatchState(
    val gameState: GameState,
    val gameResult: GameResult,
    val winner: Color?,
    val moveHistory: Map<String, Int>
)