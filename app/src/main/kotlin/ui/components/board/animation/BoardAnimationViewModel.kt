package com.agustin.tarati.ui.components.board.animation

import androidx.compose.ui.geometry.Size
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.agustin.tarati.game.core.Cob
import com.agustin.tarati.game.core.CobColor
import com.agustin.tarati.game.core.GameBoard.BoardRegion
import com.agustin.tarati.game.core.GameState
import com.agustin.tarati.game.core.Move
import com.agustin.tarati.game.core.getValidVertex
import com.agustin.tarati.game.logic.detectConversions
import com.agustin.tarati.game.logic.detectUpgrades
import com.agustin.tarati.game.logic.findClosedRegion
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import java.util.*

data class AnimatedCob(
    val vertexId: String,
    val cob: Cob,
    val currentPos: String,
    val targetPos: String,
    val animationProgress: Float = 1f,
    val upgradeProgress: Float = 1f,
    val conversionProgress: Float = 1f,
    val isConverting: Boolean = false,
    val targetColor: CobColor? = null
)

data class VisualGameState(
    val cobs: Map<String, Cob> = emptyMap(),
    val currentTurn: CobColor? = null
)

private data class AnimationGroup(
    val highlights: List<HighlightAnimation>,
    val groupId: String = UUID.randomUUID().toString(),
    val source: String = "unknown",
    val timestamp: Long = System.currentTimeMillis()
)

class BoardAnimationViewModel : ViewModel() {

    private val moveDuration = 100L
    private val convertDuration = 70L
    private val upgradeDuration = 70L
    private val animationSteps: Int = 6

