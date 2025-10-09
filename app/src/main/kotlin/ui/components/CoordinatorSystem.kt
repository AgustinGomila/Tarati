package com.agustin.tarati.ui.components

import com.agustin.tarati.game.Color
import com.agustin.tarati.game.GameBoard

object CoordinateSystem {

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
        return if (playerSide == Color.BLACK) {
            rotationMap[logicalCoord] ?: logicalCoord
        } else {
            logicalCoord
        }
    }

    fun visualToLogical(visualCoord: String, playerSide: Color): String {
        return if (playerSide == Color.BLACK) {
            inverseRotationMap[visualCoord] ?: visualCoord
        } else {
            visualCoord
        }
    }

    fun getVisualCoordinates(playerSide: Color): List<String> {
        return if (playerSide == Color.BLACK) {
            GameBoard.vertices.map { rotationMap[it] ?: it }
        } else {
            GameBoard.vertices
        }
    }
}