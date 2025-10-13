package com.agustin.tarati.game.core

import com.agustin.tarati.R

enum class Color { WHITE, BLACK }

fun Color.opponent(): Color = if (this == Color.BLACK) Color.WHITE else Color.BLACK

fun Color.getColorStringResource(): Int = when (this) {
    Color.WHITE -> R.string.white
    Color.BLACK -> R.string.black
}