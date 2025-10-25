package com.agustin.tarati.ui.components.editor

import com.agustin.tarati.game.core.CobColor
import com.agustin.tarati.game.core.CobColor.WHITE
import com.agustin.tarati.ui.screens.main.MainViewModel

class EditEvents(private val viewModel: MainViewModel) {
    fun toggleEditColor() = viewModel.toggleEditColor()
    fun toggleEditTurn() = viewModel.toggleEditTurn()
    fun togglePlayerSide() = viewModel.togglePlayerSide()
    fun rotateEditBoard() = viewModel.rotateEditBoard()
    fun clearEditBoard() = viewModel.clearEditBoard()
    fun startGameFromEditedState() = viewModel.startGameFromEditedState()
}

data class EditColorState(
    val playerSide: CobColor = WHITE,
    val editColor: CobColor = WHITE,
    val editTurn: CobColor = WHITE
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