package com.agustin.tarati.ui.screens.main

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DrawerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.unit.dp
import com.agustin.tarati.R
import com.agustin.tarati.game.ai.EvaluationConfig
import com.agustin.tarati.game.ai.TaratiAI.setEvaluationConfig
import com.agustin.tarati.game.core.Color
import com.agustin.tarati.game.core.GameState
import com.agustin.tarati.game.core.GameStatus
import com.agustin.tarati.game.core.Move
import com.agustin.tarati.game.logic.BoardOrientation
import com.agustin.tarati.game.logic.isGameOver
import com.agustin.tarati.game.logic.isPortrait
import com.agustin.tarati.game.logic.toBoardOrientation
import com.agustin.tarati.game.tutorial.TutorialState
import com.agustin.tarati.ui.components.board.BoardEvents
import com.agustin.tarati.ui.components.board.animation.BoardAnimationViewModel
import com.agustin.tarati.ui.components.turnIndicator.IndicatorEvents
import com.agustin.tarati.ui.components.turnIndicator.TurnIndicator
import com.agustin.tarati.ui.components.turnIndicator.TurnIndicatorState
import com.agustin.tarati.ui.components.tutorial.TutorialViewModel
import com.agustin.tarati.ui.localization.LocalizedText
import com.agustin.tarati.ui.screens.settings.BoardVisualState
import com.agustin.tarati.ui.theme.getBoardColors
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun MainScreenEffects(
    drawerState: DrawerState,
    isLandscape: Boolean,

    playerSide: Color,
    gameState: GameState,
    gameStatus: GameStatus,

    evalConfig: EvaluationConfig,
    aiEnabled: Boolean,
    isEditing: Boolean,
    animateEffects: Boolean,

    isTutorialActive: Boolean,
    tutorialState: TutorialState,

    aiThinkingDependencies: List<Any?>,
    calculateAIMove: (
        gameState: GameState,
        onMoveFound: (String, String) -> Unit,
    ) -> Unit,

    animationViewModel: BoardAnimationViewModel,

    onBoardOrientationChanged: (BoardOrientation) -> Unit,
    onAIMove: (String, String) -> Unit,
    onTutorialEnd: () -> Unit,
    onGameOver: () -> Unit,

    debug: Boolean
) {
    LaunchedEffect(tutorialState) {
        val tutorialState = tutorialState
        if (tutorialState == TutorialState.Completed) {
            onTutorialEnd()
        }
    }

    // Efecto para orientación del tablero
    LaunchedEffect(isLandscape, playerSide) {
        val isLandscape = isLandscape
        val playerSide = playerSide
        onBoardOrientationChanged(toBoardOrientation(isLandscape, playerSide))
    }

    // Efecto para animaciones
    LaunchedEffect(animateEffects) {
        val animateEffects = animateEffects
        animationViewModel.updateAnimateEffects(animateEffects)
    }

    // Efecto para dificultad
    LaunchedEffect(evalConfig) {
        val evalConfig = evalConfig
        setEvaluationConfig(evalConfig)
    }

    // Efecto para estado del juego
    LaunchedEffect(gameStatus, isEditing, isTutorialActive) {
        val isEditing = isEditing
        val isTutorialActive = isTutorialActive

        if (isTutorialActive || isEditing) return@LaunchedEffect

        val gameStatus = gameStatus
        if (gameStatus == GameStatus.GAME_OVER) {
            delay(1500)
            onGameOver()
        }
    }

    // Efecto para ejecutar pensamiento de IA
    LaunchedEffect(aiThinkingDependencies) {
        val gameState = gameState
        val gameStatus = gameStatus
        val playerSide = playerSide
        val aiEnabled = aiEnabled
        val isEditing = isEditing
        val onAIMove = onAIMove
        val debug = debug

        if (!aiEnabled || isEditing || gameStatus != GameStatus.PLAYING) {
            if (debug) println("DEBUG: AI blocked - gameStatus: $gameStatus, aiEnabled: $aiEnabled")
            return@LaunchedEffect
        }

        val shouldAIPlay = gameState.currentTurn != playerSide && !gameState.isGameOver()

        if (!shouldAIPlay) return@LaunchedEffect

        calculateAIMove(gameState, onAIMove)
    }

    // Efecto para drawer y tutorial
    LaunchedEffect(drawerState.currentValue) {
        if (drawerState.isOpen && isTutorialActive) {
            onTutorialEnd()
        }
    }
}

