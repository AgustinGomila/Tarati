package com.agustin.tarati.game.core

data class GameState(
    val checkers: Map<String, Checker>,
    val currentTurn: Color
) {
    fun hashBoard(): String {
        val keys = checkers.keys.sorted()
        val sb = StringBuilder()
        for (k in keys) {
            val c = checkers[k]!!
            sb.append("$k:${c.color}:${c.isUpgraded};")
        }
        sb.append("turn:${currentTurn}")
        return sb.toString()
    }
}