package com.agustin.tarati.ui.components.board.previews

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import com.agustin.tarati.game.core.CobColor
import com.agustin.tarati.game.core.CobColor.BLACK
import com.agustin.tarati.game.core.CobColor.WHITE
import com.agustin.tarati.game.core.GameState
import com.agustin.tarati.game.core.createGameState
import com.agustin.tarati.game.core.initialGameState
import com.agustin.tarati.game.logic.BoardOrientation
import com.agustin.tarati.ui.components.board.Board
import com.agustin.tarati.ui.components.board.BoardEvents
import com.agustin.tarati.ui.components.board.BoardState
import com.agustin.tarati.ui.components.board.animation.BoardAnimationViewModel
import com.agustin.tarati.ui.components.board.behaviors.BoardSelectionViewModel
import com.agustin.tarati.ui.helpers.endGameState
import com.agustin.tarati.ui.helpers.midGameState
import com.agustin.tarati.ui.screens.settings.BoardVisualState
import com.agustin.tarati.ui.theme.BoardColors
import com.agustin.tarati.ui.theme.ClassicPalette
import com.agustin.tarati.ui.theme.DarkPalette
import com.agustin.tarati.ui.theme.NaturePalette
import com.agustin.tarati.ui.theme.PaletteManager
import com.agustin.tarati.ui.theme.TaratiTheme
import com.agustin.tarati.ui.theme.rememberBoardColors


@Composable
fun BoardPreview(
    previewConfig: BoardPreviewConfig,
    viewModel: BoardSelectionViewModel = viewModel(),
    animationViewModel: BoardAnimationViewModel = viewModel(),
    debug: Boolean = false
) {
    TaratiTheme {
        Board(
            playerSide = previewConfig.playerSide,
            boardState = BoardState(
                gameState = previewConfig.gameState,
                aiEnabled = false,
                lastMove = null,
                boardOrientation = previewConfig.orientation,
                boardVisualState = previewConfig.boardVisualState,
                isEditing = previewConfig.isEditing,
            ),
            boardColors = rememberBoardColors(),
            events = createPreviewBoardEvents(debug),
            selectViewModel = viewModel,
            animationViewModel = animationViewModel,
        )
    }
}

@Preview(showBackground = true, widthDp = 400, heightDp = 600)
@Composable
fun BoardPreview_PortraitWhite() {
    PaletteManager.setPalette(DarkPalette)

    BoardPreview(
        BoardPreviewConfig(
            orientation = BoardOrientation.PORTRAIT_WHITE,
            gameState = initialGameState(BLACK),
            playerSide = BLACK,
            boardVisualState = BoardVisualState(
                edgesVisibles = false,
                verticesVisibles = false,
            ),
            boardColors = rememberBoardColors(),
        )
    )
}

@Preview(showBackground = true, widthDp = 400, heightDp = 600)
@Composable
fun BoardPreview_PortraitBlack() {
    PaletteManager.setPalette(ClassicPalette)

    BoardPreview(
        BoardPreviewConfig(
            orientation = BoardOrientation.PORTRAIT_WHITE,
            gameState = initialGameState(WHITE),
            playerSide = WHITE,
            boardVisualState = BoardVisualState(),
            boardColors = rememberBoardColors(),
        )
    )
}

@Preview(showBackground = true, widthDp = 600, heightDp = 400)
@Composable
fun BoardPreview_LandscapeBlack() {
    PaletteManager.setPalette(NaturePalette)

    BoardPreview(
        BoardPreviewConfig(
            orientation = BoardOrientation.LANDSCAPE_BLACK,
            gameState = midGameState(),
            playerSide = BLACK,
            boardVisualState = BoardVisualState(edgesVisibles = false),
            boardColors = rememberBoardColors(),
        )
    )
}

@Preview(showBackground = true, widthDp = 600, heightDp = 400)
@Composable
fun BoardPreview_Custom() {
    PaletteManager.setPalette(DarkPalette)

    val exampleGameState = createGameState {
        setTurn(WHITE)
        setCob("C2", WHITE, true)
        setCob("C8", BLACK, true)
        setCob("B1", WHITE, false)
        setCob("B4", BLACK, false)

        // Agregar piezas extra para testing
        setCob("C5", WHITE, true)
        setCob("C11", BLACK, true)
    }
    val vm = viewModel<BoardSelectionViewModel>().apply {
        updateSelectedVertex("B1")
        updateValidAdjacentVertexes(listOf("B2", "A1", "B6"))
    }
    BoardPreview(
        BoardPreviewConfig(
            orientation = BoardOrientation.LANDSCAPE_WHITE,
            gameState = exampleGameState,
            playerSide = WHITE,
            boardVisualState = BoardVisualState(verticesVisibles = false),
            boardColors = rememberBoardColors()
        ),
        viewModel = vm,
    )
}

