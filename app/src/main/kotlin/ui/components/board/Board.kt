package com.agustin.tarati.ui.components.board

import android.graphics.Paint
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.agustin.tarati.game.core.Checker
import com.agustin.tarati.game.core.Color.BLACK
import com.agustin.tarati.game.core.Color.WHITE
import com.agustin.tarati.game.core.GameBoard.adjacencyMap
import com.agustin.tarati.game.core.GameBoard.edges
import com.agustin.tarati.game.core.GameBoard.findClosestVertex
import com.agustin.tarati.game.core.GameBoard.getVisualPosition
import com.agustin.tarati.game.core.GameBoard.isForwardMove
import com.agustin.tarati.game.core.GameBoard.isValidMove
import com.agustin.tarati.game.core.GameBoard.vertices
import com.agustin.tarati.game.core.GameState
import com.agustin.tarati.game.logic.BoardOrientation
import com.agustin.tarati.game.logic.createGameState
import com.agustin.tarati.ui.helpers.endGameState
import com.agustin.tarati.ui.helpers.initialGameStateWithUpgrades
import com.agustin.tarati.ui.helpers.midGameState
import com.agustin.tarati.ui.theme.AppColors.getBoardColors
import com.agustin.tarati.ui.theme.BoardColors
import com.agustin.tarati.ui.theme.TaratiTheme

data class BoardState(
    val gameState: GameState,
    val boardOrientation: BoardOrientation = BoardOrientation.PORTRAIT_WHITE,
    val labelsVisible: Boolean = true,
    val newGame: Boolean = false,
    val isEditing: Boolean = false
)

interface BoardEvents {
    fun onMove(from: String, to: String)
    fun onEditPiece(vertexId: String)
    fun onResetCompleted()
}

@Composable
fun Board(
    modifier: Modifier = Modifier,
    state: BoardState,
    events: BoardEvents,
    viewModel: BoardViewModel = viewModel(),
) {
    LaunchedEffect(state.newGame) {
        if (state.newGame) {
            viewModel.resetSelection()
            events.onResetCompleted()
        }
    }

    val vmSelectedPiece by viewModel.selectedPiece.collectAsState()
    val vmValidMoves by viewModel.validMoves.collectAsState()

    // Calcular tamaño visual basado en el contexto
    val density = LocalDensity.current
    val vWidth = with(density) { 60.dp.toPx() }
    val boardColors = getBoardColors()

    Box(
        modifier = modifier
            .background(MaterialTheme.colorScheme.surface)
            .pointerInput(state.gameState, vmSelectedPiece, state.boardOrientation, state.isEditing) {
                detectTapGestures { offset ->
                    val closestVertex = findClosestVertex(
                        tapOffset = offset,
                        canvasWidth = size.width.toFloat(),
                        canvasHeight = size.height.toFloat(),
                        maxTapDistance = vWidth / 3,
                        orientation = state.boardOrientation
                    )

                    closestVertex?.let { logicalVertexId ->
                        if (state.isEditing) {
                            events.onEditPiece(logicalVertexId)
                        } else {
                            handleTap(
                                tappedVertex = logicalVertexId,
                                gameState = state.gameState,
                                selectedPiece = vmSelectedPiece,
                                onMove = events::onMove,
                                viewModel = viewModel
                            )
                        }
                    }
                }
            }) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawBoardContent(
                gameState = state.gameState,
                orientation = state.boardOrientation,
                labelsVisibles = state.labelsVisible,
                vWidth = vWidth,
                selectedPiece = vmSelectedPiece,
                validMoves = vmValidMoves,
                canvasSize = size,
                colors = boardColors,
            )
        }
    }
}

fun handleTap(
    tappedVertex: String,
    gameState: GameState,
    selectedPiece: String?,
    onMove: (String, String) -> Unit,
    viewModel: BoardViewModel
) {
    val debug = viewModel.isDebug

    if (debug) println("TAP HANDLED: vertex=$tappedVertex, selectedPiece=$selectedPiece")

    if (selectedPiece == null) {
        // Seleccionar pieza
        selectPiece(
            gameState = gameState,
            tappedVertex = tappedVertex,
            onSelected = { piece, moves ->
                viewModel.updateSelectedPiece(piece)
                viewModel.updateValidMoves(moves)
            })
    } else {
        // Mover pieza
        if (debug) println("Attempting move from $selectedPiece to $tappedVertex")

        if (tappedVertex != selectedPiece) {
            movePiece(
                gameState = gameState,
                selectedPiece = selectedPiece,
                tappedVertex = tappedVertex,
                onMove = { from, to ->
                    onMove(from, to)
                    viewModel.resetSelection()
                },
                onInvalid = { from, valid ->
                    viewModel.updateSelectedPiece(from)
                    viewModel.updateValidMoves(valid)
                },
                onCancel = { viewModel.resetSelection() }
            )
        } else {
            // Tocar la misma pieza: deseleccionar
            if (debug) println("Deselecting piece")
            viewModel.resetSelection()
        }
    }
}

