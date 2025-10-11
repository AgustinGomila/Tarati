package com.agustin.tarati.ui.components.board

import com.agustin.tarati.game.ai.TaratiAI.normalizedPositions
import org.junit.Assert
import org.junit.Test

class BoardNormalizationTest {

    @Test
    fun normalizedPositions_mostCoordinatesInValidRange() {
        normalizedPositions.forEach { (vertex, normalizedBoard) ->
            // Las bases D pueden tener coordenadas fuera de [0,1], ya que están fuera del tablero circular
            if (!vertex.startsWith("D")) {
                Assert.assertTrue(
                    "Vertex $vertex X coordinate should be between 0 and 1, but was ${normalizedBoard.x}",
                    normalizedBoard.x in 0f..1f
                )
                Assert.assertTrue(
                    "Vertex $vertex Y coordinate should be between 0 and 1, but was ${normalizedBoard.y}",
                    normalizedBoard.y in 0f..1f
                )
            }
        }
    }

    @Test
    fun normalizedPositions_basesHaveValidExtendedCoordinates() {
        // Verificar que las bases tengan coordenadas consistentes aunque estén fuera de [0,1]
        val d1 = normalizedPositions["D1"]
        val d2 = normalizedPositions["D2"]
        val d3 = normalizedPositions["D3"]
        val d4 = normalizedPositions["D4"]

        Assert.assertNotNull("D1 should exist", d1)
        Assert.assertNotNull("D2 should exist", d2)
        Assert.assertNotNull("D3 should exist", d3)
        Assert.assertNotNull("D4 should exist", d4)

        // D1 y D2 deberían estar en la parte superior (Y > 1)
        Assert.assertTrue("D1 should be above main board", d1!!.y > 1f)
        Assert.assertTrue("D2 should be above main board", d2!!.y > 1f)

        // D3 y D4 deberían estar en la parte inferior (Y < 0)
        Assert.assertTrue("D3 should be below main board", d3!!.y < 0f)
        Assert.assertTrue("D4 should be below main board", d4!!.y < 0f)

        // Todas las bases deberían tener X entre 0 y 1
        Assert.assertTrue("D1 X should be reasonable", d1.x in 0f..1f)
        Assert.assertTrue("D2 X should be reasonable", d2.x in 0f..1f)
        Assert.assertTrue("D3 X should be reasonable", d3.x in 0f..1f)
        Assert.assertTrue("D4 X should be reasonable", d4.x in 0f..1f)
    }

    @Test
    fun normalizedPositions_noNaNOrInfiniteValues() {
        normalizedPositions.forEach { (vertex, normalizedBoard) ->
            Assert.assertFalse("Vertex $vertex X should not be NaN", normalizedBoard.x.isNaN())
            Assert.assertFalse("Vertex $vertex Y should not be NaN", normalizedBoard.y.isNaN())
            Assert.assertFalse("Vertex $vertex X should not be infinite", normalizedBoard.x.isInfinite())
            Assert.assertFalse("Vertex $vertex Y should not be infinite", normalizedBoard.y.isInfinite())
        }
    }

    @Test
    fun normalizedPositions_centerVertexAtCenter() {
        val a1 = normalizedPositions["A1"]
        Assert.assertNotNull("A1 should exist", a1)
        // A1 debería estar cerca del centro del tablero principal
        Assert.assertEquals("A1 X should be approximately 0.5", 0.5f, a1!!.x, 0.01f)
        Assert.assertEquals("A1 Y should be approximately 0.5", 0.5f, a1.y, 0.01f)
    }

    @Test
    fun normalizedPositions_consistentWithBoardGeometry() {
        // Verificar que las posiciones normalizadas mantengan la geometría del tablero
        val a1 = normalizedPositions["A1"]!!
        val b1 = normalizedPositions["B1"]!!
        val c1 = normalizedPositions["C1"]!!
        val d1 = normalizedPositions["D1"]!!
        val d4 = normalizedPositions["D4"]!!
        val d3 = normalizedPositions["D3"]!!

        // A1 debería estar en el centro del tablero principal
        Assert.assertEquals(0.5f, a1.x, 0.1f)
        Assert.assertEquals(0.5f, a1.y, 0.1f)

        // Las bases D1 debería estar arriba del centro
        Assert.assertTrue("D1 should be above center", d1.y > 0.5f)

        // Las bases D3 debería estar abajo del centro
        Assert.assertTrue("D3 should be below center", d3.y < 0.5f)

        // Las bases B1 debería estar arriba del centro
        Assert.assertTrue("B1 should be above center", b1.y > 0.5f)

        // Las bases C1 debería estar arriba del centro
        Assert.assertTrue("C1 should be above center", c1.y > 0.5f)

        // Las bases D4 debería estar abajo del centro
        Assert.assertTrue("D4 should be below center", d4.y < 0.5f)
    }
}