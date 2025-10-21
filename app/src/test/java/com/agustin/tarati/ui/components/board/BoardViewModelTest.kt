package com.agustin.tarati.ui.components.board

import com.agustin.tarati.ui.components.board.behaviors.BoardSelectionViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class BoardViewModelTest {

    @Test
    fun initialState_hasNullSelectedVertex() {
        val viewModel = BoardSelectionViewModel()
        assertNull("Initial selected piece should be null", viewModel.selectedVertexId.value)
    }

    @Test
    fun initialState_hasEmptyValidAdjacentVertexes() {
        val viewModel = BoardSelectionViewModel()
        assertTrue(
            "Initial highlighted moves should be empty",
            viewModel.validAdjacentVertexes.value.isEmpty()
        )
    }

    @Test
    fun updateSelectedVertex_setsNewValue() {
        val viewModel = BoardSelectionViewModel()

        viewModel.updateSelectedVertex("C1")
        assertEquals("Selected piece should be C1", "C1", viewModel.selectedVertexId.value)

        viewModel.updateSelectedVertex("B2")
        assertEquals("Selected piece should be B2", "B2", viewModel.selectedVertexId.value)
    }

    @Test
    fun updateSelectedVertex_withNull_clearsSelection() {
        val viewModel = BoardSelectionViewModel()
        viewModel.updateSelectedVertex("C1")

        viewModel.updateSelectedVertex(null)
        assertNull(
            "Selected piece should be null after setting to null",
            viewModel.selectedVertexId.value
        )
    }

    @Test
    fun updateValidAdjacentVertexes_setsNewValidVertexes() {
        val viewModel = BoardSelectionViewModel()
        val moves = listOf("C2", "B1", "A1")

        viewModel.updateValidAdjacentVertexes(moves)
        assertEquals(
            "Highlighted moves should match input",
            moves, viewModel.validAdjacentVertexes.value
        )
    }

    @Test
    fun updateValidAdjacentVertexes_withEmptyList_clearsValidVertexes() {
        val viewModel = BoardSelectionViewModel()
        viewModel.updateValidAdjacentVertexes(listOf("C2", "B1"))

        viewModel.updateValidAdjacentVertexes(emptyList())
        assertTrue(
            "Highlighted moves should be empty",
            viewModel.validAdjacentVertexes.value.isEmpty()
        )
    }

    @Test
    fun resetSelection_clearsBothProperties() {
        val viewModel = BoardSelectionViewModel()
        viewModel.updateSelectedVertex("C1")
        viewModel.updateValidAdjacentVertexes(listOf("C2", "B1"))

        viewModel.resetSelection()

        assertNull(
            "Selected piece should be null after reset",
            viewModel.selectedVertexId.value
        )
        assertTrue(
            "Highlighted moves should be empty after reset",
            viewModel.validAdjacentVertexes.value.isEmpty()
        )
    }

    @Test
    fun stateFlow_emitsUpdates() {
        val viewModel = BoardSelectionViewModel()
        val selectedPieceValues = mutableListOf<String?>()
        val highlightedMovesValues = mutableListOf<List<String>>()

        // Collect initial values
        selectedPieceValues.add(viewModel.selectedVertexId.value)
        highlightedMovesValues.add(viewModel.validAdjacentVertexes.value)

        // Update and collect new values
        viewModel.updateSelectedVertex("C1")
        viewModel.updateValidAdjacentVertexes(listOf("C2"))

        selectedPieceValues.add(viewModel.selectedVertexId.value)
        highlightedMovesValues.add(viewModel.validAdjacentVertexes.value)

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