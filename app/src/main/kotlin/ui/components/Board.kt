package com.agustin.tarati.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.agustin.tarati.game.Checker
import com.agustin.tarati.game.Color.BLACK
import com.agustin.tarati.game.Color.WHITE
import com.agustin.tarati.game.GameBoard
import com.agustin.tarati.game.GameState
import com.agustin.tarati.game.TaratiAI
import com.agustin.tarati.game.TaratiAI.isForwardMove
import com.agustin.tarati.helpers.PositionHelper
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
    modifier: Modifier = Modifier
) {
    var selectedPiece by remember { mutableStateOf<String?>(null) }
    var highlightedMoves by remember { mutableStateOf<List<String>>(emptyList()) }

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
            .pointerInput(gameState, selectedPiece) {
                detectTapGestures { offset ->
                    // En el handler de taps, calculamos el "offset lógico" rotando el tap
                    // en sentido inverso si el canvas está rotado.
                    val canvasWidth = size.width.toFloat()
                    val canvasHeight = size.height.toFloat()
                    val isLandscape = canvasWidth > canvasHeight

                    val adjustedTap = if (isLandscape) {
                        inverseRotateAroundCenter(offset, canvasWidth, canvasHeight, PI / 2)
                    } else offset

                    val tappedVertex = findClosestVertex(adjustedTap, canvasWidth, canvasHeight, vWidth)
                    println("Tapped vertex: $tappedVertex at $offset (adjusted: $adjustedTap)")

                    tappedVertex?.let { vertexId ->
                        if (selectedPiece == null) {
                            // Intentar seleccionar una pieza
                            val checker = gameState.checkers[vertexId]
                            if (checker != null && checker.color == gameState.currentTurn) {
                                selectedPiece = vertexId

                                // Resaltar solo vértices adyacentes que estén vacíos
                                highlightedMoves = GameBoard.adjacencyMap[vertexId]
                                    ?.filter { to ->
                                        // Verificar que el destino esté vacío
                                        !gameState.checkers.containsKey(to) &&
                                                // Verificar dirección si no está mejorada
                                                (checker.isUpgraded || isForwardMove(checker.color, vertexId, to))
                                    } ?: emptyList()

                                println("Selected piece: $vertexId, adjacent valid moves: $highlightedMoves")
                            }
                        } else {
                            // Intentar mover la pieza seleccionada
                            if (vertexId != selectedPiece) {
                                if (TaratiAI.isValidMove(
                                        gameState,
                                        selectedPiece!!,
                                        vertexId
                                    )
                                ) {
                                    onMove(selectedPiece!!, vertexId)
                                } else {
                                    println("Invalid move from $selectedPiece to $vertexId")
                                }
                                selectedPiece = null
                                highlightedMoves = emptyList()
                            } else {
                                // Deseleccionar si se toca la misma pieza
                                selectedPiece = null
                                highlightedMoves = emptyList()
                            }
                        }
                    }
                }
            }
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val canvasWidth = size.width
            val canvasHeight = size.height

            // Detectar orientación del canvas
            val isLandscape = canvasWidth > canvasHeight

            // Fondo completo
            drawRect(color = backgroundColor)

            // Fondo circular del tablero
            val maxRadius = minOf(canvasWidth, canvasHeight) * 0.45f
            drawCircle(
                color = boardBackgroundColor,
                center = Offset(canvasWidth / 2, canvasHeight / 2),
                radius = maxRadius
            )

            // Draw edges
            GameBoard.edges.forEach { edge ->
                val fromPos = PositionHelper.getPosition(edge.first, canvasWidth to canvasHeight, vWidth)
                val toPos = PositionHelper.getPosition(edge.second, canvasWidth to canvasHeight, vWidth)

                val displayFrom = if (isLandscape) rotateAroundCenter(
                    Offset(fromPos.x, fromPos.y),
                    canvasWidth,
                    canvasHeight,
                    PI / 2
                ) else Offset(fromPos.x, fromPos.y)
                val displayTo = if (isLandscape) rotateAroundCenter(
                    Offset(toPos.x, toPos.y),
                    canvasWidth,
                    canvasHeight,
                    PI / 2
                ) else Offset(toPos.x, toPos.y)

                drawLine(
                    color = edgeColor,
                    start = displayFrom,
                    end = displayTo,
                    strokeWidth = 2f
                )
            }

            // Draw vertices
            GameBoard.vertices.forEach { vertexId ->
                val pos = PositionHelper.getPosition(vertexId, canvasWidth to canvasHeight, vWidth)
                val displayPos = if (isLandscape) rotateAroundCenter(
                    Offset(pos.x, pos.y),
                    canvasWidth,
                    canvasHeight,
                    PI / 2
                ) else Offset(pos.x, pos.y)

                val vertexColor = when {
                    vertexId == selectedPiece -> vertexSelectedColor
                    highlightedMoves.contains(vertexId) -> vertexHighlightColor
                    gameState.checkers[vertexId] != null -> vertexOccupiedColor
                    else -> vertexDefaultColor
                }

                // Vértice
                drawCircle(color = vertexColor, center = displayPos, radius = vWidth / 10)

                // Borde del vértice
                drawCircle(
                    color = textColor.copy(alpha = 0.3f),
                    center = displayPos,
                    radius = vWidth / 10,
                    style = Stroke(width = 1f)
                )

                // Etiqueta del vértice
                drawContext.canvas.nativeCanvas.apply {
                    drawText(
                        vertexId,
                        displayPos.x - vWidth / 6,
                        displayPos.y - vWidth / 6,
                        android.graphics.Paint().apply {
                            color = textColor.hashCode() // Convertir Color a Int
                            textSize = vWidth / 8
                            isAntiAlias = true
                        }
                    )
                }
            }

            // Draw checkers
            gameState.checkers.forEach { (vertexId, checker) ->
                val pos = PositionHelper.getPosition(vertexId, canvasWidth to canvasHeight, vWidth)
                val displayPos = if (isLandscape) rotateAroundCenter(
                    Offset(pos.x, pos.y),
                    canvasWidth,
                    canvasHeight,
                    PI / 2
                ) else Offset(pos.x, pos.y)

                val (checkerColor, borderColor) = when (checker.color) {
                    WHITE -> whitePieceColor to whitePieceBorderColor
                    BLACK -> blackPieceColor to blackPieceBorderColor
                }

                // Borde exterior de la pieza
                drawCircle(
                    color = borderColor,
                    center = displayPos,
                    radius = vWidth / 5,
                    style = Stroke(width = 3f)
                )

                // Pieza principal
                drawCircle(
                    color = checkerColor,
                    center = displayPos,
                    radius = vWidth / 6
                )

                // Indicador de pieza
                if (checker.isUpgraded) {
                    val upgradeColor = when (checker.color) {
                        WHITE -> blackPieceColor
                        BLACK -> whitePieceColor
                    }

                    // Anillo para pieza
                    drawCircle(
                        color = upgradeColor,
                        center = displayPos,
                        radius = vWidth / 7,
                        style = Stroke(width = 3f)
                    )

                    // Círculo interior para pieza
                    drawCircle(
                        color = upgradeColor,
                        center = displayPos,
                        radius = vWidth / 10
                    )
                }

                // Resaltado de selección
                if (vertexId == selectedPiece) {
                    drawCircle(
                        color = selectionIndicatorColor,
                        center = displayPos,
                        radius = vWidth / 4,
                        style = Stroke(width = 3f)
                    )
                }
            }
        }
    }
}

