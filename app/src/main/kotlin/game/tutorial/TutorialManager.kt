package com.agustin.tarati.game.tutorial


import com.agustin.tarati.game.core.GameState
import com.agustin.tarati.game.core.Move
import com.agustin.tarati.ui.components.board.animation.AnimationCoordinator
import com.agustin.tarati.ui.components.board.animation.AnimationEvent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class TutorialManager(
    private val animationCoordinator: AnimationCoordinator
) {
    private val coroutineScope = CoroutineScope(Dispatchers.Main)
    private var autoAdvanceJob: Job? = null

    private val _tutorialState = MutableStateFlow<TutorialState>(TutorialState.Idle)
    val tutorialState: StateFlow<TutorialState> = _tutorialState.asStateFlow()
    private fun updateTutorialState(state: TutorialState) {
        _tutorialState.value = state
    }

    private val _steps = MutableStateFlow<List<TutorialStep>>(emptyList())
    private val _currentStepIndex = MutableStateFlow(0)

    val progress: TutorialProgress
        get() = TutorialProgress(
            currentStepIndex = _currentStepIndex.value + 1,
            totalSteps = _steps.value.size
        )

    fun loadRulesTutorial() {
        _steps.value = listOf(
            IntroductionStep(),

            CenterStep(),
            BridgeStep(),
            CircumferenceStep(),
            DomesticBasesStep(),

            CobsStep(),

            BasicMovesStep(),
            CapturesStep(),
            UpgradeStep(),
            CastlingStep(),

            CompletedStep(),
        )
        _currentStepIndex.value = 0
        startTutorial()
    }

    fun startTutorial() {
        if (_steps.value.isEmpty()) return
        showStep(_steps.value[_currentStepIndex.value])
    }

    fun nextStep() {
        stopCurrentAnimations()
        autoAdvanceJob?.cancel()

        if (_currentStepIndex.value < _steps.value.size - 1) {
            _currentStepIndex.value++
            showStep(_steps.value[_currentStepIndex.value])
        } else {
            endTutorial()
        }
    }

    fun previousStep() {
        stopCurrentAnimations()
        autoAdvanceJob?.cancel()

        if (_currentStepIndex.value > 0) {
            _currentStepIndex.value--
            showStep(_steps.value[_currentStepIndex.value])
        }
    }

    fun repeatCurrentStep() {
        stopCurrentAnimations()
        autoAdvanceJob?.cancel()

        // Limpiar la cola completamente antes de repetir
        animationCoordinator.handleEvent(AnimationEvent.ClearQueue)

        // Pequeño delay para asegurar que la cola se limpió
        coroutineScope.launch {
            delay(50)
            showStep(_steps.value[_currentStepIndex.value])
        }
    }

    fun onUserMove(move: Move): Boolean {
        return when (val currentState = _tutorialState.value) {
            is TutorialState.WaitingForMove -> {
                val step = currentState.step as? InteractiveTutorialStep
                step != null && step.isExpectedMove(move)
            }

            else -> false
        }
    }

    fun getExpectedMoves(): List<Move> {
        val currentState = _tutorialState.value as? TutorialState.WaitingForMove ?: return listOf()
        val step = currentState.step as? InteractiveTutorialStep ?: return listOf()
        return step.expectedMoves
    }

    fun requestUserInteraction(expectedMove: List<Move> = listOf()) {
        val currentStep = getCurrentStep()
        if (currentStep != null) {
            updateTutorialState(TutorialState.WaitingForMove(currentStep, expectedMove))
        }
    }

    fun getCurrentStep(): TutorialStep? {
        return _steps.value.getOrNull(_currentStepIndex.value)
    }

    fun getCurrentGameState(): GameState? {
        return getCurrentStep()?.gameState
    }

    fun shouldAutoAdvance(): Boolean {
        val currentStep = getCurrentStep()
        return currentStep?.autoAdvanceDelay != null &&
                currentStep !is InteractiveTutorialStep
    }

    fun getCurrentStepDuration(): Long {
        return getCurrentStep()?.autoAdvanceDelay ?: 0L
    }

    fun isWaitingForUserInteraction(): Boolean {
        return _tutorialState.value is TutorialState.WaitingForMove
    }

    private fun showStep(step: TutorialStep) {
        // Actualizar estado del juego primero
        step.onStepStart?.invoke()

        // Determinar el estado basado en el tipo de paso
        updateTutorialState(
            when (step) {
                is InteractiveTutorialStep -> TutorialState.WaitingForMove(step, step.expectedMoves)
                else -> TutorialState.ShowingStep(step)
            }
        )

        // Iniciar animaciones del paso
        startStepAnimations(step)

        // Configurar auto-avance si es necesario
        if (shouldAutoAdvance()) {
            startAutoAdvance()
        }
    }

    private fun startStepAnimations(step: TutorialStep) {
        if (step.animations.isNotEmpty()) {
            // Usar el nombre de la clase del paso como source para mejor tracking
            val source = step::class.java.simpleName
            animationCoordinator.handleEvent(
                AnimationEvent.HighlightEvent(step.animations, source)
            )
        }
    }


    private fun stopCurrentAnimations() {
        animationCoordinator.handleEvent(AnimationEvent.StopHighlights)
    }

    private fun startAutoAdvance() {
        val delayTime = getCurrentStepDuration()
        autoAdvanceJob = coroutineScope.launch {
            delay(delayTime)
            nextStep()
        }
    }

    fun endTutorial() {
        stopCurrentAnimations()
        autoAdvanceJob?.cancel()
        updateTutorialState(TutorialState.Completed)
    }

    fun closeTutorial() {
        stopCurrentAnimations()
        autoAdvanceJob?.cancel()
        updateTutorialState(TutorialState.Idle)
    }

    fun reset() {
        closeTutorial()
        _currentStepIndex.value = 0
        _steps.value = emptyList()
    }
}