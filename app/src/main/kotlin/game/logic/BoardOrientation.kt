package com.agustin.tarati.game.logic

import com.agustin.tarati.game.core.Color

enum class BoardOrientation {
    PORTRAIT_WHITE,    // 0° - Blanco en la parte inferior
    PORTRAIT_BLACK,    // 180° - Negro en la parte inferior
    LANDSCAPE_WHITE,   // 90° - Blanco a la izquierda
    LANDSCAPE_BLACK    // 270° - Negro a la izquierda
}

fun toBoardOrientation(landScape: Boolean, playerSide: Color): BoardOrientation {
    return when {
        landScape && playerSide == Color.BLACK -> BoardOrientation.LANDSCAPE_BLACK

        landScape && playerSide == Color.WHITE -> BoardOrientation.LANDSCAPE_WHITE

        !landScape && playerSide == Color.BLACK -> BoardOrientation.PORTRAIT_BLACK

        else -> BoardOrientation.PORTRAIT_WHITE
    }
}