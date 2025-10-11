package  com.agustin.tarati.game.logic

import com.agustin.tarati.game.core.GameBoard
import org.junit.Assert
import org.junit.Test

class BoardVisualPositionTest {

    @Test
    fun getVisualPosition_handlesExtendedCoordinates() {
        val canvasWidth = 500f
        val canvasHeight = 500f
        val orientation = BoardOrientation.PORTRAIT_WHITE

        // Verificar que todas las posiciones visuales sean calculadas correctamente
        // incluso para bases con coordenadas extendidas
        GameBoard.vertices.forEach { vertex ->
            val visualPosition = GameBoard.getVisualPosition(
                vertex,
                canvasWidth,
                canvasHeight,
                orientation
            )

            // Las bases pueden estar fuera del área central, pero deberían ser posiciones válidas
            Assert.assertFalse("Vertex $vertex X should not be NaN", visualPosition.x.isNaN())
            Assert.assertFalse("Vertex $vertex Y should not be NaN", visualPosition.y.isNaN())
            Assert.assertFalse("Vertex $vertex X should not be infinite", visualPosition.x.isInfinite())
            Assert.assertFalse("Vertex $vertex Y should not be infinite", visualPosition.y.isInfinite())

            // Aunque algunas bases puedan estar fuera del canvas, deberían ser posiciones razonables
            val reasonableRange = -100f..(canvasWidth + 100f)
            Assert.assertTrue(
                "Vertex $vertex X should be in reasonable range, but was ${visualPosition.x}",
                visualPosition.x in reasonableRange
            )
            Assert.assertTrue(
                "Vertex $vertex Y should be in reasonable range, but was ${visualPosition.y}",
                visualPosition.y in reasonableRange
            )
        }
    }

    @Test
    fun getVisualPositionPortraitWhite_basesPositionedCorrectly() {
        val canvasWidth = 500f
        val canvasHeight = 500f
        val orientation = BoardOrientation.PORTRAIT_WHITE

        val d1 = GameBoard.getVisualPosition("D1", canvasWidth, canvasHeight, orientation)
        val d2 = GameBoard.getVisualPosition("D2", canvasWidth, canvasHeight, orientation)
        val d3 = GameBoard.getVisualPosition("D3", canvasWidth, canvasHeight, orientation)
        val d4 = GameBoard.getVisualPosition("D4", canvasWidth, canvasHeight, orientation)

        // En orientación PORTRAIT_WHITE, D1 y D2 deberían estar abajo, D3 y D4 arriba
        Assert.assertTrue("D1 should be below center", d1.y > canvasHeight / 2)
        Assert.assertTrue("D2 should be below center", d2.y > canvasHeight / 2)
        Assert.assertTrue("D3 should be above center", d3.y < canvasHeight / 2)
        Assert.assertTrue("D4 should be above center", d4.y < canvasHeight / 2)
    }

    @Test
    fun getVisualPositionPortraitBlack_basesPositionedCorrectly() {
        val canvasWidth = 500f
        val canvasHeight = 500f
        val orientation = BoardOrientation.PORTRAIT_BLACK

        val d1 = GameBoard.getVisualPosition("D1", canvasWidth, canvasHeight, orientation)
        val d2 = GameBoard.getVisualPosition("D2", canvasWidth, canvasHeight, orientation)
        val d3 = GameBoard.getVisualPosition("D3", canvasWidth, canvasHeight, orientation)
        val d4 = GameBoard.getVisualPosition("D4", canvasWidth, canvasHeight, orientation)

        // En orientación PORTRAIT_WHITE, D3 y D4 deberían estar abajo, D1 y D2 arriba
        Assert.assertTrue("D1 should be above center", d1.y < canvasHeight / 2)
        Assert.assertTrue("D2 should be above center", d2.y < canvasHeight / 2)
        Assert.assertTrue("D3 should be below center", d3.y > canvasHeight / 2)
        Assert.assertTrue("D4 should be below center", d4.y > canvasHeight / 2)
    }
}