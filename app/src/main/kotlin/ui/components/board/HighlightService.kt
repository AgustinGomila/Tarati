package com.agustin.tarati.ui.components.board

import com.agustin.tarati.game.core.Move

class HighlightService(private val animationViewModel: BoardAnimationViewModel) {

    fun createMoveHighlights(move: Move): List<HighlightAnimation> {
        return createMoveHighlight(move.from, move.to)
    }

    fun createValidMoveHighlights(validMoves: List<String>): List<HighlightAnimation> {
        return createValidMovesHighlights(validMoves)
    }

    fun createCaptureHighlights(capturedPieces: List<String>): List<HighlightAnimation> {
        return capturedPieces.flatMap { createCaptureHighlight(it) }
    }

    fun createUpgradeHighlights(upgradedPieces: List<String>): List<HighlightAnimation> {
        return upgradedPieces.flatMap { createUpgradeHighlight(it) }
    }

    fun animateHighlights(highlights: List<HighlightAnimation>) {
        animationViewModel.animate(highlights)
    }

    fun stopHighlights() {
        animationViewModel.stopHighlights()
    }
}