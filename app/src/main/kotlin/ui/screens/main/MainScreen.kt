package com.agustin.tarati.ui.screens.main

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
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.agustin.tarati.R
import com.agustin.tarati.game.ai.Difficulty
import com.agustin.tarati.game.ai.TaratiAI.applyMoveToBoard
import com.agustin.tarati.game.ai.TaratiAI.clearAIHistory
import com.agustin.tarati.game.ai.TaratiAI.getNextBestMove
import com.agustin.tarati.game.ai.TaratiAI.isGameOver
import com.agustin.tarati.game.ai.TaratiAI.recordRealMove
import com.agustin.tarati.game.core.Color
import com.agustin.tarati.game.core.Color.BLACK
import com.agustin.tarati.game.core.Color.WHITE
import com.agustin.tarati.game.core.GameState
import com.agustin.tarati.game.core.Move
import com.agustin.tarati.game.core.getColorStringResource
import com.agustin.tarati.game.core.initialGameState
import com.agustin.tarati.game.core.opponent
import com.agustin.tarati.game.logic.BoardOrientation
import com.agustin.tarati.game.logic.toBoardOrientation
import com.agustin.tarati.ui.components.board.Board
import com.agustin.tarati.ui.components.board.BoardEvents
import com.agustin.tarati.ui.components.board.BoardState
import com.agustin.tarati.ui.components.board.TurnIndicator
import com.agustin.tarati.ui.components.sidebar.Sidebar
import com.agustin.tarati.ui.components.sidebar.SidebarEvents
import com.agustin.tarati.ui.components.sidebar.SidebarGameState
import com.agustin.tarati.ui.components.sidebar.SidebarUIState
import com.agustin.tarati.ui.localization.LocalizedText
import com.agustin.tarati.ui.localization.localizedString
import com.agustin.tarati.ui.navigation.ScreenDestinations.SettingsScreenDest
import com.agustin.tarati.ui.screens.settings.SettingsViewModel
import com.agustin.tarati.ui.theme.TaratiTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

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
    val isLandscapeScreen = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    val viewModel: MainViewModel = viewModel()
    val debug = viewModel.isDebug

    val vmIsEditing by viewModel.isEditing.collectAsState()
    val vmEditColor by viewModel.editColor.collectAsState()
    val vmEditTurn by viewModel.editTurn.collectAsState()
    val vmEditBoardOrientation by viewModel.editBoardOrientation.collectAsState()

    val vmGameState by viewModel.gameState.collectAsState(initialGameState())
    val vmHistory by viewModel.history.collectAsState(emptyList())
    val vmMoveIndex by viewModel.moveIndex.collectAsState(-1)
    val vmAIEnabled by viewModel.aIEnabled.collectAsState(true)
    val vmPlayerSide by viewModel.playerSide.collectAsState(WHITE)

    val settingsState by settingsViewModel.settingsState.collectAsState()
    val vmDifficulty = settingsState.difficulty
    val vmLabelsVisible = settingsState.labelsVisibility

    var stopAI by remember { mutableStateOf(false) }
    var resetBoard by remember { mutableStateOf(false) }
    var lastMove by remember { mutableStateOf<Move?>(null) }

    var showGameOverDialog by remember { mutableStateOf(false) }
    var showNewGameDialog by remember { mutableStateOf(false) }
    var gameOverMessage by remember { mutableStateOf("") }
    var showAboutDialog by remember { mutableStateOf(false) }

    fun isAIThinking(): Boolean {
        return vmAIEnabled &&
                vmGameState.currentTurn != vmPlayerSide &&
                !isGameOver(vmGameState) &&
                !vmIsEditing
    }

    fun applyMove(from: String, to: String) {
        stopAI = false

        if (debug) println("Applying move: $from -> $to")

        lastMove = Move(from, to)

        val newBoardState = applyMoveToBoard(vmGameState, from, to)

        val nextTurn = vmGameState.currentTurn.opponent()
        val nextState = newBoardState.copy(currentTurn = nextTurn)

        val newEntry = Pair(Move(from, to), nextState)
        val truncated =
            if (vmMoveIndex < vmHistory.size - 1) {
                vmHistory.take(vmMoveIndex + 1)
            } else vmHistory

        val newMoveHistory = truncated + newEntry
        viewModel.updateMoveIndex(newMoveHistory.size - 1)

        viewModel.updateHistory(newMoveHistory)
        viewModel.updateGameState(nextState)

        val repetitionLoser = recordRealMove(nextState, vmGameState.currentTurn)
        if (isGameOver(nextState)) {
            gameOverMessage = when {
                repetitionLoser == null -> {
                    String.format(
                        context.getString(R.string.game_over_wins),
                        context.getString(nextState.currentTurn.opponent().getColorStringResource())
                    )
                }

                else -> {
                    String.format(
                        context.getString(R.string.game_over_triple_repetition),
                        context.getString(repetitionLoser.getColorStringResource())
                    )
                }
            }
            showGameOverDialog = true
            stopAI = true
        }
    }

    // Disparadores de Efectos
    val effectLaunchers = listOf(
        vmGameState.currentTurn,
        vmAIEnabled,
        stopAI,
        vmDifficulty,
        vmPlayerSide,
        vmIsEditing
    )

    LaunchedEffect(effectLaunchers) {
        if (!vmAIEnabled || stopAI || vmIsEditing) {
            if (debug) println("DEBUG: AI blocked - AIEnabled: $vmAIEnabled, stopAI: $stopAI, isEditing: $vmIsEditing")
            return@LaunchedEffect
        }

        // La IA juega cuando:
        // 1. Está habilitada
        // 2. No es el turno del jugador humano
        // 3. El juego no está terminado
        val shouldAIPlay = vmGameState.currentTurn != vmPlayerSide &&
                !isGameOver(vmGameState)

        if (debug) println("DEBUG: AI Check - currentTurn: ${vmGameState.currentTurn}, playerSide: $vmPlayerSide, shouldPlay: $shouldAIPlay")

        if (shouldAIPlay) {
            if (debug) println("DEBUG: AI starting to think...")

            val result = try {
                withContext(Dispatchers.Default) {
                    getNextBestMove(
                        gameState = vmGameState,
                        depth = vmDifficulty.aiDepth,
                        debug = debug
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
    }

    fun startNewGame(playerSide: Color) {
        clearAIHistory()

        viewModel.startGame(playerSide)

        showNewGameDialog = false
        showGameOverDialog = false
        showAboutDialog = false

        // Reiniciar estado de IA
        stopAI = false

        scope.launch {
            drawerState.close()
        }

        if (debug) println("New game started: playerSide=$playerSide")
        resetBoard = true
    }

    fun undoMove() {
        if (vmMoveIndex >= 0) {
            viewModel.decrementMoveIndex()
            val newState = if (vmMoveIndex < vmHistory.size) {
                vmHistory[vmMoveIndex].second
            } else {
                initialGameState()
            }
            viewModel.updateGameState(newState)
        }
        stopAI = true
    }

    fun redoMove() {
        if (vmMoveIndex < vmHistory.size - 1) {
            viewModel.incrementMoveIndex()
            val newState = vmHistory[vmMoveIndex].second
            viewModel.updateGameState(newState)
        }
    }

    fun moveToCurrentState() {
        if (vmHistory.isNotEmpty()) {
            viewModel.updateMoveIndex(vmHistory.size - 1)
            viewModel.updateGameState(vmHistory.last().second)
        }
    }

    // Diálogo About
    if (showAboutDialog) {
        AboutDialog(onDismiss = { showAboutDialog = false })
    }

    // Diálogo de fin de juego
    if (showGameOverDialog) {
        GameOverDialog(
            gameOverMessage = gameOverMessage,
            onConfirmed = { startNewGame(vmPlayerSide) },
            onDismissed = { showGameOverDialog = false }
        )
    }

    // Diálogo de confirmación de nueva partida
    if (showNewGameDialog) {
        NewGameDialog(
            onConfirmed = { startNewGame(vmPlayerSide) },
            onDismissed = { showNewGameDialog = false },
        )
    }

    val resetBoardCompleted = {
        resetBoard = false
        if (debug) println("Board reset completed")
    }

    // Función para manejar edición de piezas
    fun editPiece(vertexId: String) {
        if (vmIsEditing) {
            viewModel.editPiece(vertexId)
        }
    }

    // Calcular conteo de piezas
    val pieceCounts = remember(vmGameState) {
        val white = vmGameState.checkers.values.count { it.color == WHITE }
        val black = vmGameState.checkers.values.count { it.color == BLACK }
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

    // Controles de edición
    @Composable
    fun EditControls(isLandscapeScreen: Boolean) {
        if (isLandscapeScreen) {
            // Landscape: controles a izquierda y derecha
            Box(modifier = Modifier.fillMaxSize()) {
                // Controles a la izquierda: Color, Lado, Turno
                LeftControls(
                    modifier = Modifier.align(CenterStart),
                    vmPlayerSide = vmPlayerSide,
                    onPlayerSideToggle = { viewModel.togglePlayerSide() },
                    editColor = vmEditColor,
                    onColorToggle = { viewModel.toggleEditColor() },
                    editTurn = vmEditTurn
                ) { viewModel.toggleEditTurn() }

                // Controles a la derecha: Rotar, Limpiar, Contador y Comenzar
                RightControls(
                    modifier = Modifier.align(CenterEnd),
                    pieceCounts = pieceCounts,
                    isValidDistribution = isValidDistribution,
                    isCompletedDistribution = isCompletedDistribution,
                    onRotate = { viewModel.rotateEditBoard() },
                    onStartGame = {
                        viewModel.startGameFromEditedState()
                        resetBoard = true
                    }
                ) { viewModel.clearBoard() }
            }
        } else {
            // Portrait: controles en superior e inferior
            Box(modifier = Modifier.fillMaxSize()) {
                // Controles superiores: Color, Lado, Turno
                TopControls(
                    modifier = Modifier.align(TopCenter),
                    playerSide = vmPlayerSide,
                    onPlayerSideToggle = { viewModel.togglePlayerSide() },
                    editColor = vmEditColor,
                    onColorToggle = { viewModel.toggleEditColor() },
                    editTurn = vmEditTurn
                ) { viewModel.toggleEditTurn() }

                // Controles inferiores: Rotar, Limpiar, Contador y Comenzar
                BottomControls(
                    modifier = Modifier.align(BottomCenter),
                    pieceCounts = pieceCounts,
                    isValidDistribution = isValidDistribution,
                    isCompletedDistribution = isCompletedDistribution,
                    onRotate = { viewModel.rotateEditBoard() },
                    onStartGame = {
                        viewModel.startGameFromEditedState()
                        resetBoard = true
                    }
                ) { viewModel.clearBoard() }
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
                    viewModel.updateAIEnabled(!vmAIEnabled)
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
            }

            val sidebarGameState = SidebarGameState(
                gameState = vmGameState,
                playerSide = vmPlayerSide,
                currentMoveIndex = vmMoveIndex,
                moveHistory = vmHistory.map { it.first },
                difficulty = vmDifficulty,
                isAIEnabled = vmAIEnabled
            )

            Sidebar(
                modifier = Modifier.systemBarsPadding(),
                gameState = sidebarGameState,
                uiState = sidebarUIState,
                events = sidebarEvents,
                onUIStateChange = { newState -> sidebarUIState = newState }
            )
        }
    ) {
        Scaffold(
            topBar = { TaratiTopBar(scope, drawerState, vmIsEditing) }
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
                    if (!isLandscapeScreen && !vmIsEditing) {
                        LocalizedText(
                            id = R.string.a_board_game_by_george_spencer_brown,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )
                    }

                    CreateBoard(
                        modifier = Modifier.weight(1f),
                        state = CreateBoardState(
                            gameState = vmGameState,
                            lastMove = lastMove,
                            playerSide = vmPlayerSide,
                            isLandscapeScreen = isLandscapeScreen,
                            isEditing = vmIsEditing,
                            isAIThinking = isAIThinking(),
                            editBoardOrientation = vmEditBoardOrientation,
                            resetBoard = resetBoard,
                            labelsVisible = vmLabelsVisible
                        ),
                        events = object : BoardEvents {
                            override fun onMove(from: String, to: String) = applyMove(from, to)
                            override fun onEditPiece(from: String) = editPiece(from)
                            override fun onResetCompleted() = resetBoardCompleted()
                        },
                        content = { EditControls(isLandscapeScreen) },
                        debug = debug
                    )
                }
            }
        }
    }
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
    val isLandscapeScreen: Boolean,
    val isEditing: Boolean,
    val isAIThinking: Boolean,
    val editBoardOrientation: BoardOrientation,
    val resetBoard: Boolean,
    val labelsVisible: Boolean
)

@Composable
fun CreateBoard(
    modifier: Modifier = Modifier,
    state: CreateBoardState,
    events: BoardEvents,
    content: @Composable () -> Unit,
    debug: Boolean = false,
) {
    // Construir el estado para Board
    val boardState = BoardState(
        gameState = state.gameState,
        lastMove = state.lastMove,
        boardOrientation = if (state.isEditing) {
            state.editBoardOrientation
        } else {
            toBoardOrientation(state.isLandscapeScreen, state.playerSide)
        },
        labelsVisible = state.labelsVisible,
        newGame = state.resetBoard,
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
            state = boardState,
            events = boardEvents,
            debug = debug
        )

        if (state.isEditing) {
            content()
        } else {
            TurnIndicator(
                modifier = Modifier.align(Alignment.TopEnd),
                currentTurn = state.gameState.currentTurn,
                isAIThinking = state.isAIThinking
            )
        }
    }
}

@Composable
fun BottomControls(
    modifier: Modifier = Modifier,
    pieceCounts: PieceCounts,
    isValidDistribution: Boolean,
    isCompletedDistribution: Boolean,
    onRotate: () -> Unit,
    onStartGame: () -> Unit,
    onClearBoard: () -> Unit,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        RotateButton(onRotate)
        StartButtonAndPieceCounter(
            pieceCounts,
            isValidDistribution,
            isCompletedDistribution,
            onStartGame
        )
        ClearBoardButton(onClearBoard)
    }
}

