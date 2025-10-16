package com.agustin.tarati.ui.components.board

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class BoardViewModel : ViewModel() {
    
    private val _selectedPiece = MutableStateFlow(null as String?)
    private val _validMoves = MutableStateFlow(listOf<String>())

    val selectedPiece: StateFlow<String?> = _selectedPiece.asStateFlow()
    val validMoves: StateFlow<List<String>> = _validMoves.asStateFlow()

    fun updateSelectedPiece(newPiece: String?) {
        _selectedPiece.value = newPiece
    }

    fun updateValidMoves(newMoves: List<String>) {
        _validMoves.value = newMoves
    }

    fun resetSelection() {
        _selectedPiece.value = null
        _validMoves.value = emptyList()
    }
}