package com.agustin.tarati.ui.components.board

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import com.agustin.tarati.game.core.Color
import com.agustin.tarati.game.core.Color.BLACK
import com.agustin.tarati.game.core.Color.WHITE
import com.agustin.tarati.game.core.GameState
import com.agustin.tarati.game.core.Move
import com.agustin.tarati.game.core.createGameState
import com.agustin.tarati.game.logic.BoardOrientation
import com.agustin.tarati.ui.helpers.endGameState
import com.agustin.tarati.ui.helpers.initialGameStateWithUpgrades
import com.agustin.tarati.ui.helpers.midGameState
import com.agustin.tarati.ui.theme.BoardColors
import com.agustin.tarati.ui.theme.TaratiTheme
import com.agustin.tarati.ui.theme.getBoardColors

data class BoardState(
    val gameState: GameState,
    val lastMove: Move? = null,
    val boardOrientation: BoardOrientation = BoardOrientation.PORTRAIT_WHITE,
    val labelsVisible: Boolean = true,
    val verticesVisible: Boolean = true,
    val newGame: Boolean = false,
    val isEditing: Boolean = false,
    val showBoardPattern: Boolean = true,
    val showBoardGlow: Boolean = false,
)

interface BoardEvents {
    fun onMove(from: String, to: String)
    fun onEditPiece(from: String)
    fun onResetCompleted()
}

interface TapEvents {
    fun onSelected(from: String, valid: List<String>)
    fun onMove(from: String, to: String)
    fun onInvalid(from: String, valid: List<String>)
    fun onEditPieceRequested(from: String)
    fun onCancel()
}

@Composable
fun Board(
    modifier: Modifier = Modifier,
    boardState: BoardState,
    playerSide: Color,
    events: BoardEvents,
    selectViewModel: BoardSelectionViewModel = viewModel(),
    animationViewModel: BoardAnimationViewModel = viewModel(),
    boardColors: BoardColors,
    debug: Boolean = false
) {
    Box(
        modifier = modifier
            .background(MaterialTheme.colorScheme.surface)
    ) {
        Canvas(modifier = Modifier.matchParentSize()) {
            drawBoardBackground(
                canvasSize = size,
                orientation = boardState.boardOrientation,
                colors = boardColors,
                showPattern = boardState.showBoardPattern,
                showGlow = boardState.showBoardGlow
            )
        }

        BoardRenderer(
            modifier = Modifier.fillMaxSize(),
            playerSide = playerSide,
            selectViewModel = selectViewModel,
            animationViewModel = animationViewModel,
            boardState = boardState,
            tapEvents = object : TapEvents {
                override fun onSelected(from: String, valid: List<String>) {
                    selectViewModel.updateSelectedVertex(from)
                    selectViewModel.updateValidAdjacentVertexes(valid)
                }

                override fun onMove(from: String, to: String) {
                    events.onMove(from, to)
                    selectViewModel.resetSelection()
                }

                override fun onInvalid(from: String, valid: List<String>) {
                    selectViewModel.updateSelectedVertex(from)
                    selectViewModel.updateValidAdjacentVertexes(valid)
                }

                override fun onEditPieceRequested(from: String) {
                    events.onEditPiece(from)
                }

                override fun onCancel() {
                    selectViewModel.resetSelection()
                }
            },
            onBoardSizeChange = { animationViewModel.updateBoardSize(it) },
            onResetCompleted = events::onResetCompleted,
            colors = boardColors,
            debug = debug
        )
    }
}

// region Previews

@Composable
fun BoardPreview(
    orientation: BoardOrientation,
    gameState: GameState,
    playerSide: Color,
    labelsVisible: Boolean = true,
    verticesVisible: Boolean = true,
    isEditing: Boolean = false,
    viewModel: BoardSelectionViewModel = viewModel(),
    animationViewModel: BoardAnimationViewModel = viewModel(),
    debug: Boolean = false
) {
    val boardColors = getBoardColors()

    TaratiTheme {
        Board(
            boardState = BoardState(
                gameState = gameState,
                lastMove = null,
                boardOrientation = orientation,
                labelsVisible = labelsVisible,
                verticesVisible = verticesVisible,
                isEditing = isEditing,
            ),
            playerSide = playerSide,
            events = object : BoardEvents {
                override fun onMove(from: String, to: String) {
                    if (debug) println("Move from $from to $to")
                }

                override fun onEditPiece(from: String) {
                    if (debug) println("Edit piece at $from")
                }

                override fun onResetCompleted() {
                    if (debug) println("Reset completed")
                }
            },
            selectViewModel = viewModel,
            boardColors = boardColors,
            animationViewModel = animationViewModel,
        )
    }
}

