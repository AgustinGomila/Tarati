package com.agustin.tarati.ui.screens.main

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import com.agustin.tarati.R
import com.agustin.tarati.game.core.Color
import com.agustin.tarati.game.core.GameResult
import com.agustin.tarati.game.core.GameState
import com.agustin.tarati.game.core.MatchState
import com.agustin.tarati.game.core.getColorStringResource
import com.agustin.tarati.game.logic.getMatchState

@Composable
fun MainScreenDialogs(
    gameState: GameState,
    isAnimating: Boolean,

    showGameOverDialog: Boolean,
    onGameOverConfirmed: () -> Unit,
    onGameOverDismissed: () -> Unit,

    showNewGameDialog: Boolean,
    onNewGameConfirmed: () -> Unit,
    onNewGameDismissed: () -> Unit,

    showAboutDialog: Boolean,
    onAboutDismissed: () -> Unit
) {
    if (showGameOverDialog) {
        val matchState = gameState.getMatchState()
        val winner = matchState.winner

        if (matchState.gameResult != GameResult.PLAYING &&
            matchState.gameResult != GameResult.UNDETERMINED &&
            winner != null
        ) {

            val message = buildGameOverMessage(matchState, winner)
            GameOverDialog(
                gameOverMessage = message,
                onConfirmed = onGameOverConfirmed,
                onDismissed = onGameOverDismissed,
            )
        }
    }

    if (showNewGameDialog && !isAnimating) {
        NewGameDialog(
            onConfirmed = onNewGameConfirmed,
            onDismissed = onNewGameDismissed,
        )
    }

    if (showAboutDialog && !isAnimating) {
        AboutDialog(onDismiss = onAboutDismissed)
    }
}

@Composable
fun buildGameOverMessage(matchState: MatchState, winner: Color): String {
    val context = LocalContext.current
    return when (matchState.gameResult) {
        GameResult.TRIPLE -> {
            String.format(
                context.getString(R.string.game_over_triple_repetition),
                context.getString(winner.getColorStringResource())
            )
        }

        GameResult.MIT -> {
            String.format(
                context.getString(R.string.game_over_wins),
                context.getString(winner.getColorStringResource())
            )
        }

        else -> {
            String.format(
                context.getString(R.string.game_over_stalemit),
                context.getString(winner.getColorStringResource())
            )
        }
    }
}