package com.agustin.tarati.ui.components.board

import androidx.compose.ui.geometry.Size
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.agustin.tarati.game.core.Cob
import com.agustin.tarati.game.core.Color
import com.agustin.tarati.game.core.GameBoard.adjacencyMap
import com.agustin.tarati.game.core.GameState
import com.agustin.tarati.game.core.Move
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class AnimatedPiece(
    val vertexId: String,
    val cob: Cob,
    val currentPos: String,
    val targetPos: String,
    val animationProgress: Float = 1f,
    val upgradeProgress: Float = 1f,
    val conversionProgress: Float = 1f,
    val isConverting: Boolean = false,
    val targetColor: Color? = null
)

data class VisualGameState(
    val cobs: Map<String, Cob> = emptyMap(),
    val currentTurn: Color? = null
)

class BoardAnimationViewModel : ViewModel() {
    private val moveDuration = 100L
    private val convertDuration = 70L
    private val upgradeDuration = 70L
    private val animationSteps: Int = 6

    private val _visualState = MutableStateFlow(VisualGameState())
    val visualState: StateFlow<VisualGameState> = _visualState.asStateFlow()

    private val _animatedPieces = MutableStateFlow<Map<String, AnimatedPiece>>(emptyMap())
    val animatedPieces: StateFlow<Map<String, AnimatedPiece>> = _animatedPieces.asStateFlow()

    private val _isAnimating = MutableStateFlow(false)
    val isAnimating: StateFlow<Boolean> = _isAnimating.asStateFlow()

    private var previousVisualState: VisualGameState? = null

    private val _boardSize = MutableStateFlow(Size.Zero)
    val boardSize: StateFlow<Size> = _boardSize.asStateFlow()
    fun updateBoardSize(newBoardSize: Size) {
        _boardSize.value = newBoardSize
    }

    // Sincronizar el estado sin animación
    fun syncState(gameState: GameState) {
        if (!_isAnimating.value) {
            _visualState.value = VisualGameState(
                cobs = gameState.cobs.toMap(),
                currentTurn = gameState.currentTurn
            )
        }
    }

    fun processMove(move: Move, oldGameState: GameState, newGameState: GameState, debug: Boolean = false): Boolean {
        if (_isAnimating.value) {
            if (debug) println("animation in progress, skipping animation...")
            return false
        }

        viewModelScope.launch {
            _isAnimating.value = true

            // SINCRONIZAR: Asegurar que el estado visual coincide con el estado anterior del juego
            _visualState.value = VisualGameState(
                cobs = oldGameState.cobs.toMap(),
                currentTurn = oldGameState.currentTurn
            )
            previousVisualState = _visualState.value

            // Remover solo la pieza que se va a mover del estado visual inicial
            val visualStateWithoutMovedPiece = oldGameState.cobs - move.from
            _visualState.value = VisualGameState(
                cobs = visualStateWithoutMovedPiece,
                currentTurn = oldGameState.currentTurn
            )

            // 2. ANIMAR MOVIMIENTO
            animateMovement(move, newGameState)

            // 3. ANIMAR CONVERSIONES (después del movimiento)
            animateConversions(move, oldGameState, newGameState)

            // 4. ANIMAR UPGRADES (último)
            animateUpgrades(oldGameState, newGameState)

            // 5. ACTUALIZAR ESTADO VISUAL FINAL
            _visualState.value = VisualGameState(
                cobs = newGameState.cobs.toMap(),
                currentTurn = newGameState.currentTurn
            )

            _animatedPieces.value = emptyMap()
            _isAnimating.value = false
        }
        return true
    }

    private suspend fun animateConversions(move: Move, oldGameState: GameState, newGameState: GameState) {
        val adjacentVertices = adjacencyMap[move.to] ?: emptyList()
        val conversions = mutableListOf<Pair<String, Cob>>() // Guardar vertexId y nuevo cob

        // Detectar vértices adyacentes que cambiaron de color
        adjacentVertices.forEach { vertexId ->
            val oldCob = oldGameState.cobs[vertexId]
            val newCob = newGameState.cobs[vertexId]

            if (oldCob != null && newCob != null &&
                oldCob.color != newCob.color
            ) {
                conversions.add(vertexId to newCob)
            }
        }

        if (conversions.isEmpty()) return

        val duration = convertDuration
        val steps = animationSteps

        conversions.forEach { (vertexId, newCob) ->
            // Crear animated piece para la conversión (pieza en su posición actual)
            val animatedPiece = AnimatedPiece(
                vertexId = vertexId,
                cob = newCob,
                currentPos = vertexId,
                targetPos = vertexId,
                animationProgress = 1f,
                upgradeProgress = 1f,
                conversionProgress = 0f,
                isConverting = true,
                targetColor = newCob.color
            )

            // Agregar a piezas animadas
            _animatedPieces.value = _animatedPieces.value.toMutableMap().apply {
                put(vertexId, animatedPiece)
            }

            // Animar progreso de conversión
            repeat(steps) { step ->
                val progress = (step + 1) / steps.toFloat()
                _animatedPieces.value = _animatedPieces.value.toMutableMap().apply {
                    put(vertexId, animatedPiece.copy(conversionProgress = progress))
                }
                delay(duration / steps)
            }

            // Actualizar estado visual para esta pieza convertida
            val currentVisualState = _visualState.value.cobs.toMutableMap()
            currentVisualState[vertexId] = newCob
            _visualState.value = _visualState.value.copy(cobs = currentVisualState)

            // Remover de piezas animadas
            _animatedPieces.value -= vertexId
        }
    }

