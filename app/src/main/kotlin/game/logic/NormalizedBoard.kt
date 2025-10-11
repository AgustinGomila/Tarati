package com.agustin.tarati.game.logic

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