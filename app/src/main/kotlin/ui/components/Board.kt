package com.agustin.tarati.ui.components

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
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.agustin.tarati.game.Color
import com.agustin.tarati.game.Color.BLACK
import com.agustin.tarati.game.Color.WHITE
import com.agustin.tarati.game.GameBoard
import com.agustin.tarati.game.GameState
import com.agustin.tarati.game.TaratiAI
import com.agustin.tarati.game.createGameState
import com.agustin.tarati.helpers.AdaptivePositionHelper
import com.agustin.tarati.ui.theme.TaratiTheme
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

@Composable
fun Board(
    gameState: GameState,
    boardSize: Dp,
    vWidth: Float,
    onMove: (from: String, to: String) -> Unit,
    playerSide: Color,
    modifier: Modifier = Modifier
) {
    // ViewModel que guarda estado, historial y dificultad
    val viewModel: BoardViewModel = viewModel()

    // Observamos el gameState del ViewModel. Si es null, creamos un estado inicial
    val vmSelectedPiece by viewModel.selectedPiece.collectAsState(null as String?)
    val vmHighlightedMoves by viewModel.highlightedMoves.collectAsState(emptyList())

    // Colores del tema
    val backgroundColor = MaterialTheme.colorScheme.surface
    val boardBackgroundColor = MaterialTheme.colorScheme.surfaceVariant
    val edgeColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.6f)

    val vertexDefaultColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.8f)
    val vertexOccupiedColor = MaterialTheme.colorScheme.onSurfaceVariant
    val vertexSelectedColor = MaterialTheme.colorScheme.primary
    val vertexHighlightColor = MaterialTheme.colorScheme.tertiary

    val textColor = MaterialTheme.colorScheme.onSurface

    val blackPieceColor = MaterialTheme.colorScheme.primary
    val whitePieceColor = MaterialTheme.colorScheme.onSecondary
    val blackPieceBorderColor = MaterialTheme.colorScheme.onPrimary
    val whitePieceBorderColor = MaterialTheme.colorScheme.secondary

    val selectionIndicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)

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
                    val tappedVisualVertex =
                        findClosestVertex(offset, canvasWidth, canvasHeight, vWidth, playerSide, isLandscape)
                    println("Tapped visual vertex: $tappedVisualVertex")

                    tappedVisualVertex?.let { visualVertexId ->
                        // Convertir coordenada visual a lógica
                        val logicalVertexId = CoordinateSystem.visualToLogical(visualVertexId, playerSide)

                        println("Visual: $visualVertexId -> Logical: $logicalVertexId")
                        println("Game state checkers: ${gameState.checkers.keys}")

                        if (vmSelectedPiece == null) {
                            // Intentar seleccionar una pieza
                            val checker = gameState.checkers[logicalVertexId]
                            println("Checker at $logicalVertexId: $checker, currentTurn: ${gameState.currentTurn}")

                            if (checker != null && checker.color == gameState.currentTurn) {
                                viewModel.updateSelectedPiece(logicalVertexId)

                                // Resaltar movimientos válidos
                                val logicalMoves = GameBoard.adjacencyMap[logicalVertexId]
                                    ?.filter { to ->
                                        !gameState.checkers.containsKey(to) &&
                                                (checker.isUpgraded || TaratiAI.isForwardMove(
                                                    checker.color,
                                                    logicalVertexId,
                                                    to
                                                ))
                                    } ?: emptyList()

                                viewModel.updateHighlightedMoves(logicalMoves.map {
                                    CoordinateSystem.logicalToVisual(it, playerSide)
                                })

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
                                viewModel.updateSelectedPiece(null)
                                viewModel.updateHighlightedMoves(emptyList())
                            } else {
                                viewModel.updateSelectedPiece(null)
                                viewModel.updateHighlightedMoves(emptyList())
                            }
                        }
                    }
                }
            }
    ) {
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
                color = boardBackgroundColor,
                center = Offset(canvasWidth / 2, canvasHeight / 2),
                radius = maxRadius
            )

            // Draw edges
            GameBoard.edges.forEach { edge ->
                val fromVisual = CoordinateSystem.logicalToVisual(edge.first, playerSide)
                val toVisual = CoordinateSystem.logicalToVisual(edge.second, playerSide)

                val fromPos = getVisualPosition(fromVisual, canvasWidth, canvasHeight, vWidth, isLandscape, playerSide)
                val toPos = getVisualPosition(toVisual, canvasWidth, canvasHeight, vWidth, isLandscape, playerSide)

                drawLine(
                    color = edgeColor,
                    start = fromPos,
                    end = toPos,
                    strokeWidth = 2f
                )
            }

            // Draw vertices
            GameBoard.vertices.forEach { logicalVertexId ->
                val visualVertexId = CoordinateSystem.logicalToVisual(logicalVertexId, playerSide)
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
                    color = textColor.copy(alpha = 0.3f),
                    center = pos,
                    radius = vWidth / 10,
                    style = Stroke(width = 1f)
                )

                // Etiqueta del vértice - mostrar coordenada VISUAL
                drawContext.canvas.nativeCanvas.apply {
                    drawText(
                        visualVertexId,
                        pos.x - vWidth / 6,
                        pos.y - vWidth / 6,
                        android.graphics.Paint().apply {
                            color = textColor.hashCode()
                            textSize = vWidth / 8
                            isAntiAlias = true
                        }
                    )
                }
            }

            // Draw checkers
            gameState.checkers.forEach { (logicalVertexId, checker) ->
                val visualVertexId = CoordinateSystem.logicalToVisual(logicalVertexId, playerSide)
                val pos = getVisualPosition(visualVertexId, canvasWidth, canvasHeight, vWidth, isLandscape, playerSide)

                val (checkerColor, borderColor) = when (checker.color) {
                    WHITE -> whitePieceColor to whitePieceBorderColor
                    BLACK -> blackPieceColor to blackPieceBorderColor
                }

                // Borde exterior de la pieza
                drawCircle(
                    color = borderColor,
                    center = pos,
                    radius = vWidth / 5,
                    style = Stroke(width = 3f)
                )

                // Pieza principal
                drawCircle(
                    color = checkerColor,
                    center = pos,
                    radius = vWidth / 6
                )

                // Indicador de pieza mejorada
                if (checker.isUpgraded) {
                    val upgradeColor = when (checker.color) {
                        WHITE -> blackPieceColor
                        BLACK -> whitePieceColor
                    }

                    drawCircle(
                        color = upgradeColor,
                        center = pos,
                        radius = vWidth / 7,
                        style = Stroke(width = 3f)
                    )

                    drawCircle(
                        color = upgradeColor,
                        center = pos,
                        radius = vWidth / 10
                    )
                }

                // Resaltado de selección
                if (logicalVertexId == vmSelectedPiece) {
                    drawCircle(
                        color = selectionIndicatorColor,
                        center = pos,
                        radius = vWidth / 4,
                        style = Stroke(width = 3f)
                    )
                }
            }
        }
    }
}

