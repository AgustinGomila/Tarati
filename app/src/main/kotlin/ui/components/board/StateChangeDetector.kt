package com.agustin.tarati.ui.components.board

import com.agustin.tarati.game.core.Cob
import com.agustin.tarati.game.core.GameBoard.adjacencyMap
import com.agustin.tarati.game.core.GameState
import com.agustin.tarati.game.core.Move

class StateChangeDetector {

    fun detectConversions(move: Move, oldState: GameState, newState: GameState): List<Pair<String, Cob>> {
        val adjacentVertices = adjacencyMap[move.to] ?: emptyList()
        val conversions = mutableListOf<Pair<String, Cob>>()

        adjacentVertices.forEach { vertexId ->
            val oldCob = oldState.cobs[vertexId]
            val newCob = newState.cobs[vertexId]

            if (oldCob != null && newCob != null && oldCob.color != newCob.color) {
                conversions.add(vertexId to newCob)
            }
        }

        return conversions
    }

    fun detectUpgrades(oldState: GameState, newState: GameState): List<Pair<String, Cob>> {
        val upgrades = mutableListOf<Pair<String, Cob>>()

        newState.cobs.forEach { (vertexId, newCob) ->
            val oldCob = oldState.cobs[vertexId]

            val wasUpgraded = when {
                oldCob != null && !oldCob.isUpgraded && newCob.isUpgraded -> true
                oldCob == null && newCob.isUpgraded -> true
                else -> false
            }

            if (wasUpgraded) {
                upgrades.add(vertexId to newCob)
            }
        }

        return upgrades
    }

    fun shouldAnimateMove(oldState: GameState, newState: GameState, move: Move): Boolean {
        return oldState.cobs.containsKey(move.from) &&
                !oldState.cobs.containsKey(move.to) &&
                newState.cobs.containsKey(move.to) &&
                !newState.cobs.containsKey(move.from)
    }
}