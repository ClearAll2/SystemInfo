package com.lkonlesoft.displayinfo.view.test.touch

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.roundToInt

@Composable
fun TouchTestCanvas(
    mode: TouchTestMode,
    density: GridDensity,
    activeTouches: List<TouchPoint>,
    trails: List<PointerTrail>,
    touchedGridCells: Set<Pair<Int, Int>>,
    maxTouches: Int,
    sampleRateHz: Int,
    onPointerEvent: (List<TouchPoint>) -> Unit
) {
    val textMeasurer = rememberTextMeasurer()
    Canvas(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                awaitPointerEventScope {
                    while (true) {
                        val event = awaitPointerEvent(PointerEventPass.Main)
                        val pressedChanges = event.changes.filter { it.pressed }
                        
                        if (pressedChanges.isEmpty()) {
                            onPointerEvent(emptyList())
                        } else {
                            val newTouches = pressedChanges.map { change ->
                                TouchPoint(
                                    id = change.id.value,
                                    position = change.position,
                                    pressure = change.pressure.coerceIn(0.1f, 1.0f),
                                    color = getPointerColor(change.id.value)
                                )
                            }
                            onPointerEvent(newTouches)
                        }
                    }
                }
            }
    ) {
        val width = size.width
        val height = size.height
        // 1. Draw Grid Coverage Overlay if in GRID_COVERAGE mode
        if (mode == TouchTestMode.GRID_COVERAGE) {
            drawGridOverlay(width, height, density, touchedGridCells)
        }
        // 2. Draw Finger Trails
        drawTrails(trails)
        // 3. Draw Pressure Rings & Touch Targets for Active Pointers
        drawActivePointers(activeTouches)
        // 4. Draw Center Multi-Touch Stats (Active Count, Max Count, Sample Rate)
        drawCenterStats(
            textMeasurer = textMeasurer,
            activeCount = activeTouches.size,
            maxCount = maxTouches,
            sampleRateHz = sampleRateHz,
            gridCoveragePercent = if (mode == TouchTestMode.GRID_COVERAGE) {
                val totalCells = density.cols * density.rows
                if (totalCells > 0) (touchedGridCells.size * 100) / totalCells else 0
            } else null
        )
        // 5. Draw Pointer Tooltips next to each touch point
        if (mode == TouchTestMode.TRAILS_AND_INFO) {
            drawPointerTooltips(textMeasurer, activeTouches)
        }
    }
}

private fun DrawScope.drawGridOverlay(
    width: Float,
    height: Float,
    density: GridDensity,
    touchedCells: Set<Pair<Int, Int>>
) {
    val cellWidth = width / density.cols
    val cellHeight = height / density.rows
    // Draw touched grid cells
    for (col in 0 until density.cols) {
        for (row in 0 until density.rows) {
            val isTouched = touchedCells.contains(Pair(col, row))
            val rectOffset = Offset(col * cellWidth, row * cellHeight)
            val rectSize = Size(cellWidth, cellHeight)
            if (isTouched) {
                drawRect(
                    color = Color(0x664CAF50), // Semi-transparent green
                    topLeft = rectOffset,
                    size = rectSize
                )
            }
            drawRect(
                color = Color(0x33FFFFFF), // Subtle grid line
                topLeft = rectOffset,
                size = rectSize,
                style = Stroke(width = 1.dp.toPx())
            )
        }
    }
}

private fun DrawScope.drawTrails(trails: List<PointerTrail>) {
    for (trail in trails) {
        if (trail.points.size > 1) {
            val path = Path().apply {
                moveTo(trail.points.first().x, trail.points.first().y)
                for (i in 1 until trail.points.size) {
                    lineTo(trail.points[i].x, trail.points[i].y)
                }
            }
            drawPath(
                path = path,
                color = trail.color.copy(alpha = 0.8f),
                style = Stroke(width = 4.dp.toPx(), cap = StrokeCap.Round)
            )
        }
    }
}