private fun movePiece(
    gameState: GameState,
    selectedPiece: String,
    tappedVertex: String,
    onMove: (from: String, to: String) -> Unit,
    onInvalid: (from: String, valid: List<String>) -> Unit,
    onCancel: () -> Unit,
    debug: Boolean = false
) {
    if (tappedVertex != selectedPiece) {
        val isValid = isValidMove(gameState, selectedPiece, tappedVertex)
        if (debug) println("Move validation: $selectedPiece -> $tappedVertex = $isValid")

        if (isValid) {
            if (debug) println("Calling onMove with: $selectedPiece, $tappedVertex")
            onMove(selectedPiece, tappedVertex)
        } else {
            if (debug) println("Move is invalid")
            // Si el movimiento es inválido, seleccionar la nueva pieza si es del jugador actual
            val checker = gameState.checkers[tappedVertex]
            if (checker != null && checker.color == gameState.currentTurn) {
                val validMoves = adjacencyMap[tappedVertex]?.filter { to ->
                    !gameState.checkers.containsKey(to) && (checker.isUpgraded || isForwardMove(
                        checker.color,
                        tappedVertex,
                        to
                    ))
                } ?: emptyList()
                onInvalid(tappedVertex, validMoves)
            } else {
                onCancel()
            }
        }
    } else {
        // Tocar la misma pieza: deseleccionar
        if (debug) println("Deselecting piece")
        onCancel()
    }
}

private fun selectPiece(
    gameState: GameState,
    tappedVertex: String,
    onSelected: (String, List<String>) -> Unit,
    debug: Boolean = false
) {
    val checker = gameState.checkers[tappedVertex]
    if (debug) println("Checking piece: $checker at $tappedVertex, currentTurn: ${gameState.currentTurn}")

    if (checker != null && checker.color == gameState.currentTurn) {

        if (debug) println("Piece selected: $tappedVertex")

        // Calcular movimientos válidos
        val validMoves = adjacencyMap[tappedVertex]?.filter { to ->
            val isValid = !gameState.checkers.containsKey(to) && (checker.isUpgraded || isForwardMove(
                checker.color,
                tappedVertex,
                to
            ))
            if (debug) println("Move $tappedVertex -> $to: valid=$isValid")
            isValid
        } ?: emptyList()
        onSelected(tappedVertex, validMoves)
        if (debug) println("Highlighted moves: $validMoves")
    } else {
        if (debug) println("Cannot select: checker=$checker, currentTurn=${gameState.currentTurn}")
    }
}

private fun DrawScope.drawBoardContent(
    gameState: GameState,
    orientation: BoardOrientation,
    labelsVisibles: Boolean,
    vWidth: Float,
    selectedPiece: String?,
    validMoves: List<String>,
    canvasSize: Size,
    colors: BoardColors
) {
    // Fondo
    drawRect(color = colors.backgroundColor)

    // Calcular área cuadrada para el tablero (igual que en getVisualPosition)
    val minDimension = minOf(canvasSize.width, canvasSize.height)
    val squareSize = minDimension * 0.8f
    val offsetX = (canvasSize.width - squareSize) / 2
    val offsetY = (canvasSize.height - squareSize) / 2
    val boardCenter = Offset(offsetX + squareSize / 2, offsetY + squareSize / 2)

    // Círculo del tablero - usar la misma área cuadrada
    drawCircle(
        color = colors.boardBackgroundColor, center = boardCenter, radius = squareSize / 2
    )

    // Aristas
    drawEdges(
        drawScope = this,
        canvasSize = canvasSize,
        orientation = orientation,
        colors = colors
    )

    // Vértices
    drawVertices(
        drawScope = this,
        labelsVisibles = labelsVisibles,
        canvasSize = canvasSize,
        vWidth = vWidth,
        gameState = gameState,
        orientation = orientation,
        selectedPiece = selectedPiece,
        highlightedMoves = validMoves,
        colors = colors
    )

    // Piezas
    gameState.checkers.forEach { (vertexId, checker) ->
        val pos = getVisualPosition(vertexId, canvasSize.width, canvasSize.height, orientation)
        val baseRadius = vWidth / 5

        val (pieceColor, borderColor) = when (checker.color) {
            WHITE -> colors.whitePieceColor to colors.whitePieceBorderColor
            BLACK -> colors.blackPieceColor to colors.blackPieceBorderColor
        }

        // Dibujar pieza base
        drawPiece(
            drawScope = this,
            pos = pos,
            vertexId = vertexId,
            selectedPiece = selectedPiece,
            checker = checker,
            baseRadius = baseRadius,
            pieceColor = pieceColor,
            borderColor = borderColor,
            colors = colors
        )
    }
}

