package com.agustin.tarati.ui.components.board.draw.previews

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.agustin.tarati.game.logic.BoardOrientation
import com.agustin.tarati.ui.components.board.animation.HighlightAction
import com.agustin.tarati.ui.components.board.animation.VertexHighlight
import com.agustin.tarati.ui.components.board.draw.drawVertexHighlight
import com.agustin.tarati.ui.components.board.draw.drawVertices
import com.agustin.tarati.ui.theme.BoardColors
import com.agustin.tarati.ui.theme.ClassicPalette
import com.agustin.tarati.ui.theme.DarkPalette
import com.agustin.tarati.ui.theme.NaturePalette
import com.agustin.tarati.ui.theme.TaratiTheme


@Preview(group = "Vertices", showBackground = true, widthDp = 300, heightDp = 500)
@Composable
fun PreviewDrawVerticesWithLabels(
    boardColors: BoardColors = getBoardColors(ClassicPalette),
    boardOrientation: BoardOrientation = BoardOrientation.PORTRAIT_WHITE
) {
    TaratiTheme {
        Surface(
            modifier = Modifier.size(300.dp),
            color = MaterialTheme.colorScheme.background
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
            ) {
                Canvas(modifier = Modifier.matchParentSize()) {
                    drawVertices(
                        canvasSize = size,
                        vWidth = 60f,
                        selectedVertexId = "A1",
                        adjacentVertexes = listOf("B2", "C3"),
                        boardState = PreviewStates.populatedBoardState.copy(
                            boardVisualState = PreviewStates.populatedBoardState.boardVisualState.copy(
                                labelsVisibles = true,
                                verticesVisibles = true,
                            ),
                            boardOrientation = boardOrientation
                        ),
                        colors = boardColors
                    )
                }
                Text(
                    text = "Vértices con Etiquetas",
                    modifier = Modifier.align(Alignment.TopCenter),
                    style = MaterialTheme.typography.labelSmall
                )
            }
        }
    }
}

@Preview(group = "Vertices", showBackground = true, widthDp = 300, heightDp = 500)
@Composable
fun PreviewDrawVerticesNoLabels(
    boardColors: BoardColors = getBoardColors(NaturePalette),
    boardOrientation: BoardOrientation = BoardOrientation.PORTRAIT_WHITE
) {
    TaratiTheme {
        Surface(
            modifier = Modifier.size(300.dp),
            color = MaterialTheme.colorScheme.background
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
            ) {
                Canvas(modifier = Modifier.matchParentSize()) {
                    drawVertices(
                        canvasSize = size,
                        vWidth = 60f,
                        selectedVertexId = null,
                        adjacentVertexes = emptyList(),
                        boardState = PreviewStates.emptyBoardState.copy(
                            boardVisualState = PreviewStates.emptyBoardState.boardVisualState.copy(
                                labelsVisibles = false,
                                verticesVisibles = true
                            ),
                            boardOrientation = boardOrientation
                        ),
                        colors = boardColors
                    )
                }
                Text(
                    text = "Vértices sin Etiquetas",
                    modifier = Modifier.align(Alignment.TopCenter),
                    style = MaterialTheme.typography.labelSmall
                )
            }
        }
    }
}

@Preview(group = "Regions", showBackground = true, widthDp = 400, heightDp = 200)
@Composable
fun PreviewVertexHighlightsNature(
    boardColors: BoardColors = getBoardColors(NaturePalette),
    boardOrientation: BoardOrientation = BoardOrientation.PORTRAIT_WHITE
) {
    PreviewVertexHighlights(boardColors, boardOrientation)
}

@Preview(group = "Regions", showBackground = true, widthDp = 400, heightDp = 200)
@Composable
fun PreviewVertexHighlightsDark(
    boardColors: BoardColors = getBoardColors(DarkPalette),
    boardOrientation: BoardOrientation = BoardOrientation.PORTRAIT_WHITE
) {
    PreviewVertexHighlights(boardColors, boardOrientation)
}

@Preview(group = "Regions", showBackground = true, widthDp = 400, heightDp = 200)
@Composable
fun PreviewVertexHighlightsClassic(
    boardColors: BoardColors = getBoardColors(ClassicPalette),
    boardOrientation: BoardOrientation = BoardOrientation.PORTRAIT_WHITE
) {
    PreviewVertexHighlights(boardColors, boardOrientation)
}

@Composable
fun PreviewVertexHighlights(
    boardColors: BoardColors = getBoardColors(ClassicPalette),
    boardOrientation: BoardOrientation = BoardOrientation.PORTRAIT_WHITE
) {
    TaratiTheme {
        Column {
            Text(
                text = "Resaltados de Vértices",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Row {
                // Captura
                Surface(
                    modifier = Modifier.size(120.dp),
                    color = MaterialTheme.colorScheme.surface
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp)
                    ) {
                        Canvas(modifier = Modifier.matchParentSize()) {
                            drawVertexHighlight(
                                highlight = VertexHighlight(
                                    vertexId = "A1",
                                    action = HighlightAction.CAPTURE,
                                    pulse = true
                                ),
                                canvasSize = size,
                                orientation = boardOrientation,
                                colors = boardColors
                            )
                        }
                        Text(
                            text = "Captura",
                            modifier = Modifier.align(Alignment.TopCenter),
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                }

                // Upgrade
                Surface(
                    modifier = Modifier.size(120.dp),
                    color = MaterialTheme.colorScheme.surface
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp)
                    ) {
                        Canvas(modifier = Modifier.matchParentSize()) {
                            drawVertexHighlight(
                                highlight = VertexHighlight(
                                    vertexId = "B2",
                                    action = HighlightAction.UPGRADE,
                                    pulse = true
                                ),
                                canvasSize = size,
                                orientation = boardOrientation,
                                colors = boardColors
                            )
                        }
                        Text(
                            text = "Upgrade",
                            modifier = Modifier.align(Alignment.TopCenter),
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                }

                // Movimiento normal
                Surface(
                    modifier = Modifier.size(120.dp),
                    color = MaterialTheme.colorScheme.surface
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp)
                    ) {
                        Canvas(modifier = Modifier.matchParentSize()) {
                            drawVertexHighlight(
                                highlight = VertexHighlight(
                                    vertexId = "C3",
                                    action = HighlightAction.MOVE,
                                    pulse = false
                                ),
                                canvasSize = size,
                                orientation = boardOrientation,
                                colors = boardColors
                            )
                        }
                        Text(
                            text = "Movimiento",
                            modifier = Modifier.align(Alignment.TopCenter),
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                }
            }
        }
    }
}