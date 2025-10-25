@file:Suppress("AssignedValueIsNeverRead")

package com.agustin.tarati.ui.screens.main

import android.annotation.SuppressLint
import android.content.res.Configuration
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.agustin.tarati.game.ai.AIThinkingViewModel
import com.agustin.tarati.game.ai.EvaluationConfig
import com.agustin.tarati.game.core.CobColor.BLACK
import com.agustin.tarati.game.core.CobColor.WHITE
import com.agustin.tarati.game.core.GameStatus
import com.agustin.tarati.game.core.Move
import com.agustin.tarati.game.core.initialGameState
import com.agustin.tarati.game.logic.BoardOrientation
import com.agustin.tarati.game.logic.isInitialState
import com.agustin.tarati.game.tutorial.TutorialManager
import com.agustin.tarati.game.tutorial.TutorialState
import com.agustin.tarati.ui.components.board.animation.AnimationCoordinator
import com.agustin.tarati.ui.components.board.animation.BoardAnimationViewModel
import com.agustin.tarati.ui.components.board.helpers.HighlightService
import com.agustin.tarati.ui.components.editor.DistributionState
import com.agustin.tarati.ui.components.editor.PieceCounts
import com.agustin.tarati.ui.components.sidebar.SidebarContent
import com.agustin.tarati.ui.components.turnIndicator.TurnIndicatorState
import com.agustin.tarati.ui.components.tutorial.TutorialViewModel
import com.agustin.tarati.ui.screens.settings.SettingsViewModel
import kotlinx.coroutines.launch

