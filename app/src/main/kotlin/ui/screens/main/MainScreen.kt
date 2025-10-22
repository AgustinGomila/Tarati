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
import androidx.compose.material3.AlertDialog
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
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.agustin.tarati.R
import com.agustin.tarati.game.ai.Difficulty
import com.agustin.tarati.game.ai.EvaluationConfig
import com.agustin.tarati.game.ai.TaratiAI.applyMoveToBoard
import com.agustin.tarati.game.ai.TaratiAI.clearAIHistory
import com.agustin.tarati.game.ai.TaratiAI.getNextBestMove
import com.agustin.tarati.game.ai.TaratiAI.setEvaluationConfig
import com.agustin.tarati.game.core.Color
import com.agustin.tarati.game.core.Color.BLACK
import com.agustin.tarati.game.core.Color.WHITE
import com.agustin.tarati.game.core.GameResult
import com.agustin.tarati.game.core.GameState
import com.agustin.tarati.game.core.GameStatus
import com.agustin.tarati.game.core.Move
import com.agustin.tarati.game.core.getColorStringResource
import com.agustin.tarati.game.core.initialGameState
import com.agustin.tarati.game.core.opponent
import com.agustin.tarati.game.logic.BoardOrientation
import com.agustin.tarati.game.logic.getMatchState
import com.agustin.tarati.game.logic.isGameOver
import com.agustin.tarati.game.logic.isPortrait
import com.agustin.tarati.game.logic.toBoardOrientation
import com.agustin.tarati.game.tutorial.TutorialManager
import com.agustin.tarati.game.tutorial.TutorialState
import com.agustin.tarati.ui.components.board.Board
import com.agustin.tarati.ui.components.board.BoardEvents
import com.agustin.tarati.ui.components.board.BoardState
import com.agustin.tarati.ui.components.board.animation.AnimationCoordinator
import com.agustin.tarati.ui.components.board.animation.AnimationEvent
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@SuppressLint("UnrememberedMutableState")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    navController: NavController,
    settingsViewModel: SettingsViewModel = viewModel()
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val configuration = LocalConfiguration.current

    val viewModel: MainViewModel = viewModel()
    val debug = viewModel.isDebug

    val animationViewModel: BoardAnimationViewModel = viewModel()
    val isAnimating by animationViewModel.isAnimating.collectAsState()
    val boardSize by animationViewModel.boardSize.collectAsState()

    val animationCoordinator = remember { AnimationCoordinator(animationViewModel) }
    val highlightService = remember { HighlightService(animationViewModel) }

    val tutorialManager = remember { TutorialManager(animationCoordinator) }
    val tutorialViewModel by lazy { TutorialViewModel(tutorialManager) }

    val isEditing by viewModel.isEditing.collectAsState()
    val editColor by viewModel.editColor.collectAsState()
    val editTurn by viewModel.editTurn.collectAsState()
    val editBoardOrientation by viewModel.editBoardOrientation.collectAsState()
    val gameStatus by viewModel.gameStatus.collectAsState()

    val gameState by viewModel.gameState.collectAsState(initialGameState())
    val history by viewModel.history.collectAsState(emptyList())
    val moveIndex by viewModel.moveIndex.collectAsState(-1)

    val aiEnabled by viewModel.aIEnabled.collectAsState(true)
    val playerSide by viewModel.playerSide.collectAsState(WHITE)

    val settingsState by settingsViewModel.settingsState.collectAsState()
    val tutorialButtonVisible = settingsState.tutorialButtonVisible
    val evalConfig = EvaluationConfig.getByDifficulty(settingsState.difficulty)

    val labelsVisibles = settingsState.boardState.labelsVisibles
    val edgesVisibles = settingsState.boardState.edgesVisibles
    val verticesVisibles = settingsState.boardState.verticesVisibles

    val animateEffects = settingsState.boardState.animateEffects
    LaunchedEffect(animateEffects) {
        animationViewModel.updateAnimateEffects(animateEffects)
    }

    val boardColors = getBoardColors()

    var lastMove by remember { mutableStateOf<Move?>(null) }
    var isAIThinking by remember { mutableStateOf(false) }

    var showNewGameDialog by remember { mutableStateOf(false) }
    var showAboutDialog by remember { mutableStateOf(false) }
    var showGameOverDialog by remember { mutableStateOf(false) }

    // Orientación del Tablero
    val screenOrientation = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
    var boardOrientation by remember { mutableStateOf(BoardOrientation.PORTRAIT_WHITE) }
    LaunchedEffect(screenOrientation) {
        boardOrientation = toBoardOrientation(screenOrientation, playerSide)
    }

    // Indicador de turno
    val turnState: TurnIndicatorState by derivedStateOf {
        when {
            gameStatus == GameStatus.NEW_GAME -> TurnIndicatorState.NEUTRAL
            isAIThinking -> TurnIndicatorState.AI_THINKING
            else -> TurnIndicatorState.HUMAN_TURN
        }
    }

    // Manager del tutorial inicial
    val tutorialState by tutorialViewModel.tutorialState.collectAsState()
    val isTutorialActive = remember { mutableStateOf(false) }

    // Estado del Tutorial
    LaunchedEffect(tutorialState) {
        isTutorialActive.value = tutorialState != TutorialState.Idle && tutorialState != TutorialState.Completed
        if (!isTutorialActive.value || tutorialViewModel.tutorialGameState == null) return@LaunchedEffect

        val gs = tutorialViewModel.tutorialGameState ?: return@LaunchedEffect
        viewModel.updateGameState(gs)
    }

    // Efecto para resetear estados cuando comienza nuevo juego
    LaunchedEffect(gameStatus) {
        if (isEditing || isTutorialActive.value) return@LaunchedEffect
        if (gameStatus == GameStatus.NEW_GAME) return@LaunchedEffect
        when {
            // Estado inicial de la app
            gameState == initialGameState() && gameStatus == GameStatus.NO_PLAYING -> {
                viewModel.updateGameStatus(GameStatus.PLAYING)
            }

            gameStatus == GameStatus.GAME_OVER -> {
                delay(1500)
                showGameOverDialog = true
            }
        }
    }

    // Manejar cambios de dificultad
    LaunchedEffect(evalConfig) {
        setEvaluationConfig(evalConfig)
    }

    fun applyMove(from: String, to: String) {
        val move = Move(from, to)
        val newBoardState = applyMoveToBoard(gameState, from, to)
        val nextState = newBoardState.copy(currentTurn = gameState.currentTurn.opponent())

        // Actualizar historial
        val newEntry = Pair(move, nextState)
        val truncated =
            if (moveIndex < history.size - 1) {
                history.take(moveIndex + 1)
            } else history

        val newMoveHistory = truncated + newEntry
        viewModel.updateMoveIndex(newMoveHistory.size - 1)
        viewModel.updateHistory(newMoveHistory)

        // Animar Highlights separados
        if (animateEffects) {
            highlightService.animateHighlights(highlightService.createMoveHighlights(move))
        }

        // Usar el coordinador
        animationCoordinator.handleEvent(
            AnimationEvent.MoveEvent(move, gameState, nextState)
        )

        // Actualizar estado del juego
        viewModel.updateGameState(nextState)

        if (nextState.isGameOver())
            viewModel.updateGameStatus(GameStatus.GAME_OVER)
    }

    // Lanzadores de pensamiento de IA
    val aiThinkingLauncher = listOf(
        gameStatus,
        gameState.currentTurn,
        aiEnabled,
        playerSide,
        isEditing
    )

    // Lanzador de IA
    LaunchedEffect(aiThinkingLauncher) {
        isAIThinking = false

        if (!aiEnabled || isEditing || isTutorialActive.value || gameStatus != GameStatus.PLAYING) {
            if (debug) println("DEBUG: AI blocked - gameStatus: $gameStatus")
            return@LaunchedEffect
        }

        // La IA juega cuando:
        // 1. Está habilitada
        // 2. No es el turno del jugador humano
        // 3. El juego no está terminado
        // 4. Estamos en estado PLAYING
        val shouldAIPlay = gameState.currentTurn != playerSide &&
                !gameState.isGameOver()

        if (!shouldAIPlay) return@LaunchedEffect

        isAIThinking = true
        if (debug) println("DEBUG: AI starting to think...")

        val result = try {
            withContext(Dispatchers.Default) {
                getNextBestMove(
                    gameState = gameState,
                    debug = false
                )
            }
        } catch (t: Throwable) {
            t.printStackTrace()
            null
        }

        if (debug) println("AI calculated move: ${result?.move}")

        result?.move?.let { move ->
            applyMove(move.from, move.to)
        }
    }

    fun resetDialogs() {
        showNewGameDialog = false
        showGameOverDialog = false
        showAboutDialog = false
    }

    fun startNewGame(playerSide: Color) {
        viewModel.updateGameStatus(GameStatus.NO_PLAYING)
        clearAIHistory()
        lastMove = null

        resetDialogs()
        tutorialViewModel.endTutorial()

        scope.launch {
            drawerState.close()
        }

        highlightService.stopHighlights()
        animationCoordinator.handleEvent(AnimationEvent.SyncState)

        viewModel.startGame(playerSide)
        viewModel.updateGameStatus(GameStatus.PLAYING)

        if (debug) println("New game started: playerSide=$playerSide")
    }

    fun startTutorial() {
        viewModel.updateGameStatus(GameStatus.NO_PLAYING)

        resetDialogs()

        highlightService.stopHighlights()
        animationCoordinator.handleEvent(AnimationEvent.SyncState)

        tutorialViewModel.startTutorial()
    }

    fun endTutorial() {
        clearAIHistory()
        lastMove = null
        resetDialogs()

        viewModel.clearBoard()
        viewModel.updateGameState(initialGameState())
        viewModel.updateGameStatus(GameStatus.NO_PLAYING)

        tutorialViewModel.endTutorial()
    }

    fun undoMove() {
        if (moveIndex >= 0) {
            viewModel.decrementMoveIndex()
            val newState = if (moveIndex < history.size) {
                history[moveIndex].second
            } else {
                initialGameState()
            }
            viewModel.updateGameState(newState)
        }
    }

    fun redoMove() {
        if (moveIndex < history.size - 1) {
            viewModel.incrementMoveIndex()
            val newState = history[moveIndex].second
            viewModel.updateGameState(newState)
        }
    }

    fun moveToCurrentState() {
        if (history.isNotEmpty()) {
            viewModel.updateMoveIndex(history.size - 1)
            viewModel.updateGameState(history.last().second)
        }
    }

    // Diálogo de fin de juego
    if (showGameOverDialog) {
        val matchState = gameState.getMatchState()
        val winner = matchState.winner
        if (matchState.gameResult == GameResult.PLAYING || matchState.gameResult == GameResult.UNDETERMINED || winner == null) {
            resetDialogs()
            viewModel.updateGameStatus(GameStatus.NO_PLAYING)
            return
        }

        val message = when (matchState.gameResult) {
            GameResult.TRIPLE -> {
                String.format(
                    context.getString(R.string.game_over_triple_repetition),
                    context.getString(winner.getColorStringResource())
                )
            }

            GameResult.MIT -> {
                String.format(
                    context.getString(R.string.game_over_wins),
                    context.getString(winner.getColorStringResource())
                )
            }

            else -> {
                String.format(
                    context.getString(R.string.game_over_stalemit),
                    context.getString(winner.getColorStringResource())
                )
            }
        }

        // Juego terminado
        GameOverDialog(
            gameOverMessage = message,
            onConfirmed = {
                startNewGame(playerSide)
            },
            onDismissed = {
                resetDialogs()
                viewModel.updateGameStatus(GameStatus.NEW_GAME)
            },
        )
    }

    // Diálogo de confirmación de nueva partida
    if (showNewGameDialog && !isAnimating) {
        NewGameDialog(
            onConfirmed = { startNewGame(playerSide) },
            onDismissed = {
                resetDialogs()
                viewModel.updateGameStatus(GameStatus.NEW_GAME)
            },
        )
    }

    // Diálogo About
    if (showAboutDialog && !isAnimating) {
        AboutDialog(onDismiss = ::resetDialogs)
    }

    // Función para manejar edición de piezas
    fun editPiece(vertexId: String) {
        if (isEditing) {
            viewModel.editPiece(vertexId)
        }
    }

    // Calcular conteo de piezas
    val pieceCounts = remember(gameState) {
        val white = gameState.cobs.values.count { it.color == WHITE }
        val black = gameState.cobs.values.count { it.color == BLACK }
        PieceCounts(white, black)
    }

    // Validar distribución
    val isValidDistribution = remember(pieceCounts) {
        pieceCounts.white + pieceCounts.black <= 8 && when {
            pieceCounts.white == 7 && pieceCounts.black == 1 -> true
            pieceCounts.white == 6 && pieceCounts.black == 2 -> true
            pieceCounts.white == 5 && pieceCounts.black == 3 -> true
            pieceCounts.white == 4 && pieceCounts.black == 4 -> true
            pieceCounts.white == 3 && pieceCounts.black == 5 -> true
            pieceCounts.white == 2 && pieceCounts.black == 6 -> true
            pieceCounts.white == 1 && pieceCounts.black == 7 -> true
            pieceCounts.white + pieceCounts.black < 8 -> true
            else -> false
        }
    }

    val isCompletedDistribution = remember(pieceCounts) {
        pieceCounts.white + pieceCounts.black == 8 &&
                (pieceCounts.white == 7 && pieceCounts.black == 1 ||
                        pieceCounts.white == 6 && pieceCounts.black == 2 ||
                        pieceCounts.white == 5 && pieceCounts.black == 3 ||
                        pieceCounts.white == 4 && pieceCounts.black == 4 ||
                        pieceCounts.white == 3 && pieceCounts.black == 5 ||
                        pieceCounts.white == 2 && pieceCounts.black == 6 ||
                        pieceCounts.white == 1 && pieceCounts.black == 7)
    }

    // Manejo de movimientos durante el tutorial
    fun handleTutorialMove(from: String, to: String) {
        if (isTutorialActive.value) {
            tutorialViewModel.onMoveAttempted(
                from = from,
                to = to,
                onMoveAccepted = { tutorialViewModel.nextStep() },
                onMoveRejected = { tutorialViewModel.requestUserInteraction(it) }
                // TODO: Si el movimiento es incorrecto, mostrar un mensaje
            )
        }
    }

    // Controles de edición
    @Composable
    fun EditControls(isLandscapeScreen: Boolean) {
        if (isLandscapeScreen) {
            // Landscape: controles a izquierda y derecha
            Box(modifier = Modifier.fillMaxSize()) {
                // Controles a la izquierda: Color, Lado, Turno
                LeftControls(
                    modifier = Modifier.align(CenterStart),
                    state = ColorControlsState(
                        playerSide = playerSide,
                        editColor = editColor,
                        editTurn = editTurn,
                    ),
                    events = ColorControlsEvents(
                        onPlayerSideToggle = { viewModel.togglePlayerSide() },
                        onColorToggle = { viewModel.toggleEditColor() },
                        onTurnToggle = { viewModel.toggleEditTurn() },
                    )
                )

                // Controles a la derecha: Rotar, Limpiar, Contador y Comenzar
                RightControls(
                    modifier = Modifier.align(CenterEnd),
                    state = ActionControlsState(
                        pieceCounts = pieceCounts,
                        isValidDistribution = isValidDistribution,
                        isCompletedDistribution = isCompletedDistribution,
                    ),
                    events = ActionControlsEvents(
                        onRotate = { viewModel.rotateEditBoard() },
                        onStartGame = { viewModel.startGameFromEditedState() },
                        onClearBoard = { viewModel.clearBoard() }
                    )
                )
            }
        } else {
            // Portrait: controles en superior e inferior
            Box(modifier = Modifier.fillMaxSize()) {
                // Controles superiores: Color, Lado, Turno
                TopControls(
                    modifier = Modifier.align(TopCenter),
                    state = ColorControlsState(
                        playerSide = playerSide,
                        editColor = editColor,
                        editTurn = editTurn,
                    ),
                    events = ColorControlsEvents(
                        onPlayerSideToggle = { viewModel.togglePlayerSide() },
                        onColorToggle = { viewModel.toggleEditColor() },
                        onTurnToggle = { viewModel.toggleEditTurn() },
                    )
                )

                // Controles inferiores: Rotar, Limpiar, Contador y Comenzar
                BottomControls(
                    modifier = Modifier.align(BottomCenter),
                    state = ActionControlsState(
                        pieceCounts = pieceCounts,
                        isValidDistribution = isValidDistribution,
                        isCompletedDistribution = isCompletedDistribution,
                    ),
                    events = ActionControlsEvents(
                        onRotate = { viewModel.rotateEditBoard() },
                        onStartGame = { viewModel.startGameFromEditedState() },
                        onClearBoard = { viewModel.clearBoard() }
                    )
                )
            }
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            var sidebarUIState by remember { mutableStateOf(SidebarUIState()) }

            val sidebarEvents = object : SidebarEvents {
                override fun onMoveToCurrent() = ::moveToCurrentState.invoke()
                override fun onUndo() = ::undoMove.invoke()
                override fun onRedo() = ::redoMove.invoke()

                override fun onDifficultyChange(difficulty: Difficulty) {
                    settingsViewModel.setDifficulty(difficulty)
                }

                override fun onToggleAI() {
                    viewModel.updateAIEnabled(!aiEnabled)
                }

                override fun onSettings() {
                    navController.navigate(SettingsScreenDest.route)
                }

                override fun onNewGame(color: Color) {
                    viewModel.updatePlayerSide(color)
                    showNewGameDialog = true
                }

                override fun onEditBoard() {
                    scope.launch { drawerState.close() }
                    viewModel.toggleEditing()
                }

                override fun onAboutClick() {
                    showAboutDialog = true
                }

                override fun onTutorial() {
                    scope.launch { drawerState.close() }
                    startTutorial()
                }
            }

            val sidebarGameState = SidebarGameState(
                gameState = gameState,
                playerSide = playerSide,
                moveIndex = moveIndex,
                moveHistory = history.map { it.first },
                difficulty = evalConfig.difficulty,
                isAIEnabled = aiEnabled,
                showTutorialOption = tutorialButtonVisible,
            )

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
            topBar = { TaratiTopBar(scope, drawerState, isEditing) }
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
                            playerSide = playerSide,
                            isEditing = isEditing,
                            isTutorialActive = isTutorialActive.value,
                            isAIThinking = isAIThinking,
                            boardOrientation = boardOrientation,
                            editBoardOrientation = editBoardOrientation,
                            labelsVisible = labelsVisibles,
                            edgesVisible = edgesVisibles,
                            verticesVisible = verticesVisibles,
                        ),
                        events = object : BoardEvents {
                            override fun onMove(from: String, to: String) {
                                if (isTutorialActive.value) {
                                    handleTutorialMove(from, to)
                                } else {
                                    applyMove(from, to)
                                }
                            }

                            override fun onEditPiece(from: String) {
                                // Durante el tutorial, ignorar ediciones
                                if (!isTutorialActive.value) {
                                    editPiece(from)
                                }
                            }

                            override fun onResetCompleted() = Unit
                        },
                        boardAnimationViewModel = animationViewModel,
                        boardColors = boardColors,
                        tutorial = {
                            if (isTutorialActive.value) {
                                CreateTutorialOverlay(
                                    viewModel = tutorialViewModel,
                                    boardSize = boardSize,
                                    boardOrientation = editBoardOrientation,
                                    onEndTutorial = ::endTutorial,
                                    updateGameState = { newState -> viewModel.updateGameState(newState) },
                                )
                            }
                        },
                        content = { EditControls(!boardOrientation.isPortrait()) },
                        turnIndicator = {
                            TurnIndicator(
                                modifier = it,
                                currentTurn = gameState.currentTurn,
                                state = turnState,
                                boardColors = boardColors,
                                indicatorEvents = object : IndicatorEvents {
                                    override fun onTouch() {
                                        showNewGameDialog = true
                                    }
                                },
                            )
                        },
                        debug = debug
                    )
                }
            }
        }
    }
}

