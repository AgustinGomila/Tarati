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

            is AnimationEvent.HighlightEvent -> {
                val source = event.source ?: "unknown"
                animationViewModel.animate(event.highlights, source)
            }

            AnimationEvent.StopHighlights -> animationViewModel.stopHighlights()
            AnimationEvent.Reset -> animationViewModel.reset()
            AnimationEvent.SyncState -> animationViewModel.forceSync()
            AnimationEvent.ClearQueue -> animationViewModel.clearQueue()
        }
    }
}