package com.agustin.tarati.game.logic

import org.junit.Assert
import org.junit.Test

class BoardOrientationTest {

    @Test
    fun normalizedBoard_rotate_portraitWhite() {
        val board = NormalizedBoard(0.5f, 0.5f)
        val rotated = board.rotate(BoardOrientation.PORTRAIT_WHITE)

        Assert.assertEquals(0.5f, rotated.x, 0.01f)
        Assert.assertEquals(0.5f, rotated.y, 0.01f)
    }

    @Test
    fun normalizedBoard_rotate_portraitBlack() {
        val board = NormalizedBoard(0.5f, 0.5f)
        val rotated = board.rotate(BoardOrientation.PORTRAIT_BLACK)

        Assert.assertEquals(0.5f, rotated.x, 0.01f)
        Assert.assertEquals(0.5f, rotated.y, 0.01f)
    }

    @Test
    fun normalizedBoard_rotate_landscapeWhite() {
        val board = NormalizedBoard(0.5f, 0.5f)
        val rotated = board.rotate(BoardOrientation.LANDSCAPE_WHITE)

        Assert.assertEquals(0.5f, rotated.x, 0.01f)
        Assert.assertEquals(0.5f, rotated.y, 0.01f)
    }

    @Test
    fun normalizedBoard_rotate_landscapeBlack() {
        val board = NormalizedBoard(0.5f, 0.5f)
        val rotated = board.rotate(BoardOrientation.LANDSCAPE_BLACK)

        Assert.assertEquals(0.5f, rotated.x, 0.01f)
        Assert.assertEquals(0.5f, rotated.y, 0.01f)
    }

    @Test
    fun normalizedBoard_rotate_cornerCases() {
        val topLeft = NormalizedBoard(0f, 0f)
        val bottomRight = NormalizedBoard(1f, 1f)

        val topLeftRotated = topLeft.rotate(BoardOrientation.PORTRAIT_BLACK)
        val bottomRightRotated = bottomRight.rotate(BoardOrientation.PORTRAIT_BLACK)

        Assert.assertEquals(1f, topLeftRotated.x, 0.01f)
        Assert.assertEquals(1f, topLeftRotated.y, 0.01f)
        Assert.assertEquals(0f, bottomRightRotated.x, 0.01f)
        Assert.assertEquals(0f, bottomRightRotated.y, 0.01f)
    }
}