@file:Suppress("unused")

package com.agustin.tarati.game.core

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import com.agustin.tarati.game.core.Color.BLACK
import com.agustin.tarati.game.core.Color.WHITE
import com.agustin.tarati.game.core.GameBoard.BLACK_CASTLING_VERTEX
import com.agustin.tarati.game.core.GameBoard.WHITE_CASTLING_VERTEX
import com.agustin.tarati.game.core.GameBoard.adjacencyMap
import com.agustin.tarati.game.core.GameBoard.getCastlingMoves
import com.agustin.tarati.game.core.GameBoard.homeBases
import com.agustin.tarati.game.core.GameBoard.isForwardMove
import com.agustin.tarati.game.logic.BoardOrientation
import com.agustin.tarati.ui.components.board.draw.BoardRect
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

object GameBoard {
    private const val REFERENCE_BOARD_SIZE = 1100f
    private const val VERTEX_WIDTH = 250f
    private const val BOARD_MARGIN_PERCENT = 0.8f
    private const val FORWARD_MOVE_THRESHOLD = 10f

    // A. Absolute Center
    const val ABSOLUTE_CENTER: String = "A1"

    // B. Bridge (Boundary)
    const val BRIDGE = 'B'
    val bridgeVertices: List<String> = listOf("B1", "B2", "B3", "B4", "B5", "B6")

    // C. Circumference
    const val CIRCUMFERENCE = 'C'
    val circumferenceVertices: List<String> =
        listOf("C1", "C2", "C3", "C4", "C5", "C6", "C7", "C8", "C9", "C10", "C11", "C12")

    // D. Domestic
    const val DOMESTIC = 'D'
    val domesticVertices: List<String> = listOf("D1", "D2", "D3", "D4")

    // Vertices that are captured during castling
    const val WHITE_CASTLING_VERTEX = "B1"
    const val BLACK_CASTLING_VERTEX = "B4"

    val centerVertices: List<String> = listOf(ABSOLUTE_CENTER).union(bridgeVertices).toList()
    val vertices: List<String> = centerVertices + circumferenceVertices + domesticVertices

    // D domestic White
    val whiteDomesticEdges = listOf("D1" to "D2", "D1" to "C1", "D2" to "C2")

    // D domestic Black
    val blackDomesticEdges = listOf("D3" to "D4", "D3" to "C7", "D4" to "C8")

    // B boundary
    val bridgeEdges = listOf(
        "B1" to "B2", "B2" to "B3", "B3" to "B4",
        "B4" to "B5", "B5" to "B6", "B6" to "B1"
    )

    // C circumference
    val circumferenceEdges = listOf(
        "C1" to "C2", "C2" to "C3", "C3" to "C4",
        "C4" to "C5", "C5" to "C6", "C6" to "C7",
        "C7" to "C8", "C8" to "C9", "C9" to "C10",
        "C10" to "C11", "C11" to "C12", "C12" to "C1",
    )

    // C to B
    val bridgeToCircumferenceEdges = listOf(
        "C1" to "B1", "C2" to "B1",
        "C3" to "B2", "C4" to "B2",
        "C5" to "B3", "C6" to "B3",
        "C7" to "B4", "C8" to "B4",
        "C9" to "B5", "C10" to "B5",
        "C11" to "B6", "C12" to "B6",
    )

    // B to A (absolute Middle)
    val absoluteCenterToBridgeEdges = listOf(
        "B1" to "A1", "B2" to "A1", "B3" to "A1",
        "B4" to "A1", "B5" to "A1", "B6" to "A1"
    )

    val edges: List<Pair<String, String>> =
        whiteDomesticEdges + blackDomesticEdges + bridgeEdges + circumferenceEdges + bridgeToCircumferenceEdges + absoluteCenterToBridgeEdges

    val homeBases: Map<Color, List<String>> = mapOf(
        WHITE to listOf("C1", "C2", "D1", "D2"),
        BLACK to listOf("C7", "C8", "D3", "D4")
    )

    // Definición de las regiones del tablero
    fun getCentralRegions(): List<BoardRegion> {
        return listOf(
            // Regiones centrales -6 triángulos-
            BoardRegion(listOf("A1", "B1", "B2")),
            BoardRegion(listOf("A1", "B2", "B3")),
            BoardRegion(listOf("A1", "B3", "B4")),
            BoardRegion(listOf("A1", "B4", "B5")),
            BoardRegion(listOf("A1", "B5", "B6")),
            BoardRegion(listOf("A1", "B6", "B1")),
        )
    }

