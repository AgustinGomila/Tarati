package com.agustin.tarati.ui.components.editor

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.RotateRight
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.BottomCenter
import androidx.compose.ui.Alignment.Companion.CenterEnd
import androidx.compose.ui.Alignment.Companion.CenterStart
import androidx.compose.ui.Alignment.Companion.TopCenter
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.agustin.tarati.R
import com.agustin.tarati.game.core.CobColor
import com.agustin.tarati.game.core.CobColor.BLACK
import com.agustin.tarati.game.core.CobColor.WHITE
import com.agustin.tarati.ui.localization.LocalizedText
import com.agustin.tarati.ui.localization.localizedString


@Composable
fun EditControls(
    isLandscapeScreen: Boolean,
    colorState: EditColorState,
    actionState: EditActionState,
    editEvents: EditEvents,
) {
    val editColorEvents = EditColorEvents(
        onPlayerSideToggle = editEvents::togglePlayerSide,
        onColorToggle = editEvents::toggleEditColor,
        onTurnToggle = editEvents::toggleEditTurn
    )
    val editActionEvents = EditActionEvents(
        onRotate = editEvents::rotateEditBoard,
        onStartGame = editEvents::startGameFromEditedState,
        onClearBoard = editEvents::clearEditBoard
    )

    if (isLandscapeScreen) {
        Box(modifier = Modifier.fillMaxSize()) {
            LeftControls(
                modifier = Modifier.align(CenterStart),
                state = colorState,
                events = editColorEvents
            )

            RightControls(
                modifier = Modifier.align(CenterEnd),
                state = actionState,
                events = editActionEvents
            )
        }
    } else {
        Box(modifier = Modifier.fillMaxSize()) {
            TopControls(
                modifier = Modifier.align(TopCenter),
                state = colorState,
                events = editColorEvents
            )

            BottomControls(
                modifier = Modifier.align(BottomCenter),
                state = actionState,
                events = editActionEvents
            )
        }
    }
}

@Composable
fun LeftControls(
    modifier: Modifier = Modifier,
    state: EditColorState = EditColorState(),
    events: EditColorEvents = EditColorEvents()
) {
    Column(
        modifier = modifier.padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        ColorToggleButton(
            currentColor = state.editColor,
            onColorToggle = events.onColorToggle
        )
        Spacer(modifier = Modifier.height(16.dp))
        PlayerSideToggleButton(
            playerSide = state.playerSide,
            onPlayerSideToggle = events.onPlayerSideToggle
        )
        Spacer(modifier = Modifier.height(16.dp))
        TurnToggleButton(
            currentTurn = state.editTurn,
            onTurnToggle = events.onTurnToggle
        )
    }
}

@Composable
fun TopControls(
    modifier: Modifier = Modifier,
    state: EditColorState = EditColorState(),
    events: EditColorEvents = EditColorEvents()
) {
    Row(
        horizontalArrangement = Arrangement.SpaceEvenly,
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        ColorToggleButton(
            currentColor = state.editColor,
            onColorToggle = events.onColorToggle
        )
        PlayerSideToggleButton(
            playerSide = state.playerSide,
            onPlayerSideToggle = events.onPlayerSideToggle
        )
        TurnToggleButton(
            currentTurn = state.editTurn,
            onTurnToggle = events.onTurnToggle
        )
    }
}

@Composable
fun RightControls(
    modifier: Modifier = Modifier,
    state: EditActionState = EditActionState(),
    events: EditActionEvents = EditActionEvents()
) {
    Column(
        modifier = modifier.padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        RotateBoardButton(onClick = events.onRotate)
        Spacer(modifier = Modifier.height(16.dp))
        ClearBoardButton(onClick = events.onClearBoard)
        Spacer(modifier = Modifier.height(16.dp))
        PieceCounter(
            whiteCount = state.pieceCounts.white,
            blackCount = state.pieceCounts.black,
            isValid = state.isValidDistribution
        )
        Spacer(modifier = Modifier.height(16.dp))
        StartGameButton(
            isCompletedDistribution = state.isCompletedDistribution,
            onClick = events.onStartGame,
        )
    }
}

@Composable
fun BottomControls(
    modifier: Modifier = Modifier,
    state: EditActionState = EditActionState(),
    events: EditActionEvents = EditActionEvents()
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        RotateButton(onRotate = events.onRotate)
        StartButtonAndPieceCounter(
            pieceCounts = state.pieceCounts,
            isValidDistribution = state.isValidDistribution,
            isCompletedDistribution = state.isCompletedDistribution,
            onClick = events.onStartGame
        )
        ClearBoardButton(onClick = events.onClearBoard)
    }
}

@Composable
fun RotateBoardButton(onClick: () -> Unit) {
    FloatingActionButton(
        onClick = onClick,
        containerColor = MaterialTheme.colorScheme.primaryContainer,
        modifier = Modifier.size(40.dp)
    ) {
        Icon(
            Icons.AutoMirrored.Filled.RotateRight,
            contentDescription = localizedString(R.string.rotate_board)
        )
    }
}

@Composable
fun backgroundColor(currentColor: CobColor): Color {
    return when (currentColor) {
        WHITE -> MaterialTheme.colorScheme.surface
        BLACK -> MaterialTheme.colorScheme.onSurface
    }
}