@ExperimentalMaterial3Api
@Composable
fun MainContent(
    scope: CoroutineScope,
    drawerState: DrawerState,
    boardSize: Size,
    boardOrientation: BoardOrientation,
    editBoardOrientation: BoardOrientation,
    gameState: GameState,
    turnState: TurnIndicatorState,
    distributionState: DistributionState,
    lastMove: Move?,
    isTutorialActive: Boolean,
    isAIThinking: Boolean,
    boardVisualState: BoardVisualState,
    pieceCounts: PieceCounts,
    onEditPiece: (String) -> Unit,
    onPieceMove: (String, String) -> Unit,
    events: MainScreenEvents,
    viewModel: MainViewModel,
    animationViewModel: BoardAnimationViewModel,
    tutorialViewModel: TutorialViewModel
) {
    // Crear eventos de edición
    val editEvents = remember(viewModel) { EditEvents(viewModel = viewModel) }

    // Obtener estados actualizados
    val editColor by viewModel.editColor.collectAsState()
    val editTurn by viewModel.editTurn.collectAsState()
    val playerSide by viewModel.playerSide.collectAsState()
    val isEditing by viewModel.isEditing.collectAsState()
    val aIEnabled by viewModel.aIEnabled.collectAsState()

    LaunchedEffect(isEditing) {
        drawerState.closeIfOpen(scope)
    }

    Scaffold(
        topBar = {
            TaratiTopBar(
                scope = scope,
                drawerState = drawerState,
                isEditing = isEditing
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                if (boardOrientation.isPortrait() && !isEditing) {
                    LocalizedText(
                        id = R.string.a_board_game_by_george_spencer_brown,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                }

                CreateBoard(
                    modifier = Modifier.weight(1f),
                    state = CreateBoardState(
                        gameState = gameState,
                        lastMove = lastMove,
                        aiEnabled = aIEnabled,
                        playerSide = playerSide,
                        isEditing = isEditing,
                        isTutorialActive = isTutorialActive,
                        isAIThinking = isAIThinking,
                        boardOrientation = boardOrientation,
                        editBoardOrientation = editBoardOrientation,
                        boardVisualState = boardVisualState,
                    ),
                    events = object : BoardEvents {
                        override fun onMove(from: String, to: String) = onPieceMove(from, to)
                        override fun onEditPiece(from: String) = onEditPiece(from)
                        override fun onResetCompleted() = Unit
                    },
                    boardAnimationViewModel = animationViewModel,
                    boardVisualState = boardVisualState,
                    boardColors = getBoardColors(),
                    tutorial = {
                        if (isTutorialActive) {
                            CreateTutorialOverlay(
                                viewModel = tutorialViewModel,
                                boardSize = boardSize,
                                boardOrientation = editBoardOrientation,
                                tutorialEvents = TutorialEvents(
                                    onSkipTutorial = events::endTutorial,
                                    onFinishTutorial = events::resetTutorial
                                ),
                                updateGameState = viewModel.gameManager::updateGameState,
                            )
                        }
                    },
                    content = {
                        EditControls(
                            isLandscapeScreen = !boardOrientation.isPortrait(),
                            EditColorState(
                                playerSide = playerSide,
                                editTurn = editTurn,
                                editColor = editColor,
                            ),
                            EditActionState(
                                pieceCounts = pieceCounts,
                                isValidDistribution = distributionState.isValid,
                                isCompletedDistribution = distributionState.isCompleted,
                            ),
                            editEvents = editEvents
                        )
                    },
                    turnIndicator = {
                        TurnIndicator(
                            modifier = it,
                            currentTurn = gameState.currentTurn,
                            state = turnState,
                            boardColors = getBoardColors(),
                            indicatorEvents = object : IndicatorEvents {
                                override fun onTouch() {
                                    events.showNewGameDialog(playerSide)
                                }
                            },
                        )
                    },
                    debug = viewModel.isDebug
                )
            }
        }
    }
}

fun DrawerState.closeIfOpen(scope: CoroutineScope) {
    scope.launch {
        if (isOpen) close()
    }
}