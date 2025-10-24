package com.agustin.tarati.ui.screens.main

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.agustin.tarati.R
import com.agustin.tarati.game.core.Color
import com.agustin.tarati.game.core.GameResult
import com.agustin.tarati.game.core.GameState
import com.agustin.tarati.game.core.MatchState
import com.agustin.tarati.game.core.getColorStringResource
import com.agustin.tarati.game.logic.getMatchState
import com.agustin.tarati.ui.localization.LocalizedText

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
    onShowTutorial: () -> Unit,
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
        AboutDialog(onDismiss = onAboutDismissed, onShowTutorial = onShowTutorial)
    }
}

@Composable
fun AboutDialog(onDismiss: () -> Unit = { }, onShowTutorial: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { LocalizedText(id = (R.string.about_tarati)) },
        text = {
            Column {
                LocalizedText(
                    id = R.string.tarati_is_a_strategic_board_game_created_by_george_spencer_brown,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                LocalizedText(
                    id = (R.string.game_rules),
                    style = MaterialTheme.typography.titleSmall,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
                LocalizedText(
                    id = (R.string.players_2_white_vs_black_objective_control_the_board),
                    style = MaterialTheme.typography.bodySmall
                )

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = onShowTutorial,
                    modifier = Modifier.fillMaxWidth(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.tertiary,
                        contentColor = MaterialTheme.colorScheme.onTertiary
                    )
                ) {
                    LocalizedText(R.string.show_tutorial)
                }

                Spacer(modifier = Modifier.height(12.dp))

                LocalizedText(
                    id = (R.string.credits),
                    style = MaterialTheme.typography.titleSmall,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
                LocalizedText(
                    id = (R.string.original_concept_george_spencer_brown),
                    style = MaterialTheme.typography.bodySmall
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss
            ) {
                LocalizedText(id = (R.string.close))
            }
        }
    )
}

@Composable
fun GameOverDialog(
    gameOverMessage: String,
    onConfirmed: () -> Unit,
    onDismissed: () -> Unit = {},
) {
    AlertDialog(
        onDismissRequest = onDismissed,
        title = { LocalizedText(id = (R.string.game_over)) },
        text = { Text(gameOverMessage) },
        confirmButton = {
            Button(
                onClick = onConfirmed
            ) {
                LocalizedText(id = (R.string.new_game))
            }
        },
        dismissButton = {
            Button(
                onClick = onDismissed
            ) {
                LocalizedText(id = (R.string.continue_))
            }
        }
    )
}

@Composable
fun NewGameDialog(onConfirmed: () -> Unit, onDismissed: () -> Unit = { }) {
    AlertDialog(
        onDismissRequest = onDismissed,
        title = { LocalizedText(id = (R.string.new_game)) },
        text = { LocalizedText(id = (R.string.are_you_sure_you_want_to_start_a_new_game)) },
        confirmButton = {
            Button(
                onClick = { onConfirmed() }
            ) {
                LocalizedText(R.string.yes)
            }
        },
        dismissButton = {
            Button(
                onClick = onDismissed
            ) {
                LocalizedText(R.string.cancel)
            }
        }
    )
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

// region Previews

@Preview(name = "About Dialog", showBackground = true)
@Composable
fun PreviewAboutDialog() {
    MaterialTheme {
        AboutDialog(onDismiss = {}, onShowTutorial = {})
    }
}

// endregion Previews