@Preview(showBackground = true, widthDp = 400, heightDp = 600)
@Composable
fun BoardPreview_BlackPlayer() {
    PaletteManager.setPalette(ClassicPalette)

    TaratiTheme(true) {
        val exampleGameState = endGameState(BLACK)
        val vm = viewModel<BoardSelectionViewModel>().apply {
            updateSelectedVertex("A1")
            updateValidAdjacentVertexes(listOf("B1", "B2", "B3", "B4", "B5", "B6"))
        }
        BoardPreview(
            BoardPreviewConfig(
                orientation = BoardOrientation.PORTRAIT_BLACK,
                gameState = exampleGameState,
                playerSide = BLACK,
                boardVisualState = BoardVisualState(
                    labelsVisibles = false,
                    edgesVisibles = false,
                    verticesVisibles = false,
                ),
                boardColors = rememberBoardColors()
            ),
            viewModel = vm,
        )
    }
}

@Preview(showBackground = true, widthDp = 600, heightDp = 400)
@Composable
fun BoardPreview_Landscape_BlackPlayer() {
    PaletteManager.setPalette(DarkPalette)

    TaratiTheme {
        val exampleGameState = createGameState { setTurn(BLACK) }
        val vm = viewModel<BoardSelectionViewModel>().apply {
            updateSelectedVertex("C2")
            updateValidAdjacentVertexes(listOf("C9", "B4", "B5"))
        }
        BoardPreview(
            BoardPreviewConfig(
                orientation = BoardOrientation.LANDSCAPE_BLACK,
                gameState = exampleGameState,
                boardVisualState = BoardVisualState(labelsVisibles = false, edgesVisibles = false),
                playerSide = WHITE,
                boardColors = rememberBoardColors()
            ),
            viewModel = vm,
        )
    }
}

@Preview(showBackground = true, widthDp = 600, heightDp = 400)
@Composable
fun BoardPreview_Landscape() {
    PaletteManager.setPalette(NaturePalette)

    TaratiTheme(true) {
        val exampleGameState = createGameState {}
        val vm = viewModel<BoardSelectionViewModel>().apply {
            updateSelectedVertex("C2")
            updateValidAdjacentVertexes(listOf("C3", "B2", "B1"))
        }
        BoardPreview(
            BoardPreviewConfig(
                orientation = BoardOrientation.LANDSCAPE_WHITE,
                gameState = exampleGameState,
                playerSide = BLACK,
                boardVisualState = BoardVisualState(),
                boardColors = rememberBoardColors()
            ),
            viewModel = vm,
        )
    }
}

@Preview(showBackground = true, widthDp = 600, heightDp = 400)
@Composable
fun BoardPreview_Landscape_Editing() {
    PaletteManager.setPalette(ClassicPalette)

    TaratiTheme(true) {
        val exampleGameState = initialGameState()
        val vm = viewModel<BoardSelectionViewModel>().apply {
            updateSelectedVertex("C2")
            updateValidAdjacentVertexes(listOf("C3", "B2", "B1"))
        }
        BoardPreview(
            BoardPreviewConfig(
                orientation = BoardOrientation.LANDSCAPE_WHITE,
                gameState = exampleGameState,
                isEditing = true,
                playerSide = BLACK,
                boardVisualState = BoardVisualState(edgesVisibles = false),
                boardColors = rememberBoardColors()
            ),
            viewModel = vm,
        )
    }
}

data class BoardPreviewConfig(
    val gameState: GameState,
    val playerSide: CobColor = WHITE,
    val orientation: BoardOrientation,
    val boardVisualState: BoardVisualState,
    val isEditing: Boolean = false,
    val darkTheme: Boolean = false,
    val boardColors: BoardColors,
    val debug: Boolean = false
)

fun createPreviewBoardEvents(debug: Boolean = false): BoardEvents = object : BoardEvents {
    override fun onMove(from: String, to: String) {
        if (debug) println("Move from $from to $to")
    }

    override fun onEditPiece(from: String) {
        if (debug) println("Edit piece at $from")
    }

    override fun onResetCompleted() {
        if (debug) println("Reset completed")
    }
}