@Composable
fun RightControls(
    modifier: Modifier, pieceCounts: PieceCounts,
    isValidDistribution:
    Boolean,
    isCompletedDistribution: Boolean,
    onRotate: () -> Unit,
    onStartGame: () -> Unit,
    onClearBoard: () -> Unit,
) {
    Column(
        modifier = modifier.padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        RotateBoardButton(onClick = onRotate)
        Spacer(modifier = Modifier.height(16.dp))
        ClearBoardButton(onClick = onClearBoard)
        Spacer(modifier = Modifier.height(16.dp))
        PieceCounter(
            whiteCount = pieceCounts.white,
            blackCount = pieceCounts.black,
            isValid = isValidDistribution
        )
        Spacer(modifier = Modifier.height(16.dp))
        StartGameButton(
            isCompletedDistribution = isCompletedDistribution,
            onClick = onStartGame,
        )
    }
}

@Composable
fun LeftControls(
    modifier: Modifier, vmPlayerSide: Color,
    onPlayerSideToggle: () -> Unit,
    editColor: Color,
    onColorToggle: () -> Unit,
    editTurn: Color,
    onTurnToggle: () -> Unit
) {
    Column(
        modifier = modifier
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        ColorToggleButton(currentColor = editColor, onColorToggle = onColorToggle)
        Spacer(modifier = Modifier.height(16.dp))
        PlayerSideToggleButton(
            playerSide = vmPlayerSide,
            onPlayerSideToggle = onPlayerSideToggle
        )
        Spacer(modifier = Modifier.height(16.dp))
        TurnToggleButton(currentTurn = editTurn, onTurnToggle = onTurnToggle)
    }
}

