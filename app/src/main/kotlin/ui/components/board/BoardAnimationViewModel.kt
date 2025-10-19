package com.agustin.tarati.ui.components.board

import androidx.compose.ui.geometry.Size
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.agustin.tarati.game.core.Cob
import com.agustin.tarati.game.core.Color
import com.agustin.tarati.game.core.GameState
import com.agustin.tarati.game.core.Move
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.joinAll
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

enum class AnimationState {
    IDLE, ANIMATING
}

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

    // Mantener el estado actual pero simplificar las dependencias
    private val _animationState = MutableStateFlow(AnimationState.IDLE)
    val animationState: StateFlow<AnimationState> = _animationState.asStateFlow()

    fun processMove(move: Move, oldGameState: GameState, newGameState: GameState): Boolean {
        viewModelScope.launch {
            _animationState.value = AnimationState.ANIMATING

            // Usar el detector en lugar de lógica inline
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
        // Secuencia de animaciones independiente de la lógica del juego
        animateMovement(move, newGameState)

        if (conversions.isNotEmpty()) {
            delay(100) // Pequeño delay entre animaciones
            animateDetectedConversions(conversions)
        }

        if (upgrades.isNotEmpty()) {
            delay(100)
            animateDetectedUpgrades(upgrades)
        }

        _animationState.value = AnimationState.IDLE
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

    fun forceSync() {
        _isAnimating.value = false
        _isHighlighting.value = false
        _animatedPieces.value = emptyMap()
        _highlightAnimations.value = emptyList()
        _currentHighlight.value = null
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
        }

        // Completar movimiento
        _animatedPieces.value = mapOf(
            move.to to animatedPiece.copy(animationProgress = 1f)
        )
    }

    // Animación de Resaltados Individuales

    private val _highlightAnimations = MutableStateFlow<List<HighlightAnimation>>(emptyList())
    val highlightAnimations: StateFlow<List<HighlightAnimation>> = _highlightAnimations.asStateFlow()

    private val _currentHighlight = MutableStateFlow<HighlightAnimation?>(null)
    val currentHighlight: StateFlow<HighlightAnimation?> = _currentHighlight.asStateFlow()

    private val _isHighlighting = MutableStateFlow(false)
    val isHighlighting: StateFlow<Boolean> = _isHighlighting.asStateFlow()

    private val currentAnimations = mutableListOf<Job>()

    fun animate(highlights: List<HighlightAnimation>) {
        viewModelScope.launch {
            _isHighlighting.value = true
            _highlightAnimations.value = highlights

            for (highlight in highlights) {
                _currentHighlight.value = highlight

                when (highlight) {
                    is HighlightAnimation.Vertex -> delay(highlight.highlight.duration)
                    is HighlightAnimation.Edge -> delay(highlight.highlight.duration)
                    is HighlightAnimation.Pause -> delay(300L) // Pausa más corta para mejor fluidez
                }

                _currentHighlight.value = null
                // delay(50) // Pausa mínima entre animaciones
            }

            _highlightAnimations.value = emptyList()
            _isHighlighting.value = false
        }.also { job ->
            currentAnimations.add(job)
            job.invokeOnCompletion { currentAnimations.remove(job) }
        }
    }

    // Función para animaciones superpuestas
    fun animateConcurrent(highlights: List<HighlightAnimation>): Job {
        return viewModelScope.launch {
            _isHighlighting.value = true

            // Ejecutar todas las animaciones en paralelo
            val animationJobs = highlights.map { highlight ->
                launch {
                    _currentHighlight.value = highlight

                    when (highlight) {
                        is HighlightAnimation.Vertex -> delay(highlight.highlight.duration)
                        is HighlightAnimation.Edge -> delay(highlight.highlight.duration)
                        is HighlightAnimation.Pause -> delay(300L)
                    }

                    _currentHighlight.value = null
                }
            }

            // Esperar a que todas terminen
            animationJobs.joinAll()

            _isHighlighting.value = false
        }.also { job ->
            currentAnimations.add(job)
            job.invokeOnCompletion { currentAnimations.remove(job) }
        }
    }

    fun stopHighlights() {
        viewModelScope.launch {
            _highlightAnimations.value = emptyList()
            _currentHighlight.value = null
            _isHighlighting.value = false
        }
    }

    fun reset() {
        _visualState.value = VisualGameState()
        _animatedPieces.value = emptyMap()
        previousVisualState = null
        _isAnimating.value = false
    }
}