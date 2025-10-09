package com.agustin.tarati.ui.components.board

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class BoardViewModel : ViewModel() {
    private val _selectedPiece = MutableStateFlow(null as String?)
    private val _highlightedMoves = MutableStateFlow(listOf<String>())

    val selectedPiece: StateFlow<String?> = _selectedPiece.asStateFlow()
    val highlightedMoves: StateFlow<List<String>> = _highlightedMoves.asStateFlow()

    fun updateSelectedPiece(newPiece: String?) {
        _selectedPiece.value = newPiece
    }

    fun updateHighlightedMoves(newMoves: List<String>) {
        _highlightedMoves.value = newMoves
    }
}