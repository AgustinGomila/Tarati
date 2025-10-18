package com.agustin.tarati.ui.components.board

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import com.agustin.tarati.game.core.Color.BLACK
import com.agustin.tarati.game.core.Color.WHITE
import com.agustin.tarati.game.core.GameState
import com.agustin.tarati.game.core.Move
import com.agustin.tarati.game.core.createGameState
import com.agustin.tarati.game.logic.BoardOrientation
import com.agustin.tarati.ui.helpers.endGameState
import com.agustin.tarati.ui.helpers.initialGameStateWithUpgrades
import com.agustin.tarati.ui.helpers.midGameState
import com.agustin.tarati.ui.theme.TaratiTheme

data class BoardState(
    val gameState: GameState,
    val lastMove: Move? = null,
    val boardOrientation: BoardOrientation = BoardOrientation.PORTRAIT_WHITE,
    val labelsVisible: Boolean = true,
    val verticesVisible: Boolean = true,
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
    boardState: BoardState,
    events: BoardEvents,
    viewModel: BoardSelectionViewModel = viewModel(),
    animationViewModel: BoardAnimationViewModel = viewModel(),
    debug: Boolean = false
) {
    var prevGameState by remember { mutableStateOf<GameState?>(null) }
    val isAnimating by animationViewModel.isAnimating.collectAsState()

    // Efecto para sincronizar el estado inicial
    LaunchedEffect(Unit) {
        animationViewModel.syncState(boardState.gameState)
        prevGameState = boardState.gameState
    }

    // Efecto para procesar movimientos
    LaunchedEffect(boardState.lastMove, boardState.gameState, isAnimating) {
        val lastMove = boardState.lastMove
        if (lastMove != null && prevGameState != null && !isAnimating) {
            val currentGameState = boardState.gameState

            // Verificar que el movimiento es válido y diferente del estado anterior
            if (isValidMoveTransition(prevGameState!!, currentGameState, lastMove)) {
                val success = animationViewModel.processMove(
                    move = lastMove,
                    oldGameState = prevGameState!!,
                    newGameState = currentGameState,
                    debug = debug
                )

                if (success) {
                    prevGameState = currentGameState
                }
            }
        }
    }

    // Sincronizar estado cuando no hay animaciones y el estado cambió
    LaunchedEffect(boardState.gameState, isAnimating) {
        if (!isAnimating && boardState.gameState != prevGameState) {
            animationViewModel.syncState(boardState.gameState)
            prevGameState = boardState.gameState
        }
    }

    // Efecto para reset
    LaunchedEffect(boardState.newGame) {
        if (boardState.newGame) {
            viewModel.resetSelection()
            animationViewModel.reset()
            prevGameState = null
            events.onResetCompleted()
        }
    }

    val vmSelectedPiece by viewModel.selectedVertexId.collectAsState()
    val vmValidMoves by viewModel.validAdjacentVertexes.collectAsState()
    val visualState by animationViewModel.visualState.collectAsState()
    val animatedPieces by animationViewModel.animatedPieces.collectAsState()

    Box(
        modifier = modifier
            .background(MaterialTheme.colorScheme.surface)
    ) {
        BoardRenderer(
            modifier = Modifier.fillMaxSize(),
            selectedVertexId = vmSelectedPiece,
            validAdjacentVertexes = vmValidMoves,
            boardState = boardState.copy(
                gameState = GameState(
                    cobs = visualState.cobs,
                    currentTurn = visualState.currentTurn ?: boardState.gameState.currentTurn
                )
            ),
            animatedPieces = animatedPieces,
            tapEvents = object : TapEvents {
                override fun onSelected(from: String, valid: List<String>) {
                    if (!isAnimating) {
                        viewModel.updateSelectedVertex(from)
                        viewModel.updateValidAdjacentVertexes(valid)
                    }
                }

                override fun onMove(from: String, to: String) {
                    if (!isAnimating) {
                        events.onMove(from, to)
                        viewModel.resetSelection()
                    }
                }

                override fun onInvalid(from: String, valid: List<String>) {
                    if (!isAnimating) {
                        viewModel.updateSelectedVertex(from)
                        viewModel.updateValidAdjacentVertexes(valid)
                    }
                }

                override fun onEditPieceRequested(from: String) {
                    if (!isAnimating) {
                        events.onEditPiece(from)
                    }
                }

                override fun onCancel() {
                    if (!isAnimating) {
                        viewModel.resetSelection()
                    }
                }
            },
            onBoardSizeChange = { animationViewModel.updateBoardSize(it) },
            debug = debug
        )

        // Bloquear interacciones durante animaciones
        if (isAnimating) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .pointerInput(Unit) {
                        awaitPointerEventScope {
                            while (true) {
                                awaitPointerEvent()
                            }
                        }
                    }
            )
        }
    }
}

private fun isValidMoveTransition(oldState: GameState, newState: GameState, move: Move): Boolean {
    // Verificar que hay un cambio real en el estado
    if (oldState.cobs == newState.cobs) return false

    // Verificar que hay cambios consistentes
    val movedPieceExistsOldState = oldState.cobs.containsKey(move.from)
    val destinationFreeInOldState = !oldState.cobs.containsKey(move.to)
    val movedPieceExistsInNewState = newState.cobs.containsKey(move.to)
    val originFreeInNewState = !newState.cobs.containsKey(move.from)

    return movedPieceExistsInNewState &&
            originFreeInNewState &&
            destinationFreeInOldState &&
            movedPieceExistsOldState
}

// region Previews

@Composable
fun BoardPreview(
    orientation: BoardOrientation,
    gameState: GameState,
    labelsVisible: Boolean = true,
    verticesVisible: Boolean = true,
    isEditing: Boolean = false,
    viewModel: BoardSelectionViewModel = viewModel(),
    animationViewModel: BoardAnimationViewModel = viewModel(),
    debug: Boolean = false
) {
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
            viewModel = viewModel,
            animationViewModel = animationViewModel,
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
    BoardPreview(orientation = BoardOrientation.LANDSCAPE_WHITE, gameState = exampleGameState, viewModel = vm)
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
        BoardPreview(orientation = BoardOrientation.LANDSCAPE_WHITE, gameState = exampleGameState, viewModel = vm)
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
            viewModel = vm
        )
    }
}

// endregion Previews