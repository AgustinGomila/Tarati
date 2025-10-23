package com.agustin.tarati.ui.screens.main

import com.agustin.tarati.game.core.Color
import com.agustin.tarati.game.core.Color.WHITE

class EditEvents(private val viewModel: MainViewModel) {
    fun toggleEditColor() = viewModel.toggleEditColor()
    fun toggleEditTurn() = viewModel.toggleEditTurn()
    fun togglePlayerSide() = viewModel.togglePlayerSide()
    fun rotateEditBoard() = viewModel.rotateEditBoard()
    fun clearEditBoard() = viewModel.clearEditBoard()
    fun startGameFromEditedState() = viewModel.startGameFromEditedState()
}

data class EditColorState(
    val playerSide: Color = WHITE,
    val editColor: Color = WHITE,
    val editTurn: Color = WHITE
)

data class EditActionState(
    val pieceCounts: PieceCounts = PieceCounts(4, 4),
    val isValidDistribution: Boolean = true,
    val isCompletedDistribution: Boolean = true
)

data class EditColorEvents(
    val onPlayerSideToggle: () -> Unit = { },
    val onColorToggle: () -> Unit = { },
    val onTurnToggle: () -> Unit = { }
)

data class EditActionEvents(
    val onRotate: () -> Unit = { },
    val onStartGame: () -> Unit = { },
    val onClearBoard: () -> Unit = { }
)