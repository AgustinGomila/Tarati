@file:Suppress("AssignedValueIsNeverRead")

package com.agustin.tarati.ui.screens.main

import android.annotation.SuppressLint
import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.RotateRight
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DrawerState
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.BottomCenter
import androidx.compose.ui.Alignment.Companion.CenterEnd
import androidx.compose.ui.Alignment.Companion.CenterStart
import androidx.compose.ui.Alignment.Companion.TopCenter
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.agustin.tarati.R
import com.agustin.tarati.game.ai.AIThinkingViewModel
import com.agustin.tarati.game.ai.Difficulty
import com.agustin.tarati.game.ai.EvaluationConfig
import com.agustin.tarati.game.core.Color
import com.agustin.tarati.game.core.Color.BLACK
import com.agustin.tarati.game.core.Color.WHITE
import com.agustin.tarati.game.core.GameState
import com.agustin.tarati.game.core.GameStatus
import com.agustin.tarati.game.core.Move
import com.agustin.tarati.game.core.initialGameState
import com.agustin.tarati.game.core.opponent
import com.agustin.tarati.game.logic.BoardOrientation
import com.agustin.tarati.game.logic.isInitialState
import com.agustin.tarati.game.logic.toBoardOrientation
import com.agustin.tarati.game.tutorial.TutorialManager
import com.agustin.tarati.game.tutorial.TutorialState
import com.agustin.tarati.ui.components.board.Board
import com.agustin.tarati.ui.components.board.BoardEvents
import com.agustin.tarati.ui.components.board.BoardState
import com.agustin.tarati.ui.components.board.animation.AnimationCoordinator
import com.agustin.tarati.ui.components.board.animation.BoardAnimationViewModel
import com.agustin.tarati.ui.components.board.helpers.HighlightService
import com.agustin.tarati.ui.components.sidebar.Sidebar
import com.agustin.tarati.ui.components.sidebar.SidebarEvents
import com.agustin.tarati.ui.components.sidebar.SidebarGameState
import com.agustin.tarati.ui.components.sidebar.SidebarUIState
import com.agustin.tarati.ui.components.turnIndicator.IndicatorEvents
import com.agustin.tarati.ui.components.turnIndicator.TurnIndicator
import com.agustin.tarati.ui.components.turnIndicator.TurnIndicatorState
import com.agustin.tarati.ui.components.tutorial.TutorialOverlay
import com.agustin.tarati.ui.components.tutorial.TutorialViewModel
import com.agustin.tarati.ui.helpers.PreviewConfig
import com.agustin.tarati.ui.localization.LocalizedText
import com.agustin.tarati.ui.localization.localizedString
import com.agustin.tarati.ui.navigation.ScreenDestinations.SettingsScreenDest
import com.agustin.tarati.ui.screens.settings.SettingsViewModel
import com.agustin.tarati.ui.theme.BoardColors
import com.agustin.tarati.ui.theme.TaratiTheme
import com.agustin.tarati.ui.theme.getBoardColors
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@SuppressLint("UnrememberedMutableState")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    navController: NavController,
    viewModel: MainViewModel = viewModel(),
    settingsViewModel: SettingsViewModel = viewModel()
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
    var showAboutDialog by remember { mutableStateOf(false) }
    var showGameOverDialog by remember { mutableStateOf(false) }

    // Configuración
    val settingsState by settingsViewModel.settingsState.collectAsState()
    val evalConfig = EvaluationConfig.getByDifficulty(settingsState.difficulty)
    val labelsVisibles = settingsState.boardState.labelsVisibles
    val edgesVisibles = settingsState.boardState.edgesVisibles
    val verticesVisibles = settingsState.boardState.verticesVisibles
    val animateEffects = settingsState.boardState.animateEffects

    // Estados locales
    var lastMove by remember { mutableStateOf<Move?>(null) }
    var isAIThinking by remember { mutableStateOf(false) }

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
            gameStatus != GameStatus.PLAYING && !state.isInitialState(playerSide) -> TurnIndicatorState.NEUTRAL
            isAIThinking -> TurnIndicatorState.AI_THINKING
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
            onShowNewGameDialog = { showNewGameDialog = true },
        ) { showAboutDialog = true }
    }

    // Efectos
    MainScreenEffects(
        drawerState = drawerState,
        isLandscape = isLandscape,

        playerSide = playerSide,
        gameState = gameState,
        gameStatus = gameStatus,

        evalConfig = evalConfig,
        aiEnabled = aiEnabled,
        isEditing = isEditing,
        animateEffects = animateEffects,

        isTutorialActive = isTutorialActive.value,
        tutorialState = tutorialState,

        aiThinkingDependencies = listOf(gameStatus, gameState.currentTurn, aiEnabled, playerSide, isEditing),

        aiThinkingViewModel = aiThinkingViewModel,
        animationViewModel = animationViewModel,

        onBoardOrientationChanged = { boardOrientation = it },
        onAIThinkingChanged = { isAIThinking = it },
        onAIMove = { from, to -> checkAndApplyMove(events, from, to, viewModel) },
        onTutorialEnd = events::endTutorial,
        onGameOver = { showGameOverDialog = true },

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
        onNewGameConfirmed = {
            showNewGameDialog = false
            events.startNewGame(playerSide)
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
            labelsVisibles = labelsVisibles,
            edgesVisibles = edgesVisibles,
            verticesVisibles = verticesVisibles,
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

@Composable
fun SidebarContent(
    navController: NavController,
    gameState: GameState,
    playerSide: Color,
    moveIndex: Int,
    history: List<Pair<Move, GameState>>,
    evalConfig: EvaluationConfig,
    aiEnabled: Boolean,
    events: MainScreenEvents,
    viewModel: MainViewModel,
    settingsViewModel: SettingsViewModel
) {
    var sidebarUIState by remember { mutableStateOf(SidebarUIState()) }

    val sidebarEvents = object : SidebarEvents {
        override fun onMoveToCurrent() = viewModel.gameManager.moveToCurrentState()
        override fun onUndo() = viewModel.gameManager.undoMove()
        override fun onRedo() = viewModel.gameManager.redoMove()
        override fun onDifficultyChange(difficulty: Difficulty) = settingsViewModel.setDifficulty(difficulty)
        override fun onToggleAI() = viewModel.updateAIEnabled(!aiEnabled)
        override fun onSettings() = navController.navigate(SettingsScreenDest.route)
        override fun onNewGame(color: Color) = events.showNewGameDialog(color)
        override fun onEditBoard() = viewModel.toggleEditing()
        override fun onAboutClick() = events.showAboutDialog()
    }

    val sidebarGameState = SidebarGameState(
        gameState = gameState,
        playerSide = playerSide,
        moveIndex = moveIndex,
        moveHistory = history.map { it.first },
        difficulty = evalConfig.difficulty,
        isAIEnabled = aiEnabled,
    )

    Sidebar(
        modifier = Modifier.systemBarsPadding(),
        sidebarState = sidebarGameState,
        uiState = sidebarUIState,
        events = sidebarEvents,
        onUIStateChange = { sidebarUIState = it }
    )
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

data class TutorialEvents(
    val onCompleted: () -> Unit = { },
    val onFinishTutorial: () -> Unit = { },
    val onSkipTutorial: () -> Unit = { },
)

@Composable
fun EditControls(
    isLandscapeScreen: Boolean,
    colorState: EditColorState,
    actionState: EditActionState,
    editEvents: EditEvents,
) {
    val editColorEvents = EditColorEvents(
        onPlayerSideToggle = editEvents::togglePlayerSide,
        onColorToggle = editEvents::toggleEditColor,
        onTurnToggle = editEvents::toggleEditTurn
    )
    val editActionEvents = EditActionEvents(
        onRotate = editEvents::rotateEditBoard,
        onStartGame = editEvents::startGameFromEditedState,
        onClearBoard = editEvents::clearEditBoard
    )

    if (isLandscapeScreen) {
        Box(modifier = Modifier.fillMaxSize()) {
            LeftControls(
                modifier = Modifier.align(CenterStart),
                state = colorState,
                events = editColorEvents
            )

            RightControls(
                modifier = Modifier.align(CenterEnd),
                state = actionState,
                events = editActionEvents
            )
        }
    } else {
        Box(modifier = Modifier.fillMaxSize()) {
            TopControls(
                modifier = Modifier.align(TopCenter),
                state = colorState,
                events = editColorEvents
            )

            BottomControls(
                modifier = Modifier.align(BottomCenter),
                state = actionState,
                events = editActionEvents
            )
        }
    }
}

@Composable
fun CreateTutorialOverlay(
    viewModel: TutorialViewModel,
    boardSize: Size,
    boardOrientation: BoardOrientation,
    tutorialEvents: TutorialEvents,
    updateGameState: (GameState) -> Unit,
) {
    if (boardSize == Size.Zero) return

    TutorialOverlay(
        viewModel = viewModel,
        tutorialEvents = tutorialEvents,
        updateGameState = updateGameState,
        boardWidth = boardSize.width,
        boardHeight = boardSize.height,
        boardOrientation = boardOrientation,
    )
}

@ExperimentalMaterial3Api
@Composable
fun TaratiTopBar(scope: CoroutineScope, drawerState: DrawerState, isEditing: Boolean) {
    TopAppBar(
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = localizedString(R.string.tarati),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.SemiBold
                )
                if (isEditing) {
                    Spacer(modifier = Modifier.width(8.dp))
                    LocalizedText(
                        id = (R.string.editing),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        },
        navigationIcon = {
            IconButton(onClick = { drawerState.turnState(scope) }) {
                Icon(
                    imageVector = Icons.Default.Menu,
                    contentDescription = localizedString(R.string.menu)
                )
            }
        }
    )
}

private fun DrawerState.turnState(scope: CoroutineScope) {
    scope.launch {
        if (isClosed) open()
        else close()
    }
}

data class CreateBoardState(
    val gameState: GameState,
    val lastMove: Move?,
    val playerSide: Color,
    val aiEnabled: Boolean,
    val isEditing: Boolean,
    val isTutorialActive: Boolean,
    val isAIThinking: Boolean,
    val boardOrientation: BoardOrientation,
    val editBoardOrientation: BoardOrientation,
    val labelsVisible: Boolean,
    val verticesVisible: Boolean,
    val edgesVisible: Boolean,
)

@Composable
fun CreateBoard(
    modifier: Modifier = Modifier,
    state: CreateBoardState,
    events: BoardEvents,
    boardAnimationViewModel: BoardAnimationViewModel = viewModel(),
    boardColors: BoardColors,
    tutorial: @Composable () -> Unit,
    content: @Composable () -> Unit,
    turnIndicator: @Composable (modifier: Modifier) -> Unit,
    debug: Boolean = false,
) {
    // Construir el estado para Board
    val boardState = BoardState(
        gameState = state.gameState,
        aiEnabled = state.aiEnabled,
        lastMove = state.lastMove,
        boardOrientation = when {
            state.isEditing -> state.editBoardOrientation
            else -> state.boardOrientation
        },
        labelsVisible = state.labelsVisible,
        verticesVisible = state.verticesVisible,
        edgesVisible = state.edgesVisible,
        isEditing = state.isEditing
    )

    Box(
        modifier
            .fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        Board(
            modifier = Modifier.fillMaxSize(),
            playerSide = state.playerSide,
            boardState = boardState,
            boardColors = boardColors,
            events = boardEvents(state, events),
            animationViewModel = boardAnimationViewModel,
            debug = debug,
        )

        when {
            state.isEditing -> content()

            state.isTutorialActive -> tutorial()

            else -> turnIndicator(Modifier.align(Alignment.TopEnd))
        }
    }
}

fun boardEvents(state: CreateBoardState, events: BoardEvents): BoardEvents = object : BoardEvents {
    override fun onMove(from: String, to: String) {
        if (!state.isEditing) {
            events.onMove(from, to)
        }
    }

    override fun onEditPiece(from: String) = events.onEditPiece(from)
    override fun onResetCompleted() = events.onResetCompleted()
}

@Composable
fun LeftControls(
    modifier: Modifier = Modifier,
    state: EditColorState = EditColorState(),
    events: EditColorEvents = EditColorEvents()
) {
    Column(
        modifier = modifier.padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        ColorToggleButton(
            currentColor = state.editColor,
            onColorToggle = events.onColorToggle
        )
        Spacer(modifier = Modifier.height(16.dp))
        PlayerSideToggleButton(
            playerSide = state.playerSide,
            onPlayerSideToggle = events.onPlayerSideToggle
        )
        Spacer(modifier = Modifier.height(16.dp))
        TurnToggleButton(
            currentTurn = state.editTurn,
            onTurnToggle = events.onTurnToggle
        )
    }
}

@Composable
fun TopControls(
    modifier: Modifier = Modifier,
    state: EditColorState = EditColorState(),
    events: EditColorEvents = EditColorEvents()
) {
    Row(
        horizontalArrangement = Arrangement.SpaceEvenly,
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        ColorToggleButton(
            currentColor = state.editColor,
            onColorToggle = events.onColorToggle
        )
        PlayerSideToggleButton(
            playerSide = state.playerSide,
            onPlayerSideToggle = events.onPlayerSideToggle
        )
        TurnToggleButton(
            currentTurn = state.editTurn,
            onTurnToggle = events.onTurnToggle
        )
    }
}

@Composable
fun RightControls(
    modifier: Modifier = Modifier,
    state: EditActionState = EditActionState(),
    events: EditActionEvents = EditActionEvents()
) {
    Column(
        modifier = modifier.padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        RotateBoardButton(onClick = events.onRotate)
        Spacer(modifier = Modifier.height(16.dp))
        ClearBoardButton(onClick = events.onClearBoard)
        Spacer(modifier = Modifier.height(16.dp))
        PieceCounter(
            whiteCount = state.pieceCounts.white,
            blackCount = state.pieceCounts.black,
            isValid = state.isValidDistribution
        )
        Spacer(modifier = Modifier.height(16.dp))
        StartGameButton(
            isCompletedDistribution = state.isCompletedDistribution,
            onClick = events.onStartGame,
        )
    }
}

@Composable
fun BottomControls(
    modifier: Modifier = Modifier,
    state: EditActionState = EditActionState(),
    events: EditActionEvents = EditActionEvents()
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        RotateButton(onRotate = events.onRotate)
        StartButtonAndPieceCounter(
            pieceCounts = state.pieceCounts,
            isValidDistribution = state.isValidDistribution,
            isCompletedDistribution = state.isCompletedDistribution,
            onClick = events.onStartGame
        )
        ClearBoardButton(onClick = events.onClearBoard)
    }
}

@Composable
fun RotateBoardButton(onClick: () -> Unit) {
    FloatingActionButton(
        onClick = onClick,
        containerColor = MaterialTheme.colorScheme.primaryContainer,
        modifier = Modifier.size(40.dp)
    ) {
        Icon(
            Icons.AutoMirrored.Filled.RotateRight,
            contentDescription = localizedString(R.string.rotate_board)
        )
    }
}

@Composable
fun backgroundColor(currentColor: Color): androidx.compose.ui.graphics.Color {
    return when (currentColor) {
        WHITE -> MaterialTheme.colorScheme.surface
        BLACK -> MaterialTheme.colorScheme.onSurface
    }
}

@Composable
fun foregroundColor(currentColor: Color): androidx.compose.ui.graphics.Color {
    return when (currentColor) {
        WHITE -> MaterialTheme.colorScheme.onSurface
        BLACK -> MaterialTheme.colorScheme.surface
    }
}

@Composable
fun TurnToggleButton(currentTurn: Color, onTurnToggle: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(8.dp)
    ) {
        FloatingActionButton(
            onClick = onTurnToggle,
            containerColor = MaterialTheme.colorScheme.secondaryContainer,
            modifier = Modifier.size(40.dp)
        ) {
            val backgroundColor = backgroundColor(currentTurn)
            val contentColor = foregroundColor(currentTurn)

            Box(
                modifier = Modifier
                    .size(24.dp)
                    .background(backgroundColor, CircleShape)
                    .border(1.dp, MaterialTheme.colorScheme.outline, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (currentTurn == WHITE)
                        localizedString(R.string.w)
                    else localizedString(R.string.b),
                    style = MaterialTheme.typography.labelSmall,
                    color = contentColor
                )
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        LocalizedText(
            R.string.turn,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun PlayerSideToggleButton(playerSide: Color, onPlayerSideToggle: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(8.dp)
    ) {
        FloatingActionButton(
            onClick = onPlayerSideToggle,
            containerColor = MaterialTheme.colorScheme.tertiaryContainer,
            modifier = Modifier.size(40.dp)
        ) {
            val backgroundColor = backgroundColor(playerSide)
            val contentColor = foregroundColor(playerSide)

            Box(
                modifier = Modifier
                    .size(24.dp)
                    .background(backgroundColor, CircleShape)
                    .border(1.dp, MaterialTheme.colorScheme.outline, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (playerSide == WHITE)
                        localizedString(R.string.w)
                    else localizedString(R.string.b),
                    style = MaterialTheme.typography.labelSmall,
                    color = contentColor
                )
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            localizedString(R.string.side),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun StartGameButton(isCompletedDistribution: Boolean, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        enabled = isCompletedDistribution,
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isCompletedDistribution) MaterialTheme.colorScheme.primary
            else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
        )
    ) {
        LocalizedText(R.string.start_game)
        Spacer(modifier = Modifier.width(8.dp))
        Icon(Icons.Default.PlayArrow, contentDescription = localizedString(R.string.start))
    }
}

@Composable
fun ClearBoardButton(onClick: () -> Unit) {
    FloatingActionButton(
        onClick = onClick,
        containerColor = MaterialTheme.colorScheme.errorContainer
    ) {
        Icon(Icons.Default.Delete, contentDescription = localizedString(R.string.clear_board))
    }
}

@Composable
fun StartButtonAndPieceCounter(
    pieceCounts: PieceCounts,
    isValidDistribution: Boolean,
    isCompletedDistribution: Boolean,
    onClick: () -> Unit
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        PieceCounter(
            whiteCount = pieceCounts.white,
            blackCount = pieceCounts.black,
            isValid = isValidDistribution
        )
        Spacer(modifier = Modifier.height(8.dp))
        Button(
            onClick = onClick,
            enabled = isCompletedDistribution,
            colors = ButtonDefaults.buttonColors(
                containerColor = if (isCompletedDistribution) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
            )
        ) {
            LocalizedText(R.string.start_game)
            Spacer(modifier = Modifier.width(8.dp))
            Icon(Icons.Default.PlayArrow, contentDescription = localizedString(R.string.start))
        }
    }
}

@Composable
fun RotateButton(onRotate: () -> Unit) {
    FloatingActionButton(
        onClick = onRotate,
        containerColor = MaterialTheme.colorScheme.primaryContainer
    ) {
        Icon(
            Icons.AutoMirrored.Filled.RotateRight,
            contentDescription = localizedString(R.string.rotate_board)
        )
    }
}

@Composable
fun ColorToggleButton(currentColor: Color, onColorToggle: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(8.dp)
    ) {
        FloatingActionButton(
            onClick = onColorToggle,
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            modifier = Modifier.size(40.dp)
        ) {
            val backgroundColor = backgroundColor(currentColor)
            val contentColor = foregroundColor(currentColor)

            Box(
                modifier = Modifier
                    .size(24.dp)
                    .background(backgroundColor, CircleShape)
                    .border(1.dp, MaterialTheme.colorScheme.outline, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (currentColor == WHITE)
                        localizedString(R.string.w)
                    else localizedString(R.string.b),
                    style = MaterialTheme.typography.labelSmall,
                    color = contentColor
                )
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        LocalizedText(
            R.string.piece,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

// Contador de Piezas en Edición de Tablero
@Composable
fun PieceCounter(whiteCount: Int, blackCount: Int, isValid: Boolean) {
    Card(
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isValid) MaterialTheme.colorScheme.surface
            else MaterialTheme.colorScheme.errorContainer
        )
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Contador blanco
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .background(MaterialTheme.colorScheme.surface, CircleShape)
                    .border(1.dp, MaterialTheme.colorScheme.outline, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    whiteCount.toString(),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Text(" — ", modifier = Modifier.padding(horizontal = 8.dp))

            // Contador negro
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .background(MaterialTheme.colorScheme.onSurface, CircleShape)
                    .border(1.dp, MaterialTheme.colorScheme.outline, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    blackCount.toString(),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.surface
                )
            }

            // Indicador de validez
            if (!isValid) {
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    Icons.Default.Warning,
                    localizedString(R.string.invalid_schema),
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

// region Previews

@Preview(showBackground = true)
@Composable
fun EditingModePreviewContent(
    darkTheme: Boolean = false,
    isLandscape: Boolean = false,
    boardOrientation: BoardOrientation = if (isLandscape) BoardOrientation.LANDSCAPE_BLACK else BoardOrientation.PORTRAIT_WHITE
) {
    TaratiTheme(darkTheme = darkTheme) {
        val exampleGameState = initialGameState()

        var isEditing by remember { mutableStateOf(true) }
        var aiEnabled by remember { mutableStateOf(false) }
        var editColor by remember { mutableStateOf(WHITE) }
        var editTurn by remember { mutableStateOf(WHITE) }
        var playerSide by remember { mutableStateOf(WHITE) }

        val pieceCounts = PieceCounts(4, 4)
        val isValidDistribution = true
        val isCompletedDistribution = true

        // Crear estado para Board
        val boardState = BoardState(
            gameState = exampleGameState,
            lastMove = null,
            aiEnabled = aiEnabled,
            boardOrientation = boardOrientation,
            labelsVisible = false,
            verticesVisible = true,
            edgesVisible = true,
            isEditing = isEditing,
        )

        // Crear eventos para Board
        val boardEvents = createPreviewBoardEvents(false)
        val boardColors = getBoardColors()

        Box(modifier = Modifier.fillMaxSize()) {
            Board(
                modifier = Modifier.fillMaxSize(),
                playerSide = playerSide,
                boardState = boardState,
                boardColors = boardColors,
                events = boardEvents,
            )

            EditControlsPreview(
                isLandscapeScreen = isLandscape,
                colorState = EditColorState(
                    playerSide = playerSide,
                    editColor = editColor,
                    editTurn = editTurn
                ),
                colorEvents = EditColorEvents(
                    onColorToggle = { editColor = editColor.opponent() },
                    onPlayerSideToggle = { playerSide = playerSide.opponent() },
                    onTurnToggle = { editTurn = editTurn.opponent() }
                ),
                actionState = EditActionState(
                    pieceCounts = pieceCounts,
                    isValidDistribution = isValidDistribution,
                    isCompletedDistribution = isCompletedDistribution
                ),
                actionEvents = EditActionEvents(
                    onClearBoard = { /* No-op en preview */ }
                )
            )
        }
    }
}

// Función base reutilizable para los previews
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MainScreenPreviewContent(
    config: PreviewConfig = PreviewConfig()
) {
    TaratiTheme(darkTheme = config.darkTheme) {
        val drawerState = rememberDrawerState(initialValue = config.drawerStateValue)
        val scope = rememberCoroutineScope()

        val exampleMoveHistory = listOf(
            Move("C1", "B1"),
            Move("C7", "B4"),
            Move("B1", "A1"),
            Move("B4", "A1")
        )

        // Estado del juego para el preview
        var gameState by remember { mutableStateOf(initialGameState()) }
        var playerSide by remember { mutableStateOf(config.playerSide) }
        var isEditing by remember { mutableStateOf(config.isEditing) }
        var isTutorial by remember { mutableStateOf(config.isTutorialActive) }
        var labelsVisible by remember { mutableStateOf(config.labelsVisible) }
        var verticesVisible by remember { mutableStateOf(config.verticesVisible) }
        var edgesVisible by remember { mutableStateOf(config.edgesVisible) }

        // Estado UI para el Sidebar
        var sidebarUIState by remember { mutableStateOf(SidebarUIState()) }

        // Implementación de eventos para el preview
        val sidebarEvents = createPreviewSidebarEvents(
            currentIsEditing = isEditing,
            onGameStateUpdate = { gameState = it },
            onPlayerSideUpdate = { playerSide = it },
            onEditingUpdate = { isEditing = it },
            debug = config.debug
        )

        // Crear el estado del juego para el Sidebar
        val sidebarGameState = SidebarGameState(
            gameState = gameState,
            playerSide = playerSide,
            moveIndex = 2,
            moveHistory = exampleMoveHistory,
            difficulty = Difficulty.DEFAULT,
            isAIEnabled = true,
        )

        // Estado y eventos para CreateBoard
        val createBoardState = CreateBoardState(
            gameState = gameState,
            lastMove = null,
            playerSide = playerSide,
            aiEnabled = true,
            isEditing = isEditing,
            isTutorialActive = isTutorial,
            isAIThinking = false,
            boardOrientation = toBoardOrientation(config.landScape, playerSide),
            editBoardOrientation = toBoardOrientation(config.landScape, playerSide),
            labelsVisible = labelsVisible,
            verticesVisible = verticesVisible,
            edgesVisible = edgesVisible,
        )

        val createBoardEvents = createPreviewBoardEvents(config.debug)
        val indicatorState = TurnIndicatorState.HUMAN_TURN
        val indicatorEvents = createPreviewIndicatorEvents(config.debug)
        val boardColors = getBoardColors()

        ModalNavigationDrawer(
            drawerState = drawerState,
            drawerContent = {
                Sidebar(
                    modifier = Modifier.systemBarsPadding(),
                    sidebarState = sidebarGameState,
                    uiState = sidebarUIState,
                    events = sidebarEvents,
                    onUIStateChange = { newState -> sidebarUIState = newState }
                )
            }
        ) {
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
                        if (!config.landScape && !isEditing) {
                            LocalizedText(
                                id = R.string.a_board_game_by_george_spencer_brown,
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(bottom = 16.dp)
                            )
                        }

                        CreateBoard(
                            modifier = Modifier.weight(1f),
                            state = createBoardState,
                            events = createBoardEvents,
                            boardColors = boardColors,
                            tutorial = { },
                            content = {
                                EditControlsPreview(
                                    isLandscapeScreen = config.landScape,
                                    colorState = EditColorState(
                                        playerSide = playerSide,
                                        editColor = WHITE,
                                        editTurn = WHITE
                                    ),
                                    colorEvents = EditColorEvents(),
                                    actionState = EditActionState(
                                        pieceCounts = PieceCounts(4, 4),
                                        isValidDistribution = true,
                                        isCompletedDistribution = true
                                    ),
                                    actionEvents = EditActionEvents()
                                )
                            },
                            turnIndicator = {
                                TurnIndicator(
                                    modifier = it,
                                    state = indicatorState,
                                    currentTurn = gameState.currentTurn,
                                    boardColors = boardColors,
                                    indicatorEvents = indicatorEvents
                                )
                            }
                        )
                    }
                }
            }
        }
    }
}

// Previews Portrait
@Preview(showBackground = true, device = "spec:width=411dp,height=891dp")
@Composable
fun MainScreenPreview_WithDrawer_Portrait() {
    MainScreenPreviewContent(
        config = PreviewConfig(drawerStateValue = DrawerValue.Open)
    )
}

@Preview(showBackground = true, device = "spec:width=411dp,height=891dp")
@Composable
fun MainScreenPreview_WithDrawer_Portrait_Dark() {
    MainScreenPreviewContent(
        config = PreviewConfig(
            darkTheme = true,
            drawerStateValue = DrawerValue.Open
        )
    )
}

// Previews Landscape
@Preview(showBackground = true, device = "spec:width=891dp,height=411dp")
@Composable
fun MainScreenPreview_WithDrawer_Landscape() {
    MainScreenPreviewContent(
        config = PreviewConfig(
            drawerStateValue = DrawerValue.Open,
            landScape = true
        )
    )
}

@Preview(showBackground = true, device = "spec:width=891dp,height=411dp")
@Composable
fun MainScreenPreview_WithDrawer_Landscape_Dark() {
    MainScreenPreviewContent(
        config = PreviewConfig(
            darkTheme = true,
            drawerStateValue = DrawerValue.Open,
            landScape = true
        )
    )
}

// Previews con drawer cerrado (para ver el contenido principal)
@Preview(showBackground = true, device = "spec:width=411dp,height=891dp")
@Composable
fun MainScreenPreview_DrawerClosed_Portrait() {
    MainScreenPreviewContent(
        config = PreviewConfig(isEditing = true)
    )
}

@Preview(showBackground = true, device = "spec:width=891dp,height=411dp")
@Composable
fun MainScreenPreview_DrawerClosed_Landscape() {
    MainScreenPreviewContent(
        config = PreviewConfig(
            landScape = true,
            isEditing = true
        )
    )
}

// Preview adicional: Juego en progreso con más movimientos
@Preview(showBackground = true, device = "spec:width=411dp,height=891dp")
@Composable
fun MainScreenPreview_GameInProgress() {
    MainScreenPreviewContent(
        config = PreviewConfig(drawerStateValue = DrawerValue.Open)
    )
}

// Previews de EditControls actualizados
@Preview(showBackground = true, widthDp = 400, heightDp = 200)
@Composable
fun EditControlsPreview_Portrait() {
    TaratiTheme {
        EditControlsPreview(isLandscapeScreen = false)
    }
}

@Preview(showBackground = true, widthDp = 800, heightDp = 200)
@Composable
fun EditControlsPreview_Landscape() {
    TaratiTheme {
        EditControlsPreview(isLandscapeScreen = true)
    }
}

@Preview(showBackground = true, widthDp = 400, heightDp = 200)
@Composable
fun EditControlsPreview_InvalidDistribution() {
    TaratiTheme {
        EditControlsPreview(
            isLandscapeScreen = false,
            actionState = EditActionState(
                pieceCounts = PieceCounts(8, 0),
                isValidDistribution = false,
                isCompletedDistribution = false
            )
        )
    }
}

@Preview(showBackground = true, widthDp = 400, heightDp = 200)
@Composable
fun EditControlsPreview_CompletedDistribution() {
    TaratiTheme {
        EditControlsPreview(
            isLandscapeScreen = false,
            actionState = EditActionState(
                pieceCounts = PieceCounts(7, 1),
                isValidDistribution = true,
                isCompletedDistribution = true
            )
        )
    }
}

@Preview(showBackground = true, widthDp = 400, heightDp = 200)
@Composable
fun EditControlsPreview_BlackColor() {
    TaratiTheme {
        EditControlsPreview(
            isLandscapeScreen = false,
            colorState = EditColorState(
                editColor = BLACK,
                playerSide = BLACK,
                editTurn = BLACK
            )
        )
    }
}

@Composable
fun EditControlsPreview(
    isLandscapeScreen: Boolean,
    colorState: EditColorState = EditColorState(),
    colorEvents: EditColorEvents = EditColorEvents(),
    actionState: EditActionState = EditActionState(),
    actionEvents: EditActionEvents = EditActionEvents()
) {
    if (isLandscapeScreen) {
        // Landscape: controles a izquierda y derecha
        Box(modifier = Modifier.fillMaxSize()) {
            LeftControls(
                modifier = Modifier.align(CenterStart),
                state = colorState,
                events = colorEvents
            )

            RightControls(
                modifier = Modifier.align(CenterEnd),
                state = actionState,
                events = actionEvents
            )
        }
    } else {
        // Portrait: controles en superior e inferior
        Box(modifier = Modifier.fillMaxSize()) {
            TopControls(
                modifier = Modifier.align(TopCenter),
                state = colorState,
                events = colorEvents
            )

            BottomControls(
                modifier = Modifier.align(BottomCenter),
                state = actionState,
                events = actionEvents
            )
        }
    }
}

// Previews para componentes individuales
@Preview(showBackground = true, widthDp = 200, heightDp = 300)
@Composable
fun LeftControlsPreview() {
    TaratiTheme {
        LeftControls(
            state = EditColorState(
                playerSide = WHITE,
                editColor = WHITE,
                editTurn = WHITE
            ),
            events = EditColorEvents()
        )
    }
}

@Preview(showBackground = true, widthDp = 200, heightDp = 300)
@Composable
fun RightControlsPreview() {
    TaratiTheme {
        RightControls(
            state = EditActionState(
                pieceCounts = PieceCounts(4, 4),
                isValidDistribution = true,
                isCompletedDistribution = true
            ),
            events = EditActionEvents()
        )
    }
}

@Preview(showBackground = true, widthDp = 400, heightDp = 100)
@Composable
fun TopControlsPreview() {
    TaratiTheme {
        TopControls(
            state = EditColorState(
                playerSide = WHITE,
                editColor = WHITE,
                editTurn = WHITE
            ),
            events = EditColorEvents()
        )
    }
}

@Preview(showBackground = true, widthDp = 400, heightDp = 100)
@Composable
fun BottomControlsPreview() {
    TaratiTheme {
        BottomControls(
            state = EditActionState(
                pieceCounts = PieceCounts(4, 4),
                isValidDistribution = true,
                isCompletedDistribution = true
            ),
            events = EditActionEvents()
        )
    }
}

@Composable
private fun createPreviewSidebarEvents(
    currentIsEditing: Boolean,
    onGameStateUpdate: (GameState) -> Unit,
    onPlayerSideUpdate: (Color) -> Unit,
    onEditingUpdate: (Boolean) -> Unit,
    debug: Boolean
): SidebarEvents = object : SidebarEvents {
    override fun onMoveToCurrent() = onGameStateUpdate(initialGameState())
    override fun onUndo() {}
    override fun onRedo() {}
    override fun onDifficultyChange(difficulty: Difficulty) {
        if (debug) println("Difficulty changed to: $difficulty")
    }

    override fun onToggleAI() {
        if (debug) println("AI toggled")
    }

    override fun onSettings() {
        if (debug) println("Settings clicked")
    }

    override fun onNewGame(color: Color) {
        onPlayerSideUpdate(color)
        onGameStateUpdate(initialGameState())
        if (debug) println("New game with side: $color")
    }

    override fun onEditBoard() {
        onEditingUpdate(!currentIsEditing)
        if (debug) println("Edit board: $currentIsEditing")
    }

    override fun onAboutClick() {
        if (debug) println("About clicked")
    }
}

private fun createPreviewBoardEvents(debug: Boolean): BoardEvents = object : BoardEvents {
    override fun onMove(from: String, to: String) {
        if (debug) println("Move from $from to $to")
    }

    override fun onEditPiece(from: String) {
        if (debug) println("Edit piece at $from")
    }

    override fun onResetCompleted() {
        if (debug) println("Board reset completed")
    }
}

private fun createPreviewIndicatorEvents(debug: Boolean): IndicatorEvents = object : IndicatorEvents {
    override fun onTouch() {
        if (debug) println("Indicator turn clicked")
    }
}

// endregion Previews