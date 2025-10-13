package com.agustin.tarati.game.core

import androidx.compose.ui.geometry.Offset
import com.agustin.tarati.game.core.Color.BLACK
import com.agustin.tarati.game.core.Color.WHITE
import com.agustin.tarati.game.logic.BoardOrientation
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

object GameBoard {
    private const val REFERENCE_BOARD_SIZE = 1100f
    private const val VERTEX_WIDTH = 250f
    private const val BOARD_MARGIN_PERCENT = 0.8f
    private const val FORWARD_MOVE_THRESHOLD = 10f

    val vertices: List<String> = listOf(
        "A1", // Absolute Middle
        "B1", "B2", "B3", "B4", "B5", "B6", // Boundary
        "C1", "C2", "C3", "C4", "C5", "C6", "C7", "C8", "C9", "C10", "C11", "C12", // Circumference
        "D1", "D2", "D3", "D4" // Domestic
    )

    val centerVertices: List<String> = listOf("A1", "B1", "B2", "B3", "B4", "B5", "B6")

    val edges: List<Pair<String, String>> = listOf(
        // Home base White
        "D1" to "D2", "D1" to "C1", "D2" to "C2",
        // Home base Black
        "D3" to "D4", "D3" to "C7", "D4" to "C8",
        // C circumference
        "C1" to "C2", "C2" to "C3", "C3" to "C4",
        "C4" to "C5", "C5" to "C6", "C6" to "C7",
        "C7" to "C8", "C8" to "C9", "C9" to "C10",
        "C10" to "C11", "C11" to "C12", "C12" to "C1",
        // B boundary
        "B1" to "B2", "B2" to "B3", "B3" to "B4",
        "B4" to "B5", "B5" to "B6", "B6" to "B1",
        // C to B
        "C1" to "B1", "C2" to "B1",
        "C3" to "B2", "C4" to "B2",
        "C5" to "B3", "C6" to "B3",
        "C7" to "B4", "C8" to "B4",
        "C9" to "B5", "C10" to "B5",
        "C11" to "B6", "C12" to "B6",
        // B to A (absolute Middle)
        "B1" to "A1", "B2" to "A1", "B3" to "A1",
        "B4" to "A1", "B5" to "A1", "B6" to "A1"
    )

    val homeBases: Map<Color, List<String>> = mapOf(
        WHITE to listOf("C1", "C2", "D1", "D2"),
        BLACK to listOf("C7", "C8", "D3", "D4")
    )

    // Mapa de adyacencia optimizado
    val adjacencyMap: Map<String, List<String>> by lazy {
        val map = mutableMapOf<String, MutableList<String>>()

        // Inicializar con todos los vértices
        vertices.forEach { vertex ->
            map[vertex] = mutableListOf()
        }

        // Llenar con conexiones
        edges.forEach { (from, to) ->
            map[from]?.add(to)
            map[to]?.add(from)
        }

        map.toMap()
    }

    fun getVisualPosition(
        logicalVertexId: String,
        canvasWidth: Float,
        canvasHeight: Float,
        orientation: BoardOrientation
    ): Offset {
        val normalized = normalizedPositions[logicalVertexId]
            ?: throw IllegalArgumentException("Unknown vertex: $logicalVertexId")

        val rotated = normalized.rotate(orientation)

        // Calcular el área cuadrada centrada
        val minDimension = minOf(canvasWidth, canvasHeight)
        val squareSize = minDimension * BOARD_MARGIN_PERCENT // 80% del lado menor, con margen

        // Calcular offsets para centrar el cuadrado
        val offsetX = (canvasWidth - squareSize) / 2
        val offsetY = (canvasHeight - squareSize) / 2

        // Mapear coordenadas normalizadas al área cuadrada
        val x = offsetX + rotated.x * squareSize
        val y = offsetY + rotated.y * squareSize

        return Offset(x, y)
    }

    val normalizedPositions: Map<String, NormalizedBoard> by lazy {
        val tempMap = mutableMapOf<String, NormalizedBoard>()
        val referenceSize = REFERENCE_BOARD_SIZE to REFERENCE_BOARD_SIZE // Tamaño de referencia para normalizar

        vertices.forEach { vertexId ->
            val position = getPosition(vertexId, referenceSize, VERTEX_WIDTH)
            // Normalizar las coordenadas (0-1)
            val normalizedX = position.x / referenceSize.first
            val normalizedY = position.y / referenceSize.second
            tempMap[vertexId] = NormalizedBoard(normalizedX, normalizedY)
        }

        tempMap.toMap()
    }