@Composable
fun foregroundColor(currentColor: CobColor): Color {
    return when (currentColor) {
        WHITE -> MaterialTheme.colorScheme.onSurface
        BLACK -> MaterialTheme.colorScheme.surface
    }
}

@Composable
fun TurnToggleButton(currentTurn: CobColor, onTurnToggle: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(8.dp)
    ) {
        FloatingActionButton(
            onClick = onTurnToggle,
            containerColor = MaterialTheme.colorScheme.secondaryContainer,
            modifier = Modifier.size(40.dp)
        ) {
            val backgroundColor = backgroundColor(currentTurn)
            val contentColor = foregroundColor(currentTurn)

            Box(
                modifier = Modifier
                    .size(24.dp)
                    .background(backgroundColor, CircleShape)
                    .border(1.dp, MaterialTheme.colorScheme.outline, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (currentTurn == WHITE)
                        localizedString(R.string.w)
                    else localizedString(R.string.b),
                    style = MaterialTheme.typography.labelSmall,
                    color = contentColor
                )
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        LocalizedText(
            R.string.turn,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun PlayerSideToggleButton(playerSide: CobColor, onPlayerSideToggle: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(8.dp)
    ) {
        FloatingActionButton(
            onClick = onPlayerSideToggle,
            containerColor = MaterialTheme.colorScheme.tertiaryContainer,
            modifier = Modifier.size(40.dp)
        ) {
            val backgroundColor = backgroundColor(playerSide)
            val contentColor = foregroundColor(playerSide)

            Box(
                modifier = Modifier
                    .size(24.dp)
                    .background(backgroundColor, CircleShape)
                    .border(1.dp, MaterialTheme.colorScheme.outline, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (playerSide == WHITE)
                        localizedString(R.string.w)
                    else localizedString(R.string.b),
                    style = MaterialTheme.typography.labelSmall,
                    color = contentColor
                )
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            localizedString(R.string.side),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun StartGameButton(isCompletedDistribution: Boolean, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        enabled = isCompletedDistribution,
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isCompletedDistribution) MaterialTheme.colorScheme.primary
            else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
        )
    ) {
        LocalizedText(R.string.start_game)
        Spacer(modifier = Modifier.width(8.dp))
        Icon(Icons.Default.PlayArrow, contentDescription = localizedString(R.string.start))
    }
}

@Composable
fun ClearBoardButton(onClick: () -> Unit) {
    FloatingActionButton(
        onClick = onClick,
        containerColor = MaterialTheme.colorScheme.errorContainer
    ) {
        Icon(Icons.Default.Delete, contentDescription = localizedString(R.string.clear_board))
    }
}

@Composable
fun StartButtonAndPieceCounter(
    pieceCounts: PieceCounts,
    isValidDistribution: Boolean,
    isCompletedDistribution: Boolean,
    onClick: () -> Unit
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        PieceCounter(
            whiteCount = pieceCounts.white,
            blackCount = pieceCounts.black,
            isValid = isValidDistribution
        )
        Spacer(modifier = Modifier.height(8.dp))
        Button(
            onClick = onClick,
            enabled = isCompletedDistribution,
            colors = ButtonDefaults.buttonColors(
                containerColor = if (isCompletedDistribution) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
            )
        ) {
            LocalizedText(R.string.start_game)
            Spacer(modifier = Modifier.width(8.dp))
            Icon(Icons.Default.PlayArrow, contentDescription = localizedString(R.string.start))
        }
    }
}

@Composable
fun RotateButton(onRotate: () -> Unit) {
    FloatingActionButton(
        onClick = onRotate,
        containerColor = MaterialTheme.colorScheme.primaryContainer
    ) {
        Icon(
            Icons.AutoMirrored.Filled.RotateRight,
            contentDescription = localizedString(R.string.rotate_board)
        )
    }
}

@Composable
fun ColorToggleButton(currentColor: CobColor, onColorToggle: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(8.dp)
    ) {
        FloatingActionButton(
            onClick = onColorToggle,
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            modifier = Modifier.size(40.dp)
        ) {
            val backgroundColor = backgroundColor(currentColor)
            val contentColor = foregroundColor(currentColor)

            Box(
                modifier = Modifier
                    .size(24.dp)
                    .background(backgroundColor, CircleShape)
                    .border(1.dp, MaterialTheme.colorScheme.outline, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (currentColor == WHITE)
                        localizedString(R.string.w)
                    else localizedString(R.string.b),
                    style = MaterialTheme.typography.labelSmall,
                    color = contentColor
                )
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        LocalizedText(
            R.string.piece,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

// Contador de Piezas en Edición de Tablero
@Composable
fun PieceCounter(whiteCount: Int, blackCount: Int, isValid: Boolean) {
    Card(
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isValid) MaterialTheme.colorScheme.surface
            else MaterialTheme.colorScheme.errorContainer
        )
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Contador blanco
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .background(MaterialTheme.colorScheme.surface, CircleShape)
                    .border(1.dp, MaterialTheme.colorScheme.outline, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    whiteCount.toString(),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Text(" — ", modifier = Modifier.padding(horizontal = 8.dp))

            // Contador negro
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .background(MaterialTheme.colorScheme.onSurface, CircleShape)
                    .border(1.dp, MaterialTheme.colorScheme.outline, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    blackCount.toString(),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.surface
                )
            }

            // Indicador de validez
            if (!isValid) {
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    Icons.Default.Warning,
                    localizedString(R.string.invalid_schema),
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}