fun drawPiece(
    drawScope: DrawScope,
    pos: Offset,
    vertexId: String,
    selectedPiece: String?,
    checker: Checker,
    baseRadius: Float,
    pieceColor: Color,
    borderColor: Color,
    colors: BoardColors
) {
    drawScope.drawCircle(color = borderColor, center = pos, radius = baseRadius, style = Stroke(width = 3f))
    drawScope.drawCircle(color = pieceColor, center = pos, radius = baseRadius * 0.8f)

    // Indicador de mejora
    if (checker.isUpgraded) {
        val upgradeColor =
            if (checker.color == WHITE) colors.blackPieceColor
            else colors.whitePieceColor

        drawScope.drawCircle(
            color = upgradeColor,
            center = pos,
            radius = baseRadius * 0.6f,
            style = Stroke(width = 2f)
        )
        drawScope.drawCircle(
            color = upgradeColor,
            center = pos,
            radius = baseRadius * 0.2f
        )
    }

    // Resaltado de selección
    if (vertexId == selectedPiece) {
        drawScope.drawCircle(
            color = colors.selectionIndicatorColor,
            center = pos,
            radius = baseRadius * 1.2f,
            style = Stroke(width = 3f)
        )
    }
}

fun drawVertices(
    drawScope: DrawScope,
    labelsVisibles: Boolean,
    canvasSize: Size,
    vWidth: Float,
    gameState: GameState,
    orientation: BoardOrientation,
    selectedPiece: String?,
    highlightedMoves: List<String>,
    colors: BoardColors
) {
    vertices.forEach { vertexId ->
        val pos = getVisualPosition(vertexId, canvasSize.width, canvasSize.height, orientation)
        val checker = gameState.checkers[vertexId]

        val vertexColor = when {
            vertexId == selectedPiece -> colors.vertexSelectedColor
            highlightedMoves.contains(vertexId) -> colors.vertexHighlightColor
            checker != null -> colors.vertexOccupiedColor
            else -> colors.vertexDefaultColor
        }

        drawScope.drawCircle(color = vertexColor, center = pos, radius = vWidth / 10)

        // Borde del vértice
        drawScope.drawCircle(
            color = colors.textColor.copy(alpha = 0.3f), center = pos, radius = vWidth / 10, style = Stroke(width = 1f)
        )

        if (labelsVisibles) {
            // Etiqueta del vértice
            drawScope.drawContext.canvas.nativeCanvas.apply {
                drawText(
                    vertexId, pos.x - vWidth / 5, pos.y - vWidth / 5, Paint().apply {
                        color = colors.textColor.hashCode()
                        textSize = vWidth / 6
                        isAntiAlias = true
                    })
            }
        }
    }
}

fun drawEdges(drawScope: DrawScope, canvasSize: Size, orientation: BoardOrientation, colors: BoardColors) {
    edges.forEach { (from, to) ->
        val fromPos = getVisualPosition(from, canvasSize.width, canvasSize.height, orientation)
        val toPos = getVisualPosition(to, canvasSize.width, canvasSize.height, orientation)
        drawScope.drawLine(color = colors.edgeColor, start = fromPos, end = toPos, strokeWidth = 6f)
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
                boardOrientation = orientation,
                labelsVisible = labelsVisible,
                isEditing = isEditing,
            ),
            events = object : BoardEvents {
                override fun onMove(from: String, to: String) {
                    if (debug) println("Move from $from to $to")
                }

                override fun onEditPiece(vertexId: String) {
                    if (debug) println("Edit piece at $vertexId")
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