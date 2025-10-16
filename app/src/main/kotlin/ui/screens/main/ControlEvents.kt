package com.agustin.tarati.ui.screens.main

data class ColorControlsEvents(
    val onPlayerSideToggle: () -> Unit = { },
    val onColorToggle: () -> Unit = { },
    val onTurnToggle: () -> Unit = { }
)

data class ActionControlsEvents(
    val onRotate: () -> Unit = { },
    val onStartGame: () -> Unit = { },
    val onClearBoard: () -> Unit = { }
)