@Composable
fun CreateTutorialOverlay(
    viewModel: TutorialViewModel,
    boardSize: Size,
    boardOrientation: BoardOrientation,
    onEndTutorial: () -> Unit,
    updateGameState: (GameState) -> Unit,
) {
    if (boardSize == Size.Zero) return

    TutorialOverlay(
        viewModel = viewModel,
        onCompleted = onEndTutorial,
        updateGameState = { updateGameState(it) },
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
                LocalizedText(id = (R.string.tarati))
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
            IconButton(
                onClick = {
                    scope.launch {
                        if (drawerState.isClosed) drawerState.open()
                        else drawerState.close()
                    }
                }
            ) {
                Icon(Icons.Default.Menu, contentDescription = localizedString(R.string.menu))
            }
        }
    )
}

data class CreateBoardState(
    val gameState: GameState,
    val lastMove: Move?,
    val playerSide: Color,
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

    // Construir los eventos para Board
    val boardEvents = object : BoardEvents {
        override fun onMove(from: String, to: String) {
            if (!state.isEditing) {
                events.onMove(from, to)
            }
        }

        override fun onEditPiece(from: String) {
            events.onEditPiece(from)
        }

        override fun onResetCompleted() {
            events.onResetCompleted()
        }
    }

    Box(
        modifier
            .fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        Board(
            modifier = Modifier.fillMaxSize(),
            boardState = boardState,
            playerSide = state.playerSide,
            events = boardEvents,
            debug = debug,
            boardColors = boardColors,
            animationViewModel = boardAnimationViewModel,
        )

        when {
            state.isEditing -> content()

            state.isTutorialActive -> tutorial()

            else -> turnIndicator(Modifier.align(Alignment.TopEnd))
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
                boardState = boardState,
                playerSide = playerSide,
                events = boardEvents,
                boardColors = boardColors,
            )

            EditControlsPreview(
                isLandscapeScreen = isLandscape,
                colorState = ColorControlsState(
                    playerSide = playerSide,
                    editColor = editColor,
                    editTurn = editTurn
                ),
                colorEvents = ColorControlsEvents(
                    onColorToggle = { editColor = editColor.opponent() },
                    onPlayerSideToggle = { playerSide = playerSide.opponent() },
                    onTurnToggle = { editTurn = editTurn.opponent() }
                ),
                actionState = ActionControlsState(
                    pieceCounts = pieceCounts,
                    isValidDistribution = isValidDistribution,
                    isCompletedDistribution = isCompletedDistribution
                ),
                actionEvents = ActionControlsEvents(
                    onClearBoard = { /* No-op en preview */ }
                )
            )
        }
    }
}

