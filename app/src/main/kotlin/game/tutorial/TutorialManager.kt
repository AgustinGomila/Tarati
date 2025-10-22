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
            completeTutorial()
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

    fun skipTutorial() {
        stopCurrentAnimations()
        autoAdvanceJob?.cancel()
        completeTutorial()
    }

    fun repeatCurrentStep() {
        stopCurrentAnimations()
        autoAdvanceJob?.cancel()
        showStep(_steps.value[_currentStepIndex.value])
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
            _tutorialState.value = TutorialState.WaitingForMove(currentStep, expectedMove)
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
        _tutorialState.value = when (step) {
            is InteractiveTutorialStep -> {
                TutorialState.WaitingForMove(step, step.expectedMoves)
            }

            else -> {
                if (step.interactionRequired) {
                    TutorialState.WaitingForInteraction(step)
                } else {
                    TutorialState.ShowingStep(step)
                }
            }
        }

        // Iniciar animaciones del paso
        startStepAnimations(step)

        // Configurar auto-avance si es necesario
        if (shouldAutoAdvance()) {
            startAutoAdvance()
        }
    }

    private fun startStepAnimations(step: TutorialStep) {
        if (step.animations.isNotEmpty()) {
            animationCoordinator.handleEvent(
                AnimationEvent.HighlightEvent(step.animations)
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

    private fun completeTutorial() {
        stopCurrentAnimations()
        _tutorialState.value = TutorialState.Completed
        autoAdvanceJob?.cancel()
    }

    fun reset() {
        stopCurrentAnimations()
        _tutorialState.value = TutorialState.Idle
        _currentStepIndex.value = 0
        _steps.value = emptyList()
        autoAdvanceJob?.cancel()
    }
}