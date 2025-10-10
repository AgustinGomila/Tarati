package com.agustin.tarati.game.core

import androidx.compose.ui.geometry.Offset
import com.agustin.tarati.game.core.Color.BLACK
import com.agustin.tarati.game.logic.BoardOrientation
import com.agustin.tarati.ui.components.board.normalizedPositions
import kotlin.math.pow
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
        val squareSize = minDimension * 0.8f // 80% del lado menor, con margen

        // Calcular offsets para centrar el cuadrado
        val offsetX = (canvasWidth - squareSize) / 2
        val offsetY = (canvasHeight - squareSize) / 2

        // Mapear coordenadas normalizadas al área cuadrada
        val x = offsetX + rotated.x * squareSize
        val y = offsetY + rotated.y * squareSize

        return Offset(x, y)
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

        val visualVertices = vertices

        visualVertices.forEach { logicalVertexId ->
            val pos = getVisualPosition(logicalVertexId, canvasWidth, canvasHeight, orientation)
            val distance = sqrt((tapOffset.x - pos.x).pow(2) + (tapOffset.y - pos.y).pow(2))

            if (distance < maxTapDistance && distance < minDistance) {
                minDistance = distance
                closestVertex = logicalVertexId // Devolvemos coordenada lógica
            }
        }

        return closestVertex
    }
}