package com.agustin.tarati.game.core

enum class Color { WHITE, BLACK }

fun Color.switchColor(): Color = if (this == Color.BLACK) Color.WHITE else Color.BLACK