// Función para obtener posición visual considerando landscape
private fun getVisualPosition(
    visualVertexId: String,
    canvasWidth: Float,
    canvasHeight: Float,
    vWidth: Float,
    isLandscape: Boolean,
    playerSide: Color
): Offset {
    val basePos = AdaptivePositionHelper.getPosition(visualVertexId, canvasWidth to canvasHeight, vWidth, isLandscape)

    // Aplicar rotación adicional si el jugador es negro
    return if (playerSide == BLACK && !isLandscape) {
        rotateAroundCenter(basePos, canvasWidth, canvasHeight)
    } else {
        basePos
    }
}

// Función para encontrar vértice más cercano considerando playerSide y orientación
private fun findClosestVertex(
    tapOffset: Offset,
    canvasWidth: Float,
    canvasHeight: Float,
    vWidth: Float,
    playerSide: Color,
    isLandscape: Boolean
): String? {
    var closestVertex: String? = null
    var minDistance = Float.MAX_VALUE
    val maxTapDistance = vWidth / 2

    val visualVertices = CoordinateSystem.getVisualCoordinates(playerSide)

    visualVertices.forEach { visualVertexId ->
        val pos = getVisualPosition(visualVertexId, canvasWidth, canvasHeight, vWidth, isLandscape, playerSide)
        val distance = sqrt((tapOffset.x - pos.x).pow(2) + (tapOffset.y - pos.y).pow(2))

        if (distance < maxTapDistance && distance < minDistance) {
            minDistance = distance
            closestVertex = visualVertexId
        }
    }

    return closestVertex
}

// Funciones de rotación (mantener las existentes)
private fun rotateAroundCenter(point: Offset, width: Float, height: Float, angleRad: Double = PI): Offset {
    val cx = width / 2f
    val cy = height / 2f
    val tx = point.x - cx
    val ty = point.y - cy
    val cosA = cos(angleRad)
    val sinA = sin(angleRad)
    val rx = (tx * cosA - ty * sinA).toFloat()
    val ry = (tx * sinA + ty * cosA).toFloat()
    return Offset(rx + cx, ry + cy)
}

@Preview(showBackground = true, widthDp = 400, heightDp = 500)
@Composable
fun BoardPreview_WithUpgrades() {
    TaratiTheme {
        val exampleGameState = initialGameStateWithUpgrades()

        Box(modifier = Modifier.fillMaxSize()) {
            Board(
                gameState = exampleGameState,
                boardSize = 350.dp,
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
                boardSize = 350.dp,
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
                gameState = exampleGameState,
                boardSize = 350.dp,
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
fun BoardPreview_BlackPlayer() {
    TaratiTheme(true) {
        val exampleGameState = createGameState { setTurn(BLACK) }
        Box(modifier = Modifier.fillMaxSize()) {
            Board(
                gameState = exampleGameState,
                boardSize = 350.dp,
                vWidth = ((500.dp) / 3f).value,
                onMove = { from, to ->
                    println("Move from $from to $to")
                },
                playerSide = BLACK, // Jugador como negras
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
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
                gameState = exampleGameState,
                boardSize = 350.dp,
                vWidth = ((500.dp) / 3f).value,
                onMove = { from, to ->
                    println("Move from $from to $to")
                },
                playerSide = BLACK,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
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
                gameState = exampleGameState,
                boardSize = 350.dp,
                vWidth = ((500.dp) / 3f).value,
                onMove = { from, to ->
                    println("Move from $from to $to")
                },
                playerSide = WHITE,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            )
        }
    }
}