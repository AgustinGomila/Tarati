package com.agustin.tarati.ui.components.board

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
            is AnimationEvent.ConcurrentHighlightEvent -> animationViewModel.animateConcurrent(event.highlights)
            AnimationEvent.StopHighlights -> animationViewModel.stopHighlights()
            AnimationEvent.Reset -> animationViewModel.reset()
            AnimationEvent.SyncState -> animationViewModel.forceSync()
        }
    }
}