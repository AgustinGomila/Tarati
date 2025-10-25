package com.agustin.tarati.ui.components.editor.previews

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment.Companion.BottomCenter
import androidx.compose.ui.Alignment.Companion.CenterEnd
import androidx.compose.ui.Alignment.Companion.CenterStart
import androidx.compose.ui.Alignment.Companion.TopCenter
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.agustin.tarati.game.core.CobColor.BLACK
import com.agustin.tarati.game.core.CobColor.WHITE
import com.agustin.tarati.game.core.initialGameState
import com.agustin.tarati.game.core.opponent
import com.agustin.tarati.game.logic.BoardOrientation
import com.agustin.tarati.ui.components.board.Board
import com.agustin.tarati.ui.components.board.BoardState
import com.agustin.tarati.ui.components.board.previews.createPreviewBoardEvents
import com.agustin.tarati.ui.components.editor.BottomControls
import com.agustin.tarati.ui.components.editor.EditActionEvents
import com.agustin.tarati.ui.components.editor.EditActionState
import com.agustin.tarati.ui.components.editor.EditColorEvents
import com.agustin.tarati.ui.components.editor.EditColorState
import com.agustin.tarati.ui.components.editor.LeftControls
import com.agustin.tarati.ui.components.editor.PieceCounts
import com.agustin.tarati.ui.components.editor.RightControls
import com.agustin.tarati.ui.components.editor.TopControls
import com.agustin.tarati.ui.screens.settings.BoardVisualState
import com.agustin.tarati.ui.theme.TaratiTheme
import com.agustin.tarati.ui.theme.getBoardColors


@Preview(showBackground = true)
@Composable
fun EditingModePreviewContent(
    darkTheme: Boolean = false,
    isLandscape: Boolean = false,
    boardOrientation: BoardOrientation = if (isLandscape) BoardOrientation.LANDSCAPE_BLACK else BoardOrientation.PORTRAIT_WHITE
) {
    TaratiTheme(darkTheme = darkTheme) {
        val exampleGameState = initialGameState()

        var isEditing by remember { mutableStateOf(true) }
        var aiEnabled by remember { mutableStateOf(false) }
        var editColor by remember { mutableStateOf(WHITE) }
        var editTurn by remember { mutableStateOf(WHITE) }
        var playerSide by remember { mutableStateOf(WHITE) }

        val pieceCounts = PieceCounts(4, 4)
        val isValidDistribution = true
        val isCompletedDistribution = true

        // Crear estado para Board
        val boardState = BoardState(
            gameState = exampleGameState,
            lastMove = null,
            aiEnabled = aiEnabled,
            boardOrientation = boardOrientation,
            boardVisualState = BoardVisualState(
                labelsVisibles = false,
                verticesVisibles = true,
                edgesVisibles = true,
            ),
            isEditing = isEditing,
        )

        // Crear eventos para Board
        val boardEvents = createPreviewBoardEvents(false)
        val boardColors = getBoardColors()

        Box(modifier = Modifier.fillMaxSize()) {
            Board(
                modifier = Modifier.fillMaxSize(),
                playerSide = playerSide,
                boardState = boardState,
                boardColors = boardColors,
                events = boardEvents,
            )

            EditControlsPreview(
                isLandscapeScreen = isLandscape,
                colorState = EditColorState(
                    playerSide = playerSide,
                    editColor = editColor,
                    editTurn = editTurn
                ),
                colorEvents = EditColorEvents(
                    onColorToggle = { editColor = editColor.opponent() },
                    onPlayerSideToggle = { playerSide = playerSide.opponent() },
                    onTurnToggle = { editTurn = editTurn.opponent() }
                ),
                actionState = EditActionState(
                    pieceCounts = pieceCounts,
                    isValidDistribution = isValidDistribution,
                    isCompletedDistribution = isCompletedDistribution
                ),
                actionEvents = EditActionEvents(
                    onClearBoard = { /* No-op en preview */ }
                )
            )
        }
    }
}

