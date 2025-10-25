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

    val highlightEdge1Color: Color
    val highlightEdge2Color: Color
    val highlightEdge3Color: Color

    val highlightVertexCapture1Color: Color
    val highlightVertexCapture2Color: Color
    val highlightVertexCapture3Color: Color
    val highlightVertexAdjacent1Color: Color
    val highlightVertexAdjacent2Color: Color
    val highlightVertexAdjacent3Color: Color
    val highlightVertexUpgrade1Color: Color
    val highlightVertexUpgrade2Color: Color
    val highlightVertexUpgrade3Color: Color

    val highlightRegion1Color: Color
    val highlightRegion2Color: Color
    val highlightRegion3Color: Color
}

object ClassicPalette : BoardPalette {
    override val name = "Classic"

    override val blackCobBorderColor = Color(0xFFCBBFBF)
    override val blackCobColor = Color(0xFF181717)
    override val boardBackground = Color(0xFF867567)
    override val boardEdgeColor = Color(0xFF57381A)
    override val boardPerimeterColor = Color(0xFF968271)
    override val boardPatternBorderColor = Color(0xFF654321)
    override val boardPatternColor1 = Color(0xFFC5915B)
    override val boardPatternColor2 = Color(0xFFD5A76A)
    override val boardPatternColor3 = Color(0xFFECD2A5)
    override val boardVertexColor = Color(0xFF382617)
    override val neutralColor = Color(0xFFB75B3E)
    override val selectionIndicatorColor = Color(0x801894F6)
    override val textColor = Color(0xFF212121)
    override val vertexAdjacentColor = Color(0xFFC9AD58)
    override val vertexOccupiedColor = Color(0xFF333333)
    override val vertexSelectedColor = Color(0xFF2196F3)
    override val whiteCobBorderColor = Color(0xFF2F2C2C)
    override val whiteCobColor = Color(0xFFDED7D3)

    override val highlightEdge1Color = Color(0xFF19ADF3)
    override val highlightEdge2Color = Color(0xFFAB63DC)
    override val highlightEdge3Color = Color(0xFF0AE0A3)

    override val highlightVertexCapture1Color = Color(0xFF36D8F4)
    override val highlightVertexCapture2Color = Color(0xFF6BF9E4)
    override val highlightVertexCapture3Color = Color(0xFF0FC8E0)
    override val highlightVertexAdjacent1Color = Color(0xFFC2884A)
    override val highlightVertexAdjacent2Color = Color(0xFFBF9B6F)
    override val highlightVertexAdjacent3Color = Color(0xFFC7A43A)
    override val highlightVertexUpgrade1Color = Color(0xFFEF7C2F)
    override val highlightVertexUpgrade2Color = Color(0xFFF9AD6B)
    override val highlightVertexUpgrade3Color = Color(0xFFE0710F)

    override val highlightRegion1Color = Color(0xFFEFD47C)
    override val highlightRegion2Color = Color(0xFFECE2C2)
    override val highlightRegion3Color = Color(0xFFDCB73D)
}

object DarkPalette : BoardPalette {
    override val name = "Dark"

    override val blackCobBorderColor = Color(0xFFA59EAF)
    override val blackCobColor = Color(0xFF192454)
    override val boardBackground = Color(0xFF756F98)
    override val boardEdgeColor = Color(0xFF503375)
    override val boardPerimeterColor = Color(0xFF8E7EBB)
    override val boardPatternBorderColor = Color(0xFF1DB2A5)
    override val boardPatternColor1 = Color(0xFF6848C5)
    override val boardPatternColor2 = Color(0xFF7647C2)
    override val boardPatternColor3 = Color(0xFF8456C5)
    override val boardVertexColor = Color(0xFF333757)
    override val neutralColor = Color(0xFF4F7C52)
    override val selectionIndicatorColor = Color(0x80EC5B75)
    override val textColor = Color(0xFFFFFFFF)
    override val vertexAdjacentColor = Color(0xFFC9589C)
    override val vertexOccupiedColor = Color(0xFF16B6A7)
    override val vertexSelectedColor = Color(0xFFD3AA2E)
    override val whiteCobBorderColor = Color(0xFF382750)
    override val whiteCobColor = Color(0xFFA496C0)

    override val highlightEdge1Color = Color(0xFFCD57FF)
    override val highlightEdge2Color = Color(0xFF8DC46E)
    override val highlightEdge3Color = Color(0xFFD53E3E)

    override val highlightVertexCapture1Color = Color(0xFFE736F4)
    override val highlightVertexCapture2Color = Color(0xFFB26BF9)
    override val highlightVertexCapture3Color = Color(0xFFA10FE0)
    override val highlightVertexAdjacent1Color = Color(0xFFCF6679)
    override val highlightVertexAdjacent2Color = Color(0xFFE0919F)
    override val highlightVertexAdjacent3Color = Color(0xFFB5475E)
    override val highlightVertexUpgrade1Color = Color(0xFF85F436)
    override val highlightVertexUpgrade2Color = Color(0xFF90E074)
    override val highlightVertexUpgrade3Color = Color(0xFF0FE032)

    override val highlightRegion1Color = Color(0xFF03DAC6)
    override val highlightRegion2Color = Color(0xFF66FFF0)
    override val highlightRegion3Color = Color(0xFF00B3A1)
}

object NaturePalette : BoardPalette {
    override val name = "Nature"

    override val blackCobBorderColor = Color(0xFF42322A)
    override val blackCobColor = Color(0xFF193341)
    override val boardBackground = Color(0xFF5C8498)
    override val boardEdgeColor = Color(0xFF704F45)
    override val boardPerimeterColor = Color(0xFF5D999B)
    override val boardPatternBorderColor = Color(0xFF325921)
    override val boardPatternColor1 = Color(0xFF6D914E)
    override val boardPatternColor2 = Color(0xFF7CA25E)
    override val boardPatternColor3 = Color(0xFFACBD99)
    override val boardVertexColor = Color(0xFF2E404E)
    override val neutralColor = Color(0xFFC9A030)
    override val selectionIndicatorColor = Color(0x80E58A12)
    override val textColor = Color(0xFF216E27)
    override val vertexAdjacentColor = Color(0xFF58C9A9)
    override val vertexOccupiedColor = Color(0xFF1C5B20)
    override val vertexSelectedColor = Color(0xFFF57C00)
    override val whiteCobBorderColor = Color(0xFF4D3528)
    override val whiteCobColor = Color(0xFFDEC8A5)

    override val highlightEdge1Color = Color(0xFFF33939)
    override val highlightEdge2Color = Color(0xFFCAE099)
    override val highlightEdge3Color = Color(0xFF25BED2)

    override val highlightVertexCapture1Color = Color(0xFFEEF436)
    override val highlightVertexCapture2Color = Color(0xFFDAF96B)
    override val highlightVertexCapture3Color = Color(0xFF89E00F)
    override val highlightVertexAdjacent1Color = Color(0xFF36B8F4)
    override val highlightVertexAdjacent2Color = Color(0xFF6BCAF9)
    override val highlightVertexAdjacent3Color = Color(0xFF0F9DE0)
    override val highlightVertexUpgrade1Color = Color(0xFFF4B536)
    override val highlightVertexUpgrade2Color = Color(0xFFF9A46B)
    override val highlightVertexUpgrade3Color = Color(0xFFE0630F)

    override val highlightRegion1Color = Color(0xFFAD4E7F)
    override val highlightRegion2Color = Color(0xFFE363DD)
    override val highlightRegion3Color = Color(0xFFCB488C)
}

val availablePalettes = listOf(
    ClassicPalette,
    DarkPalette,
    NaturePalette
)