@SuppressLint("UnrememberedMutableState")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    navController: NavController,
    viewModel: MainViewModel = viewModel(),
    settingsViewModel: SettingsViewModel = viewModel(),
) {
    // Estados y ViewModels
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    LocalContext.current
    val configuration = LocalConfiguration.current

    val aiThinkingViewModel: AIThinkingViewModel = viewModel()

    val animationViewModel: BoardAnimationViewModel = viewModel()
    val isAnimating by animationViewModel.isAnimating.collectAsState()
    val boardSize by animationViewModel.boardSize.collectAsState()

    val animationCoordinator = remember { AnimationCoordinator(animationViewModel) }
    val highlightService = remember { HighlightService(animationViewModel) }

    val tutorialManager = remember { TutorialManager(animationCoordinator) }
    val tutorialViewModel by lazy { TutorialViewModel(tutorialManager) }

    // Estados del ViewModel
    val isEditing by viewModel.isEditing.collectAsState()
    val editBoardOrientation by viewModel.editBoardOrientation.collectAsState()
    val gameStatus by viewModel.gameManager.gameStatus.collectAsState()
    val gameState by viewModel.gameManager.gameState.collectAsState(initialGameState())
    val history by viewModel.gameManager.history.collectAsState(emptyList())
    val moveIndex by viewModel.gameManager.moveIndex.collectAsState(-1)
    val aiEnabled by viewModel.aIEnabled.collectAsState(true)
    val playerSide by viewModel.playerSide.collectAsState(WHITE)

    // Estados locales para diálogos
    var showNewGameDialog by remember { mutableStateOf(false) }
    var attemptNewGameColor by remember { mutableStateOf(WHITE) }
    var showAboutDialog by remember { mutableStateOf(false) }
    var showGameOverDialog by remember { mutableStateOf(false) }

    // Configuración
    val settingsState by settingsViewModel.settingsState.collectAsState()
    val evalConfig = EvaluationConfig.getByDifficulty(settingsState.difficulty)
    val boardVisualState = settingsState.boardVisualState

    // Estados locales
    var lastMove by remember { mutableStateOf<Move?>(null) }
    val isAIThinking by aiThinkingViewModel.isAIThinking.collectAsState(false)

    // Estados derivados
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
    var boardOrientation by remember { mutableStateOf(BoardOrientation.PORTRAIT_WHITE) }

    val tutorialState by tutorialViewModel.tutorialState.collectAsState()
    val isTutorialActive = remember {
        derivedStateOf {
            tutorialState != TutorialState.Idle && tutorialState != TutorialState.Completed
        }
    }

    val turnState: TurnIndicatorState by derivedStateOf {
        val state = viewModel.gameManager.gameState.value
        when {
            isAIThinking -> TurnIndicatorState.AI_THINKING
            gameStatus != GameStatus.PLAYING && !state.isInitialState(playerSide) -> TurnIndicatorState.NEUTRAL
            else -> TurnIndicatorState.HUMAN_TURN
        }
    }

    val pieceCounts = remember(gameState) {
        val white = gameState.cobs.values.count { it.color == WHITE }
        val black = gameState.cobs.values.count { it.color == BLACK }
        PieceCounts(white, black)
    }

    val distributionState = remember(pieceCounts) {
        DistributionState.fromPieceCounts(pieceCounts)
    }

    // Eventos con callbacks para diálogos
    val events = remember {
        MainScreenEvents(
            scope = scope,
            drawerState = drawerState,
            animationCoordinator = animationCoordinator,
            highlightService = highlightService,
            viewModel = viewModel,
            tutorialViewModel = tutorialViewModel,
            onShowNewGameDialog = {
                attemptNewGameColor = it
                showNewGameDialog = true
            },
        ) { showAboutDialog = true }
    }

    // Efectos
    MainScreenEffects(
        drawerState = drawerState,
        isLandscape = isLandscape,

        gameState = gameState,
        playerSide = playerSide,
        gameStatus = gameStatus,
        onGameOver = { showGameOverDialog = true },

        animateEffects = boardVisualState.animateEffects,
        animationViewModel = animationViewModel,

        aiEnabled = aiEnabled,
        evalConfig = evalConfig,
        aiThinkingDependencies = listOf(gameStatus, gameState.currentTurn, aiEnabled, playerSide, isEditing),
        calculateAIMove = { gameState, move ->
            scope.launch {
                aiThinkingViewModel.calculateAIMove(gameState, move, debug = true)
            }
        },
        onAIMove = { from, to ->
            checkAndApplyMove(events, from, to, viewModel)
        },

        isEditing = isEditing,
        onBoardOrientationChanged = { boardOrientation = it },

        isTutorialActive = isTutorialActive.value,
        tutorialState = tutorialState,
        onTutorialEnd = events::resetTutorial,

        debug = viewModel.isDebug
    )

    // Diálogos
    MainScreenDialogs(
        gameState = gameState,
        isAnimating = isAnimating,
        showGameOverDialog = showGameOverDialog,
        onGameOverConfirmed = {
            showGameOverDialog = false
            events.startNewGame(playerSide)
        },
        onGameOverDismissed = {
            showGameOverDialog = false
            events.stopGame()
        },
        showNewGameDialog = showNewGameDialog,
        attemptNewGameCobColor = attemptNewGameColor,
        onNewGameConfirmed = {
            showNewGameDialog = false
            events.startNewGame(it)
        },
        onNewGameDismissed = {
            showNewGameDialog = false
            events.stopGame()
        },
        onShowTutorial = {
            showAboutDialog = false
            events.startTutorial()
        },
        showAboutDialog = showAboutDialog
    ) {
        showAboutDialog = false
    }

    // UI Principal
    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            SidebarContent(
                navController = navController,
                gameState = gameState,
                playerSide = playerSide,
                moveIndex = moveIndex,
                history = history,
                evalConfig = evalConfig,
                aiEnabled = aiEnabled,
                events = events,
                viewModel = viewModel,
                settingsViewModel = settingsViewModel
            )
        }
    ) {
        MainContent(
            scope = scope,
            drawerState = drawerState,
            boardSize = boardSize,
            boardOrientation = boardOrientation,
            editBoardOrientation = editBoardOrientation,
            gameState = gameState,
            turnState = turnState,
            distributionState = distributionState,
            lastMove = lastMove,
            isTutorialActive = isTutorialActive.value,
            isAIThinking = isAIThinking,
            pieceCounts = pieceCounts,
            onEditPiece = viewModel::editPiece,
            onPieceMove = { from, to ->
                when {
                    isTutorialActive.value -> handleTutorialMove(from, to, tutorialViewModel)
                    else -> {
                        checkAndApplyMove(
                            events = events,
                            from = from,
                            to = to,
                            viewModel = viewModel
                        )
                    }
                }
            },
            events = events,
            viewModel = viewModel,
            boardVisualState = boardVisualState,
            animationViewModel = animationViewModel,
            tutorialViewModel = tutorialViewModel
        )
    }
}

fun checkAndApplyMove(
    events: MainScreenEvents,
    from: String,
    to: String,
    viewModel: MainViewModel
) {
    // Obtener estados
    val gameStatus = viewModel.gameManager.gameStatus.value
    val gameState = viewModel.gameManager.gameState.value

    if (gameStatus != GameStatus.PLAYING && gameState.isInitialState(viewModel.playerSide.value)) {
        viewModel.gameManager.updateGameStatus(GameStatus.PLAYING)
    }

    events.applyMove(from, to, gameState)
}

// Función auxiliar para movimientos del tutorial
private fun handleTutorialMove(
    from: String,
    to: String,
    tutorialViewModel: TutorialViewModel
) {
    tutorialViewModel.onMoveAttempted(
        from = from,
        to = to,
        onMoveAccepted = { tutorialViewModel.nextStep() },
        onMoveRejected = { tutorialViewModel.requestUserInteraction(it) }
    )
}