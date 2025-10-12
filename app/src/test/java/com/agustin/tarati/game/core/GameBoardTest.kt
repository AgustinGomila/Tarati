package com.agustin.tarati.game.core

import com.agustin.tarati.game.core.Color.BLACK
import com.agustin.tarati.game.core.Color.WHITE
import com.agustin.tarati.game.core.GameBoard.adjacencyMap
import com.agustin.tarati.game.core.GameBoard.edges
import com.agustin.tarati.game.core.GameBoard.getVisualPosition
import com.agustin.tarati.game.core.GameBoard.homeBases
import com.agustin.tarati.game.core.GameBoard.vertices
import com.agustin.tarati.game.logic.BoardOrientation
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class GameBoardTest {

    @Test
    fun adjacencyMap_containsAllVertices() {
        vertices.forEach { vertex ->
            assertTrue(
                "Adjacency map should contain all vertices",
                adjacencyMap.containsKey(vertex)
            )
        }
    }

    @Test
    fun adjacencyMap_hasBidirectionalConnections() {
        edges.forEach { (from, to) ->
            assertTrue(
                "$from should connect to $to",
                adjacencyMap[from]?.contains(to) == true
            )
            assertTrue(
                "$to should connect to $from",
                adjacencyMap[to]?.contains(from) == true
            )
        }
    }

    @Test
    fun homeBases_containCorrectVertices() {
        val whiteHome = homeBases[WHITE]!!
        val blackHome = homeBases[BLACK]!!

        assertEquals("White home should have 4 vertices", 4, whiteHome.size)
        assertEquals("Black home should have 4 vertices", 4, blackHome.size)

        assertTrue("White home should contain C1", whiteHome.contains("C1"))
        assertTrue("White home should contain C2", whiteHome.contains("C2"))
        assertTrue("Black home should contain C7", blackHome.contains("C7"))
        assertTrue("Black home should contain C8", blackHome.contains("C8"))
    }

    @Test
    fun getVisualPosition_returnsCorrectPosition() {
        val position = getVisualPosition(
            "A1",
            500f,
            500f,
            BoardOrientation.PORTRAIT_WHITE
        )

        assertTrue(
            "Position should be within canvas bounds",
            position.x in 0f..500f
        )
        assertTrue(
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

        assertNotNull("Should find a vertex for nearby tap", vertex)
        assertTrue(
            "Found vertex should be in vertices list",
            vertices.contains(vertex)
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

        assertNull("Should return null when no vertex is close enough", vertex)
    }
}