package com.agustin.tarati.ui.screens.main

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.agustin.tarati.R
import com.agustin.tarati.game.core.Color
import com.agustin.tarati.game.core.GameResult
import com.agustin.tarati.game.core.GameState
import com.agustin.tarati.game.core.MatchState
import com.agustin.tarati.game.core.getColorStringResource
import com.agustin.tarati.game.logic.getMatchState
import com.agustin.tarati.ui.localization.LocalizedText
import com.agustin.tarati.ui.localization.localizedString

@Composable
fun MainScreenDialogs(
    gameState: GameState,
    isAnimating: Boolean,

    showGameOverDialog: Boolean,
    onGameOverConfirmed: () -> Unit,
    onGameOverDismissed: () -> Unit,

    showNewGameDialog: Boolean,
    onNewGameConfirmed: (color: Color) -> Unit,
    onNewGameDismissed: () -> Unit,
    attemptNewGameColor: Color,

    showAboutDialog: Boolean,
    onShowTutorial: () -> Unit,
    onAboutDismissed: () -> Unit,
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
            onConfirmed = { onNewGameConfirmed(attemptNewGameColor) },
            onDismissed = onNewGameDismissed,
        )
    }

    if (showAboutDialog && !isAnimating) {
        AboutDialog(onDismiss = onAboutDismissed, onShowTutorial = onShowTutorial)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutDialog(
    onDismiss: () -> Unit = {},
    onShowTutorial: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface,
        tonalElevation = 8.dp,
        dragHandle = {
            // Drag handle por defecto
            BottomSheetDefaults.DragHandle()
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp)
        ) {
            // Header
            AboutHeader()

            Spacer(modifier = Modifier.height(16.dp))

            // Contenido principal con scroll
            AboutContent(onShowTutorial)

            // Actions - Siempre fijas en la parte inferior
            AboutActions(onDismiss)
        }
    }
}

@Composable
private fun AboutHeader() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(
            imageVector = Icons.Default.Info,
            contentDescription = null,
            modifier = Modifier.size(24.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Text(
            text = localizedString(R.string.about_tarati),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
fun AboutContent(onShowTutorial: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = localizedString(R.string.tarati_is_a_strategic_board_game_created_by_george_spencer_brown),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
            lineHeight = MaterialTheme.typography.bodyMedium.lineHeight * 1.2
        )

        // Game Rules Section
        AboutGameRules()

        // Tutorial Button
        AboutTutorial(onShowTutorial)

        // Credits Section
        AboutCredits()
    }
}

@Composable
fun AboutActions(onDismiss: () -> Unit = {}) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp),
        horizontalArrangement = Arrangement.End
    ) {
        TextButton(
            onClick = onDismiss,
            shape = RoundedCornerShape(10.dp)
        ) {
            Text(
                text = localizedString(R.string.close),
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
fun AboutGameRules() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = localizedString(R.string.game_rules).uppercase(),
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Text(
                text = localizedString(R.string.players_2_white_vs_black_objective_control_the_board),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
fun AboutTutorial(onShowTutorial: () -> Unit) {
    FilledTonalButton(
        onClick = onShowTutorial,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
    ) {
        Icon(
            imageVector = Icons.Default.PlayArrow,
            contentDescription = null,
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = localizedString(R.string.show_tutorial),
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
fun AboutCredits() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = localizedString(R.string.credits).uppercase(),
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Text(
                text = localizedString(R.string.original_concept_george_spencer_brown),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
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
                onClick = onConfirmed
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