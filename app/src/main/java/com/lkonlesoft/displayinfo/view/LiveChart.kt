package com.lkonlesoft.displayinfo.view

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp

@Composable
fun LiveChart(
    dataPoints: List<Float>,
    modifier: Modifier = Modifier,
    lineColor: Color = MaterialTheme.colorScheme.primary,
    fillColor: Color = MaterialTheme.colorScheme.primary.copy(alpha = 0.25f),
    gridColor: Color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
) {
    Canvas(modifier = modifier.fillMaxWidth().height(100.dp)) {
        val width = size.width
        val height = size.height

        // Draw horizontal grid lines (0%, 25%, 50%, 75%, 100%)
        val steps = 4
        for (i in 0..steps) {
            val y = height * (i.toFloat() / steps)
            drawLine(
                color = gridColor,
                start = Offset(0f, y),
                end = Offset(width, y),
                strokeWidth = 1.dp.toPx()
            )
        }

        if (dataPoints.isEmpty()) return@Canvas

        val maxPoints = 30f
        val points = if (dataPoints.size > 30) dataPoints.takeLast(30) else dataPoints
        val stepX = if (points.size > 1) width / (maxPoints - 1f) else width

        val path = Path()
        val fillPath = Path()

        points.forEachIndexed { index, value ->
            val normalizedValue = (value / 100f).coerceIn(0f, 100f)
            val x = index * stepX
            val y = height - (normalizedValue * height)

            if (index == 0) {
                path.moveTo(x, y)
                fillPath.moveTo(x, height)
                fillPath.lineTo(x, y)
            } else {
                path.lineTo(x, y)
                fillPath.lineTo(x, y)
            }
        }

        if (points.isNotEmpty()) {
            val lastX = (points.size - 1) * stepX
            fillPath.lineTo(lastX, height)
            fillPath.close()
        }

        // Draw gradient fill under curve
        drawPath(
            path = fillPath,
            brush = Brush.verticalGradient(
                colors = listOf(fillColor, fillColor.copy(alpha = 0.01f)),
                startY = 0f,
                endY = height
            )
        )

        // Draw line curve
        drawPath(
            path = path,
            color = lineColor,
            style = Stroke(
                width = 2.5.dp.toPx(),
                cap = StrokeCap.Round,
                join = StrokeJoin.Round
            )
        )
    }
}