    fun getCircumferenceRegions(): List<BoardRegion> {
        return listOf(
            // Regiones de circunferencia (6 triángulos y 6 cuadrados)
            BoardRegion(listOf("B1", "C1", "C2")),
            BoardRegion(listOf("B1", "C2", "C3", "B2")),
            BoardRegion(listOf("B2", "C3", "C4")),
            BoardRegion(listOf("B2", "C4", "C5", "B3")),
            BoardRegion(listOf("B3", "C5", "C6")),
            BoardRegion(listOf("B3", "C6", "C7", "B4")),
            BoardRegion(listOf("B4", "C7", "C8")),
            BoardRegion(listOf("B4", "C8", "C9", "B5")),
            BoardRegion(listOf("B5", "C9", "C10")),
            BoardRegion(listOf("B5", "C10", "C11", "B6")),
            BoardRegion(listOf("B6", "C11", "C12")),
            BoardRegion(listOf("B6", "C12", "C1", "B1")),
        )
    }

    fun getDomesticRegions(): List<BoardRegion> {
        return listOf(
            // Regiones domésticas -2 cuadrados-
            BoardRegion(listOf("C1", "C2", "D2", "D1")),
            BoardRegion(listOf("C7", "C8", "D4", "D3")),
        )
    }

    val vertexToRegions: Map<String, List<BoardRegion>> by lazy {
        getAllRegions()
            .flatMap { region -> region.vertices.map { vertex -> vertex to region } }
            .groupBy({ it.first }, { it.second })
    }

    fun getAllRegions(): List<BoardRegion> {
        return getDomesticRegions() + getCentralRegions() + getCircumferenceRegions()
    }

    // Regiones del tablero
    data class BoardRegion(val vertices: List<String>)

    // Mapa de adyacencia
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

    fun getBoardRect(vertices: List<String>, canvasSize: Size, orientation: BoardOrientation): BoardRect {
        val positions = vertices.distinct().map { vertexId ->
            getVisualPosition(vertexId, canvasSize.width, canvasSize.height, orientation)
        }

        val minX = positions.minOf { it.x }
        val maxX = positions.maxOf { it.x }
        val minY = positions.minOf { it.y }
        val maxY = positions.maxOf { it.y }

        return BoardRect(
            topLeft = Offset(minX, minY),
            size = Size(width = maxX - minX, height = maxY - minY)
        )
    }

    fun getVisualPosition(
        vertexId: String,
        canvasWidth: Float,
        canvasHeight: Float,
        orientation: BoardOrientation
    ): Offset {
        val normalized = normalizedPositions[vertexId]
            ?: throw IllegalArgumentException("Unknown vertex: $vertexId")

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

        vertices.forEach { vertexId ->
            val pos = getVisualPosition(vertexId, canvasWidth, canvasHeight, orientation)
            val distance = sqrt((tapOffset.x - pos.x).pow(2) + (tapOffset.y - pos.y).pow(2))

            if (distance < maxTapDistance && distance < minDistance) {
                minDistance = distance
                closestVertex = vertexId // Devolvemos coordenada lógica
            }
        }

        return closestVertex
    }

    fun isForwardMove(color: Color, from: String, to: String): Boolean {
        val boardCenter = VERTEX_WIDTH to VERTEX_WIDTH
        val vWidth = VERTEX_WIDTH
        val fromPos = getPosition(from, boardCenter, vWidth)
        val toPos = getPosition(to, boardCenter, vWidth)

        return when (color) {
            WHITE -> fromPos.y - toPos.y > FORWARD_MOVE_THRESHOLD
            else -> toPos.y - fromPos.y > FORWARD_MOVE_THRESHOLD
        }
    }

    fun isValidMove(gs: GameState, from: String, to: String): Boolean {
        val cob = gs.cobs[from] ?: return false
        val move = Move(from, to)
        if (move.isCastling(cob.color) && gs.getPosibleCastling(from, cob) != null)
            return true

        val isAdjacent = adjacencyMap[from]?.contains(to) ?: false
        if (!isAdjacent) return false

        return when {
            gs.cobs.containsKey(to) -> false
            cob.color != gs.currentTurn -> false
            !cob.isUpgraded -> isForwardMove(cob.color, from, to)
            else -> true
        }
    }

