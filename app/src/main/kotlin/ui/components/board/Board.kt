package com.agustin.tarati.ui.components.board

import android.graphics.Paint
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.agustin.tarati.game.ai.TaratiAI
import com.agustin.tarati.game.core.Color
import com.agustin.tarati.game.core.Color.BLACK
import com.agustin.tarati.game.core.Color.WHITE
import com.agustin.tarati.game.core.GameBoard
import com.agustin.tarati.game.core.GameBoard.findClosestVertex
import com.agustin.tarati.game.core.GameBoard.getVisualPosition
import com.agustin.tarati.game.core.GameBoard.logicalToVisual
import com.agustin.tarati.game.core.GameBoard.visualToLogical
import com.agustin.tarati.game.core.GameState
import com.agustin.tarati.game.logic.createGameState
import com.agustin.tarati.ui.preview.endGameState
import com.agustin.tarati.ui.preview.initialGameStateWithUpgrades
import com.agustin.tarati.ui.preview.midGameState
import com.agustin.tarati.ui.theme.AppColors.getBoardColors
import com.agustin.tarati.ui.theme.TaratiTheme

@Composable
fun Board(
    modifier: Modifier = Modifier,
    gameState: GameState,
    vWidth: Float,
    onMove: (from: String, to: String) -> Unit,
    playerSide: Color,
    selectedPiece: String? = null,
    highlightedMoves: List<String> = listOf()
) {
    // ViewModel que guarda la pieza seleccionada y las piezas resaltadas
    val viewModel: BoardViewModel = viewModel()

    // Observamos el gameState del ViewModel. Si es null, creamos un estado inicial
    val vmSelectedPiece by viewModel.selectedPiece.collectAsState(selectedPiece)
    val vmHighlightedMoves by viewModel.highlightedMoves.collectAsState(highlightedMoves)

    // Colores del tema
    val backgroundColor = getBoardColors().backgroundColor
    val blackPieceBorderColor = getBoardColors().blackPieceBorderColor
    val blackPieceColor = getBoardColors().blackPieceColor
    val boardBackgroundColor = getBoardColors().boardBackgroundColor
    val edgeColor = getBoardColors().edgeColor
    val selectionIndicatorColor = getBoardColors().selectionIndicatorColor
    val textColor = getBoardColors().textColor
    val vertexDefaultColor = getBoardColors().vertexDefaultColor
    val vertexHighlightColor = getBoardColors().vertexHighlightColor
    val vertexOccupiedColor = getBoardColors().vertexOccupiedColor
    val vertexSelectedColor = getBoardColors().vertexSelectedColor
    val whitePieceBorderColor = getBoardColors().whitePieceBorderColor
    val whitePieceColor = getBoardColors().whitePieceColor

    Box(
        modifier = modifier
            .background(MaterialTheme.colorScheme.surface)
            .pointerInput(gameState, vmSelectedPiece, playerSide) {
                detectTapGestures { offset ->
                    val canvasWidth = size.width.toFloat()
                    val canvasHeight = size.height.toFloat()
                    val isLandscape = canvasWidth > canvasHeight

                    println("=== TAP DETECTED ===")
                    println("Canvas: ${canvasWidth}x$canvasHeight, Landscape: $isLandscape")
                    println("Raw offset: $offset")

                    // Manejo de landscape
                    val tappedVisualVertex = findClosestVertex(
                        tapOffset = offset,
                        canvasWidth = canvasWidth,
                        canvasHeight = canvasHeight,
                        vWidth = vWidth,
                        playerSide = playerSide,
                        isLandscape = isLandscape
                    )
                    println("Tapped visual vertex: $tappedVisualVertex")

                    tappedVisualVertex?.let { visualVertexId ->
                        // Convertir coordenada visual a lógica
                        val logicalVertexId = visualToLogical(visualVertexId, playerSide)

                        println("Visual: $visualVertexId -> Logical: $logicalVertexId")
                        println("Game state checkers: ${gameState.checkers.keys}")

                        if (vmSelectedPiece == null) {
                            // Intentar seleccionar una pieza
                            val checker = gameState.checkers[logicalVertexId]
                            println("Checker at $logicalVertexId: $checker, currentTurn: ${gameState.currentTurn}")

                            if (checker != null && checker.color == gameState.currentTurn) {
                                viewModel.updateSelectedPiece(logicalVertexId)

                                // Resaltar movimientos válidos
                                val logicalMoves = GameBoard.adjacencyMap[logicalVertexId]?.filter { to ->
                                    !gameState.checkers.containsKey(to) && (checker.isUpgraded || TaratiAI.isForwardMove(
                                        checker.color, logicalVertexId, to
                                    ))
                                } ?: emptyList()

                                viewModel.updateHighlightedMoves(
                                    logicalMoves.map { logicalToVisual(it, playerSide) })

                                println("Selected piece: $logicalVertexId")
                                println("Logical moves: $logicalMoves")
                                println("Visual highlights: $vmHighlightedMoves")
                            }
                        } else {
                            // Intentar mover la pieza seleccionada
                            if (logicalVertexId != vmSelectedPiece) {
                                println("Attempting move from $vmSelectedPiece to $logicalVertexId")

                                if (TaratiAI.isValidMove(gameState, vmSelectedPiece!!, logicalVertexId)) {
                                    onMove(vmSelectedPiece!!, logicalVertexId)
                                }
                            }
                            viewModel.updateSelectedPiece(null)
                            viewModel.updateHighlightedMoves(emptyList())
                        }
                    }
                }
            }) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val canvasWidth = size.width
            val canvasHeight = size.height
            val isLandscape = canvasWidth > canvasHeight

            if (isLandscape) {
                // En landscape, rotar 90 grados y centrar
                rotate(90f, pivot = Offset(canvasWidth / 2, canvasHeight / 2)) { }
            } else if (playerSide == BLACK) {
                // En portrait, rotar 180 grados si el jugador es negro
                rotate(180f, pivot = Offset(canvasWidth / 2, canvasHeight / 2)) {}
            }

            // Fondo completo
            drawRect(color = backgroundColor)

            // Rotación visual del tablero
            if (playerSide == BLACK) {
                rotate(180f, pivot = Offset(canvasWidth / 2, canvasHeight / 2)) {}
            }

            // Fondo circular del tablero
            val maxRadius = minOf(canvasWidth, canvasHeight) * 0.45f
            drawCircle(
                color = boardBackgroundColor, center = Offset(canvasWidth / 2, canvasHeight / 2), radius = maxRadius
            )

            // Draw edges
            GameBoard.edges.forEach { edge ->
                val fromVisual = logicalToVisual(edge.first, playerSide)
                val toVisual = logicalToVisual(edge.second, playerSide)

                val fromPos = getVisualPosition(fromVisual, canvasWidth, canvasHeight, vWidth, isLandscape, playerSide)
                val toPos = getVisualPosition(toVisual, canvasWidth, canvasHeight, vWidth, isLandscape, playerSide)

                drawLine(
                    color = edgeColor, start = fromPos, end = toPos, strokeWidth = 2f
                )
            }

            // Draw vertices
            GameBoard.vertices.forEach { logicalVertexId ->
                val visualVertexId = logicalToVisual(logicalVertexId, playerSide)
                val pos = getVisualPosition(visualVertexId, canvasWidth, canvasHeight, vWidth, isLandscape, playerSide)

                val checker = gameState.checkers[logicalVertexId]
                val vertexColor = when {
                    logicalVertexId == vmSelectedPiece -> vertexSelectedColor

                    vmHighlightedMoves.contains(visualVertexId) -> vertexHighlightColor

                    checker != null -> vertexOccupiedColor

                    else -> vertexDefaultColor
                }

                // Vértice
                drawCircle(color = vertexColor, center = pos, radius = vWidth / 10)

                // Borde del vértice
                drawCircle(
                    color = textColor.copy(alpha = 0.3f), center = pos, radius = vWidth / 10, style = Stroke(width = 1f)
                )

                // Etiqueta del vértice - mostrar coordenada VISUAL
                drawContext.canvas.nativeCanvas.apply {
                    drawText(
                        visualVertexId, pos.x - vWidth / 6, pos.y - vWidth / 6, Paint().apply {
                            color = textColor.hashCode()
                            textSize = vWidth / 8
                            isAntiAlias = true
                        })
                }
            }

            // Draw checkers
            gameState.checkers.forEach { (logicalVertexId, checker) ->
                val visualVertexId = logicalToVisual(logicalVertexId, playerSide)
                val pos = getVisualPosition(visualVertexId, canvasWidth, canvasHeight, vWidth, isLandscape, playerSide)

                val (checkerColor, borderColor) = when (checker.color) {
                    WHITE -> whitePieceColor to whitePieceBorderColor
                    BLACK -> blackPieceColor to blackPieceBorderColor
                }

                // Borde exterior de la pieza
                drawCircle(
                    color = borderColor, center = pos, radius = vWidth / 5, style = Stroke(width = 3f)
                )

                // Pieza principal
                drawCircle(
                    color = checkerColor, center = pos, radius = vWidth / 6
                )

                // Indicador de pieza mejorada
                if (checker.isUpgraded) {
                    val upgradeColor = when (checker.color) {
                        WHITE -> blackPieceColor
                        BLACK -> whitePieceColor
                    }

                    drawCircle(
                        color = upgradeColor, center = pos, radius = vWidth / 7, style = Stroke(width = 3f)
                    )

                    drawCircle(
                        color = upgradeColor, center = pos, radius = vWidth / 10
                    )
                }

                // Resaltado de selección
                if (logicalVertexId == vmSelectedPiece) {
                    drawCircle(
                        color = selectionIndicatorColor, center = pos, radius = vWidth / 4, style = Stroke(width = 3f)
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 400, heightDp = 500)
@Composable
fun BoardPreview_WithUpgrades() {
    TaratiTheme {
        val exampleGameState = initialGameStateWithUpgrades()
        Box(modifier = Modifier.fillMaxSize()) {
            Board(
                gameState = exampleGameState,
                vWidth = ((500.dp) / 3f).value,
                onMove = { from, to -> println("Move from $from to $to") },
                playerSide = WHITE,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            )
        }
    }
}

@Preview(showBackground = true, widthDp = 400, heightDp = 500)
@Composable
fun BoardPreview_MidGame() {
    TaratiTheme(true) {
        val exampleGameState = midGameState()
        Box(modifier = Modifier.fillMaxSize()) {
            Board(
                gameState = exampleGameState,
                vWidth = ((500.dp) / 3f).value,
                onMove = { from, to -> println("Move from $from to $to") },
                playerSide = BLACK,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            )
        }
    }
}

@Preview(showBackground = true, widthDp = 400, heightDp = 500)
@Composable
fun BoardPreview_Custom() {
    TaratiTheme {
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

        Box(modifier = Modifier.fillMaxSize()) {
            Board(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                gameState = exampleGameState,
                vWidth = ((500.dp) / 3f).value,
                onMove = { from, to -> println("Move from $from to $to") },
                playerSide = WHITE,
                selectedPiece = "B1",
                highlightedMoves = listOf("B2", "A1", "B6")
            )
        }
    }
}

@Preview(showBackground = true, widthDp = 400, heightDp = 500)
@Composable
fun BoardPreview_BlackPlayer() {
    TaratiTheme(true) {
        val exampleGameState = endGameState(BLACK)
        Box(modifier = Modifier.fillMaxSize()) {
            Board(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                gameState = exampleGameState,
                vWidth = ((500.dp) / 3f).value,
                onMove = { from, to ->
                    println("Move from $from to $to")
                },
                playerSide = BLACK,
                selectedPiece = "A1",
                highlightedMoves = listOf("B1", "B2", "B3", "B4", "B5", "B6")
            )
        }
    }
}

@Preview(showBackground = true, widthDp = 800, heightDp = 400)
@Composable
fun BoardPreview_Landscape_BlackPlayer() {
    TaratiTheme {
        val exampleGameState = createGameState { setTurn(BLACK) }
        Box(modifier = Modifier.fillMaxSize()) {
            Board(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                gameState = exampleGameState,
                vWidth = ((500.dp) / 3f).value,
                onMove = { from, to ->
                    println("Move from $from to $to")
                },
                playerSide = BLACK,
                selectedPiece = "C2",
                highlightedMoves = listOf("C9", "B4", "B5")
            )
        }
    }
}

@Preview(showBackground = true, widthDp = 800, heightDp = 400)
@Composable
fun BoardPreview_Landscape_Debug() {
    TaratiTheme(true) {
        val exampleGameState = createGameState {}
        Box(modifier = Modifier.fillMaxSize()) {
            Board(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                gameState = exampleGameState,
                vWidth = ((500.dp) / 3f).value,
                onMove = { from, to ->
                    println("Move from $from to $to")
                },
                playerSide = WHITE,
                selectedPiece = "C2",
                highlightedMoves = listOf("C3", "B2", "B1")
            )
        }
    }
}