    fun processMove(move: Move, oldGameState: GameState, newGameState: GameState): Boolean {
        viewModelScope.launch {
            val conversions = oldGameState.detectConversions(move, newGameState)
            val upgrades = oldGameState.detectUpgrades(newGameState)
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

        val animatedCob = AnimatedCob(
            vertexId = move.to,
            cob = cob,
            currentPos = move.from,
            targetPos = move.to,
            animationProgress = 0f,
            upgradeProgress = 1f,
            conversionProgress = 1f
        )

        _animatedPieces.value = mapOf(move.to to animatedCob)

        val duration = moveDuration
        val steps = animationSteps
        val stepDelay = duration / steps

        repeat(steps) { step ->
            val progress = (step + 1) / steps.toFloat()
            _animatedPieces.value = mapOf(
                move.to to animatedCob.copy(animationProgress = progress)
            )

            // Efecto de estela en el movimiento
            if (_animateEffects.value && step == steps / 2) {
                animateParallel(createMoveHighlight(move.from, move.to))
            }
            delay(stepDelay)
        }

        if (_animateEffects.value) {
            // Efectos especiales sobre casillas libres amenazadas cuando llega a destino
            val highlights = createValidMovesHighlights(newGameState.getValidVertex(move.to, cob)).toMutableList()

            // Efecto sobre regiones cerradas
            newGameState.findClosedRegion(move.to, cob.color)?.let {
                highlights.add(createRegionHighlight(it))
            }

            animateSerie(highlights)
        }

        // Completar movimiento
        _animatedPieces.value = mapOf(
            move.to to animatedCob.copy(animationProgress = 1f)
        )
    }

    private suspend fun animateDetectedConversions(conversions: List<Pair<String, Cob>>) {
        if (conversions.isEmpty()) return

        val duration = convertDuration
        val steps = animationSteps
        val stepDelay = duration / steps

        conversions.forEach { (vertexId, newCob) ->
            // Crear animated piece para la conversión (pieza en su posición actual)
            val animatedCob = AnimatedCob(
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
                put(vertexId, animatedCob)
            }

            // Animar progreso de conversión
            repeat(steps) { step ->
                val progress = (step + 1) / steps.toFloat()
                _animatedPieces.value = _animatedPieces.value.toMutableMap().apply {
                    put(vertexId, animatedCob.copy(conversionProgress = progress))
                }
                // Efecto sobre piezas capturadas
                if (_animateEffects.value && step == steps / 2) {
                    // Efectos especiales
                    animateParallel(createCaptureHighlight(vertexId))
                }
                delay(stepDelay)
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
        val stepDelay = duration / steps

        upgrades.forEach { (vertexId, newCob) ->
            // Solo animar si la pieza no está actualmente siendo animada por movimiento
            if (!_animatedPieces.value.containsKey(vertexId)) {
                val animatedCob = AnimatedCob(
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
                    put(vertexId, animatedCob)
                }

                // Animar progreso de upgrade
                repeat(steps) { step ->
                    val progress = (step + 1) / steps.toFloat()
                    _animatedPieces.value = _animatedPieces.value.toMutableMap().apply {
                        put(vertexId, animatedCob.copy(upgradeProgress = progress))
                    }
                    // Efectos sobre piezas mejoradas
                    if (_animateEffects.value && step == steps / 3) {
                        animateParallel(createUpgradeHighlight(vertexId))
                    }
                    delay(stepDelay)
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

    fun animateParallel(highlights: List<HighlightAnimation>): Job {
        return viewModelScope.launch {
            _currentHighlights.value = highlights

            val maxDuration = highlights.maxOfOrNull {
                when (it) {
                    is HighlightAnimation.Vertex -> it.highlight.duration
                    is HighlightAnimation.Edge -> it.highlight.duration
                    is HighlightAnimation.Region -> it.highlight.duration
                    is HighlightAnimation.Pause -> it.duration
                }
            } ?: 0L

            delay(maxDuration)
            _currentHighlights.value = emptyList()
        }
    }

    private val _visualState = MutableStateFlow(VisualGameState())
    val visualState: StateFlow<VisualGameState> = _visualState.asStateFlow()

    private val _animatedPieces = MutableStateFlow<Map<String, AnimatedCob>>(emptyMap())
    val animatedPieces: StateFlow<Map<String, AnimatedCob>> = _animatedPieces.asStateFlow()

    private val _animateEffects = MutableStateFlow(false)
    fun updateAnimateEffects(animate: Boolean) {
        _animateEffects.value = animate
    }

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

    fun stopHighlights() {
        viewModelScope.launch {
            _currentHighlights.value = emptyList()
        }
    }

    fun reset() {
        forceSync()
        _visualState.value = VisualGameState()
        previousVisualState = null
    }

    fun forceSync() {
        clearQueue()
        stopHighlights()
        _isAnimating.value = false
        _animatedPieces.value = emptyMap()
    }

    private val animationQueue = mutableListOf<AnimationGroup>()
    private var isProcessingQueue = false
    private var currentAnimationJob: Job? = null

    private fun isDuplicateGroup(newGroup: AnimationGroup, existingGroup: AnimationGroup): Boolean {
        if (newGroup.highlights.size != existingGroup.highlights.size) return false

        return newGroup.highlights.zip(existingGroup.highlights).all { (new, existing) ->
            when (new) {
                is HighlightAnimation.Vertex if existing is HighlightAnimation.Vertex ->
                    areVertexHighlightsEqual(new.highlight, existing.highlight)

                is HighlightAnimation.Edge if existing is HighlightAnimation.Edge ->
                    areEdgeHighlightsEqual(new.highlight, existing.highlight)

                is HighlightAnimation.Region if existing is HighlightAnimation.Region ->
                    areRegionHighlightsEqual(new.highlight, existing.highlight)

                is HighlightAnimation.Pause if existing is HighlightAnimation.Pause ->
                    new.duration == existing.duration

                else -> false
            }
        }
    }

    private fun areVertexHighlightsEqual(a: VertexHighlight, b: VertexHighlight): Boolean {
        return a.vertexId == b.vertexId &&
                a.pulse == b.pulse &&
                a.duration == b.duration
    }

    private fun areEdgeHighlightsEqual(a: EdgeHighlight, b: EdgeHighlight): Boolean {
        return a.from == b.from &&
                a.to == b.to &&
                a.pulse == b.pulse &&
                a.duration == b.duration
    }

    private fun areRegionHighlightsEqual(a: RegionHighlight, b: RegionHighlight): Boolean {
        return a.region == b.region &&
                a.pulse == b.pulse &&
                a.duration == b.duration
    }

    fun animateSerie(highlights: List<HighlightAnimation>, source: String = "unknown"): Job {
        return viewModelScope.launch {
            val newGroup = AnimationGroup(
                highlights = highlights,
                source = source,
                timestamp = System.currentTimeMillis()
            )

            if (!shouldAddToQueue(newGroup)) {
                return@launch
            }

            animationQueue.add(newGroup)
            processQueue()
        }
    }

    @Suppress("unused")
    fun clearQueueAndAdd(highlights: List<HighlightAnimation>, source: String = "unknown"): Job {
        return viewModelScope.launch {
            clearQueue()
            val newGroup = AnimationGroup(
                highlights = highlights,
                source = source,
                timestamp = System.currentTimeMillis()
            )
            animationQueue.add(newGroup)
            processQueue()
        }
    }

    private fun shouldAddToQueue(newGroup: AnimationGroup): Boolean {
        // Si la cola está vacía, siempre agregar
        if (animationQueue.isEmpty()) return true

        // Verificar si el último grupo en la cola es idéntico
        val lastGroup = animationQueue.last()
        if (isDuplicateGroup(newGroup, lastGroup)) {
            println("Filtering duplicate animation group from source: ${newGroup.source}")
            return false
        }

        // Opcional: verificar contra todos los grupos en la cola
        // WARNING: Esto podría ser costoso para colas muy largas.
        val hasExactDuplicate = animationQueue.any { existingGroup ->
            isDuplicateGroup(newGroup, existingGroup)
        }

        return !hasExactDuplicate
    }

    private fun processQueue() {
        if (isProcessingQueue || animationQueue.isEmpty()) return

        isProcessingQueue = true
        currentAnimationJob = viewModelScope.launch {
            while (animationQueue.isNotEmpty()) {
                val group = animationQueue.removeAt(0)
                executeAnimationGroup(group)
            }
            isProcessingQueue = false
        }
    }

    private suspend fun executeAnimationGroup(group: AnimationGroup) {
        val jobs = group.highlights.map { highlight ->
            viewModelScope.launch {
                when (highlight) {
                    is HighlightAnimation.Vertex -> animateVertex(highlight.highlight)
                    is HighlightAnimation.Edge -> animateEdge(highlight.highlight)
                    is HighlightAnimation.Region -> animateRegion(highlight.highlight)
                    is HighlightAnimation.Pause -> delay(highlight.duration)
                }
            }
        }
        jobs.joinAll()

        val maxPostDelay = group.highlights.maxOfOrNull {
            when (it) {
                is HighlightAnimation.Vertex -> it.highlight.postDelay
                is HighlightAnimation.Edge -> it.highlight.postDelay
                is HighlightAnimation.Region -> it.highlight.postDelay
                is HighlightAnimation.Pause -> 0L
            }
        } ?: 0L

        if (maxPostDelay > 0) delay(maxPostDelay)
    }

    private suspend fun animateVertex(highlight: VertexHighlight) {
        // Delay inicial si existe
        if (highlight.startDelay > 0) {
            delay(highlight.startDelay)
        }

        // Agregar a highlights activos
        _currentHighlights.value += HighlightAnimation.Vertex(highlight)

        // Esperar la duración de la animación
        delay(highlight.duration)

        // Remover del estado (a menos que sea persistente)
        if (!highlight.persistent) {
            _currentHighlights.value -= HighlightAnimation.Vertex(highlight)
        }

        // Post-delay si existe
        if (highlight.postDelay > 0) {
            delay(highlight.postDelay)
        }
    }

    private suspend fun animateEdge(highlight: EdgeHighlight) {
        if (highlight.startDelay > 0) {
            delay(highlight.startDelay)
        }

        _currentHighlights.value += HighlightAnimation.Edge(highlight)
        delay(highlight.duration)

        if (!highlight.persistent) {
            _currentHighlights.value -= HighlightAnimation.Edge(highlight)
        }

        if (highlight.postDelay > 0) {
            delay(highlight.postDelay)
        }
    }

    private suspend fun animateRegion(highlight: RegionHighlight) {
        if (highlight.startDelay > 0) {
            delay(highlight.startDelay)
        }

        _currentHighlights.value += HighlightAnimation.Region(highlight)
        delay(highlight.duration)

        if (!highlight.persistent) {
            _currentHighlights.value -= HighlightAnimation.Region(highlight)
        }

        if (highlight.postDelay > 0) {
            delay(highlight.postDelay)
        }
    }

    fun clearQueue() {
        viewModelScope.launch {
            animationQueue.clear()
            currentAnimationJob?.cancel()
            isProcessingQueue = false
        }
    }

    fun isAnimating(): Boolean {
        return isProcessingQueue || animationQueue.isNotEmpty()
    }

    @Suppress("unused")
    fun clearSpecificHighlights(
        vertexIds: List<String> = emptyList(),
        edges: List<Pair<String, String>> = emptyList(),
        regions: List<BoardRegion> = emptyList(),
    ) {
        viewModelScope.launch {
            _currentHighlights.value = _currentHighlights.value.filterNot { highlight ->
                when (highlight) {
                    is HighlightAnimation.Vertex -> vertexIds.contains(highlight.highlight.vertexId)
                    is HighlightAnimation.Edge -> edges.any { it.first == highlight.highlight.from && it.second == highlight.highlight.to }
                    is HighlightAnimation.Region -> regions.any { it == highlight.highlight.region }
                    is HighlightAnimation.Pause -> false
                }
            }
        }
    }

    @Suppress("unused")
    fun loadTutorialSequence(sequences: List<List<HighlightAnimation>>) {
        viewModelScope.launch {
            clearQueue()
            sequences.forEach { sequence ->
                animationQueue.add(AnimationGroup(sequence))
            }
            processQueue()
        }
    }
}