@Preview(showBackground = true, widthDp = 400, heightDp = 500)
@Composable
fun BoardPreview_PortraitWhite() {
    BoardPreview(
        BoardOrientation.PORTRAIT_WHITE, initialGameStateWithUpgrades(),
        playerSide = BLACK,
    )
}

@Preview(showBackground = true, widthDp = 400, heightDp = 500)
@Composable
fun BoardPreview_PortraitBlack() {
    BoardPreview(
        BoardOrientation.PORTRAIT_WHITE, initialGameStateWithUpgrades(),
        playerSide = WHITE,
    )
}

@Preview(showBackground = true, widthDp = 800, heightDp = 400)
@Composable
fun BoardPreview_LandscapeBlack() {
    BoardPreview(
        BoardOrientation.LANDSCAPE_BLACK, midGameState(),
        playerSide = BLACK,
    )
}

@Preview(showBackground = true, widthDp = 800, heightDp = 400)
@Composable
fun BoardPreview_Custom() {
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
        orientation = BoardOrientation.LANDSCAPE_WHITE,
        gameState = exampleGameState,
        playerSide = WHITE,
        viewModel = vm
    )
}

@Preview(showBackground = true, widthDp = 400, heightDp = 500)
@Composable
fun BoardPreview_BlackPlayer() {
    TaratiTheme(true) {
        val exampleGameState = endGameState(BLACK)
        val vm = viewModel<BoardSelectionViewModel>().apply {
            updateSelectedVertex("A1")
            updateValidAdjacentVertexes(listOf("B1", "B2", "B3", "B4", "B5", "B6"))
        }
        BoardPreview(
            orientation = BoardOrientation.PORTRAIT_BLACK,
            gameState = exampleGameState,
            playerSide = BLACK,
            labelsVisible = false,
            viewModel = vm
        )
    }
}

@Preview(showBackground = true, widthDp = 800, heightDp = 400)
@Composable
fun BoardPreview_Landscape_BlackPlayer() {
    TaratiTheme {
        val exampleGameState = createGameState { setTurn(BLACK) }
        val vm = viewModel<BoardSelectionViewModel>().apply {
            updateSelectedVertex("C2")
            updateValidAdjacentVertexes(listOf("C9", "B4", "B5"))
        }
        BoardPreview(
            orientation = BoardOrientation.LANDSCAPE_BLACK,
            gameState = exampleGameState,
            labelsVisible = false,
            playerSide = WHITE,
            viewModel = vm
        )
    }
}

@Preview(showBackground = true, widthDp = 800, heightDp = 400)
@Composable
fun BoardPreview_Landscape() {
    TaratiTheme(true) {
        val exampleGameState = createGameState {}
        val vm = viewModel<BoardSelectionViewModel>().apply {
            updateSelectedVertex("C2")
            updateValidAdjacentVertexes(listOf("C3", "B2", "B1"))
        }
        BoardPreview(
            orientation = BoardOrientation.LANDSCAPE_WHITE,
            gameState = exampleGameState,
            playerSide = BLACK,
            viewModel = vm
        )
    }
}

@Preview(showBackground = true, widthDp = 800, heightDp = 400)
@Composable
fun BoardPreview_Landscape_Editing() {
    TaratiTheme(true) {
        val exampleGameState = createGameState {}
        val vm = viewModel<BoardSelectionViewModel>().apply {
            updateSelectedVertex("C2")
            updateValidAdjacentVertexes(listOf("C3", "B2", "B1"))
        }
        BoardPreview(
            orientation = BoardOrientation.LANDSCAPE_WHITE,
            gameState = exampleGameState,
            isEditing = true,
            playerSide = BLACK,
            viewModel = vm
        )
    }
}

// endregion Previews