@file:Suppress("AssignedValueIsNeverRead")
@file:OptIn(ExperimentalMaterial3Api::class)

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lens
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material.icons.filled.SquareFoot
import androidx.compose.material.icons.outlined.SmartToy
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.agustin.tarati.R
import com.agustin.tarati.game.ai.Difficulty
import com.agustin.tarati.game.core.Color
import com.agustin.tarati.game.core.Color.BLACK
import com.agustin.tarati.game.core.Color.WHITE
import com.agustin.tarati.game.core.GameState
import com.agustin.tarati.game.core.Move
import com.agustin.tarati.game.core.getColorStringResource
import com.agustin.tarati.game.logic.getWinner
import com.agustin.tarati.ui.helpers.customGameState
import com.agustin.tarati.ui.localization.localizedString

data class SidebarGameState(
    val gameState: GameState,
    val playerSide: Color,
    val moveIndex: Int,
    val moveHistory: List<Move>,
    val difficulty: Difficulty,
    val isAIEnabled: Boolean,
)

data class SidebarUIState(
    val isDifficultyExpanded: Boolean = false
)

interface SidebarEvents {
    fun onMoveToCurrent()
    fun onUndo()
    fun onRedo()
    fun onDifficultyChange(difficulty: Difficulty)
    fun onToggleAI()
    fun onSettings()
    fun onNewGame(color: Color)
    fun onEditBoard()
    fun onAboutClick()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Sidebar(
    modifier: Modifier = Modifier,
    sidebarState: SidebarGameState,
    uiState: SidebarUIState = SidebarUIState(),
    events: SidebarEvents,
    onUIStateChange: (SidebarUIState) -> Unit = {}
) {
    Surface(
        modifier = modifier
            .width(300.dp)
            .fillMaxHeight(),
        tonalElevation = 8.dp,
        shape = RoundedCornerShape(topEnd = 16.dp, bottomEnd = 16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header con título y configuración
            SidebarHeader(onSettings = events::onSettings)

            // Estado del juego
            GameStatusCard(
                gameState = sidebarState.gameState,
                playerSide = sidebarState.playerSide
            )

            // Controles de juego
            GameControlsSection(
                playerSide = sidebarState.playerSide,
                onNewGame = events::onNewGame,
                onEditBoard = events::onEditBoard,
            )

            // Configuración de IA
            AIConfigurationCard(
                playerSide = sidebarState.playerSide,
                expanded = uiState.isDifficultyExpanded,
                onExpandedChange = { expanded ->
                    onUIStateChange(uiState.copy(isDifficultyExpanded = expanded))
                },
                difficulty = sidebarState.difficulty,
                onDifficultyChange = events::onDifficultyChange,
                isAIEnabled = sidebarState.isAIEnabled,
                onToggleAI = events::onToggleAI,
            )

            // Historial de movimientos
            MoveHistorySection(
                modifier = Modifier.weight(1f),
                currentMoveIndex = sidebarState.moveIndex,
                moveHistory = sidebarState.moveHistory,
                onUndo = events::onUndo,
                onRedo = events::onRedo,
                onMoveToCurrent = events::onMoveToCurrent,
            )

            // Footer
            AboutFooter(onAboutClick = events::onAboutClick)
        }
    }
}

@Composable
private fun SidebarHeader(onSettings: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = localizedString(R.string.tarati),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        IconButton(
            onClick = onSettings,
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Icon(
                imageVector = Icons.Filled.Settings,
                contentDescription = stringResource(R.string.settings),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun GameStatusCard(gameState: GameState, playerSide: Color) {
    val winner = gameState.getWinner()
    val relevantSide = winner ?: gameState.currentTurn

    val backgroundColor = when (relevantSide) {
        WHITE -> MaterialTheme.colorScheme.primaryContainer
        else -> MaterialTheme.colorScheme.secondaryContainer
    }

    val textColor = when (relevantSide) {
        WHITE -> MaterialTheme.colorScheme.onPrimaryContainer
        else -> MaterialTheme.colorScheme.onSecondaryContainer
    }

    val statusText = when {
        winner != null -> {
            val resultText = when (playerSide) {
                winner -> R.string.you_won
                else -> R.string.you_lost
            }
            localizedString(resultText, localizedString(winner.getColorStringResource()))
        }

        else -> {
            val turnText = localizedString(gameState.currentTurn.getColorStringResource())
            val sideTurnText = localizedString(
                when (gameState.currentTurn) {
                    playerSide -> R.string.your_turn
                    else -> R.string.opponent_turn
                }
            )
            "$turnText • $sideTurnText"
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Text(
            text = statusText,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium,
            color = textColor,
            modifier = Modifier.padding(16.dp)
        )
    }
}

@Composable
private fun GameControlsSection(
    playerSide: Color,
    onNewGame: (color: Color) -> Unit,
    onEditBoard: () -> Unit,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = localizedString(R.string.new_game).uppercase(),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.Medium
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            ColorChoiceButton(
                color = WHITE,
                isSelected = playerSide == WHITE,
                onClick = { onNewGame(WHITE) },
                modifier = Modifier.weight(1f)
            )

            ColorChoiceButton(
                color = BLACK,
                isSelected = playerSide == BLACK,
                onClick = { onNewGame(BLACK) },
                modifier = Modifier.weight(1f)
            )

            IconButton(
                onClick = onEditBoard,
                modifier = Modifier
                    .align(Alignment.CenterVertically)
                    .size(54.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Icon(
                    imageVector = Icons.Filled.SquareFoot,
                    contentDescription = stringResource(R.string.edit),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun ColorChoiceButton(
    color: Color,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val backgroundColor = when (color) {
        WHITE -> if (isSelected) MaterialTheme.colorScheme.primary
        else MaterialTheme.colorScheme.surfaceVariant

        else -> if (isSelected) MaterialTheme.colorScheme.secondary
        else MaterialTheme.colorScheme.surfaceVariant
    }

    val tintColor = when (color) {
        WHITE -> if (isSelected) MaterialTheme.colorScheme.onPrimary
        else MaterialTheme.colorScheme.onSurfaceVariant

        else -> if (isSelected) MaterialTheme.colorScheme.onSecondary
        else MaterialTheme.colorScheme.onSurfaceVariant
    }

    IconButton(
        onClick = onClick,
        modifier = modifier
            .size(54.dp)
            .fillMaxHeight(1f)
            .clip(RoundedCornerShape(12.dp))
            .background(backgroundColor),
    ) {
        Icon(
            imageVector = if (color == WHITE) Icons.Filled.Lens else Icons.Filled.Lens,
            contentDescription = stringResource(R.string.edit),
            tint = tintColor
        )
    }
}

@Composable
private fun AIConfigurationCard(
    playerSide: Color,
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    difficulty: Difficulty,
    onDifficultyChange: (Difficulty) -> Unit,
    isAIEnabled: Boolean,
    onToggleAI: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = localizedString(R.string.ai_opponent),
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                AIToggleButton(
                    isAIEnabled = isAIEnabled,
                    onToggle = onToggleAI
                )
            }

            if (isAIEnabled) {
                DifficultySelector(
                    expanded = expanded,
                    onExpandedChange = onExpandedChange,
                    difficulty = difficulty,
                    onDifficultyChange = onDifficultyChange
                )
            }
        }
    }
}

@Composable
private fun AIToggleButton(
    isAIEnabled: Boolean,
    onToggle: () -> Unit
) {
    IconButton(
        onClick = onToggle,
        modifier = Modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(
                if (isAIEnabled) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.surface
            )
    ) {
        Icon(
            imageVector = if (isAIEnabled) Icons.Filled.SmartToy else Icons.Outlined.SmartToy,
            contentDescription = stringResource(
                if (isAIEnabled) R.string.disable_ai else R.string.enable_ai
            ),
            tint = if (isAIEnabled) MaterialTheme.colorScheme.onPrimary
            else MaterialTheme.colorScheme.outline,
            modifier = Modifier.size(20.dp)
        )
    }
}

@Composable
private fun MoveHistorySection(
    modifier: Modifier,
    currentMoveIndex: Int,
    moveHistory: List<Move>,
    onUndo: () -> Unit,
    onRedo: () -> Unit,
    onMoveToCurrent: () -> Unit,
) {
    Column(
        modifier = modifier.fillMaxHeight(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = localizedString(R.string.move_history).uppercase(),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.Medium
        )

        // Controles de navegación
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedButton(
                onClick = onUndo,
                enabled = currentMoveIndex > 0,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(10.dp)
            ) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text("Undo")
            }

            OutlinedButton(
                onClick = onRedo,
                enabled = currentMoveIndex < moveHistory.size - 1,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Redo")
                Spacer(modifier = Modifier.width(4.dp))
                Icon(
                    Icons.AutoMirrored.Filled.ArrowForward,
                    null,
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        // Lista de movimientos
        Card(
            modifier = modifier
                .fillMaxWidth()
                .weight(1f, false),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight()
                    .height(200.dp)
                    .padding(8.dp)
            ) {
                itemsIndexed(moveHistory.chunked(2)) { index, moves ->
                    val moveNumber = index + 1
                    val whiteMove = moves.firstOrNull()
                    val blackMove = moves.getOrNull(1)

                    MoveHistoryRow(
                        moveNumber = moveNumber,
                        whiteMove = whiteMove,
                        blackMove = blackMove,
                        currentMoveIndex = currentMoveIndex,
                        rowIndex = index,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )

                    if (index < moveHistory.chunked(2).size - 1) {
                        HorizontalDivider(
                            modifier = Modifier.padding(horizontal = 8.dp),
                            thickness = 0.5.dp,
                            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                        )
                    }
                }
            }
        }

        // Botón para volver al final
        if (currentMoveIndex != moveHistory.size - 1) {
            Button(
                onClick = onMoveToCurrent,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Jump to Current Position")
            }
        }
    }
}

@Composable
private fun MoveHistoryRow(
    moveNumber: Int,
    whiteMove: Move?,
    blackMove: Move?,
    currentMoveIndex: Int,
    rowIndex: Int,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // Número del movimiento
        Text(
            text = "$moveNumber.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.width(24.dp)
        )

        // Movimiento blanco
        MoveText(
            move = whiteMove,
            isCurrent = currentMoveIndex == rowIndex * 2,
            modifier = Modifier.weight(1f)
        )

        // Movimiento negro
        MoveText(
            move = blackMove,
            isCurrent = currentMoveIndex == rowIndex * 2 + 1,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun MoveText(
    move: Move?,
    isCurrent: Boolean,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.CenterStart
    ) {
        if (move != null) {
            Text(
                text = "${move.from}→${move.to}",
                style = MaterialTheme.typography.bodySmall,
                color = if (isCurrent) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.onSurface
                },
                fontWeight = if (isCurrent) FontWeight.Medium else FontWeight.Normal
            )
        }
    }
}

@Composable
private fun AboutFooter(onAboutClick: () -> Unit) {
    TextButton(
        onClick = onAboutClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Icon(
            imageVector = Icons.Default.Info,
            contentDescription = localizedString(R.string.about),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = localizedString(R.string.about_tarati),
            color = MaterialTheme.colorScheme.primary
        )
    }
}

@ExperimentalMaterial3Api
@Composable
private fun DifficultySelector(
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
                .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable, true)
                .fillMaxWidth(),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                disabledContainerColor = MaterialTheme.colorScheme.surface,
            ),
            shape = RoundedCornerShape(10.dp)
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { onExpandedChange(false) }
        ) {
            Difficulty.ALL.forEach { difficultyOption ->
                DropdownMenuItem(
                    text = {
                        Text(
                            text = stringResource(difficultyOption.displayNameRes),
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

// region Previews

class PreviewSidebarEvents : SidebarEvents {
    override fun onMoveToCurrent() {}
    override fun onUndo() {}
    override fun onRedo() {}
    override fun onDifficultyChange(difficulty: Difficulty) {}
    override fun onToggleAI() {}
    override fun onSettings() {}
    override fun onNewGame(color: Color) {}
    override fun onEditBoard() {}
    override fun onAboutClick() {}
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

        val sidebarGameState = SidebarGameState(
            gameState = exampleGameState,
            playerSide = WHITE,
            moveIndex = 2,
            moveHistory = exampleMoveHistory,
            difficulty = Difficulty.DEFAULT,
            isAIEnabled = true,
        )

        Sidebar(
            sidebarState = sidebarGameState,
            events = PreviewSidebarEvents()
        )
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
            Move("C1", "B1"), Move("C7", "B4"),
            Move("C1", "B1"), Move("C7", "B4"),
            Move("C1", "B1"), Move("C7", "B4"),
            Move("C1", "B1"), Move("C7", "B4"),
            Move("C1", "B1"), Move("C7", "B4"),
            Move("C1", "B1"), Move("C7", "B4"),
            Move("C1", "B1"), Move("C7", "B4"),
            Move("C1", "B1"), Move("C7", "B4"),
            Move("C1", "B1"), Move("C7", "B4"),
            Move("C1", "B1"), Move("C7", "B4"),
            Move("C1", "B1"), Move("C7", "B4"),
            Move("C1", "B1"), Move("C7", "B4"),
            Move("C1", "B1"), Move("C7", "B4"),
            Move("C1", "B1"), Move("C7", "B4"),
            Move("C1", "B1"), Move("C7", "B4"),
            Move("C1", "B1"), Move("C7", "B4"),
            Move("C1", "B1"), Move("C7", "B4"),
            Move("C1", "B1"), Move("C7", "B4"),
        )
        val sidebarGameState = SidebarGameState(
            gameState = exampleGameState,
            playerSide = BLACK,
            moveIndex = 1,
            moveHistory = exampleMoveHistory,
            difficulty = Difficulty.HARD,
            isAIEnabled = false,
        )

        Sidebar(
            sidebarState = sidebarGameState,
            events = PreviewSidebarEvents()
        )
    }
}

@Preview(showBackground = true, widthDp = 280, heightDp = 800)
@Composable
fun SidebarPreview_ExpandedDropdown() {
    MaterialTheme {
        val exampleGameState = customGameState()
        val exampleMoveHistory = listOf(
            Move("C1", "B1"),
            Move("C7", "B4")
        )

        val sidebarGameState = SidebarGameState(
            gameState = exampleGameState,
            playerSide = WHITE,
            moveIndex = 1,
            moveHistory = exampleMoveHistory,
            difficulty = Difficulty.DEFAULT,
            isAIEnabled = true,
        )

        var uiState by remember { mutableStateOf(SidebarUIState(isDifficultyExpanded = true)) }

        Sidebar(
            sidebarState = sidebarGameState,
            uiState = uiState,
            events = PreviewSidebarEvents(),
            onUIStateChange = { uiState = it }
        )
    }
}

// endregion Previews