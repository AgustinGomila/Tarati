package com.agustin.tarati.ui.theme

import androidx.compose.ui.graphics.Color

interface BoardPalette {
    val name: String

    val blackCobBorderColor: Color
    val blackCobColor: Color
    val boardBackground: Color
    val boardEdgeColor: Color
    val boardPerimeterColor: Color
    val boardPatternBorderColor: Color
    val boardPatternColor1: Color
    val boardPatternColor2: Color
    val boardPatternColor3: Color
    val boardVertexColor: Color
    val neutralColor: Color
    val selectionIndicatorColor: Color
    val textColor: Color
    val vertexAdjacentColor: Color
    val vertexOccupiedColor: Color
    val vertexSelectedColor: Color
    val whiteCobBorderColor: Color
    val whiteCobColor: Color

    val highlightEdgeFirst: Color
    val highlightEdgeSecond: Color
    val highlightEdgeThird: Color

    val highlightVertexCapture1Color: Color
    val highlightVertexCapture2Color: Color
    val highlightVertexCapture3Color: Color
    val highlightVertexAdjacent1Color: Color
    val highlightVertexAdjacent2Color: Color
    val highlightVertexAdjacent3Color: Color
    val highlightVertexUpgrade1Color: Color
    val highlightVertexUpgrade2Color: Color
    val highlightVertexUpgrade3Color: Color

    val highlightRegionFirst: Color
    val highlightRegionSecond: Color
    val highlightRegionThird: Color
}

object ClassicPalette : BoardPalette {
    override val name = "Classic"

    override val blackCobBorderColor = Color(0xFFCBBFBF)
    override val blackCobColor = Color(0xFF181717)
    override val boardBackground = Color(0xFF867567)
    override val boardEdgeColor = Color(0xFF57381A)
    override val boardPerimeterColor = Color(0xFF968271)
    override val boardPatternBorderColor = Color(0xFF654321)
    override val boardPatternColor1 = Color(0xFF8B4513)
    override val boardPatternColor2 = Color(0xFFC2945A)
    override val boardPatternColor3 = Color(0xFFECD2A5)
    override val boardVertexColor = Color(0xFF382617)
    override val neutralColor = Color(0xFFB75B3E)
    override val selectionIndicatorColor = Color(0x662196F3)
    override val textColor = Color(0xFF212121)
    override val vertexAdjacentColor = Color(0xFFC9AD58)
    override val vertexOccupiedColor = Color(0xFF333333)
    override val vertexSelectedColor = Color(0xFF2196F3)
    override val whiteCobBorderColor = Color(0xFF2F2C2C)
    override val whiteCobColor = Color(0xFFDED7D3)

    override val highlightEdgeFirst = Color(0xFFDED760)
    override val highlightEdgeSecond = Color(0xFFE8E191)
    override val highlightEdgeThird = Color(0xFFD4C93A)

    override val highlightVertexCapture1Color = Color(0xFF36D8F4)
    override val highlightVertexCapture2Color = Color(0xFF6BF9E4)
    override val highlightVertexCapture3Color = Color(0xFF0FC8E0)
    override val highlightVertexAdjacent1Color = Color(0xFFC2884A)
    override val highlightVertexAdjacent2Color = Color(0xFFBF9B6F)
    override val highlightVertexAdjacent3Color = Color(0xFFC7A43A)
    override val highlightVertexUpgrade1Color = Color(0xFFEF7C2F)
    override val highlightVertexUpgrade2Color = Color(0xFFF9AD6B)
    override val highlightVertexUpgrade3Color = Color(0xFFE0710F)

    override val highlightRegionFirst = Color(0xFFEFD47C)
    override val highlightRegionSecond = Color(0xFFE5D8AE)
    override val highlightRegionThird = Color(0xFFE9C44A)
}

object DarkPalette : BoardPalette {
    override val name = "Dark"

