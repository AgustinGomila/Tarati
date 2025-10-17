package com.agustin.tarati.game.ai

import androidx.annotation.StringRes
import com.agustin.tarati.R

enum class Difficulty(
    @param:StringRes val displayNameRes: Int,
    val depth: Int
) {
    EASY(R.string.difficulty_easy, 3),
    MEDIUM(R.string.difficulty_medium, 6),
    HARD(R.string.difficulty_hard, 9),
    CHAMPION(R.string.difficulty_champion, 12);

    val aiDepth: Int
        get() = when (this) {
            EASY -> 2
            MEDIUM -> 4
            HARD -> 6
            CHAMPION -> 8
        }

    companion object {
        val ALL = entries
        val MIN = EASY
        val DEFAULT = MEDIUM
        val MAX = CHAMPION

        fun getByDepth(depth: Int): Difficulty =
            ALL.firstOrNull { it.depth == depth } ?: DEFAULT
    }
}