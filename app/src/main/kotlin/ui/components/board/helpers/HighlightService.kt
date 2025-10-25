package com.agustin.tarati.ui.components.board.helpers

import com.agustin.tarati.game.core.Move
import com.agustin.tarati.ui.components.board.animation.BoardAnimationViewModel
import com.agustin.tarati.ui.components.board.animation.HighlightAnimation
import com.agustin.tarati.ui.components.board.animation.createMoveHighlight

class HighlightService(private val animationViewModel: BoardAnimationViewModel) {

    fun createMoveHighlights(move: Move): List<HighlightAnimation> {
        return createMoveHighlight(move.from, move.to)
    }

    fun animateHighlights(highlights: List<HighlightAnimation>) {
        animationViewModel.animateSerie(highlights)
    }

    fun stopHighlights() {
        animationViewModel.stopHighlights()
    }
}