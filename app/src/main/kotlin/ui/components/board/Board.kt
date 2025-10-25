package com.agustin.tarati.ui.components.board

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.agustin.tarati.game.core.CobColor
import com.agustin.tarati.game.core.GameState
import com.agustin.tarati.game.core.Move
import com.agustin.tarati.game.logic.BoardOrientation
import com.agustin.tarati.ui.components.board.animation.BoardAnimationViewModel
import com.agustin.tarati.ui.components.board.behaviors.BoardSelectionViewModel
import com.agustin.tarati.ui.components.board.draw.BoardRenderer
import com.agustin.tarati.ui.components.board.draw.drawBoardBackground
import com.agustin.tarati.ui.screens.settings.BoardVisualState
import com.agustin.tarati.ui.theme.BoardColors

data class BoardState(
    val gameState: GameState,
    val lastMove: Move? = null,
    val boardOrientation: BoardOrientation = BoardOrientation.PORTRAIT_WHITE,
    val boardVisualState: BoardVisualState,
    val newGame: Boolean = false,
    val aiEnabled: Boolean = true,
    val isEditing: Boolean = false,
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

data class CreateBoardState(
    val gameState: GameState,
    val lastMove: Move?,
    val playerSide: CobColor,
    val aiEnabled: Boolean,
    val isEditing: Boolean,
    val isTutorialActive: Boolean,
    val isAIThinking: Boolean,
    val boardOrientation: BoardOrientation,
    val editBoardOrientation: BoardOrientation,
    val boardVisualState: BoardVisualState
)

fun boardEvents(state: CreateBoardState, events: BoardEvents): BoardEvents = object : BoardEvents {
    override fun onMove(from: String, to: String) {
        if (!state.isEditing) {
            events.onMove(from, to)
        }
    }

    override fun onEditPiece(from: String) = events.onEditPiece(from)
    override fun onResetCompleted() = events.onResetCompleted()
}

@Composable
fun CreateBoard(
    modifier: Modifier = Modifier,
    state: CreateBoardState,
    events: BoardEvents,
    boardAnimationViewModel: BoardAnimationViewModel = viewModel(),
    boardVisualState: BoardVisualState,
    boardColors: BoardColors,
    tutorial: @Composable () -> Unit,
    content: @Composable () -> Unit,
    turnIndicator: @Composable (modifier: Modifier) -> Unit,
    debug: Boolean = false,
) {
    // Construir el estado para Board
    val boardState = BoardState(
        gameState = state.gameState,
        aiEnabled = state.aiEnabled,
        lastMove = state.lastMove,
        boardOrientation = when {
            state.isEditing -> state.editBoardOrientation
            else -> state.boardOrientation
        },
        boardVisualState = boardVisualState,
        isEditing = state.isEditing
    )

    Box(
        modifier
            .fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        Board(
            modifier = Modifier.fillMaxSize(),
            playerSide = state.playerSide,
            boardState = boardState,
            boardColors = boardColors,
            events = boardEvents(state, events),
            animationViewModel = boardAnimationViewModel,
            debug = debug,
        )

        when {
            state.isEditing -> content()

            state.isTutorialActive -> tutorial()

            else -> turnIndicator(Modifier.align(Alignment.TopEnd))
        }
    }
}

@Composable
fun Board(
    modifier: Modifier = Modifier,
    playerSide: CobColor,
    boardState: BoardState,
    boardColors: BoardColors,
    events: BoardEvents,
    selectViewModel: BoardSelectionViewModel = viewModel(),
    animationViewModel: BoardAnimationViewModel = viewModel(),
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
                regionsVisible = boardState.boardVisualState.regionsVisibles,
                perimeterVisible = boardState.boardVisualState.perimeterVisible
            )
        }

        BoardRenderer(
            modifier = Modifier.fillMaxSize(),
            playerSide = playerSide,
            boardState = boardState,
            colors = boardColors,
            tapEvents = tapEvents(selectViewModel, events),
            selectViewModel = selectViewModel,
            animationViewModel = animationViewModel,
            onBoardSizeChange = { animationViewModel.updateBoardSize(it) },
            onResetCompleted = events::onResetCompleted,
            debug = debug
        )
    }
}

fun tapEvents(selectViewModel: BoardSelectionViewModel, events: BoardEvents): TapEvents = object : TapEvents {
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
}