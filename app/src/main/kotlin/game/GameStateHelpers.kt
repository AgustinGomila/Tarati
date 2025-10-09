package com.agustin.tarati.game

// Función de extensión para modificar piezas
fun GameState.modifyChecker(position: String, color: Color? = null, isUpgraded: Boolean? = null): GameState {
    val newCheckers = checkers.toMutableMap()

    if (color == null && isUpgraded == null) {
        // Si ambos son null, eliminar la pieza
        newCheckers.remove(position)
    } else {
        val currentChecker = newCheckers[position]
        val newColor = color ?: currentChecker?.color ?: Color.WHITE
        val newUpgraded = isUpgraded ?: currentChecker?.isUpgraded ?: false

        newCheckers[position] = Checker(newColor, newUpgraded)
    }

    return this.copy(checkers = newCheckers)
}

// Función para mover piezas
fun GameState.moveChecker(from: String, to: String): GameState {
    val newCheckers = checkers.toMutableMap()
    val checker = newCheckers[from] ?: return this

    newCheckers.remove(from)
    newCheckers[to] = checker

    return this.copy(checkers = newCheckers)
}

// Función para cambiar el turno
fun GameState.withTurn(newTurn: Color): GameState {
    return this.copy(currentTurn = newTurn)
}

// Función helper para usar el builder
fun createGameState(block: GameStateBuilder.() -> Unit): GameState {
    return GameStateBuilder().apply(block).build()
}