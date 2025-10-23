package com.agustin.tarati.game.ai

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.agustin.tarati.game.ai.TaratiAI.getNextBestMove
import com.agustin.tarati.game.core.Color
import com.agustin.tarati.game.core.GameState
import com.agustin.tarati.game.core.GameStatus
import com.agustin.tarati.game.logic.isGameOver
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AIThinkingViewModel : ViewModel() {
    var isAIThinking by mutableStateOf(false)
        private set

    suspend fun calculateAIMove(
        gameState: GameState,
        gameStatus: GameStatus,
        playerSide: Color,
        aiEnabled: Boolean,
        isEditing: Boolean,
        onMoveFound: (String, String) -> Unit,
        debug: Boolean = false,
    ) {
        if (!aiEnabled || isEditing || gameStatus != GameStatus.PLAYING) {
            if (debug) println("DEBUG: AI blocked - gameStatus: $gameStatus")
            return
        }

        val shouldAIPlay = gameState.currentTurn != playerSide &&
                !gameState.isGameOver()

        if (!shouldAIPlay) return

        isAIThinking = true
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
            isAIThinking = false
        }

        if (debug) println("AI calculated move: ${result?.move}")

        result?.move?.let { move ->
            onMoveFound(move.from, move.to)
        }
    }
}