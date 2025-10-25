package com.agustin.tarati.game.core

import com.agustin.tarati.R

enum class CobColor { WHITE, BLACK }

fun CobColor.opponent(): CobColor = if (this == CobColor.BLACK) CobColor.WHITE else CobColor.BLACK

fun CobColor.getColorStringResource(): Int = when (this) {
    CobColor.WHITE -> R.string.white
    CobColor.BLACK -> R.string.black
}