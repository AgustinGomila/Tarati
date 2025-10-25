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
import com.agustin.tarati.ui.components.board.animation.EdgeHighlight
import com.agustin.tarati.ui.components.board.draw.drawEdgeHighlight
import com.agustin.tarati.ui.components.board.draw.drawEdges
import com.agustin.tarati.ui.theme.BoardColors
import com.agustin.tarati.ui.theme.ClassicPalette
import com.agustin.tarati.ui.theme.DarkPalette
import com.agustin.tarati.ui.theme.NaturePalette
import com.agustin.tarati.ui.theme.TaratiTheme

@Preview(group = "Edges", showBackground = true, widthDp = 300, heightDp = 500)
@Composable
fun PreviewDrawEdgesNature(
    boardColors: BoardColors = getBoardColors(NaturePalette),
    boardOrientation: BoardOrientation = BoardOrientation.PORTRAIT_WHITE
) {
    PreviewDrawEdges(boardColors, boardOrientation)
}

@Preview(group = "Edges", showBackground = true, widthDp = 300, heightDp = 500)
@Composable
fun PreviewDrawEdgesDark(
    boardColors: BoardColors = getBoardColors(DarkPalette),
    boardOrientation: BoardOrientation = BoardOrientation.PORTRAIT_WHITE
) {
    PreviewDrawEdges(boardColors, boardOrientation)
}

@Preview(group = "Edges", showBackground = true, widthDp = 300, heightDp = 500)
@Composable
fun PreviewDrawEdgesClassic(
    boardColors: BoardColors = getBoardColors(ClassicPalette),
    boardOrientation: BoardOrientation = BoardOrientation.PORTRAIT_WHITE
) {
    PreviewDrawEdges(boardColors, boardOrientation)
}

@Composable
fun PreviewDrawEdges(
    boardColors: BoardColors = getBoardColors(ClassicPalette),
    boardOrientation: BoardOrientation = BoardOrientation.PORTRAIT_WHITE
) {
    TaratiTheme {
        Surface(
            modifier = Modifier.size(300.dp),
            color = MaterialTheme.colorScheme.background
        ) {
            FullBackground(boardColors = boardColors, boardOrientation = boardOrientation)
            Box(
                modifier = Modifier
                    .fillMaxSize()
            ) {
                Canvas(modifier = Modifier.matchParentSize()) {
                    drawEdges(
                        canvasSize = size,
                        orientation = boardOrientation,
                        boardState = PreviewStates.emptyBoardState.copy(boardOrientation = boardOrientation),
                        colors = boardColors
                    )
                }
                Text(
                    text = "Aristas del Tablero",
                    modifier = Modifier.align(Alignment.TopCenter),
                    style = MaterialTheme.typography.labelSmall
                )
            }
        }
    }
}

@Preview(group = "Edges", showBackground = true)
@Composable
fun PreviewEdgeHighlightsNature(
    boardColors: BoardColors = getBoardColors(DarkPalette),
    boardOrientation: BoardOrientation = BoardOrientation.PORTRAIT_WHITE
) {
    PreviewEdgeHighlights(boardColors, boardOrientation)
}

@Preview(group = "Edges", showBackground = true)
@Composable
fun PreviewEdgeHighlightsDark(
    boardColors: BoardColors = getBoardColors(NaturePalette),
    boardOrientation: BoardOrientation = BoardOrientation.PORTRAIT_WHITE
) {
    PreviewEdgeHighlights(boardColors, boardOrientation)
}

@Preview(group = "Edges", showBackground = true)
@Composable
fun PreviewEdgeHighlightsClassic(
    boardColors: BoardColors = getBoardColors(ClassicPalette),
    boardOrientation: BoardOrientation = BoardOrientation.PORTRAIT_WHITE
) {
    PreviewEdgeHighlights(boardColors, boardOrientation)
}

@Composable
fun PreviewEdgeHighlights(
    boardColors: BoardColors = getBoardColors(ClassicPalette),
    boardOrientation: BoardOrientation = BoardOrientation.PORTRAIT_WHITE
) {
    TaratiTheme {
        Column {
            Text(
                text = "Resaltados de Aristas",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
            Spacer(modifier = Modifier.height(16.dp))

            Row {
                // Highlight con pulso
                Surface(
                    modifier = Modifier.size(150.dp),
                    color = MaterialTheme.colorScheme.surface
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp)
                    ) {
                        Canvas(modifier = Modifier.matchParentSize()) {
                            drawEdgeHighlight(
                                highlight = EdgeHighlight(
                                    from = "B1",
                                    to = "D4",
                                    pulse = true
                                ),
                                canvasSize = size,
                                orientation = boardOrientation,
                                colors = boardColors
                            )
                        }
                        Text(
                            text = "Con Pulso",
                            modifier = Modifier.align(Alignment.TopCenter),
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                }

                // Highlight sin pulso
                Surface(
                    modifier = Modifier.size(150.dp),
                    color = MaterialTheme.colorScheme.surface
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp)
                    ) {
                        Canvas(modifier = Modifier.matchParentSize()) {
                            drawEdgeHighlight(
                                highlight = EdgeHighlight(
                                    from = "C3",
                                    to = "D4",
                                    pulse = false
                                ),
                                canvasSize = size,
                                orientation = boardOrientation,
                                colors = boardColors
                            )
                        }
                        Text(
                            text = "Sin Pulso",
                            modifier = Modifier.align(Alignment.TopCenter),
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                }
            }
        }
    }
}