private fun findClosestVertex(tapOffset: Offset, canvasWidth: Float, canvasHeight: Float, vWidth: Float): String? {
    var closestVertex: String? = null
    var minDistance = Float.MAX_VALUE
    val maxTapDistance = vWidth / 2

    GameBoard.vertices.forEach { vertexId ->
        val pos = PositionHelper.getPosition(vertexId, canvasWidth to canvasHeight, vWidth)
        val distance = sqrt((tapOffset.x - pos.x).pow(2) + (tapOffset.y - pos.y).pow(2))

        if (distance < maxTapDistance && distance < minDistance) {
            minDistance = distance
            closestVertex = vertexId
        }
    }

    return closestVertex
}

// Rotación auxiliar: rota 'point' alrededor del centro del canvas (width/2, height/2) por angleRad (radianes)
private fun rotateAroundCenter(point: Offset, width: Float, height: Float, angleRad: Double): Offset {
    val cx = width / 2f
    val cy = height / 2f

    // Translate to origin
    val tx = point.x - cx
    val ty = point.y - cy

    // Rotate
    val cosA = cos(angleRad)
    val sinA = sin(angleRad)
    val rx = (tx * cosA - ty * sinA).toFloat()
    val ry = (tx * sinA + ty * cosA).toFloat()

    // Translate back
    return Offset(rx + cx, ry + cy)
}

