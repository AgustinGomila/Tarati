package com.agustin.tarati.ui.screens.main

import com.agustin.tarati.game.core.Color
import com.agustin.tarati.game.core.Color.WHITE

data class ColorControlsState(
    val playerSide: Color = WHITE,
    val editColor: Color = WHITE,
    val editTurn: Color = WHITE
)

data class ActionControlsState(
    val pieceCounts: PieceCounts = PieceCounts(4, 4),
    val isValidDistribution: Boolean = true,
    val isCompletedDistribution: Boolean = true
)