@Composable
fun LeftControls(
    modifier: Modifier = Modifier,
    state: ColorControlsState = ColorControlsState(),
    events: ColorControlsEvents = ColorControlsEvents()
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
    state: ColorControlsState = ColorControlsState(),
    events: ColorControlsEvents = ColorControlsEvents()
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
    state: ActionControlsState = ActionControlsState(),
    events: ActionControlsEvents = ActionControlsEvents()
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
    state: ActionControlsState = ActionControlsState(),
    events: ActionControlsEvents = ActionControlsEvents()
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

@Composable
fun AboutDialog(onDismiss: () -> Unit = { }) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { LocalizedText(id = (R.string.about_tarati)) },
        text = {
            Column {
                LocalizedText(
                    id = R.string.tarati_is_a_strategic_board_game_created_by_george_spencer_brown,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                LocalizedText(
                    id = (R.string.game_rules),
                    style = MaterialTheme.typography.titleSmall,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
                LocalizedText(
                    id = (R.string.players_2_white_vs_black_objective_control_the_board),
                    style = MaterialTheme.typography.bodySmall
                )

                Spacer(modifier = Modifier.height(12.dp))

                LocalizedText(
                    id = (R.string.credits),
                    style = MaterialTheme.typography.titleSmall,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
                LocalizedText(
                    id = (R.string.original_concept_george_spencer_brown),
                    style = MaterialTheme.typography.bodySmall
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss
            ) {
                LocalizedText(id = (R.string.close))
            }
        }
    )
}

@Composable
fun GameOverDialog(
    gameOverMessage: String,
    onConfirmed: () -> Unit,
    onDismissed: () -> Unit = {},
) {
    AlertDialog(
        onDismissRequest = onDismissed,
        title = { LocalizedText(id = (R.string.game_over)) },
        text = { Text(gameOverMessage) },
        confirmButton = {
            Button(
                onClick = onConfirmed
            ) {
                LocalizedText(id = (R.string.new_game))
            }
        },
        dismissButton = {
            Button(
                onClick = onDismissed
            ) {
                LocalizedText(id = (R.string.continue_))
            }
        }
    )
}

@Composable
fun NewGameDialog(onConfirmed: () -> Unit, onDismissed: () -> Unit = { }) {
    AlertDialog(
        onDismissRequest = onDismissed,
        title = { LocalizedText(id = (R.string.new_game)) },
        text = { LocalizedText(id = (R.string.are_you_sure_you_want_to_start_a_new_game)) },
        confirmButton = {
            Button(
                onClick = { onConfirmed() }
            ) {
                LocalizedText(R.string.yes)
            }
        },
        dismissButton = {
            Button(
                onClick = onDismissed
            ) {
                LocalizedText(R.string.cancel)
            }
        }
    )
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
            debug = config.debug,
            scope = scope,
            drawerState = drawerState,
            currentIsEditing = isEditing,
            onGameStateUpdate = { gameState = it },
            onPlayerSideUpdate = { playerSide = it },
            onEditingUpdate = { isEditing = it }
        )

        // Crear el estado del juego para el Sidebar
        val sidebarGameState = SidebarGameState(
            gameState = gameState,
            playerSide = playerSide,
            moveIndex = 2,
            moveHistory = exampleMoveHistory,
            difficulty = Difficulty.DEFAULT,
            isAIEnabled = true,
            showTutorialOption = true
        )

        // Estado y eventos para CreateBoard
        val createBoardState = CreateBoardState(
            gameState = gameState,
            lastMove = null,
            playerSide = playerSide,
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
                                    colorState = ColorControlsState(
                                        playerSide = playerSide,
                                        editColor = WHITE,
                                        editTurn = WHITE
                                    ),
                                    colorEvents = ColorControlsEvents(),
                                    actionState = ActionControlsState(
                                        pieceCounts = PieceCounts(4, 4),
                                        isValidDistribution = true,
                                        isCompletedDistribution = true
                                    ),
                                    actionEvents = ActionControlsEvents()
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
            actionState = ActionControlsState(
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
            actionState = ActionControlsState(
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
            colorState = ColorControlsState(
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
    colorState: ColorControlsState = ColorControlsState(),
    colorEvents: ColorControlsEvents = ColorControlsEvents(),
    actionState: ActionControlsState = ActionControlsState(),
    actionEvents: ActionControlsEvents = ActionControlsEvents()
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
            state = ColorControlsState(
                playerSide = WHITE,
                editColor = WHITE,
                editTurn = WHITE
            ),
            events = ColorControlsEvents()
        )
    }
}

@Preview(showBackground = true, widthDp = 200, heightDp = 300)
@Composable
fun RightControlsPreview() {
    TaratiTheme {
        RightControls(
            state = ActionControlsState(
                pieceCounts = PieceCounts(4, 4),
                isValidDistribution = true,
                isCompletedDistribution = true
            ),
            events = ActionControlsEvents()
        )
    }
}

@Preview(showBackground = true, widthDp = 400, heightDp = 100)
@Composable
fun TopControlsPreview() {
    TaratiTheme {
        TopControls(
            state = ColorControlsState(
                playerSide = WHITE,
                editColor = WHITE,
                editTurn = WHITE
            ),
            events = ColorControlsEvents()
        )
    }
}

@Preview(showBackground = true, widthDp = 400, heightDp = 100)
@Composable
fun BottomControlsPreview() {
    TaratiTheme {
        BottomControls(
            state = ActionControlsState(
                pieceCounts = PieceCounts(4, 4),
                isValidDistribution = true,
                isCompletedDistribution = true
            ),
            events = ActionControlsEvents()
        )
    }
}

@Composable
private fun createPreviewSidebarEvents(
    debug: Boolean,
    scope: CoroutineScope,
    drawerState: DrawerState,
    currentIsEditing: Boolean,
    onGameStateUpdate: (GameState) -> Unit,
    onPlayerSideUpdate: (Color) -> Unit,
    onEditingUpdate: (Boolean) -> Unit
): SidebarEvents {
    return object : SidebarEvents {
        override fun onMoveToCurrent() {
            onGameStateUpdate(initialGameState())
        }

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
            scope.launch { drawerState.close() }
            onEditingUpdate(!currentIsEditing)
            if (debug) println("Edit board: $currentIsEditing")
        }

        override fun onAboutClick() {
            if (debug) println("About clicked")
        }

        override fun onTutorial() {
            if (debug) println("Show tutorial!")
        }
    }
}

private fun createPreviewBoardEvents(debug: Boolean): BoardEvents {
    return object : BoardEvents {
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
}

private fun createPreviewIndicatorEvents(debug: Boolean): IndicatorEvents {
    return object : IndicatorEvents {
        override fun onTouch() {
            if (debug) println("Indicator turn clicked")
        }
    }
}

// endregion Previews