// Inversa para los taps (rotación en sentido contrario)
private fun inverseRotateAroundCenter(point: Offset, width: Float, height: Float, angleRad: Double): Offset {
    // Rotar con -angleRad
    return rotateAroundCenter(point, width, height, -angleRad)
}

@Preview(showBackground = true, widthDp = 400, heightDp = 500)
@Composable
fun BoardPreview() {
    TaratiTheme {
        val exampleGameState = GameState(
            checkers = mapOf(
                "C1" to Checker(WHITE, false),
                "C2" to Checker(WHITE, false),
                "D1" to Checker(WHITE, false),
                "D2" to Checker(WHITE, false),
                "C7" to Checker(BLACK, false),
                "C8" to Checker(BLACK, false),
                "D3" to Checker(BLACK, false),
                "D4" to Checker(BLACK, false)
            ),
            currentTurn = WHITE
        )

        Box(modifier = Modifier.fillMaxSize()) {
            Board(
                gameState = exampleGameState,
                boardSize = 350.dp,
                vWidth = ((500.dp) / 3f).value,
                onMove = { from, to ->
                    println("Move from $from to $to")
                },
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            )
        }
    }
}

@Preview(showBackground = true, widthDp = 400, heightDp = 500)
@Composable
fun BoardPreview_Dark() {
    TaratiTheme(true) {
        val exampleGameState = GameState(
            checkers = mapOf(
                "C1" to Checker(WHITE, false),
                "C2" to Checker(WHITE, true),
                "D1" to Checker(WHITE, false),
                "D2" to Checker(WHITE, false),
                "C7" to Checker(BLACK, false),
                "C8" to Checker(BLACK, true),
                "D3" to Checker(BLACK, false),
                "D4" to Checker(BLACK, false)
            ),
            currentTurn = WHITE
        )

        Box(modifier = Modifier.fillMaxSize()) {
            Board(
                gameState = exampleGameState,
                boardSize = 350.dp,
                vWidth = ((500.dp) / 3f).value,
                onMove = { from, to ->
                    println("Move from $from to $to")
                },
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            )
        }
    }
}

@Preview(showBackground = true, widthDp = 500, heightDp = 400)
@Composable
fun BoardPreview_Dark_Landscape() {
    TaratiTheme(true) {
        val exampleGameState = GameState(
            checkers = mapOf(
                "C1" to Checker(WHITE, false),
                "C2" to Checker(WHITE, true),
                "D1" to Checker(WHITE, false),
                "D2" to Checker(WHITE, false),
                "C7" to Checker(BLACK, false),
                "C8" to Checker(BLACK, true),
                "D3" to Checker(BLACK, false),
                "D4" to Checker(BLACK, false)
            ),
            currentTurn = WHITE
        )

        Box(modifier = Modifier.fillMaxSize()) {
            Board(
                gameState = exampleGameState,
                boardSize = 350.dp,
                vWidth = ((500.dp) / 3f).value,
                onMove = { from, to ->
                    println("Move from $from to $to")
                },
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            )
        }
    }
}

@Preview(showBackground = true, widthDp = 500, heightDp = 400)
@Composable
fun BoardPreview_Landscape() {
    TaratiTheme {
        val exampleGameState = GameState(
            checkers = mapOf(
                "C1" to Checker(WHITE, false),
                "C2" to Checker(WHITE, false),
                "D1" to Checker(WHITE, false),
                "D2" to Checker(WHITE, false),
                "C7" to Checker(BLACK, false),
                "C8" to Checker(BLACK, false),
                "D3" to Checker(BLACK, false),
                "D4" to Checker(BLACK, false)
            ),
            currentTurn = WHITE
        )

        Box(modifier = Modifier.fillMaxSize()) {
            Board(
                gameState = exampleGameState,
                boardSize = 350.dp,
                vWidth = ((500.dp) / 3f).value,
                onMove = { from, to ->
                    println("Move from $from to $to")
                },
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            )
        }
    }
}