    fun findClosestVertex(
        tapOffset: Offset,
        canvasWidth: Float,
        canvasHeight: Float,
        maxTapDistance: Float,
        orientation: BoardOrientation
    ): String? {
        var closestVertex: String? = null
        var minDistance = Float.MAX_VALUE

        vertices.forEach { logicalVertexId ->
            val pos = getVisualPosition(logicalVertexId, canvasWidth, canvasHeight, orientation)
            val distance = sqrt((tapOffset.x - pos.x).pow(2) + (tapOffset.y - pos.y).pow(2))

            if (distance < maxTapDistance && distance < minDistance) {
                minDistance = distance
                closestVertex = logicalVertexId // Devolvemos coordenada lógica
            }
        }

        return closestVertex
    }

    fun isForwardMove(color: Color, from: String, to: String): Boolean {
        val boardCenter = VERTEX_WIDTH to VERTEX_WIDTH
        val vWidth = VERTEX_WIDTH
        val fromPos = getPosition(from, boardCenter, vWidth)
        val toPos = getPosition(to, boardCenter, vWidth)

        return if (color == WHITE) {
            fromPos.y - toPos.y > FORWARD_MOVE_THRESHOLD
        } else {
            toPos.y - fromPos.y > FORWARD_MOVE_THRESHOLD
        }
    }

    fun isValidMove(gs: GameState, from: String, to: String): Boolean {
        val isAdjacent = adjacencyMap[from]?.contains(to) ?: false
        if (!isAdjacent) return false

        val checker = gs.checkers[from] ?: return false
        if (gs.checkers.containsKey(to)) return false

        if (checker.color != gs.currentTurn) return false

        if (!checker.isUpgraded) {
            val boardCenter = VERTEX_WIDTH to VERTEX_WIDTH
            val vWidth = VERTEX_WIDTH

            val fromPos = getPosition(from, boardCenter, vWidth)
            val toPos = getPosition(to, boardCenter, vWidth)

            return if (checker.color == WHITE) {
                fromPos.y - toPos.y > FORWARD_MOVE_THRESHOLD
            } else {
                toPos.y - fromPos.y > FORWARD_MOVE_THRESHOLD
            }
        }

        return true
    }

    fun getAllPossibleMoves(gameState: GameState): MutableList<Move> {
        val possibleMoves = mutableListOf<Move>()

        for ((from, checker) in gameState.checkers) {
            if (checker.color != gameState.currentTurn) continue

            // Usar el mapa de adyacencia para obtener solo vértices conectados
            val connectedVertices = adjacencyMap[from] ?: emptyList()
            for (to in connectedVertices) {
                if (isValidMove(gameState, from, to)) {
                    possibleMoves.add(Move(from, to))
                }
            }
        }

        return possibleMoves
    }

    fun getPosition(vertexId: String, boardSize: Pair<Float, Float>, vWidth: Float): Position {
        val (width, height) = boardSize
        val centerX = width / 2
        val centerY = height / 2

        if (vertexId == "A1") {
            return Position(centerX, centerY)
        }

        val type = vertexId[0]
        val position = vertexId.substring(1).toInt()

        return when (type) {
            'B' -> {
                val angle = (position - 1) * (Math.PI / 3)
                Position(
                    x = centerX + vWidth * cos(angle + Math.PI / 2).toFloat(),
                    y = centerY + vWidth * sin(angle + Math.PI / 2).toFloat()
                )
            }

            'C' -> {
                val angle = (position - 1) * (Math.PI / 6) - Math.PI / 12 + Math.PI / 2
                val radius =
                    vWidth * (1 + sqrt(11.0 / 13)).toFloat() - (Math.PI / 12).toFloat() + (Math.PI / 2).toFloat()
                Position(
                    x = centerX + radius * cos(angle).toFloat(),
                    y = centerY + radius * sin(angle).toFloat()
                )
            }

            'D' -> {
                val down = if (position > 2) -1 else 1
                val left = if (position == 1 || position == 4) 1 else -1
                Position(
                    x = centerX + vWidth / 2 / left,
                    y = centerY + vWidth * 3 * down
                )
            }

            else -> Position(centerX, centerY)
        }
    }
}

data class NormalizedBoard(val x: Float, val y: Float) {
    fun rotate(orientation: BoardOrientation): NormalizedBoard {
        return when (orientation) {
            BoardOrientation.PORTRAIT_WHITE -> NormalizedBoard(x, y)
            BoardOrientation.PORTRAIT_BLACK -> NormalizedBoard(1 - x, 1 - y)
            BoardOrientation.LANDSCAPE_WHITE -> NormalizedBoard(y, 1 - x)
            BoardOrientation.LANDSCAPE_BLACK -> NormalizedBoard(1 - y, x)
        }
    }
}