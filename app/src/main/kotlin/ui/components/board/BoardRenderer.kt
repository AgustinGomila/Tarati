package com.agustin.tarati.ui.components.board

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
import androidx.compose.ui.geometry.Offset
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
import com.agustin.tarati.ui.theme.AppColors.getBoardColors
import com.agustin.tarati.ui.theme.BoardColors
import kotlin.math.roundToInt

@Composable
fun BoardRenderer(
    modifier: Modifier = Modifier,
    playerSide: Color,
    selectViewModel: BoardSelectionViewModel,
    animationViewModel: BoardAnimationViewModel,
    boardState: BoardState,
    tapEvents: TapEvents,
    onBoardSizeChange: (Size) -> Unit,
    onResetCompleted: () -> Unit,
    debug: Boolean = false
) {
    var prevGameState by remember { mutableStateOf<GameState?>(null) }

    val isAnimating by animationViewModel.isAnimating.collectAsState()
    val selectedVertexId by selectViewModel.selectedVertexId.collectAsState()
    val validAdjacentVertexes by selectViewModel.validAdjacentVertexes.collectAsState()

    val visualState by animationViewModel.visualState.collectAsState()
    val animatedPieces by animationViewModel.animatedPieces.collectAsState()
    val currentHighlight by animationViewModel.currentHighlights.collectAsState()

    val stateChangeDetector = remember { StateChangeDetector() }

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

    // Efecto para procesar movimientos
    LaunchedEffect(boardState.lastMove, boardState.gameState, isAnimating) {
        val lastMove = boardState.lastMove
        if (lastMove != null && prevGameState != null && !isAnimating) {
            val currentGameState = boardState.gameState

            // Verificar que el movimiento es válido y diferente del estado anterior
            if (stateChangeDetector.shouldAnimateMove(prevGameState!!, currentGameState, lastMove)) {
                val success = animationViewModel.processMove(
                    move = lastMove,
                    oldGameState = prevGameState!!,
                    newGameState = currentGameState
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

    val boardState = boardState.copy(
        gameState = GameState(
            cobs = visualState.cobs,
            currentTurn = visualState.currentTurn ?: boardState.gameState.currentTurn
        )
    )

    val colors = getBoardColors()
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
                containerWidthPx = coords.size.width
                containerHeightPx = coords.size.height
                onBoardSizeChange(coords.size.toSize())
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
        Canvas(modifier = Modifier.matchParentSize()) {
            drawRect(color = colors.backgroundColor)
            val size = size

            drawCircle(
                color = colors.boardBackgroundColor,
                radius = minOf(size.width, size.height) * 0.8f / 2,
                center = Offset(size.width / 2, size.height / 2)
            )

            drawEdges(
                canvasSize = size,
                orientation = orientation,
                colors = colors
            )

            drawVertices(
                canvasSize = size,
                vWidth = visualWidth,
                selectedVertexId = selectedVertexId,
                adjacentVertexes = validAdjacentVertexes,
                boardState = boardState,
                colors = colors
            )

            currentHighlight.forEach { highlight ->
                when (highlight) {
                    is HighlightAnimation.Vertex -> {
                        drawVertexHighlight(
                            highlight = highlight.highlight,
                            canvasSize = size,
                            orientation = orientation,
                            colors = colors
                        )
                    }

                    is HighlightAnimation.Edge -> {
                        drawEdgeHighlight(
                            highlight = highlight.highlight,
                            canvasSize = size,
                            orientation = orientation,
                            colors = colors
                        )
                    }

                    is HighlightAnimation.Pause -> {
                        // No dibujar nada durante pausas
                    }
                }
            }
        }

        // Dibujar piezas estáticas (excluyendo las que están siendo animadas)
        boardState.gameState.cobs.forEach { (vertexId, cob) ->
            if (!animatedPieces.containsKey(vertexId)) {
                key(vertexId) {
                    StaticPieceComposable(
                        vertexId = vertexId,
                        cob = cob,
                        containerWidth = containerWidthPx,
                        containerHeight = containerHeightPx,
                        orientation = orientation,
                        selectedPiece = selectedVertexId,
                        colors = colors
                    )
                }
            }
        }

        // Dibujar piezas animadas
        animatedPieces.values.forEach { animatedPiece ->
            key(
                animatedPiece.vertexId + animatedPiece.animationProgress +
                        animatedPiece.upgradeProgress + animatedPiece.conversionProgress
            ) {
                AnimatedPieceComposable(
                    animatedPiece = animatedPiece,
                    containerWidth = containerWidthPx,
                    containerHeight = containerHeightPx,
                    orientation = orientation,
                    selectedPiece = selectedVertexId,
                    colors = colors
                )
            }
        }
    }
}

@Composable
fun AnimatedPieceComposable(
    animatedPiece: AnimatedPiece,
    containerWidth: Int,
    containerHeight: Int,
    orientation: BoardOrientation,
    selectedPiece: String?,
    colors: BoardColors
) {
    val density = LocalDensity.current
    val piecePx = with(density) { (60.dp.toPx() / 5f) }
    val pieceDp = with(density) { piecePx.toDp() }

    // Usar animación inmediata para mejor rendimiento
    val currentPos = getVisualPosition(
        animatedPiece.currentPos,
        containerWidth.toFloat(),
        containerHeight.toFloat(),
        orientation
    )
    val targetPos = getVisualPosition(
        animatedPiece.targetPos,
        containerWidth.toFloat(),
        containerHeight.toFloat(),
        orientation
    )

    val currentX = currentPos.x + (targetPos.x - currentPos.x) * animatedPiece.animationProgress
    val currentY = currentPos.y + (targetPos.y - currentPos.y) * animatedPiece.animationProgress

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
            drawAnimatedPiece(
                selectedVertexId = selectedPiece,
                vertexId = animatedPiece.vertexId,
                animatedPiece = animatedPiece,
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
            drawPiece(
                selectedVertexId = selectedPiece,
                vertexId = vertexId,
                cob = cob,
                colors = colors
            )
        }
    }
}