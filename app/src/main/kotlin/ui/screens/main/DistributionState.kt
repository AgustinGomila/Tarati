package com.agustin.tarati.ui.screens.main

data class DistributionState(
    val isValid: Boolean,
    val isCompleted: Boolean
) {
    companion object {
        fun fromPieceCounts(pieceCounts: PieceCounts): DistributionState {
            val total = pieceCounts.white + pieceCounts.black
            val isValid = when {
                total > 8 -> false
                pieceCounts.white == 7 && pieceCounts.black == 1 -> true
                pieceCounts.white == 6 && pieceCounts.black == 2 -> true
                pieceCounts.white == 5 && pieceCounts.black == 3 -> true
                pieceCounts.white == 4 && pieceCounts.black == 4 -> true
                pieceCounts.white == 3 && pieceCounts.black == 5 -> true
                pieceCounts.white == 2 && pieceCounts.black == 6 -> true
                pieceCounts.white == 1 && pieceCounts.black == 7 -> true
                total < 8 -> true
                else -> false
            }

            val isCompleted = total == 8 && isValid

            return DistributionState(isValid, isCompleted)
        }
    }
}