package com.agustin.tarati.ui.components.sidebar

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
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material.icons.outlined.SmartToy
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.agustin.tarati.R
import com.agustin.tarati.game.ai.Difficulty
import com.agustin.tarati.game.ai.TaratiAI.getWinner
import com.agustin.tarati.game.core.Color
import com.agustin.tarati.game.core.Color.BLACK
import com.agustin.tarati.game.core.Color.WHITE
import com.agustin.tarati.game.core.GameState
import com.agustin.tarati.game.core.Move
import com.agustin.tarati.game.core.getColorStringResource
import com.agustin.tarati.ui.localization.LocalizedText
import com.agustin.tarati.ui.localization.localizedString
import com.agustin.tarati.ui.preview.customGameState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Sidebar(
    modifier: Modifier = Modifier,
    gameState: GameState,
    playerSide: Color,
    currentMoveIndex: Int,
    moveHistory: List<Move>,
    onMoveToCurrent: () -> Unit,
    onUndo: () -> Unit,
    onRedo: () -> Unit,
    difficulty: Difficulty,
    onDifficultyChange: (Difficulty) -> Unit,
    isAIEnabled: Boolean,
    onToggleAI: () -> Unit,
    onSettings: () -> Unit,
    onNewGame: (Color) -> Unit,
    onEditBoard: () -> Unit,
    onAboutClick: () -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .width(280.dp)
            .fillMaxHeight()
            .background(MaterialTheme.colorScheme.surface)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Título de la Aplicación
        LocalizedText(
            id = R.string.tarati,
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onSurface
        )

        SettingsButton(onSettings)

        // Controles para iniciar una nueva partida
        GameSettingsControls(
            playerSide = playerSide,
            onNewGame = { onNewGame(it) },
            onEditBoard = onEditBoard
        )

        // Sección de IA y Dificultad en una fila
        AIDifficultyControls(
            playerSide = playerSide,
            expanded = expanded,
            onExpandedChange = { expanded = !expanded },
            difficulty = difficulty,
            onDifficultyChange = onDifficultyChange,
            isAIEnabled = isAIEnabled,
            onToggleAI = onToggleAI,
        )

        // Indicador de turno con información del jugador
        GameStateIndicator(gameState, playerSide)

        // Controles del historial de la partida
        HistorialControls(currentMoveIndex, moveHistory, onUndo, onRedo, onMoveToCurrent)

        // Espacio flexible para empujar el About hacia abajo
        Spacer(modifier = Modifier.weight(1f))

        // Botón About en la parte inferior
        AboutButton(onAboutClick)
    }
}

@Composable
fun SettingsButton(onSettings: () -> Unit) {
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
}

@Composable
fun GameSettingsControls(playerSide: Color, onNewGame: (Color) -> Unit, onEditBoard: () -> Unit) {
    LocalizedText(
        id = R.string.new_game,
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Button(
            onClick = { onNewGame(WHITE) },
            modifier = Modifier.weight(1f),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (playerSide == WHITE)
                    MaterialTheme.colorScheme.primary
                else
                    MaterialTheme.colorScheme.surfaceVariant,
                contentColor = if (playerSide == WHITE)
                    MaterialTheme.colorScheme.onPrimary
                else
                    MaterialTheme.colorScheme.onSurfaceVariant
            )
        ) {
            LocalizedText(R.string.w)
        }

        Button(
            onClick = { onNewGame(BLACK) },
            modifier = Modifier.weight(1f),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (playerSide == BLACK)
                    MaterialTheme.colorScheme.primary
                else
                    MaterialTheme.colorScheme.surfaceVariant,
                contentColor = if (playerSide == BLACK)
                    MaterialTheme.colorScheme.onPrimary
                else
                    MaterialTheme.colorScheme.onSurfaceVariant
            )
        ) {
            LocalizedText(R.string.b)
        }

        // Botón de Edición
        IconButton(
            onClick = onEditBoard,
            modifier = Modifier.size(48.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.Edit,
                contentDescription = stringResource(R.string.edit),
                tint = MaterialTheme.colorScheme.tertiary,
            )
        }
    }
}

@Composable
fun AboutButton(onAboutClick: () -> Unit) {
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
            contentDescription = localizedString(R.string.about),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.width(8.dp))
        LocalizedText(R.string.about_tarati)
    }
}

@Composable
fun HistorialControls(
    currentMoveIndex: Int,
    moveHistory: List<Move>,
    onUndo: () -> Unit,
    onRedo: () -> Unit,
    onMoveToCurrent: () -> Unit
) {
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
}

