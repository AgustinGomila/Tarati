package com.agustin.tarati.ui.components.board.behaviors

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class BoardSelectionViewModel : ViewModel() {

    private val _selectedVertexId = MutableStateFlow(null as String?)
    private val _validAdjacentVertexes = MutableStateFlow(listOf<String>())

    val selectedVertexId: StateFlow<String?> = _selectedVertexId.asStateFlow()
    val validAdjacentVertexes: StateFlow<List<String>> = _validAdjacentVertexes.asStateFlow()

    fun updateSelectedVertex(newPiece: String?) {
        _selectedVertexId.value = newPiece
    }

    fun updateValidAdjacentVertexes(newMoves: List<String>) {
        _validAdjacentVertexes.value = newMoves
    }

    fun resetSelection() {
        _selectedVertexId.value = null
        _validAdjacentVertexes.value = emptyList()
    }
}