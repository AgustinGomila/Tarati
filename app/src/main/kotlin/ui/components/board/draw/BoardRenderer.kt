package com.agustin.tarati.ui.components.board.draw

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.toSize
import com.agustin.tarati.game.core.Cob
import com.agustin.tarati.game.core.Color
import com.agustin.tarati.game.core.GameBoard.getVisualPosition
import com.agustin.tarati.game.core.GameState
import com.agustin.tarati.game.logic.BoardOrientation
import com.agustin.tarati.game.logic.isEmptyBoard
import com.agustin.tarati.game.logic.shouldAnimateMove
import com.agustin.tarati.ui.components.board.BoardState
import com.agustin.tarati.ui.components.board.TapEvents
import com.agustin.tarati.ui.components.board.animation.AnimatedCob
import com.agustin.tarati.ui.components.board.animation.BoardAnimationViewModel
import com.agustin.tarati.ui.components.board.animation.HighlightAnimation
import com.agustin.tarati.ui.components.board.behaviors.BoardSelectionViewModel
import com.agustin.tarati.ui.components.board.behaviors.tapGestures
import com.agustin.tarati.ui.theme.BoardColors
import kotlin.math.roundToInt

@Composable
fun BoardRenderer(
    modifier: Modifier = Modifier,
    playerSide: Color,
    boardState: BoardState,
    colors: BoardColors,
    tapEvents: TapEvents,
    selectViewModel: BoardSelectionViewModel,
    animationViewModel: BoardAnimationViewModel,
    onBoardSizeChange: (Size) -> Unit,
    onResetCompleted: () -> Unit,
    debug: Boolean = false
) {
    var prevGameState by remember { mutableStateOf<GameState?>(null) }

    val selectedVertexId by selectViewModel.selectedVertexId.collectAsState()
    val validAdjacentVertexes by selectViewModel.validAdjacentVertexes.collectAsState()

    val visualState by animationViewModel.visualState.collectAsState()
    val animatedPieces by animationViewModel.animatedPieces.collectAsState()
    val currentHighlight by animationViewModel.currentHighlights.collectAsState()

    // Efecto para sincronizar el estado inicial
    LaunchedEffect(Unit) {
        if (boardState.newGame) {
            selectViewModel.resetSelection()
            animationViewModel.reset()
            prevGameState = null
            onResetCompleted()
        } else {
            animationViewModel.syncState(boardState.gameState)
            prevGameState = boardState.gameState
        }
    }

    // Efecto para actualizar el estado del tablero
    LaunchedEffect(boardState.gameState) {
        val gameState = boardState.gameState

        // Tablero sin piezas
        if (gameState.isEmptyBoard()) {
            selectViewModel.resetSelection()
            animationViewModel.reset()
            prevGameState = null
            return@LaunchedEffect
        }

        // Sincronizar si el estado cambió
        if (gameState != prevGameState) {
            animationViewModel.syncState(gameState)
            prevGameState = gameState
        }
    }

    // Efecto para procesar el último movimiento y actualizar el tablero
    LaunchedEffect(boardState.lastMove) {
        val lastMove = boardState.lastMove
        val prevState = prevGameState

        if (lastMove == null || prevState == null) return@LaunchedEffect

        val newState = boardState.gameState

        // Procesar el último movimiento si es necesario
        if (!prevState.shouldAnimateMove(newState, lastMove)) return@LaunchedEffect

        val success = animationViewModel.processMove(
            move = lastMove,
            oldGameState = prevState,
            newGameState = newState
        )

        if (success) {
            prevGameState = newState
        }
    }

    val currentBoardState = boardState.copy(
        gameState = GameState(
            cobs = visualState.cobs,
            currentTurn = visualState.currentTurn ?: boardState.gameState.currentTurn
        )
    )

    val density = LocalDensity.current
    val visualWidth by remember { mutableFloatStateOf(with(density) { 60.dp.toPx() }) }

    var containerWidthPx by remember { mutableIntStateOf(0) }
    var containerHeightPx by remember { mutableIntStateOf(0) }

    val orientation = boardState.boardOrientation
    val editorMode = boardState.isEditing

    Box(
        modifier = modifier
            .fillMaxSize()
            .onGloballyPositioned { coords ->
                val newSize = coords.size.toSize()
                containerWidthPx = coords.size.width
                containerHeightPx = coords.size.height
                onBoardSizeChange(newSize)
            }
            .pointerInput(
                visualWidth,
                boardState.gameState,
                playerSide,
                selectedVertexId,
                orientation,
                editorMode,
                tapEvents,
                debug
            ) {
                tapGestures(
                    visualWidth = visualWidth,
                    gameState = boardState.gameState,
                    playerSide = playerSide,
                    from = selectedVertexId,
                    orientation = orientation,
                    editorMode = editorMode,
                    tapEvents = tapEvents,
                    debug = debug
                )
            }
    ) {
        // Canvas dinámico - se redibuja frecuentemente
        Canvas(modifier = Modifier.matchParentSize()) {
            drawEdges(
                canvasSize = size,
                orientation = boardState.boardOrientation,
                boardState = currentBoardState,
                colors = colors
            )

            drawVertices(
                canvasSize = size,
                vWidth = visualWidth,
                selectedVertexId = selectedVertexId,
                adjacentVertexes = validAdjacentVertexes,
                boardState = currentBoardState,
                colors = colors
            )

            currentHighlight.forEach { highlight ->
                when (highlight) {
                    is HighlightAnimation.Vertex -> {
                        drawVertexHighlight(
                            highlight = highlight.highlight,
                            canvasSize = size,
                            orientation = boardState.boardOrientation,
                        )
                    }

                    is HighlightAnimation.Edge -> {
                        drawEdgeHighlight(
                            highlight = highlight.highlight,
                            canvasSize = size,
                            orientation = boardState.boardOrientation,
                        )
                    }

                    is HighlightAnimation.Region -> {
                        drawRegionHighlight(
                            highlight = highlight.highlight,
                            canvasSize = size,
                            orientation = boardState.boardOrientation,
                        )
                    }

                    is HighlightAnimation.Pause -> {
                        // No dibujar nada durante pausas
                    }
                }
            }
        }

        // Piezas estáticas (excluyendo las que están siendo animadas)
        boardState.gameState.cobs.forEach { (vertexId, cob) ->
            if (!animatedPieces.containsKey(vertexId)) {
                key(vertexId) {
                    StaticPieceComposable(
                        vertexId = vertexId,
                        cob = cob,
                        containerWidth = containerWidthPx,
                        containerHeight = containerHeightPx,
                        orientation = boardState.boardOrientation,
                        selectedPiece = selectedVertexId,
                        colors = colors
                    )
                }
            }
        }

        // Piezas animadas
        animatedPieces.values.forEach { animatedPiece ->
            key(
                animatedPiece.vertexId + animatedPiece.animationProgress +
                        animatedPiece.upgradeProgress + animatedPiece.conversionProgress
            ) {
                AnimatedPieceComposable(
                    animatedCob = animatedPiece,
                    containerWidth = containerWidthPx,
                    containerHeight = containerHeightPx,
                    orientation = boardState.boardOrientation,
                    selectedPiece = selectedVertexId,
                    colors = colors
                )
            }
        }
    }
}

