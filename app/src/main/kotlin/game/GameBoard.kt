package com.agustin.tarati.game

enum class Color { WHITE, BLACK }

object GameBoard {
    val vertices: List<String> = listOf(
        "A1",
        "B1", "B2", "B3", "B4", "B5", "B6",
        "C1", "C2", "C3", "C4", "C5", "C6", "C7", "C8", "C9", "C10", "C11", "C12",
        "D1", "D2", "D3", "D4"
    )

    val edges: List<Pair<String, String>> = listOf(
        // Home base White
        "D1" to "D2", "D1" to "C1", "D2" to "C2",
        // Home base Black
        "D3" to "D4", "D3" to "C7", "D4" to "C8",
        // C circumference
        "C1" to "C2", "C2" to "C3", "C3" to "C4", "C4" to "C5", "C5" to "C6",
        "C6" to "C7", "C7" to "C8", "C8" to "C9", "C9" to "C10", "C10" to "C11",
        "C11" to "C12", "C12" to "C1",
        // B boundary
        "B1" to "B2", "B2" to "B3", "B3" to "B4", "B4" to "B5", "B5" to "B6", "B6" to "B1",
        // C to B
        "C1" to "B1", "C2" to "B1",
        "C3" to "B2", "C4" to "B2",
        "C5" to "B3", "C6" to "B3",
        "C7" to "B4", "C8" to "B4",
        "C9" to "B5", "C10" to "B5",
        "C11" to "B6", "C12" to "B6",
        // B to A
        "B1" to "A1", "B2" to "A1", "B3" to "A1", "B4" to "A1", "B5" to "A1", "B6" to "A1"
    )

    val homeBases: Map<Color, List<String>> = mapOf(
        Color.WHITE to listOf("C1", "C2", "D1", "D2"),
        Color.BLACK to listOf("C7", "C8", "D3", "D4")
    )

    // Mapa de adyacencia optimizado
    val adjacencyMap: Map<String, List<String>> by lazy {
        val map = mutableMapOf<String, MutableList<String>>()

        // Inicializar con todos los vértices
        vertices.forEach { vertex ->
            map[vertex] = mutableListOf()
        }

        // Llenar con conexiones
        edges.forEach { (from, to) ->
            map[from]?.add(to)
            map[to]?.add(from)
        }

        map.toMap()
    }
}