@Composable
fun GameStateIndicator(gameState: GameState, playerSide: Color) {
    val winner = getWinner(gameState)
    val relevantSide = winner ?: gameState.currentTurn

    val (color, backgroundColor) = when (relevantSide) {
        WHITE -> MaterialTheme.colorScheme.onPrimary to MaterialTheme.colorScheme.primary
        else -> MaterialTheme.colorScheme.onSecondary to MaterialTheme.colorScheme.secondary
    }

    val textToShow = when {
        winner != null -> {
            val resultText = when (playerSide) {
                winner -> R.string.you_won
                else -> R.string.you_lost
            }
            localizedString(resultText, localizedString(playerSide.getColorStringResource()))
        }

        else -> {
            val turnText = localizedString(gameState.currentTurn.getColorStringResource())
            val sideTurnText = localizedString(
                when (gameState.currentTurn) {
                    playerSide -> R.string.your_turn
                    else -> R.string.opponent_turn
                }
            )
            "$turnText: $sideTurnText"
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(backgroundColor),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = textToShow,
            color = color,
            style = MaterialTheme.typography.bodyLarge
        )
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AIDifficultyControls(
    playerSide: Color,
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    difficulty: Difficulty,
    onDifficultyChange: (Difficulty) -> Unit,
    isAIEnabled: Boolean,
    onToggleAI: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        // Selector de dificultad
        Column(
            modifier = Modifier.weight(1f)
        ) {
            LocalizedText(
                id = R.string.ai_difficulty,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(4.dp))
            DifficultySelector(expanded, onExpandedChange, difficulty, onDifficultyChange)
        }

        // Botón de toggle AI (icono solamente)
        Spacer(modifier = Modifier.width(8.dp))
        AIEnabledButton(playerSide, isAIEnabled, onToggleAI)
    }
}

@Composable
fun AIEnabledButton(playerSide: Color, isAIEnabled: Boolean, onToggleAI: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(
                color = if (playerSide == BLACK)
                    MaterialTheme.colorScheme.secondary
                else
                    MaterialTheme.colorScheme.primary,
            ),
        contentAlignment = Alignment.Center
    ) {
        IconButton(
            onClick = onToggleAI,
            modifier = Modifier.size(48.dp)
        ) {
            Icon(
                imageVector =
                    if (isAIEnabled) Icons.Filled.SmartToy
                    else Icons.Outlined.SmartToy,
                contentDescription = stringResource(
                    if (isAIEnabled) R.string.disable_ai
                    else R.string.enable_ai
                ),
                tint =
                    if (isAIEnabled) MaterialTheme.colorScheme.onPrimary
                    else MaterialTheme.colorScheme.outline
            )
        }
    }
}

@ExperimentalMaterial3Api
@Composable
fun DifficultySelector(
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    difficulty: Difficulty,
    onDifficultyChange: (Difficulty) -> Unit
) {
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = onExpandedChange
    ) {
        TextField(
            value = stringResource(difficulty.displayNameRes),
            onValueChange = {},
            readOnly = true,
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
            },
            modifier = Modifier
                .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                .fillMaxWidth()
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { onExpandedChange(false) }
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
                        onExpandedChange(false)
                    }
                )
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 280, heightDp = 800)
@Composable
fun SidebarPreview() {
    MaterialTheme {
        val exampleGameState = customGameState()
        val exampleMoveHistory = listOf(
            Move("C1", "B1"),
            Move("C7", "B4"),
            Move("B1", "A1"),
            Move("B4", "A1")
        )

        Sidebar(
            gameState = exampleGameState,
            playerSide = WHITE,
            currentMoveIndex = 2,
            moveHistory = exampleMoveHistory,
            onMoveToCurrent = { },
            onUndo = { },
            onRedo = { },
            difficulty = Difficulty.DEFAULT,
            onDifficultyChange = { },
            isAIEnabled = true,
            onToggleAI = { },
            onSettings = { },
            onNewGame = { },
            onEditBoard = { },
        ) { }
    }
}

@Preview(showBackground = true, widthDp = 280, heightDp = 800)
@Composable
fun SidebarPreview_Dark() {
    MaterialTheme(
        colorScheme = darkColorScheme()
    ) {
        val exampleGameState = customGameState()
        val exampleMoveHistory = listOf(
            Move("C1", "B1"),
            Move("C7", "B4")
        )

        Sidebar(
            gameState = exampleGameState,
            playerSide = BLACK,
            currentMoveIndex = 1,
            moveHistory = exampleMoveHistory,
            onMoveToCurrent = { },
            onUndo = { },
            onRedo = { },
            difficulty = Difficulty.HARD,
            onDifficultyChange = { },
            isAIEnabled = false,
            onToggleAI = { },
            onSettings = { },
            onNewGame = { },
            onEditBoard = { },
        ) { }
    }
}


@Preview(showBackground = true, widthDp = 280, heightDp = 800)
@Composable
fun SidebarPreview_CustomState() {
    MaterialTheme {
        val exampleGameState = customGameState()
        val exampleMoveHistory = listOf(
            Move("C1", "B1"),
            Move("C7", "B4"),
            Move("B1", "A1"),
            Move("B4", "A1")
        )

        Sidebar(
            gameState = exampleGameState,
            playerSide = WHITE,
            currentMoveIndex = 2,
            moveHistory = exampleMoveHistory,
            onMoveToCurrent = { },
            onUndo = { },
            onRedo = { },
            difficulty = Difficulty.MEDIUM,
            onDifficultyChange = { },
            isAIEnabled = true,
            onToggleAI = { },
            onSettings = { },
            onNewGame = { },
            onEditBoard = { },
        ) { }
    }
}