@Composable
fun AnimatedPieceComposable(
    animatedCob: AnimatedCob,
    containerWidth: Int,
    containerHeight: Int,
    orientation: BoardOrientation,
    selectedPiece: String?,
    colors: BoardColors
) {
    val density = LocalDensity.current
    val piecePx = with(density) { (60.dp.toPx() / 5f) }
    val pieceDp = with(density) { piecePx.toDp() }

    val currentPos = getVisualPosition(
        animatedCob.currentPos,
        containerWidth.toFloat(),
        containerHeight.toFloat(),
        orientation
    )
    val targetPos = getVisualPosition(
        animatedCob.targetPos,
        containerWidth.toFloat(),
        containerHeight.toFloat(),
        orientation
    )

    val currentX = currentPos.x + (targetPos.x - currentPos.x) * animatedCob.animationProgress
    val currentY = currentPos.y + (targetPos.y - currentPos.y) * animatedCob.animationProgress

    val offset = IntOffset(
        (currentX - piecePx).roundToInt(),
        (currentY - piecePx).roundToInt()
    )

    Box(
        modifier = Modifier
            .offset { offset }
            .size(pieceDp * 2f)
    ) {
        Canvas(modifier = Modifier.matchParentSize()) {
            drawAnimatedCob(
                selectedVertexId = selectedPiece,
                vertexId = animatedCob.vertexId,
                animatedCob = animatedCob,
                colors = colors
            )
        }
    }
}

@Composable
fun StaticPieceComposable(
    vertexId: String,
    cob: Cob,
    containerWidth: Int,
    containerHeight: Int,
    orientation: BoardOrientation,
    selectedPiece: String?,
    colors: BoardColors
) {
    val density = LocalDensity.current
    val piecePx = with(density) { (60.dp.toPx() / 5f) }
    val pieceDp = with(density) { piecePx.toDp() }

    val pos = getVisualPosition(
        vertexId,
        containerWidth.toFloat(),
        containerHeight.toFloat(),
        orientation
    )

    val offset = IntOffset(
        (pos.x - piecePx).roundToInt(),
        (pos.y - piecePx).roundToInt()
    )

    Box(
        modifier = Modifier
            .offset { offset }
            .size(pieceDp * 2f)
    ) {
        Canvas(modifier = Modifier.matchParentSize()) {
            drawCob(
                selectedVertexId = selectedPiece,
                vertexId = vertexId,
                cob = cob,
                colors = colors
            )
        }
    }
}