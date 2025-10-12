package com.agustin.tarati.ui.components.board

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class BoardViewModelTest {

    @Test
    fun initialState_hasNullSelectedPiece() {
        val viewModel = BoardViewModel()
        assertNull("Initial selected piece should be null", viewModel.selectedPiece.value)
    }

    @Test
    fun initialState_hasEmptyHighlightedMoves() {
        val viewModel = BoardViewModel()
        assertTrue(
            "Initial highlighted moves should be empty",
            viewModel.validMoves.value.isEmpty()
        )
    }

    @Test
    fun updateSelectedPiece_setsNewValue() {
        val viewModel = BoardViewModel()

        viewModel.updateSelectedPiece("C1")
        assertEquals("Selected piece should be C1", "C1", viewModel.selectedPiece.value)

        viewModel.updateSelectedPiece("B2")
        assertEquals("Selected piece should be B2", "B2", viewModel.selectedPiece.value)
    }

    @Test
    fun updateSelectedPiece_withNull_clearsSelection() {
        val viewModel = BoardViewModel()
        viewModel.updateSelectedPiece("C1")

        viewModel.updateSelectedPiece(null)
        assertNull(
            "Selected piece should be null after setting to null",
            viewModel.selectedPiece.value
        )
    }

    @Test
    fun updateValidMoves_setsNewMoves() {
        val viewModel = BoardViewModel()
        val moves = listOf("C2", "B1", "A1")

        viewModel.updateValidMoves(moves)
        assertEquals(
            "Highlighted moves should match input",
            moves, viewModel.validMoves.value
        )
    }

    @Test
    fun updateValidMoves_withEmptyList_clearsMoves() {
        val viewModel = BoardViewModel()
        viewModel.updateValidMoves(listOf("C2", "B1"))

        viewModel.updateValidMoves(emptyList())
        assertTrue(
            "Highlighted moves should be empty",
            viewModel.validMoves.value.isEmpty()
        )
    }

    @Test
    fun resetSelection_clearsBothProperties() {
        val viewModel = BoardViewModel()
        viewModel.updateSelectedPiece("C1")
        viewModel.updateValidMoves(listOf("C2", "B1"))

        viewModel.resetSelection()

        assertNull(
            "Selected piece should be null after reset",
            viewModel.selectedPiece.value
        )
        assertTrue(
            "Highlighted moves should be empty after reset",
            viewModel.validMoves.value.isEmpty()
        )
    }

    @Test
    fun stateFlow_emitsUpdates() {
        val viewModel = BoardViewModel()
        val selectedPieceValues = mutableListOf<String?>()
        val highlightedMovesValues = mutableListOf<List<String>>()

        // Collect initial values
        selectedPieceValues.add(viewModel.selectedPiece.value)
        highlightedMovesValues.add(viewModel.validMoves.value)

        // Update and collect new values
        viewModel.updateSelectedPiece("C1")
        viewModel.updateValidMoves(listOf("C2"))

        selectedPieceValues.add(viewModel.selectedPiece.value)
        highlightedMovesValues.add(viewModel.validMoves.value)

        // Verify state changes
        assertNull("First selected piece should be null", selectedPieceValues[0])
        assertEquals("Second selected piece should be C1", "C1", selectedPieceValues[1])

        assertTrue(
            "First highlighted moves should be empty",
            highlightedMovesValues[0].isEmpty()
        )
        assertEquals(
            "Second highlighted moves should have one item",
            listOf("C2"), highlightedMovesValues[1]
        )
    }
}