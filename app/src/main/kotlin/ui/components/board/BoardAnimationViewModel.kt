package com.agustin.tarati.ui.components.board

import androidx.compose.ui.geometry.Size
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.agustin.tarati.game.core.Cob
import com.agustin.tarati.game.core.Color
import com.agustin.tarati.game.core.GameState
import com.agustin.tarati.game.core.Move
import com.agustin.tarati.game.core.getValidVertex
import kotlinx.coroutines.Job
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

class BoardAnimationViewModel(
    private val stateChangeDetector: StateChangeDetector = StateChangeDetector()
) : ViewModel() {

    private val moveDuration = 100L
    private val convertDuration = 70L
    private val upgradeDuration = 70L
    private val animationSteps: Int = 6

    fun processMove(move: Move, oldGameState: GameState, newGameState: GameState): Boolean {
        viewModelScope.launch {
            val conversions = stateChangeDetector.detectConversions(move, oldGameState, newGameState)
            val upgrades = stateChangeDetector.detectUpgrades(oldGameState, newGameState)
            animateMoveSequence(move, conversions, upgrades, newGameState)
        }
        return true
    }

    private suspend fun animateMoveSequence(
        move: Move,
        conversions: List<Pair<String, Cob>>,
        upgrades: List<Pair<String, Cob>>,
        newGameState: GameState
    ) {
        animateMovement(move, newGameState)
        if (conversions.isNotEmpty()) animateDetectedConversions(conversions)
        if (upgrades.isNotEmpty()) animateDetectedUpgrades(upgrades)
    }

    suspend fun animateMovement(move: Move, newGameState: GameState) {
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
            if (step == steps - 1) {
                // Efectos especiales
                animate(createValidMovesHighlights(newGameState.getValidVertex(move.to, cob)))
            }
        }

        // Completar movimiento
        _animatedPieces.value = mapOf(
            move.to to animatedPiece.copy(animationProgress = 1f)
        )
    }

    private suspend fun animateDetectedConversions(conversions: List<Pair<String, Cob>>) {
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
                if (step == steps - 1) {
                    // Efectos especiales
                    animate(createCaptureHighlight(vertexId))
                }
            }

            // Actualizar estado visual para esta pieza convertida
            val currentVisualState = _visualState.value.cobs.toMutableMap()
            currentVisualState[vertexId] = newCob
            _visualState.value = _visualState.value.copy(cobs = currentVisualState)

            // Remover de piezas animadas
            _animatedPieces.value -= vertexId
        }
    }

    private suspend fun animateDetectedUpgrades(upgrades: List<Pair<String, Cob>>) {
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
                    upgradeProgress = 0f,
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
                    if (step == steps - 1) {
                        // Efectos especiales
                        animate(createUpgradeHighlight(vertexId))
                    }
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

    // Animación de Resaltados Individuales

    private val _currentHighlights = MutableStateFlow<List<HighlightAnimation>>(emptyList())
    val currentHighlights: StateFlow<List<HighlightAnimation>> = _currentHighlights.asStateFlow()

    fun animate(highlights: List<HighlightAnimation>): Job {
        return viewModelScope.launch {
            _currentHighlights.value = highlights

            val maxDuration = highlights.maxOfOrNull {
                when (it) {
                    is HighlightAnimation.Vertex -> it.highlight.duration
                    is HighlightAnimation.Edge -> it.highlight.duration
                    is HighlightAnimation.Pause -> 200L
                }
            } ?: 0L

            delay(maxDuration)
            _currentHighlights.value = emptyList()
        }
    }

    fun stopHighlights() {
        viewModelScope.launch {
            _currentHighlights.value = emptyList()
        }
    }

    fun reset() {
        _visualState.value = VisualGameState()
        _animatedPieces.value = emptyMap()
        previousVisualState = null
        _isAnimating.value = false
    }

    fun forceSync() {
        _isAnimating.value = false
        _animatedPieces.value = emptyMap()
        _currentHighlights.value = emptyList()
    }
}