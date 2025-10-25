package com.agustin.tarati.ui.components.board.draw.previews

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.agustin.tarati.game.core.Cob
import com.agustin.tarati.game.core.CobColor
import com.agustin.tarati.game.core.GameState
import com.agustin.tarati.game.core.initialGameState
import com.agustin.tarati.game.logic.BoardOrientation
import com.agustin.tarati.ui.components.board.BoardState
import com.agustin.tarati.ui.components.board.TapEvents
import com.agustin.tarati.ui.components.board.animation.BoardAnimationViewModel
import com.agustin.tarati.ui.components.board.behaviors.BoardSelectionViewModel
import com.agustin.tarati.ui.components.board.draw.BoardRenderer
import com.agustin.tarati.ui.components.board.draw.drawBoardBackground
import com.agustin.tarati.ui.screens.settings.BoardVisualState
import com.agustin.tarati.ui.theme.BoardColors
import com.agustin.tarati.ui.theme.BoardPalette
import com.agustin.tarati.ui.theme.ClassicPalette
import com.agustin.tarati.ui.theme.DarkPalette
import com.agustin.tarati.ui.theme.NaturePalette
import com.agustin.tarati.ui.theme.PaletteManager
import com.agustin.tarati.ui.theme.TaratiTheme
import com.agustin.tarati.ui.theme.getBoardColors

@Preview(group = "Board", showBackground = true, widthDp = 300, heightDp = 500)
@Composable
fun PreviewBoardRendererEmpty(
    selectionViewModel: BoardSelectionViewModel = viewModel(),
    animationViewModel: BoardAnimationViewModel = viewModel(),
    boardColors: BoardColors = getBoardColors(ClassicPalette),
    boardOrientation: BoardOrientation = BoardOrientation.PORTRAIT_WHITE
) {
    FullBackground(boardColors = boardColors, boardOrientation = boardOrientation)
    TaratiTheme {
        Box(
            modifier = Modifier
                .size(400.dp)
        ) {
            BoardRenderer(
                modifier = Modifier.matchParentSize(),
                playerSide = CobColor.WHITE,
                boardState = PreviewStates.emptyBoardState.copy(boardOrientation = boardOrientation),
                colors = boardColors,
                tapEvents = tapEventsPreview,
                selectViewModel = selectionViewModel,
                animationViewModel = animationViewModel,
                onBoardSizeChange = {},
                onResetCompleted = {},
                debug = false
            )
        }
    }
}

@Preview(group = "Board", showBackground = true, widthDp = 300, heightDp = 500)
@Composable
fun PreviewBoardRendererWithPieces(
    selectionViewModel: BoardSelectionViewModel = viewModel(),
    animationViewModel: BoardAnimationViewModel = viewModel(),
    boardColors: BoardColors = getBoardColors(ClassicPalette),
    boardOrientation: BoardOrientation = BoardOrientation.PORTRAIT_WHITE
) {
    FullBackground(boardColors = boardColors, boardOrientation = boardOrientation)
    TaratiTheme {
        Box(
            modifier = Modifier
                .size(400.dp)
        ) {
            BoardRenderer(
                modifier = Modifier.matchParentSize(),
                playerSide = CobColor.WHITE,
                boardState = PreviewStates.populatedBoardState.copy(boardOrientation = boardOrientation),
                colors = boardColors,
                tapEvents = tapEventsPreview,
                selectViewModel = selectionViewModel,
                animationViewModel = animationViewModel,
                onBoardSizeChange = {},
                onResetCompleted = {},
                debug = false
            )
        }
    }
}

@Preview(group = "Board", showBackground = true, widthDp = 300, heightDp = 500)
@Composable
fun PreviewBoardRendererEditingMode(
    selectionViewModel: BoardSelectionViewModel = viewModel(),
    animationViewModel: BoardAnimationViewModel = viewModel(),
    boardColors: BoardColors = getBoardColors(DarkPalette),
    boardOrientation: BoardOrientation = BoardOrientation.PORTRAIT_WHITE
) {
    FullBackground(boardColors = boardColors, boardOrientation = boardOrientation)
    TaratiTheme {
        Box(
            modifier = Modifier
                .size(400.dp)
        ) {
            BoardRenderer(
                modifier = Modifier.matchParentSize(),
                playerSide = CobColor.BLACK,
                boardState = PreviewStates.editingBoardState.copy(boardOrientation = boardOrientation),
                colors = boardColors,
                tapEvents = tapEventsPreview,
                selectViewModel = selectionViewModel,
                animationViewModel = animationViewModel,
                onBoardSizeChange = {},
                onResetCompleted = {},
                debug = true
            )
        }
    }
}

