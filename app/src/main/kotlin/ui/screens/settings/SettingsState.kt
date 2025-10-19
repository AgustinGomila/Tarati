package com.agustin.tarati.ui.screens.settings

import com.agustin.tarati.game.ai.Difficulty
import com.agustin.tarati.ui.localization.AppLanguage
import com.agustin.tarati.ui.theme.AppTheme

data class BoardState(
    val labelsVisibles: Boolean = true,
    val verticesVisibles: Boolean = true,
)

data class SettingsState(
    val appTheme: AppTheme = AppTheme.MODE_AUTO,
    val difficulty: Difficulty = Difficulty.DEFAULT,
    val language: AppLanguage = AppLanguage.SPANISH,
    val tutorialButtonVisible: Boolean = true,
    val boardState: BoardState = BoardState(),
)