package com.agustin.tarati.game.ai

import androidx.lifecycle.ViewModel
import com.agustin.tarati.game.ai.TaratiAI.getNextBestMove
import com.agustin.tarati.game.core.GameState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext

class AIThinkingViewModel : ViewModel() {

    private val _isAIThinking = MutableStateFlow(false)
    val isAIThinking: StateFlow<Boolean> = _isAIThinking.asStateFlow()

    suspend fun calculateAIMove(
        gameState: GameState,
        onMoveFound: (String, String) -> Unit,
        debug: Boolean = false,
    ) {
        _isAIThinking.value = true
        if (debug) println("DEBUG: AI starting to think...")

        val result = try {
            withContext(Dispatchers.Default) {
                getNextBestMove(
                    gameState = gameState,
                    debug = false
                )
            }
        } catch (t: Throwable) {
            t.printStackTrace()
            null
        } finally {
            _isAIThinking.value = false
        }

        if (debug) println("AI calculated move: ${result?.move}")

        result?.move?.let { move ->
            onMoveFound(move.from, move.to)
        }
    }
}