@Composable
fun TopControls(
    modifier: Modifier = Modifier,
    playerSide: Color,
    onPlayerSideToggle: () -> Unit,
    editColor: Color,
    onColorToggle: () -> Unit,
    editTurn: Color,
    onTurnToggle: () -> Unit
) {
    Row(
        horizontalArrangement = Arrangement.SpaceEvenly,
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        ColorToggleButton(currentColor = editColor, onColorToggle = onColorToggle)
        PlayerSideToggleButton(
            playerSide = playerSide,
            onPlayerSideToggle = onPlayerSideToggle
        )
        TurnToggleButton(currentTurn = editTurn, onTurnToggle = onTurnToggle)
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
    darkTheme: Boolean = false,
    drawerStateValue: DrawerValue = DrawerValue.Closed,
    playerSide: Color = WHITE,
    landScape: Boolean = false,
    isEditing: Boolean = false,
    labelsVisible: Boolean = true,
    debug: Boolean = false,
) {
    TaratiTheme(darkTheme = darkTheme) {
        val drawerState = rememberDrawerState(initialValue = drawerStateValue)
        val scope = rememberCoroutineScope()

        val exampleMoveHistory = listOf(
            Move("C1", "B1"),
            Move("C7", "B4"),
            Move("B1", "A1"),
            Move("B4", "A1")
        )

        // Estado del juego para el preview
        var previewGameState by remember { mutableStateOf(initialGameState()) }
        var currentPlayerSide by remember { mutableStateOf(playerSide) }
        var currentIsEditing by remember { mutableStateOf(isEditing) }
        var currentLabelsVisible by remember { mutableStateOf(labelsVisible) }

        // Estado UI para el Sidebar
        var sidebarUIState by remember { mutableStateOf(SidebarUIState()) }

        // Implementación de eventos para el preview
        val sidebarEvents = object : SidebarEvents {
            override fun onMoveToCurrent() {
                previewGameState = initialGameState()
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
                currentPlayerSide = color
                previewGameState = initialGameState()
                if (debug) println("New game with side: $color")
            }

            override fun onEditBoard() {
                scope.launch { drawerState.close() }
                currentIsEditing = !currentIsEditing
                if (debug) println("Edit board: $currentIsEditing")
            }

            override fun onAboutClick() {
                if (debug) println("About clicked")
            }
        }

        // Crear el estado del juego para el Sidebar
        val sidebarGameState = SidebarGameState(
            gameState = previewGameState,
            playerSide = currentPlayerSide,
            currentMoveIndex = 2,
            moveHistory = exampleMoveHistory,
            difficulty = Difficulty.DEFAULT,
            isAIEnabled = true
        )

        // Estado y eventos para CreateBoard
        val createBoardState = CreateBoardState(
            gameState = previewGameState,
            lastMove = null,
            playerSide = currentPlayerSide,
            isLandscapeScreen = landScape,
            isEditing = currentIsEditing,
            isAIThinking = false,
            editBoardOrientation = toBoardOrientation(landScape, currentPlayerSide),
            resetBoard = false,
            labelsVisible = currentLabelsVisible
        )

        val createBoardEvents = object : BoardEvents {
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

        ModalNavigationDrawer(
            drawerState = drawerState,
            drawerContent = {
                Sidebar(
                    modifier = Modifier.systemBarsPadding(),
                    gameState = sidebarGameState,
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
                        isEditing = currentIsEditing
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
                        if (!landScape && !currentIsEditing) {
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
                            content = {
                                EditControlsPreview(
                                    isLandscapeScreen = landScape,
                                    pieceCounts = PieceCounts(4, 4),
                                    isValidDistribution = true,
                                    isCompletedDistribution = true,
                                    editColor = WHITE,
                                    onEditColorToggle = { },
                                    playerSide = currentPlayerSide,
                                    onPlayerSideToggle = { },
                                    editTurn = WHITE,
                                    onEditTurnToggle = { },
                                    onRotateBoard = { },
                                    onClearBoard = { }
                                ) { }
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
        drawerStateValue = DrawerValue.Open,
    )
}

@Preview(showBackground = true, device = "spec:width=411dp,height=891dp")
@Composable
fun MainScreenPreview_WithDrawer_Portrait_Dark() {
    MainScreenPreviewContent(
        darkTheme = true,
        drawerStateValue = DrawerValue.Open,
    )
}

// Previews Landscape
@Preview(showBackground = true, device = "spec:width=891dp,height=411dp")
@Composable
fun MainScreenPreview_WithDrawer_Landscape() {
    MainScreenPreviewContent(
        drawerStateValue = DrawerValue.Open,
        landScape = true,
    )
}

@Preview(showBackground = true, device = "spec:width=891dp,height=411dp")
@Composable
fun MainScreenPreview_WithDrawer_Landscape_Dark() {
    MainScreenPreviewContent(
        darkTheme = true,
        drawerStateValue = DrawerValue.Open,
        landScape = true,
    )
}

// Previews con drawer cerrado (para ver el contenido principal)
@Preview(showBackground = true, device = "spec:width=411dp,height=891dp")
@Composable
fun MainScreenPreview_DrawerClosed_Portrait() {
    MainScreenPreviewContent(
        isEditing = true,
    )
}

// Preview adicional: Juego en progreso con más movimientos
@Preview(showBackground = true, device = "spec:width=411dp,height=891dp")
@Composable
fun MainScreenPreview_GameInProgress() {
    MainScreenPreviewContent(
        drawerStateValue = DrawerValue.Open,
    )
}

@Preview(showBackground = true, device = "spec:width=891dp,height=411dp")
@Composable
fun MainScreenPreview_DrawerClosed_Landscape() {
    MainScreenPreviewContent(
        landScape = true,
        isEditing = true,
    )
}

@Preview(showBackground = true, device = "spec:width=411dp,height=891dp")
@Composable
fun MainScreenPreview_Editing_Portrait() {
    EditingModePreviewContent(
        darkTheme = false,
        isLandscape = false,
        boardOrientation = BoardOrientation.PORTRAIT_WHITE
    )
}

@Preview(showBackground = true, device = "spec:width=891dp,height=411dp")
@Composable
fun MainScreenPreview_Editing_Landscape() {
    EditingModePreviewContent(
        darkTheme = false,
        isLandscape = true,
        boardOrientation = BoardOrientation.LANDSCAPE_BLACK
    )
}

@Preview(showBackground = true, device = "spec:width=411dp,height=891dp")
@Composable
fun MainScreenPreview_Editing_Portrait_Dark() {
    EditingModePreviewContent(
        darkTheme = true,
        isLandscape = false,
        boardOrientation = BoardOrientation.PORTRAIT_WHITE
    )
}

@Preview(showBackground = true, device = "spec:width=891dp,height=411dp")
@Composable
fun MainScreenPreview_Editing_Landscape_Dark() {
    EditingModePreviewContent(
        darkTheme = true,
        isLandscape = true,
        boardOrientation = BoardOrientation.LANDSCAPE_BLACK
    )
}

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
            isEditing = isEditing,
        )

        // Crear eventos para Board
        val boardEvents = object : BoardEvents {
            override fun onMove(from: String, to: String) {}
            override fun onEditPiece(from: String) {}
            override fun onResetCompleted() {}
        }

        Box(modifier = Modifier.fillMaxSize()) {
            Board(
                modifier = Modifier.fillMaxSize(),
                state = boardState,
                events = boardEvents
            )

            EditControlsPreview(
                isLandscapeScreen = isLandscape,
                pieceCounts = pieceCounts,
                isValidDistribution = isValidDistribution,
                isCompletedDistribution = isCompletedDistribution,
                editColor = editColor,
                onEditColorToggle = { editColor = editColor.opponent() },
                playerSide = playerSide,
                onPlayerSideToggle = { playerSide = playerSide.opponent() },
                editTurn = editTurn,
                onEditTurnToggle = { editTurn = editTurn.opponent() },
                onRotateBoard = { /* No-op en preview */ },
                onClearBoard = { }
            ) { isEditing = false }
        }
    }
}

// Preview básico de controles en portrait
@Preview(showBackground = true, widthDp = 400, heightDp = 200)
@Composable
fun EditControlsPreview_Portrait() {
    TaratiTheme {
        EditControlsPreview(isLandscapeScreen = false)
    }
}

// Preview básico de controles en landscape
@Preview(showBackground = true, widthDp = 800, heightDp = 200)
@Composable
fun EditControlsPreview_Landscape() {
    TaratiTheme {
        EditControlsPreview(isLandscapeScreen = true)
    }
}

// Preview con distribución inválida
@Preview(showBackground = true, widthDp = 400, heightDp = 200)
@Composable
fun EditControlsPreview_InvalidDistribution() {
    TaratiTheme {
        EditControlsPreview(
            isLandscapeScreen = false,
            pieceCounts = PieceCounts(8, 0),
            isValidDistribution = false,
            isCompletedDistribution = false
        )
    }
}

// Preview con distribución completa
@Preview(showBackground = true, widthDp = 400, heightDp = 200)
@Composable
fun EditControlsPreview_CompletedDistribution() {
    TaratiTheme {
        EditControlsPreview(
            isLandscapeScreen = false,
            pieceCounts = PieceCounts(7, 1),
            isValidDistribution = true,
            isCompletedDistribution = true
        )
    }
}

// Preview con colores diferentes
@Preview(showBackground = true, widthDp = 400, heightDp = 200)
@Composable
fun EditControlsPreview_BlackColor() {
    TaratiTheme {
        EditControlsPreview(
            isLandscapeScreen = false,
            editColor = BLACK,
            playerSide = BLACK,
            editTurn = BLACK
        )
    }
}

@Composable
fun EditControlsPreview(
    isLandscapeScreen: Boolean,
    pieceCounts: PieceCounts = PieceCounts(4, 4),
    isValidDistribution: Boolean = true,
    isCompletedDistribution: Boolean = true,
    editColor: Color = WHITE,
    onEditColorToggle: () -> Unit = { },
    playerSide: Color = WHITE,
    onPlayerSideToggle: () -> Unit = { },
    editTurn: Color = WHITE,
    onEditTurnToggle: () -> Unit = { },
    onRotateBoard: () -> Unit = { },
    onClearBoard: () -> Unit = { },
    onStartGame: () -> Unit = { }
) {
    // TODO: Agrupar parámetros a 6 o menos.

    if (isLandscapeScreen) {
        // Landscape: controles a izquierda y derecha
        Box(modifier = Modifier.fillMaxSize()) {
            LeftControls(
                modifier = Modifier.align(CenterStart),
                vmPlayerSide = playerSide,
                onPlayerSideToggle = onPlayerSideToggle,
                editColor = editColor,
                onColorToggle = onEditColorToggle,
                editTurn = editTurn,
                onTurnToggle = onEditTurnToggle
            )

            RightControls(
                modifier = Modifier.align(CenterEnd),
                pieceCounts = pieceCounts,
                isValidDistribution = isValidDistribution,
                isCompletedDistribution = isCompletedDistribution,
                onRotate = onRotateBoard,
                onStartGame = onStartGame,
                onClearBoard = onClearBoard
            )
        }
    } else {
        // Portrait: controles en superior e inferior
        Box(modifier = Modifier.fillMaxSize()) {
            TopControls(
                modifier = Modifier.align(TopCenter),
                playerSide = playerSide,
                onPlayerSideToggle = onPlayerSideToggle,
                editColor = editColor,
                onColorToggle = onEditColorToggle,
                editTurn = editTurn,
                onTurnToggle = onEditTurnToggle
            )

            BottomControls(
                modifier = Modifier.align(BottomCenter),
                pieceCounts = pieceCounts,
                isValidDistribution = isValidDistribution,
                isCompletedDistribution = isCompletedDistribution,
                onRotate = onRotateBoard,
                onStartGame = onStartGame,
                onClearBoard = onClearBoard
            )
        }
    }
}