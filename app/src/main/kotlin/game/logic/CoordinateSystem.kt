package com.agustin.tarati.game.logic

enum class BoardOrientation {
    PORTRAIT_WHITE,    // 0° - Blanco en la parte inferior
    PORTRAIT_BLACK,    // 180° - Negro en la parte inferior
    LANDSCAPE_WHITE,   // 90° - Blanco a la izquierda
    LANDSCAPE_BLACK    // 270° - Negro a la izquierda
}

data class NormalizedCoord(val x: Float, val y: Float) {
    fun rotate(orientation: BoardOrientation): NormalizedCoord {
        return when (orientation) {
            BoardOrientation.PORTRAIT_WHITE -> NormalizedCoord(x, y)
            BoardOrientation.PORTRAIT_BLACK -> NormalizedCoord(1 - x, 1 - y)
            BoardOrientation.LANDSCAPE_WHITE -> NormalizedCoord(y, 1 - x)
            BoardOrientation.LANDSCAPE_BLACK -> NormalizedCoord(1 - y, x)
        }
    }
}