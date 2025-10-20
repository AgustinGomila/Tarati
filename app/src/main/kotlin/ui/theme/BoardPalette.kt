package com.agustin.tarati.ui.theme

import androidx.compose.ui.graphics.Color

interface BoardPalette {
    val name: String
    val neutralColor: Color
    val vertexOccupiedColor: Color
    val vertexSelectedColor: Color
    val vertexHighlightColor: Color
    val textColor: Color
    val blackPieceColor: Color
    val whitePieceColor: Color
    val blackPieceBorderColor: Color
    val whitePieceBorderColor: Color
    val selectionIndicatorColor: Color
    val boardVertexColor: Color
    val boardEdgeColor: Color
    val boardBackground: Color
    val boardPatternColor1: Color
    val boardPatternColor2: Color
    val boardPatternColor3: Color
    val boardPatternBorderColor: Color
    val boardGlowColor: Color
}

object ClassicPalette : BoardPalette {
    override val name = "Classic"
    override val neutralColor = Color(0xFFB75B3E)
    override val vertexOccupiedColor = Color(0xFF333333)
    override val vertexSelectedColor = Color(0xFF2196F3)
    override val vertexHighlightColor = Color(0xFFFF9800)
    override val textColor = Color(0xFF212121)
    override val blackPieceColor = Color(0xFF181717)
    override val whitePieceColor = Color(0xFFDED7D3)
    override val blackPieceBorderColor = Color(0xFFCBBFBF)
    override val whitePieceBorderColor = Color(0xFF2F2C2C)
    override val selectionIndicatorColor = Color(0x662196F3)
    override val boardVertexColor = Color(0xFF382617)
    override val boardEdgeColor = Color(0xFF57381A)
    override val boardBackground = Color(0xFF867567)
    override val boardPatternColor1 = Color(0xFF8B4513)
    override val boardPatternColor2 = Color(0xFFC2945A)
    override val boardPatternColor3 = Color(0xFFECD2A5)
    override val boardPatternBorderColor = Color(0xFF654321)
    override val boardGlowColor = Color(0xFF87CEEB)
}

object DarkPalette : BoardPalette {
    override val name = "Dark"
    override val neutralColor = Color(0xFF4F7C52)
    override val vertexOccupiedColor = Color(0xFF16B6A7)
    override val vertexSelectedColor = Color(0xFFD3AA2E)
    override val vertexHighlightColor = Color(0xFF018786)
    override val textColor = Color(0xFFFFFFFF)
    override val blackPieceColor = Color(0xFF192454)
    override val whitePieceColor = Color(0xFFA496C0)
    override val blackPieceBorderColor = Color(0xFFA59EAF)
    override val whitePieceBorderColor = Color(0xFF382750)
    override val selectionIndicatorColor = Color(0x66CF6679)
    override val boardVertexColor = Color(0xFF333757)
    override val boardEdgeColor = Color(0xFF503375)
    override val boardBackground = Color(0xFF756F98)
    override val boardPatternColor1 = Color(0xFF422791)
    override val boardPatternColor2 = Color(0xFF7133AF)
    override val boardPatternColor3 = Color(0xFF7446B6)
    override val boardPatternBorderColor = Color(0xFF1DB2A5)
    override val boardGlowColor = Color(0xFFBB86FC)
}

object NaturePalette : BoardPalette {
    override val name = "Nature"
    override val neutralColor = Color(0xFFC9A030)
    override val vertexOccupiedColor = Color(0xFF1C5B20)
    override val vertexSelectedColor = Color(0xFFF57C00)
    override val vertexHighlightColor = Color(0xFF7E2BA1)
    override val textColor = Color(0xFF216E27)
    override val blackPieceColor = Color(0xFF193341)
    override val whitePieceColor = Color(0xFFDEC8A5)
    override val blackPieceBorderColor = Color(0xFF42322A)
    override val whitePieceBorderColor = Color(0xFF4D3528)
    override val selectionIndicatorColor = Color(0x66F57C00)
    override val boardVertexColor = Color(0xFF2E404E)
    override val boardEdgeColor = Color(0xFF704F45)
    override val boardBackground = Color(0xFF5C8498)
    override val boardPatternColor1 = Color(0xFF4C6432)
    override val boardPatternColor2 = Color(0xFF759A59)
    override val boardPatternColor3 = Color(0xFFADBD9B)
    override val boardPatternBorderColor = Color(0xFF325921)
    override val boardGlowColor = Color(0xFF48754D)
}

val availablePalettes = listOf(
    ClassicPalette,
    DarkPalette,
    NaturePalette
)