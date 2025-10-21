package com.agustin.tarati.ui.components.board.animation

class AnimationCoordinator(
    private val animationViewModel: BoardAnimationViewModel
) {
    fun handleEvent(event: AnimationEvent) {
        when (event) {
            is AnimationEvent.MoveEvent -> animationViewModel.processMove(
                event.move,
                event.oldGameState,
                event.newGameState
            )

            is AnimationEvent.HighlightEvent -> animationViewModel.animate(event.highlights)
            AnimationEvent.StopHighlights -> animationViewModel.stopHighlights()
            AnimationEvent.Reset -> animationViewModel.reset()
            AnimationEvent.SyncState -> animationViewModel.forceSync()
        }
    }
}