    private suspend fun animateUpgrades(oldGameState: GameState, newGameState: GameState) {
        val upgrades = mutableListOf<Pair<String, Cob>>() // Guardar vertexId y nuevo cob

        // Detectar piezas que se mejoraron (incluyendo la pieza movida)
        newGameState.cobs.forEach { (vertexId, newCob) ->
            val oldCob = oldGameState.cobs[vertexId]

            val wasUpgraded = when {
                // Pieza existente que se mejoró
                oldCob != null && !oldCob.isUpgraded && newCob.isUpgraded -> true
                // Pieza nueva que está mejorada (movida a base enemiga)
                oldCob == null && newCob.isUpgraded -> true
                else -> false
            }

            if (wasUpgraded) {
                upgrades.add(vertexId to newCob)
            }
        }

        if (upgrades.isEmpty()) return

        val duration = upgradeDuration
        val steps = animationSteps

        upgrades.forEach { (vertexId, newCob) ->
            // Solo animar si la pieza no está actualmente siendo animada por movimiento
            if (!_animatedPieces.value.containsKey(vertexId)) {
                val animatedPiece = AnimatedPiece(
                    vertexId = vertexId,
                    cob = newCob,
                    currentPos = vertexId,
                    targetPos = vertexId,
                    animationProgress = 1f,
                    upgradeProgress = 0f, // Comenzar en 0
                    conversionProgress = 1f
                )

                // Agregar a piezas animadas
                _animatedPieces.value = _animatedPieces.value.toMutableMap().apply {
                    put(vertexId, animatedPiece)
                }

                // Animar progreso de upgrade
                repeat(steps) { step ->
                    val progress = (step + 1) / steps.toFloat()
                    _animatedPieces.value = _animatedPieces.value.toMutableMap().apply {
                        put(vertexId, animatedPiece.copy(upgradeProgress = progress))
                    }
                    delay(duration / steps)
                }

                // Actualizar estado visual para esta pieza mejorada
                val currentVisualState = _visualState.value.cobs.toMutableMap()
                currentVisualState[vertexId] = newCob
                _visualState.value = _visualState.value.copy(cobs = currentVisualState)

                // Remover de piezas animadas
                _animatedPieces.value -= vertexId
            } else {
                // Si la pieza ya está siendo animada (por movimiento), solo actualizar el estado visual
                val currentVisualState = _visualState.value.cobs.toMutableMap()
                currentVisualState[vertexId] = newCob
                _visualState.value = _visualState.value.copy(cobs = currentVisualState)
            }
        }
    }

    private suspend fun animateMovement(move: Move, newGameState: GameState) {
        val cob = newGameState.cobs[move.to] ?: return

        val animatedPiece = AnimatedPiece(
            vertexId = move.to,
            cob = cob,
            currentPos = move.from,
            targetPos = move.to,
            animationProgress = 0f,
            upgradeProgress = 1f,
            conversionProgress = 1f
        )

        _animatedPieces.value = mapOf(move.to to animatedPiece)

        val duration = moveDuration
        val steps = animationSteps
        val stepDelay = duration / steps

        repeat(steps) { step ->
            val progress = (step + 1) / steps.toFloat()
            _animatedPieces.value = mapOf(
                move.to to animatedPiece.copy(animationProgress = progress)
            )
            delay(stepDelay)
        }

        // Completar movimiento
        _animatedPieces.value = mapOf(
            move.to to animatedPiece.copy(animationProgress = 1f)
        )
    }

    fun reset() {
        _visualState.value = VisualGameState()
        _animatedPieces.value = emptyMap()
        previousVisualState = null
        _isAnimating.value = false
    }
}