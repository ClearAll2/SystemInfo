package com.lkonlesoft.displayinfo.view.test.touch

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color

enum class TouchTestMode {
    TRAILS_AND_INFO,
    GRID_COVERAGE
}

enum class GridDensity(val cols: Int, val rows: Int, val label: String) {
    LOW(5, 10, "5x10"),
    MEDIUM(10, 20, "10x20"),
    HIGH(20, 40, "20x40")
}

data class TouchPoint(
    val id: Long,
    val position: Offset,
    val pressure: Float,
    val color: Color
)

data class PointerTrail(
    val id: Long,
    val color: Color,
    val points: List<Offset>
)

val pointerColors = listOf(
    Color(0xFFFF5252), // Red
    Color(0xFF448AFF), // Blue
    Color(0xFF69F0AE), // Green
    Color(0xFFFFD740), // Yellow
    Color(0xFFE040FB), // Magenta
    Color(0xFF1DE9B6), // Teal
    Color(0xFFFF6E40), // Orange
    Color(0xFF7C4DFF), // Purple
    Color(0xFFEEFF41), // Lime
    Color(0xFFFF4081)  // Pink
)

fun getPointerColor(id: Long): Color {
    val index = (id.toInt() and 0x7FFFFFFF) % pointerColors.size
    return pointerColors[index]
}