    fun getPosition(vertexId: String, boardSize: Pair<Float, Float>, vWidth: Float): Offset {
        val (width, height) = boardSize
        val centerX = width / 2
        val centerY = height / 2

        if (vertexId == ABSOLUTE_CENTER) {
            return Offset(centerX, centerY)
        }

        val type = vertexId[0]
        val position = vertexId.substring(1).toInt()

        return when (type) {
            BRIDGE -> {
                val angle = (position - 1) * (Math.PI / 3)
                Offset(
                    x = centerX + vWidth * cos(angle + Math.PI / 2).toFloat(),
                    y = centerY + vWidth * sin(angle + Math.PI / 2).toFloat()
                )
            }

            CIRCUMFERENCE -> {
                val angle = (position - 1) * (Math.PI / 6) - Math.PI / 12 + Math.PI / 2
                val radius =
                    vWidth * (1 + sqrt(11.0 / 13)).toFloat() - (Math.PI / 12).toFloat() + (Math.PI / 2).toFloat()
                Offset(
                    x = centerX + radius * cos(angle).toFloat(),
                    y = centerY + radius * sin(angle).toFloat()
                )
            }

            DOMESTIC -> {
                val down = if (position > 2) -1 else 1
                val left = if (position == 1 || position == 4) 1 else -1
                Offset(
                    x = centerX + vWidth / 2 / left,
                    y = centerY + vWidth * 3 * down
                )
            }

            else -> Offset(centerX, centerY)
        }
    }

    fun getCastlingMoves(color: Color, from: String): Move? {
        return if (color == WHITE) {
            when (from) {
                "C2" -> return Move("C2", "C1")
                "C1" -> return Move("C1", "C2")
                else -> null
            }
        } else {
            when (from) {
                "C7" -> return Move("C7", "C8")
                "C8" -> return Move("C8", "C7")
                else -> null
            }
        }
    }
}

fun GameState.getValidVertex(from: String, cob: Cob): List<String> {
    val validVertex = adjacencyMap[from]?.filter { to ->
        val isValid = !this.cobs.containsKey(to) &&
                (cob.isUpgraded || isForwardMove(cob.color, from, to))
        isValid
    } ?: emptyList()

    return getPosibleCastling(from, cob)?.let { validVertex + it.to } ?: validVertex
}

fun GameState.getPosibleCastling(from: String, cob: Cob): Move? {
    if (cob.isUpgraded) return null
    val move = getCastlingMoves(cob.color, from) ?: return null

    if (this.cobs[move.to] != null) return null
    val flipVertex = if (cob.color == WHITE) WHITE_CASTLING_VERTEX else BLACK_CASTLING_VERTEX

    return if (this.cobs[flipVertex]?.takeIf { it.color == cob.color.opponent() } == null) null
    else move
}

fun Move.isCastling(cobColor: Color): Boolean {
    return when (cobColor) {
        WHITE -> from in listOf("C1", "C2") && to in listOf("C1", "C2")
        BLACK -> from in listOf("C7", "C8") && to in listOf("C7", "C8")
    }
}

fun Move.countFlipsByType(oldState: GameState, newState: GameState): Pair<Int, Int> {
    var rocFlips = 0
    var cobFlips = 0

    val cob = oldState.cobs[this.from]!!

    // Capturas especiales
    if (this.isCastling(cob.color)) {
        val targetPosition = if (cob.color == WHITE) WHITE_CASTLING_VERTEX else BLACK_CASTLING_VERTEX
        val targetCob = oldState.cobs[targetPosition]
        if (targetCob != null) {
            if (targetCob.isUpgraded) rocFlips++ else cobFlips++
        }
    }

    // Capturas normales (adyacentes)
    for (vertex in adjacencyMap[this.to] ?: emptyList()) {
        val oldCob = oldState.cobs[vertex]
        val newCob = newState.cobs[vertex]

        if (oldCob != null && newCob != null && oldCob.color != newCob.color) {
            if (oldCob.isUpgraded) rocFlips++ else cobFlips++
        }
    }

    return Pair(rocFlips, cobFlips)
}

fun Move.isUpgradeMove(oldState: GameState, newState: GameState): Boolean {
    val movingCob = oldState.cobs[this.from] ?: return false

    // Si la pieza ya estaba mejorada, no puede mejorar nuevamente
    if (movingCob.isUpgraded) return false

    // Caso especial: enroque (castling) - no cuenta como mejora
    if (this.isCastling(movingCob.color)) {
        return false
    }

    // Verificar si la pieza se movió a la base ENEMIGA
    val enemyBase = homeBases[movingCob.color.opponent()] ?: emptyList()
    val isOnEnemyBase = this.to in enemyBase

    if (!isOnEnemyBase) return false

    // Verificar que en el nuevo estado la pieza está mejorada
    val newCob = newState.cobs[this.to]
    return newCob?.isUpgraded == true
}

fun Move.isPotentialUpgradeMove(oldState: GameState): Boolean {
    val movingCob = oldState.cobs[this.from] ?: return false

    // Si la pieza ya estaba mejorada, no puede mejorar nuevamente
    if (movingCob.isUpgraded) return false

    // Caso especial: enroque (castling) - no cuenta como mejora
    if (this.isCastling(movingCob.color)) {
        return false
    }

    // Verificar si la pieza se mueve a la base enemiga
    val enemyBase = homeBases[movingCob.color.opponent()] ?: emptyList()
    return this.to in enemyBase
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