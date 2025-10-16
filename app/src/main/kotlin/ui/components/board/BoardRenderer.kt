package com.agustin.tarati.ui.components.board

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.agustin.tarati.game.core.Checker
import com.agustin.tarati.game.core.GameBoard.findClosestVertex
import com.agustin.tarati.game.core.GameBoard.getVisualPosition
import com.agustin.tarati.ui.theme.AppColors.getBoardColors
import kotlin.math.roundToInt

data class VisualPiece(
    val vertexId: String,
    val checker: Checker,
    val pos: Offset,
)

/**
 * BoardRenderer: fondo + vertices/edges (Canvas) + piezas overlay animadas

 * - selectedPiece / validMoves: piezas y vértices resaltados
 * - boardState: contiene gameState: fuente de verdad
 *        y modificadores visuales y de comportamiento
 * - tapEvents: eventos sobre el tablero
 */
@Composable
fun BoardRenderer(
    modifier: Modifier = Modifier,
    selectedPiece: String?,
    validMoves: List<String>,
    boardState: BoardState,
    tapEvents: TapEvents,
    debug: Boolean = false
) {
    val colors = getBoardColors()
    val density = LocalDensity.current
    val visualWidth by lazy { (with(density) { 60.dp.toPx() }) }

    // Tamaño del área del tablero en px (onGloballyPositioned)
    var containerWidthPx by remember { mutableIntStateOf(0) }
    var containerHeightPx by remember { mutableIntStateOf(0) }

    val gameState = boardState.gameState
    val orientation = boardState.boardOrientation
    val editorMode = boardState.isEditing
    val lastMove = boardState.lastMove

    if (debug) println("Last movement: $lastMove")

    // Derivar lista de entidades visuales (posiciones absolutas en px)
    val visualPieces by remember(gameState, orientation, containerWidthPx, containerHeightPx) {
        derivedStateOf {
            if (containerWidthPx == 0 || containerHeightPx == 0) return@derivedStateOf emptyList()
            val w = containerWidthPx.toFloat()
            val h = containerHeightPx.toFloat()
            gameState.checkers.mapNotNull { (vertexId, checker) ->
                val offset = getVisualPosition(vertexId, w, h, orientation)
                VisualPiece(
                    vertexId = vertexId,
                    checker = Checker(
                        color = checker.color,
                        isUpgraded = checker.isUpgraded
                    ),
                    pos = offset
                )
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .onGloballyPositioned { coords ->
                containerWidthPx = coords.size.width
                containerHeightPx = coords.size.height
            }
            .pointerInput(gameState, selectedPiece, orientation, editorMode) {
                detectTapGestures { offset ->
                    val closestVertex = findClosestVertex(
                        tapOffset = offset,
                        canvasWidth = size.width.toFloat(),
                        canvasHeight = size.height.toFloat(),
                        maxTapDistance = visualWidth / 3,
                        orientation = orientation
                    )

                    closestVertex?.let { logicalVertexId ->
                        if (editorMode) {
                            tapEvents.onEditPieceRequested(logicalVertexId)
                        } else {
                            handleTap(
                                tappedVertex = logicalVertexId,
                                gameState = gameState,
                                selectedPiece = selectedPiece,
                                tapEvents = tapEvents,
                                debug = debug
                            )
                        }
                    }
                }
            }
    ) {
        // Dibujar fondo/edges/vertices con Canvas
        Canvas(modifier = Modifier.matchParentSize()) {

            // Fondo
            drawRect(color = colors.backgroundColor)
            val canvasSize = size

            // Círculo del tablero
            drawCircle(
                color = colors.boardBackgroundColor, radius = minOf(size.width, size.height) * 0.8f / 2,
                center = Offset(size.width / 2, size.height / 2)
            )

            drawEdges(
                drawScope = this,
                canvasSize = canvasSize,
                orientation = orientation,
                colors = colors
            )

            drawVertices(
                drawScope = this,
                canvasSize = canvasSize,
                vWidth = visualWidth,
                selectedPiece = selectedPiece,
                validMoves = validMoves,
                boardState = boardState,
                colors = colors
            )
        }

        // Overlay: piezas como composables independientes posicionadas
        visualPieces.forEach { visualPiece ->
            key(visualPiece.vertexId) {
                // animar x,y objetivo
                val animX by animateFloatAsState(
                    targetValue = visualPiece.pos.x,
                    animationSpec = tween(durationMillis = 260)
                )
                val animY by animateFloatAsState(
                    targetValue = visualPiece.pos.y,
                    animationSpec = tween(durationMillis = 260)
                )

                // Tamaño visual de la pieza
                val piecePx = with(LocalDensity.current) { (60.dp.toPx() / 5f) }
                val pieceDp = with(LocalDensity.current) { piecePx.toDp() }

                // Posicionar centrando la pieza
                val offset = IntOffset(
                    (animX - piecePx).roundToInt(),
                    (animY - piecePx).roundToInt()
                )

                // Un Canvas pequeño para la pieza. Esto permite escalar/alfa.
                Box(
                    modifier = Modifier
                        .offset { offset }
                        .size(pieceDp * 2f) // tamaño razonable para el "slot" de la pieza
                ) {
                    Canvas(modifier = Modifier.matchParentSize()) {
                        drawPiece(selectedPiece, visualPiece.vertexId, visualPiece.checker, colors)
                    }
                }
            }
        }
    }
}