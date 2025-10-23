package com.agustin.tarati.ui.components.board.animation

import com.agustin.tarati.game.core.GameState
import com.agustin.tarati.game.core.Move

sealed class AnimationEvent {
    data class MoveEvent(val move: Move, val oldGameState: GameState, val newGameState: GameState) : AnimationEvent()
    data class HighlightEvent(val highlights: List<HighlightAnimation>, val source: String? = null) : AnimationEvent()
    object StopHighlights : AnimationEvent()
    object Reset : AnimationEvent()
    object SyncState : AnimationEvent()
    object ClearQueue : AnimationEvent()
}