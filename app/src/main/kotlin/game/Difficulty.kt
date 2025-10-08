package com.agustin.tarati.game

import androidx.annotation.StringRes
import com.agustin.tarati.R

data class Difficulty(
    @param:StringRes val displayNameRes: Int,
    val depth: Int
) {
    companion object {
        val EASY = Difficulty(R.string.difficulty_easy, 3)
        val MEDIUM = Difficulty(R.string.difficulty_medium, 6)
        val HARD = Difficulty(R.string.difficulty_hard, 9)
        val CHAMPION = Difficulty(R.string.difficulty_champion, 12)

        val ALL = listOf(EASY, MEDIUM, HARD, CHAMPION)
        val DEFAULT = MEDIUM
    }
}