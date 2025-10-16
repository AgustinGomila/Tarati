package com.agustin.tarati.ui.components.board

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import com.agustin.tarati.game.core.Color.BLACK
import com.agustin.tarati.game.core.Color.WHITE
import com.agustin.tarati.game.core.GameState
import com.agustin.tarati.game.core.Move
import com.agustin.tarati.game.logic.BoardOrientation
import com.agustin.tarati.game.logic.createGameState
import com.agustin.tarati.ui.helpers.endGameState
import com.agustin.tarati.ui.helpers.initialGameStateWithUpgrades
import com.agustin.tarati.ui.helpers.midGameState
import com.agustin.tarati.ui.theme.TaratiTheme

data class BoardState(
    val gameState: GameState,
    val lastMove: Move? = null,
    val boardOrientation: BoardOrientation = BoardOrientation.PORTRAIT_WHITE,
    val labelsVisible: Boolean = true,
    val newGame: Boolean = false,
    val isEditing: Boolean = false
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
    state: BoardState,
    events: BoardEvents,
    viewModel: BoardViewModel = viewModel(),
    debug: Boolean = false
) {
    LaunchedEffect(state.newGame) {
        if (state.newGame) {
            viewModel.resetSelection()
            events.onResetCompleted()
        }
    }

    val vmSelectedPiece by viewModel.selectedPiece.collectAsState()
    val vmValidMoves by viewModel.validMoves.collectAsState()

    Box(
        modifier = modifier
            .background(MaterialTheme.colorScheme.surface)
    ) {
        BoardRenderer(
            modifier = Modifier.fillMaxSize(),
            selectedPiece = vmSelectedPiece,
            validMoves = vmValidMoves,
            boardState = state,
            tapEvents = object : TapEvents {
                override fun onSelected(from: String, valid: List<String>) {
                    viewModel.updateSelectedPiece(from)
                    viewModel.updateValidMoves(valid)
                }

                override fun onMove(from: String, to: String) {
                    events.onMove(from, to)
                    viewModel.resetSelection()
                }

                override fun onInvalid(from: String, valid: List<String>) {
                    viewModel.updateSelectedPiece(from)
                    viewModel.updateValidMoves(valid)
                }

                override fun onEditPieceRequested(from: String) {
                    events.onEditPiece(from)
                }

                override fun onCancel() {
                    viewModel.resetSelection()
                }
            },
            debug = debug
        )
    }
}

@Composable
fun BoardPreview(
    orientation: BoardOrientation,
    gameState: GameState,
    labelsVisible: Boolean = true,
    isEditing: Boolean = false,
    viewModel: BoardViewModel = viewModel(),
    debug: Boolean = false
) {
    TaratiTheme {
        Board(
            state = BoardState(
                gameState = gameState,
                lastMove = null,
                boardOrientation = orientation,
                labelsVisible = labelsVisible,
                isEditing = isEditing,
            ),
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
            viewModel = viewModel
        )
    }
}

@Preview(showBackground = true, widthDp = 400, heightDp = 500)
@Composable
fun BoardPreview_PortraitWhite() {
    BoardPreview(BoardOrientation.PORTRAIT_WHITE, initialGameStateWithUpgrades())
}

@Preview(showBackground = true, widthDp = 400, heightDp = 500)
@Composable
fun BoardPreview_PortraitBlack() {
    BoardPreview(BoardOrientation.PORTRAIT_WHITE, initialGameStateWithUpgrades())
}

@Preview(showBackground = true, widthDp = 800, heightDp = 400)
@Composable
fun BoardPreview_LandscapeBlack() {
    BoardPreview(BoardOrientation.LANDSCAPE_BLACK, midGameState())
}

@Preview(showBackground = true, widthDp = 800, heightDp = 400)
@Composable
fun BoardPreview_Custom() {
    val exampleGameState = createGameState {
        setTurn(WHITE)
        setChecker("C2", WHITE, true)
        setChecker("C8", BLACK, true)
        setChecker("B1", WHITE, false)
        setChecker("B4", BLACK, false)

        // Agregar piezas extra para testing
        setChecker("C5", WHITE, true)
        setChecker("C11", BLACK, true)
    }
    val vm = viewModel<BoardViewModel>().apply {
        updateSelectedPiece("B1")
        updateValidMoves(listOf("B2", "A1", "B6"))
    }
    BoardPreview(orientation = BoardOrientation.LANDSCAPE_WHITE, gameState = exampleGameState, viewModel = vm)
}

@Preview(showBackground = true, widthDp = 400, heightDp = 500)
@Composable
fun BoardPreview_BlackPlayer() {
    TaratiTheme(true) {
        val exampleGameState = endGameState(BLACK)
        val vm = viewModel<BoardViewModel>().apply {
            updateSelectedPiece("A1")
            updateValidMoves(listOf("B1", "B2", "B3", "B4", "B5", "B6"))
        }
        BoardPreview(
            orientation = BoardOrientation.PORTRAIT_BLACK,
            gameState = exampleGameState,
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
        val vm = viewModel<BoardViewModel>().apply {
            updateSelectedPiece("C2")
            updateValidMoves(listOf("C9", "B4", "B5"))
        }
        BoardPreview(
            orientation = BoardOrientation.LANDSCAPE_BLACK,
            gameState = exampleGameState,
            labelsVisible = false,
            viewModel = vm
        )
    }
}

@Preview(showBackground = true, widthDp = 800, heightDp = 400)
@Composable
fun BoardPreview_Landscape() {
    TaratiTheme(true) {
        val exampleGameState = createGameState {}
        val vm = viewModel<BoardViewModel>().apply {
            updateSelectedPiece("C2")
            updateValidMoves(listOf("C3", "B2", "B1"))
        }
        BoardPreview(orientation = BoardOrientation.LANDSCAPE_WHITE, gameState = exampleGameState, viewModel = vm)
    }
}

@Preview(showBackground = true, widthDp = 800, heightDp = 400)
@Composable
fun BoardPreview_Landscape_Editing() {
    TaratiTheme(true) {
        val exampleGameState = createGameState {}
        val vm = viewModel<BoardViewModel>().apply {
            updateSelectedPiece("C2")
            updateValidMoves(listOf("C3", "B2", "B1"))
        }
        BoardPreview(
            orientation = BoardOrientation.LANDSCAPE_WHITE,
            gameState = exampleGameState,
            isEditing = true,
            viewModel = vm
        )
    }
}