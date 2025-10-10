package com.agustin.tarati.game.core

import androidx.compose.ui.geometry.Offset
import com.agustin.tarati.game.core.Color.BLACK
import com.agustin.tarati.game.logic.PositionHelper
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

object GameBoard {
    val vertices: List<String> = listOf(
        "A1",
        "B1", "B2", "B3", "B4", "B5", "B6",
        "C1", "C2", "C3", "C4", "C5", "C6", "C7", "C8", "C9", "C10", "C11", "C12",
        "D1", "D2", "D3", "D4"
    )

    val edges: List<Pair<String, String>> = listOf(
        // Home base White
        "D1" to "D2", "D1" to "C1", "D2" to "C2",
        // Home base Black
        "D3" to "D4", "D3" to "C7", "D4" to "C8",
        // C circumference
        "C1" to "C2", "C2" to "C3", "C3" to "C4", "C4" to "C5", "C5" to "C6",
        "C6" to "C7", "C7" to "C8", "C8" to "C9", "C9" to "C10", "C10" to "C11",
        "C11" to "C12", "C12" to "C1",
        // B boundary
        "B1" to "B2", "B2" to "B3", "B3" to "B4", "B4" to "B5", "B5" to "B6", "B6" to "B1",
        // C to B
        "C1" to "B1", "C2" to "B1",
        "C3" to "B2", "C4" to "B2",
        "C5" to "B3", "C6" to "B3",
        "C7" to "B4", "C8" to "B4",
        "C9" to "B5", "C10" to "B5",
        "C11" to "B6", "C12" to "B6",
        // B to A
        "B1" to "A1", "B2" to "A1", "B3" to "A1", "B4" to "A1", "B5" to "A1", "B6" to "A1"
    )

    val homeBases: Map<Color, List<String>> = mapOf(
        Color.WHITE to listOf("C1", "C2", "D1", "D2"),
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

    // Mapa de rotación 180 grados (para negro en portrait)
    private val rotationMap = mapOf(
        "A1" to "A1",
        "B1" to "B4", "B2" to "B5", "B3" to "B6",
        "B4" to "B1", "B5" to "B2", "B6" to "B3",
        "C1" to "C7", "C2" to "C8", "C3" to "C9", "C4" to "C10", "C5" to "C11", "C6" to "C12",
        "C7" to "C1", "C8" to "C2", "C9" to "C3", "C10" to "C4", "C11" to "C5", "C12" to "C6",
        "D1" to "D3", "D2" to "D4", "D3" to "D1", "D4" to "D2"
    )

    private val inverseRotationMap by lazy {
        rotationMap.entries.associate { (key, value) -> value to key }
    }

    fun logicalToVisual(logicalCoord: String, playerSide: Color): String {
        return if (playerSide == BLACK) {
            rotationMap[logicalCoord] ?: logicalCoord
        } else {
            logicalCoord
        }
    }

    fun visualToLogical(visualCoord: String, playerSide: Color): String {
        return if (playerSide == BLACK) {
            inverseRotationMap[visualCoord] ?: visualCoord
        } else {
            visualCoord
        }
    }

    fun getVisualCoordinates(playerSide: Color): List<String> {
        return if (playerSide == BLACK) {
            vertices.map { rotationMap[it] ?: it }
        } else {
            vertices
        }
    }

    // Función para obtener posición visual considerando landscape
    fun getVisualPosition(
        visualVertexId: String,
        canvasWidth: Float,
        canvasHeight: Float,
        vWidth: Float,
        isLandscape: Boolean,
        playerSide: Color
    ): Offset {
        val basePos = getAdaptativePosition(visualVertexId, canvasWidth to canvasHeight, vWidth, isLandscape)

        // Aplicar rotación adicional si el jugador es negro
        return if (playerSide == BLACK && !isLandscape) {
            rotateAroundCenter(basePos, canvasWidth, canvasHeight)
        } else {
            basePos
        }
    }

    fun getAdaptativePosition(
        vertexId: String,
        canvasSize: Pair<Float, Float>,
        vWidth: Float,
        isLandscape: Boolean = false
    ): Offset {
        val (width, height) = canvasSize

        // Si está en landscape, tratar el canvas como portrait para el cálculo
        val effectiveWidth = if (isLandscape) height else width
        val effectiveHeight = if (isLandscape) width else height

        // Luego usar la PositionHelper original con las dimensiones efectivas
        val basePos = PositionHelper.getPosition(vertexId, effectiveWidth to effectiveHeight, vWidth)
        val offset = Offset(basePos.x, basePos.y)

        // Si está en landscape, rotar la posición
        return if (isLandscape) {
            Offset(basePos.y, effectiveWidth - basePos.x)
        } else {
            offset
        }
    }

    // Función para encontrar vértice más cercano considerando playerSide y orientación
    fun findClosestVertex(
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

        val visualVertices = getVisualCoordinates(playerSide)

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
    fun rotateAroundCenter(point: Offset, width: Float, height: Float, angleRad: Double = PI): Offset {
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
}