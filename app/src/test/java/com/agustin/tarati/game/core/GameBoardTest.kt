package com.agustin.tarati.game.core

import com.agustin.tarati.game.core.Color.BLACK
import com.agustin.tarati.game.core.Color.WHITE
import com.agustin.tarati.game.core.GameBoard.getVisualPosition
import com.agustin.tarati.game.logic.BoardOrientation
import org.junit.Assert
import org.junit.Test

class GameBoardTest {

    @Test
    fun adjacencyMap_containsAllVertices() {
        GameBoard.vertices.forEach { vertex ->
            Assert.assertTrue(
                "Adjacency map should contain all vertices",
                GameBoard.adjacencyMap.containsKey(vertex)
            )
        }
    }

    @Test
    fun adjacencyMap_hasBidirectionalConnections() {
        GameBoard.edges.forEach { (from, to) ->
            Assert.assertTrue(
                "$from should connect to $to",
                GameBoard.adjacencyMap[from]?.contains(to) == true
            )
            Assert.assertTrue(
                "$to should connect to $from",
                GameBoard.adjacencyMap[to]?.contains(from) == true
            )
        }
    }

    @Test
    fun homeBases_containCorrectVertices() {
        val whiteHome = GameBoard.homeBases[WHITE]!!
        val blackHome = GameBoard.homeBases[BLACK]!!

        Assert.assertEquals("White home should have 4 vertices", 4, whiteHome.size)
        Assert.assertEquals("Black home should have 4 vertices", 4, blackHome.size)

        Assert.assertTrue("White home should contain C1", whiteHome.contains("C1"))
        Assert.assertTrue("White home should contain C2", whiteHome.contains("C2"))
        Assert.assertTrue("Black home should contain C7", blackHome.contains("C7"))
        Assert.assertTrue("Black home should contain C8", blackHome.contains("C8"))
    }

    @Test
    fun getVisualPosition_returnsCorrectPosition() {
        val position = getVisualPosition(
            "A1",
            500f,
            500f,
            BoardOrientation.PORTRAIT_WHITE
        )

        Assert.assertTrue(
            "Position should be within canvas bounds",
            position.x in 0f..500f
        )
        Assert.assertTrue(
            "Position should be within canvas bounds",
            position.y in 0f..500f
        )
    }

    @Test
    fun findClosestVertex_findsNearbyVertex() {
        // Test with coordinates close to a known vertex position
        val vertex = GameBoard.findClosestVertex(
            androidx.compose.ui.geometry.Offset(250f, 250f),
            500f,
            500f,
            50f,
            BoardOrientation.PORTRAIT_WHITE
        )

        Assert.assertNotNull("Should find a vertex for nearby tap", vertex)
        Assert.assertTrue(
            "Found vertex should be in vertices list",
            GameBoard.vertices.contains(vertex)
        )
    }

    @Test
    fun findClosestVertex_tooFar_returnsNull() {
        val vertex = GameBoard.findClosestVertex(
            androidx.compose.ui.geometry.Offset(10f, 10f),
            500f,
            500f,
            5f, // Very small max distance
            BoardOrientation.PORTRAIT_WHITE
        )

        Assert.assertNull("Should return null when no vertex is close enough", vertex)
    }
}