@Preview(showBackground = true, widthDp = 400, heightDp = 200)
@Composable
fun EditControlsPreview_Portrait() {
    TaratiTheme {
        EditControlsPreview(isLandscapeScreen = false)
    }
}

@Preview(showBackground = true, widthDp = 800, heightDp = 200)
@Composable
fun EditControlsPreview_Landscape() {
    TaratiTheme {
        EditControlsPreview(isLandscapeScreen = true)
    }
}

@Preview(showBackground = true, widthDp = 400, heightDp = 200)
@Composable
fun EditControlsPreview_InvalidDistribution() {
    TaratiTheme {
        EditControlsPreview(
            isLandscapeScreen = false,
            actionState = EditActionState(
                pieceCounts = PieceCounts(8, 0),
                isValidDistribution = false,
                isCompletedDistribution = false
            )
        )
    }
}

@Preview(showBackground = true, widthDp = 400, heightDp = 200)
@Composable
fun EditControlsPreview_CompletedDistribution() {
    TaratiTheme {
        EditControlsPreview(
            isLandscapeScreen = false,
            actionState = EditActionState(
                pieceCounts = PieceCounts(7, 1),
                isValidDistribution = true,
                isCompletedDistribution = true
            )
        )
    }
}

@Preview(showBackground = true, widthDp = 400, heightDp = 200)
@Composable
fun EditControlsPreview_BlackColor() {
    TaratiTheme {
        EditControlsPreview(
            isLandscapeScreen = false,
            colorState = EditColorState(
                editColor = BLACK,
                playerSide = BLACK,
                editTurn = BLACK
            )
        )
    }
}

@Composable
fun EditControlsPreview(
    isLandscapeScreen: Boolean,
    colorState: EditColorState = EditColorState(),
    colorEvents: EditColorEvents = EditColorEvents(),
    actionState: EditActionState = EditActionState(),
    actionEvents: EditActionEvents = EditActionEvents()
) {
    if (isLandscapeScreen) {
        // Landscape: controles a izquierda y derecha
        Box(modifier = Modifier.fillMaxSize()) {
            LeftControls(
                modifier = Modifier.align(CenterStart),
                state = colorState,
                events = colorEvents
            )

            RightControls(
                modifier = Modifier.align(CenterEnd),
                state = actionState,
                events = actionEvents
            )
        }
    } else {
        // Portrait: controles en superior e inferior
        Box(modifier = Modifier.fillMaxSize()) {
            TopControls(
                modifier = Modifier.align(TopCenter),
                state = colorState,
                events = colorEvents
            )

            BottomControls(
                modifier = Modifier.align(BottomCenter),
                state = actionState,
                events = actionEvents
            )
        }
    }
}

@Preview(showBackground = true, widthDp = 200, heightDp = 300)
@Composable
fun LeftControlsPreview() {
    TaratiTheme {
        LeftControls(
            state = EditColorState(
                playerSide = WHITE,
                editColor = WHITE,
                editTurn = WHITE
            ),
            events = EditColorEvents()
        )
    }
}

@Preview(showBackground = true, widthDp = 200, heightDp = 300)
@Composable
fun RightControlsPreview() {
    TaratiTheme {
        RightControls(
            state = EditActionState(
                pieceCounts = PieceCounts(4, 4),
                isValidDistribution = true,
                isCompletedDistribution = true
            ),
            events = EditActionEvents()
        )
    }
}

@Preview(showBackground = true, widthDp = 400, heightDp = 100)
@Composable
fun TopControlsPreview() {
    TaratiTheme {
        TopControls(
            state = EditColorState(
                playerSide = WHITE,
                editColor = WHITE,
                editTurn = WHITE
            ),
            events = EditColorEvents()
        )
    }
}

@Preview(showBackground = true, widthDp = 400, heightDp = 100)
@Composable
fun BottomControlsPreview() {
    TaratiTheme {
        BottomControls(
            state = EditActionState(
                pieceCounts = PieceCounts(4, 4),
                isValidDistribution = true,
                isCompletedDistribution = true
            ),
            events = EditActionEvents()
        )
    }
}