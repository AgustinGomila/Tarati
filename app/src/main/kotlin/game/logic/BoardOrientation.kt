package com.agustin.tarati.game.logic

import com.agustin.tarati.game.core.CobColor

enum class BoardOrientation {
    PORTRAIT_WHITE,  // 0° - Blanco en la parte inferior
    PORTRAIT_BLACK,  // 180° - Negro en la parte inferior
    LANDSCAPE_WHITE, // 90° - Blanco a la izquierda
    LANDSCAPE_BLACK  // 270° - Negro a la izquierda
}

fun BoardOrientation.isPortrait(): Boolean {
    return this == BoardOrientation.PORTRAIT_WHITE || this == BoardOrientation.PORTRAIT_BLACK
}

fun toBoardOrientation(landScape: Boolean, playerSide: CobColor): BoardOrientation {
    return when {
        landScape && playerSide == CobColor.BLACK -> BoardOrientation.LANDSCAPE_BLACK
        landScape && playerSide == CobColor.WHITE -> BoardOrientation.LANDSCAPE_WHITE
        !landScape && playerSide == CobColor.BLACK -> BoardOrientation.PORTRAIT_BLACK
        else -> BoardOrientation.PORTRAIT_WHITE
    }
}