package com.agustin.tarati.ui.components.board

import androidx.compose.ui.geometry.Size
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.agustin.tarati.game.core.Cob
import com.agustin.tarati.game.core.GameState
import com.agustin.tarati.game.core.Move
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class BoardAnimationViewModelAlt : ViewModel() {
    private val moveDuration = 100L
    private val convertDuration = 70L
    private val upgradeDuration = 70L
    private val animationSteps: Int = 6

    // Estados principales
    private val _visualState = MutableStateFlow(VisualGameState())
    val visualState: StateFlow<VisualGameState> = _visualState.asStateFlow()

    private val _animatedPieces = MutableStateFlow<Map<String, AnimatedPiece>>(emptyMap())
    val animatedPieces: StateFlow<Map<String, AnimatedPiece>> = _animatedPieces.asStateFlow()

    private val _isAnimating = MutableStateFlow(false)
    val isAnimating: StateFlow<Boolean> = _isAnimating.asStateFlow()

    private val _boardSize = MutableStateFlow(Size.Zero)
    val boardSize: StateFlow<Size> = _boardSize.asStateFlow()

    // Estados de highlight
    private val _highlightAnimations = MutableStateFlow<List<HighlightAnimation>>(emptyList())
    val highlightAnimations: StateFlow<List<HighlightAnimation>> = _highlightAnimations.asStateFlow()

    private val _currentHighlight = MutableStateFlow<HighlightAnimation?>(null)
    val currentHighlight: StateFlow<HighlightAnimation?> = _currentHighlight.asStateFlow()

    private val _isHighlighting = MutableStateFlow(false)
    val isHighlighting: StateFlow<Boolean> = _isHighlighting.asStateFlow()

    // Control de jobs
    private var currentAnimationJob: Job? = null
    private var currentHighlightJob: Job? = null

    // Servicios
    private val stateChangeDetector = StateChangeDetector()

    fun updateBoardSize(newBoardSize: Size) {
        _boardSize.value = newBoardSize
    }

    fun syncState(gameState: GameState) {
        if (!_isAnimating.value) {
            updateVisualState(gameState)
        }
    }

    fun forceSync() {
        currentAnimationJob?.cancel()
        currentHighlightJob?.cancel()
        _isAnimating.value = false
        _isHighlighting.value = false
        _animatedPieces.value = emptyMap()
        _highlightAnimations.value = emptyList()
        _currentHighlight.value = null
    }

    fun processMove(move: Move, oldGameState: GameState, newGameState: GameState, debug: Boolean = false): Boolean {
        currentAnimationJob?.cancel()

        currentAnimationJob = viewModelScope.launch {
            _isAnimating.value = true

            try {
                // 1. Preparar estado inicial
                val visualStateWithoutMovedPiece = oldGameState.cobs - move.from
                _visualState.value = VisualGameState(
                    cobs = visualStateWithoutMovedPiece,
                    currentTurn = oldGameState.currentTurn
                )

                // 2. Animar movimiento
                animateMovement(move, newGameState)

                // 3. Detectar y animar conversiones
                val conversions = stateChangeDetector.detectConversions(move, oldGameState, newGameState)
                if (conversions.isNotEmpty()) {
                    animateConversions(conversions)
                }

                // 4. Detectar y animar upgrades
                val upgrades = stateChangeDetector.detectUpgrades(oldGameState, newGameState)
                if (upgrades.isNotEmpty()) {
                    animateUpgrades(upgrades)
                }

                // 5. Estado final
                updateVisualState(newGameState)

            } finally {
                _animatedPieces.value = emptyMap()
                _isAnimating.value = false
            }
        }
        return true
    }

    private suspend fun animateConversions(conversions: List<Pair<String, Cob>>) {
        val duration = convertDuration
        val steps = animationSteps

        conversions.forEach { (vertexId, newCob) ->
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

            _animatedPieces.value = _animatedPieces.value.toMutableMap().apply {
                put(vertexId, animatedPiece)
            }

            repeat(steps) { step ->
                val progress = (step + 1) / steps.toFloat()
                _animatedPieces.value = _animatedPieces.value.toMutableMap().apply {
                    put(vertexId, animatedPiece.copy(conversionProgress = progress))
                }
                delay(duration / steps)
            }

            updatePieceInVisualState(vertexId, newCob)
            _animatedPieces.value -= vertexId
        }
    }

    private suspend fun animateUpgrades(upgrades: List<Pair<String, Cob>>) {
        val duration = upgradeDuration
        val steps = animationSteps

        upgrades.forEach { (vertexId, newCob) ->
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

                _animatedPieces.value = _animatedPieces.value.toMutableMap().apply {
                    put(vertexId, animatedPiece)
                }

                repeat(steps) { step ->
                    val progress = (step + 1) / steps.toFloat()
                    _animatedPieces.value = _animatedPieces.value.toMutableMap().apply {
                        put(vertexId, animatedPiece.copy(upgradeProgress = progress))
                    }
                    delay(duration / steps)
                }

                updatePieceInVisualState(vertexId, newCob)
                _animatedPieces.value -= vertexId
            } else {
                updatePieceInVisualState(vertexId, newCob)
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

        val steps = animationSteps
        val stepDelay = moveDuration / steps

        repeat(steps) { step ->
            val progress = (step + 1) / steps.toFloat()
            _animatedPieces.value = mapOf(
                move.to to animatedPiece.copy(animationProgress = progress)
            )
            delay(stepDelay)
        }

        _animatedPieces.value = mapOf(
            move.to to animatedPiece.copy(animationProgress = 1f)
        )
    }

    // Funciones de highlight
    fun animate(highlights: List<HighlightAnimation>) {
        currentHighlightJob?.cancel()

        currentHighlightJob = viewModelScope.launch {
            _isHighlighting.value = true
            _highlightAnimations.value = highlights

            for (highlight in highlights) {
                _currentHighlight.value = highlight

                when (highlight) {
                    is HighlightAnimation.Vertex -> delay(highlight.highlight.duration)
                    is HighlightAnimation.Edge -> delay(highlight.highlight.duration)
                    is HighlightAnimation.Pause -> delay(300L)
                }

                _currentHighlight.value = null
            }

            _highlightAnimations.value = emptyList()
            _isHighlighting.value = false
        }
    }

    fun animateConcurrent(highlights: List<HighlightAnimation>): Job {
        currentHighlightJob?.cancel()

        return viewModelScope.launch {
            _isHighlighting.value = true

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

            animationJobs.forEach { it.join() }
            _isHighlighting.value = false
        }.also { job ->
            currentHighlightJob = job
            job.invokeOnCompletion { currentHighlightJob = null }
        }
    }

    fun stopHighlights() {
        currentHighlightJob?.cancel()
        viewModelScope.launch {
            _highlightAnimations.value = emptyList()
            _currentHighlight.value = null
            _isHighlighting.value = false
        }
    }

    fun reset() {
        currentAnimationJob?.cancel()
        currentHighlightJob?.cancel()

        _visualState.value = VisualGameState()
        _animatedPieces.value = emptyMap()
        _isAnimating.value = false
        _highlightAnimations.value = emptyList()
        _currentHighlight.value = null
        _isHighlighting.value = false
        _boardSize.value = Size.Zero
    }

    private fun updateVisualState(gameState: GameState) {
        _visualState.value = VisualGameState(
            cobs = gameState.cobs.toMap(),
            currentTurn = gameState.currentTurn
        )
    }

    private fun updatePieceInVisualState(vertexId: String, cob: Cob) {
        val currentVisualState = _visualState.value.cobs.toMutableMap()
        currentVisualState[vertexId] = cob
        _visualState.value = _visualState.value.copy(cobs = currentVisualState)
    }
}