@Preview(group = "Board", showBackground = true, widthDp = 500, heightDp = 300)
@Composable
fun PreviewBoardBackgroundFull(
    boardColors: BoardColors = getBoardColors(ClassicPalette),
    boardOrientation: BoardOrientation = BoardOrientation.LANDSCAPE_WHITE
) {
    TaratiTheme {
        Surface(
            modifier = Modifier.size(300.dp),
            color = MaterialTheme.colorScheme.background
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
            ) {
                FullBackground(Modifier.matchParentSize(), boardOrientation, boardColors)
                Text(
                    text = "Fondo Completo",
                    modifier = Modifier.align(Alignment.TopCenter),
                    style = MaterialTheme.typography.labelSmall
                )
            }
        }
    }
}

@Composable
fun FullBackground(modifier: Modifier = Modifier, boardOrientation: BoardOrientation, boardColors: BoardColors) {
    Canvas(modifier = modifier) {
        drawBoardBackground(
            canvasSize = size,
            orientation = boardOrientation,
            colors = boardColors,
            regionsVisible = true,
            perimeterVisible = true
        )
    }
}

@Preview(group = "Board", showBackground = true, widthDp = 500, heightDp = 300)
@Composable
fun PreviewBoardBackgroundNoRegions(
    boardColors: BoardColors = getBoardColors(DarkPalette),
    boardOrientation: BoardOrientation = BoardOrientation.LANDSCAPE_BLACK
) {
    TaratiTheme {
        Surface(
            modifier = Modifier.size(300.dp),
            color = MaterialTheme.colorScheme.background
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
            ) {
                Canvas(modifier = Modifier.matchParentSize()) {
                    drawBoardBackground(
                        canvasSize = size,
                        orientation = boardOrientation,
                        colors = boardColors,
                        regionsVisible = false,
                        perimeterVisible = true
                    )
                }
                Text(
                    text = "Sin Regiones",
                    modifier = Modifier.align(Alignment.TopCenter),
                    style = MaterialTheme.typography.labelSmall
                )
            }
        }
    }
}

@Preview(group = "Board", showBackground = true, widthDp = 300, heightDp = 500)
@Composable
fun PreviewBoardBackgroundNoPerimeter(boardColors: BoardColors = getBoardColors(NaturePalette)) {
    TaratiTheme {
        Surface(
            modifier = Modifier.size(300.dp),
            color = MaterialTheme.colorScheme.background
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
            ) {
                Canvas(modifier = Modifier.matchParentSize()) {
                    drawBoardBackground(
                        canvasSize = size,
                        orientation = BoardOrientation.PORTRAIT_WHITE,
                        colors = boardColors,
                        regionsVisible = true,
                        perimeterVisible = false
                    )
                }
                Text(
                    text = "Sin Perímetro",
                    modifier = Modifier.align(Alignment.TopCenter),
                    style = MaterialTheme.typography.labelSmall
                )
            }
        }
    }
}

object PreviewStates {
    val emptyBoardState = BoardState(
        gameState = initialGameState(),
        boardVisualState = BoardVisualState(),
        boardOrientation = BoardOrientation.PORTRAIT_WHITE,
        isEditing = false,
        aiEnabled = false,
        newGame = false,
        lastMove = null
    )

    val populatedBoardState = BoardState(
        gameState = GameState(
            cobs = mapOf(
                "A1" to Cob(CobColor.WHITE, false),
                "B2" to Cob(CobColor.WHITE, true),
                "C3" to Cob(CobColor.BLACK, false),
                "D4" to Cob(CobColor.BLACK, true)
            ),
            currentTurn = CobColor.BLACK
        ),
        boardVisualState = BoardVisualState(
            labelsVisibles = true,
            verticesVisibles = true,
            edgesVisibles = true,
            regionsVisibles = true,
            perimeterVisible = true,
            animateEffects = true,
        ),
        boardOrientation = BoardOrientation.PORTRAIT_WHITE,
        isEditing = false,
        aiEnabled = false,
        newGame = false,
        lastMove = null
    )

    val editingBoardState = BoardState(
        gameState = GameState(
            cobs = mapOf(
                "A1" to Cob(CobColor.WHITE, false),
                "B5" to Cob(CobColor.BLACK, true)
            ),
            currentTurn = CobColor.WHITE
        ),
        boardVisualState = BoardVisualState(),
        boardOrientation = BoardOrientation.PORTRAIT_WHITE,
        isEditing = true,
        aiEnabled = false,
        newGame = false,
        lastMove = null
    )
}

@Composable
fun getBoardColors(palette: BoardPalette): BoardColors {
    PaletteManager.setPalette(palette)
    return getBoardColors()
}

val tapEventsPreview: TapEvents = object : TapEvents {
    override fun onSelected(from: String, valid: List<String>) {}
    override fun onMove(from: String, to: String) {}
    override fun onInvalid(from: String, valid: List<String>) {}
    override fun onEditPieceRequested(from: String) {}
    override fun onCancel() {}
}