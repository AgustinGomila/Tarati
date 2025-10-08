package com.agustin.tarati.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.agustin.tarati.R
import com.agustin.tarati.game.Checker
import com.agustin.tarati.game.Color.BLACK
import com.agustin.tarati.game.Color.WHITE
import com.agustin.tarati.game.Difficulty
import com.agustin.tarati.game.GameState
import com.agustin.tarati.game.Move
import com.agustin.tarati.ui.localization.LocalizedText
import com.agustin.tarati.ui.localization.localizedString

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Sidebar(
    gameState: GameState,
    moveHistory: List<Move>,
    currentMoveIndex: Int,
    isAIEnabled: Boolean,
    difficulty: Difficulty,
    onSettings: () -> Unit,
    onNewGame: () -> Unit,
    onToggleAI: () -> Unit,
    onDifficultyChange: (Difficulty) -> Unit,
    onUndo: () -> Unit,
    onRedo: () -> Unit,
    onMoveToCurrent: () -> Unit,
    onAboutClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    val context = LocalContext.current

    Column(
        modifier = modifier
            .width(280.dp)
            .fillMaxHeight()
            .background(MaterialTheme.colorScheme.surface)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        LocalizedText(
            id = R.string.tarati,
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onSurface
        )

        Button(
            onClick = onSettings,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.tertiary,
                contentColor = MaterialTheme.colorScheme.onTertiary
            )
        ) {
            LocalizedText(R.string.settings)
        }

        Button(
            onClick = onNewGame,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            )
        ) {
            LocalizedText(id = R.string.new_game)
        }

        Button(
            onClick = onToggleAI,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.secondary,
                contentColor = MaterialTheme.colorScheme.onSecondary
            )
        ) {
            LocalizedText(
                if (isAIEnabled) R.string.disable_ai
                else R.string.enable_ai
            )
        }

        // Selector de dificultad
        Column {
            LocalizedText(
                id = R.string.ai_difficulty,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(4.dp))
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = !expanded }
            ) {
                TextField(
                    value = stringResource(difficulty.displayNameRes),
                    onValueChange = {},
                    readOnly = true,
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                    },
                    // Enfoque compatible - sin textFieldColors específico
                    modifier = Modifier
                        .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                        .fillMaxWidth()
                )

                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    Difficulty.ALL.forEach { difficultyOption ->
                        DropdownMenuItem(
                            text = {
                                LocalizedText(
                                    difficultyOption.displayNameRes,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            },
                            onClick = {
                                onDifficultyChange(difficultyOption)
                                expanded = false
                            }
                        )
                    }
                }
            }
        }

        // Indicador de turno
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(
                    if (gameState.currentTurn == BLACK)
                        MaterialTheme.colorScheme.secondary
                    else
                        MaterialTheme.colorScheme.primary
                )
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            val t = localizedString(R.string.current_turn)
            val t2 = localizedString(
                when (gameState.currentTurn) {
                    BLACK -> R.string.black
                    else -> R.string.white
                }
            )

            Text(
                String.format(t, t2),
                color = if (gameState.currentTurn == BLACK)
                    MaterialTheme.colorScheme.onSecondary
                else
                    MaterialTheme.colorScheme.onPrimary,
                style = MaterialTheme.typography.bodyLarge
            )
        }

        LocalizedText(
            id = R.string.move_history,
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onSurface
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = onUndo,
                enabled = currentMoveIndex > 0,
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                )
            ) {
                LocalizedText(R.string.back)
            }

            Button(
                onClick = onRedo,
                enabled = currentMoveIndex < moveHistory.size - 1,
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                )
            ) {
                LocalizedText(R.string.forward)
            }
        }

        if (currentMoveIndex != moveHistory.size - 1) {
            Button(
                onClick = onMoveToCurrent,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.tertiary,
                    contentColor = MaterialTheme.colorScheme.onTertiary
                )
            ) {
                LocalizedText(R.string.move_to_current)
            }
        }

        // Historial de movimientos
        Box(
            modifier = Modifier
                .height(200.dp) // Altura fija para el historial
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
        ) {
            val scrollState = rememberScrollState()

            Column(
                modifier = Modifier
                    .verticalScroll(scrollState)
                    .padding(12.dp)
            ) {
                moveHistory.reversed().forEachIndexed { index, move ->
                    val actualIndex = moveHistory.size - 1 - index
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(vertical = 4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(12.dp)
                                .clip(RoundedCornerShape(3.dp))
                                .background(
                                    if (actualIndex == currentMoveIndex)
                                        MaterialTheme.colorScheme.primary
                                    else
                                        MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                                )
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "${actualIndex + 1} · ${move.from} → ${move.to}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }

        // Espacio flexible para empujar el About hacia abajo
        Spacer(modifier = Modifier.weight(1f))

        // Botón About en la parte inferior
        TextButton(
            onClick = onAboutClick,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            colors = ButtonDefaults.textButtonColors(
                contentColor = MaterialTheme.colorScheme.primary
            )
        ) {
            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = context.getString(R.string.about),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(8.dp))
            LocalizedText(R.string.about_tarati)
        }
    }
}

// Función auxiliar para crear un tema oscuro en los previews
private fun darkColorScheme() = androidx.compose.material3.darkColorScheme()

@Preview(showBackground = true, widthDp = 280, heightDp = 800)
@Composable
fun SidebarPreview() {
    MaterialTheme {
        val exampleGameState = GameState(
            checkers = mapOf(
                "C1" to Checker(WHITE, false),
                "C2" to Checker(WHITE, false),
                "D1" to Checker(WHITE, false),
                "D2" to Checker(WHITE, false),
                "C7" to Checker(BLACK, false),
                "C8" to Checker(BLACK, false),
                "D3" to Checker(BLACK, false),
                "D4" to Checker(BLACK, false)
            ),
            currentTurn = WHITE
        )

        val exampleMoveHistory = listOf(
            Move("C1", "B1"),
            Move("C7", "B4"),
            Move("B1", "A1"),
            Move("B4", "A1")
        )

        Sidebar(
            gameState = exampleGameState,
            moveHistory = exampleMoveHistory,
            currentMoveIndex = 2,
            isAIEnabled = true,
            difficulty = Difficulty.DEFAULT,
            onSettings = { },
            onNewGame = { },
            onToggleAI = { },
            onDifficultyChange = { },
            onUndo = { },
            onRedo = { },
            onMoveToCurrent = { },
            onAboutClick = { }
        )
    }
}

@Preview(showBackground = true, widthDp = 280, heightDp = 800)
@Composable
fun SidebarPreview_Dark() {
    MaterialTheme(
        colorScheme = darkColorScheme()
    ) {
        val exampleGameState = GameState(
            checkers = mapOf(
                "C1" to Checker(WHITE, false),
                "C2" to Checker(WHITE, false),
                "B1" to Checker(WHITE, false),
                "C7" to Checker(BLACK, false),
                "C8" to Checker(BLACK, false),
                "B4" to Checker(BLACK, false)
            ),
            currentTurn = BLACK
        )

        val exampleMoveHistory = listOf(
            Move("C1", "B1"),
            Move("C7", "B4")
        )

        Sidebar(
            gameState = exampleGameState,
            moveHistory = exampleMoveHistory,
            currentMoveIndex = 1,
            isAIEnabled = false,
            difficulty = Difficulty.HARD,
            onSettings = { },
            onNewGame = { },
            onToggleAI = { },
            onDifficultyChange = { },
            onUndo = { },
            onRedo = { },
            onMoveToCurrent = { },
            onAboutClick = { }
        )
    }
}