private fun DrawScope.drawActivePointers(activeTouches: List<TouchPoint>) {
    for (touch in activeTouches) {
        val baseRadius = 40.dp.toPx()
        val pressureRadius = baseRadius * (0.8f + touch.pressure * 0.8f)
        // Outer glowing pressure ring
        drawCircle(
            color = touch.color.copy(alpha = 0.25f),
            radius = pressureRadius * 1.5f,
            center = touch.position
        )
        // Concentric pressure stroke ring
        drawCircle(
            color = touch.color,
            radius = pressureRadius,
            center = touch.position,
            style = Stroke(width = 3.dp.toPx())
        )
        // Center solid core
        drawCircle(
            color = touch.color,
            radius = 10.dp.toPx(),
            center = touch.position
        )
        // Crosshairs
        val crossHairSize = 24.dp.toPx()
        drawLine(
            color = Color.White.copy(alpha = 0.8f),
            start = Offset(touch.position.x - crossHairSize, touch.position.y),
            end = Offset(touch.position.x + crossHairSize, touch.position.y),
            strokeWidth = 2.dp.toPx()
        )
        drawLine(
            color = Color.White.copy(alpha = 0.8f),
            start = Offset(touch.position.x, touch.position.y - crossHairSize),
            end = Offset(touch.position.x, touch.position.y + crossHairSize),
            strokeWidth = 2.dp.toPx()
        )
    }
}

private fun DrawScope.drawCenterStats(
    textMeasurer: TextMeasurer,
    activeCount: Int,
    maxCount: Int,
    sampleRateHz: Int,
    gridCoveragePercent: Int?
) {
    val center = Offset(size.width / 2f, size.height / 2f)
    val countText = "$activeCount"
    val countStyle = TextStyle(
        color = Color.White.copy(alpha = 0.85f),
        fontSize = 72.sp,
        fontWeight = FontWeight.ExtraBold,
        fontFamily = FontFamily.Monospace
    )
    val countLayout = textMeasurer.measure(countText, countStyle)
    // Center Big Active Count
    drawText(
        textLayoutResult = countLayout,
        topLeft = Offset(
            center.x - countLayout.size.width / 2f,
            center.y - countLayout.size.height / 2f - 20.dp.toPx()
        )
    )
    // Subtitle Stats (Max Touches, Sample Rate, Coverage)
    val subText = StringBuilder("MAX: $maxCount | RATE: ${sampleRateHz}Hz").apply {
        if (gridCoveragePercent != null) {
            append(" | COVERAGE: $gridCoveragePercent%")
        }
    }.toString()
    val subStyle = TextStyle(
        color = Color.White.copy(alpha = 0.65f),
        fontSize = 14.sp,
        fontWeight = FontWeight.Medium,
        fontFamily = FontFamily.Monospace
    )
    val subLayout = textMeasurer.measure(subText, subStyle)
    drawText(
        textLayoutResult = subLayout,
        topLeft = Offset(
            center.x - subLayout.size.width / 2f,
            center.y + countLayout.size.height / 2f - 10.dp.toPx()
        )
    )
}

private fun DrawScope.drawPointerTooltips(
    textMeasurer: TextMeasurer,
    activeTouches: List<TouchPoint>
) {
    for (touch in activeTouches) {
        val infoText = buildString {
            append("P${touch.id}: (${touch.position.x.roundToInt()}, ${touch.position.y.roundToInt()}) ")
            append("P:${(touch.pressure * 100).roundToInt()}%")
        }
        val style = TextStyle(
            color = Color.White,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace
        )
        val layout = textMeasurer.measure(infoText, style)

        val tooltipOffset = Offset(
            x = (touch.position.x + 45.dp.toPx()).coerceAtMost(size.width - layout.size.width - 16f),
            y = (touch.position.y - 30.dp.toPx()).coerceAtLeast(16f)
        )
        // Tooltip Background Card
        drawRoundRect(
            color = Color.Black.copy(alpha = 0.7f),
            topLeft = Offset(tooltipOffset.x - 8f, tooltipOffset.y - 4f),
            size = Size(layout.size.width + 16f, layout.size.height + 8f),
            cornerRadius = CornerRadius(8f, 8f)
        )
        drawText(
            textLayoutResult = layout,
            topLeft = tooltipOffset
        )
    }
}