    override val blackCobBorderColor = Color(0xFFA59EAF)
    override val blackCobColor = Color(0xFF192454)
    override val boardBackground = Color(0xFF756F98)
    override val boardEdgeColor = Color(0xFF503375)
    override val boardPerimeterColor = Color(0xFF8477A8)
    override val boardPatternBorderColor = Color(0xFF1DB2A5)
    override val boardPatternColor1 = Color(0xFF422791)
    override val boardPatternColor2 = Color(0xFF7133AF)
    override val boardPatternColor3 = Color(0xFF7446B6)
    override val boardVertexColor = Color(0xFF333757)
    override val neutralColor = Color(0xFF4F7C52)
    override val selectionIndicatorColor = Color(0x66CF6679)
    override val textColor = Color(0xFFFFFFFF)
    override val vertexAdjacentColor = Color(0xFFC9589C)
    override val vertexOccupiedColor = Color(0xFF16B6A7)
    override val vertexSelectedColor = Color(0xFFD3AA2E)
    override val whiteCobBorderColor = Color(0xFF382750)
    override val whiteCobColor = Color(0xFFA496C0)

    override val highlightEdgeFirst = Color(0xFFA05DE0)
    override val highlightEdgeSecond = Color(0xFFBE96D7)
    override val highlightEdgeThird = Color(0xFFA070DA)

    override val highlightVertexCapture1Color = Color(0xFFE736F4)
    override val highlightVertexCapture2Color = Color(0xFFB26BF9)
    override val highlightVertexCapture3Color = Color(0xFFA10FE0)
    override val highlightVertexAdjacent1Color = Color(0xFFCF6679)
    override val highlightVertexAdjacent2Color = Color(0xFFE0919F)
    override val highlightVertexAdjacent3Color = Color(0xFFB5475E)
    override val highlightVertexUpgrade1Color = Color(0xFF85F436)
    override val highlightVertexUpgrade2Color = Color(0xFF90E074)
    override val highlightVertexUpgrade3Color = Color(0xFF0FE032)

    override val highlightRegionFirst = Color(0xFF03DAC6)
    override val highlightRegionSecond = Color(0xFF66FFF0)
    override val highlightRegionThird = Color(0xFF00B3A1)
}

object NaturePalette : BoardPalette {
    override val name = "Nature"

    override val blackCobBorderColor = Color(0xFF42322A)
    override val blackCobColor = Color(0xFF193341)
    override val boardBackground = Color(0xFF5C8498)
    override val boardEdgeColor = Color(0xFF704F45)
    override val boardPerimeterColor = Color(0xFF5B9597)
    override val boardPatternBorderColor = Color(0xFF325921)
    override val boardPatternColor1 = Color(0xFF4C6432)
    override val boardPatternColor2 = Color(0xFFADBD9B)
    override val boardPatternColor3 = Color(0xFF759A59)
    override val boardVertexColor = Color(0xFF2E404E)
    override val neutralColor = Color(0xFFC9A030)
    override val selectionIndicatorColor = Color(0x66F57C00)
    override val textColor = Color(0xFF216E27)
    override val vertexAdjacentColor = Color(0xFF58C9A9)
    override val vertexOccupiedColor = Color(0xFF1C5B20)
    override val vertexSelectedColor = Color(0xFFF57C00)
    override val whiteCobBorderColor = Color(0xFF4D3528)
    override val whiteCobColor = Color(0xFFDEC8A5)

    override val highlightEdgeFirst = Color(0xFFF3A621)
    override val highlightEdgeSecond = Color(0xFFF6BC5F)
    override val highlightEdgeThird = Color(0xFFE0920A)

    override val highlightVertexCapture1Color = Color(0xFFEEF436)
    override val highlightVertexCapture2Color = Color(0xFFDAF96B)
    override val highlightVertexCapture3Color = Color(0xFF89E00F)
    override val highlightVertexAdjacent1Color = Color(0xFF36B8F4)
    override val highlightVertexAdjacent2Color = Color(0xFF6BCAF9)
    override val highlightVertexAdjacent3Color = Color(0xFF0F9DE0)
    override val highlightVertexUpgrade1Color = Color(0xFFF4B536)
    override val highlightVertexUpgrade2Color = Color(0xFFF9A46B)
    override val highlightVertexUpgrade3Color = Color(0xFFE0630F)

    override val highlightRegionFirst = Color(0xFF48754D)
    override val highlightRegionSecond = Color(0xFF5E9664)
    override val highlightRegionThird = Color(0xFF365A3A)
}

val availablePalettes = listOf(
    ClassicPalette,
    DarkPalette,
    NaturePalette
)