package com.lkonlesoft.displayinfo.view.test

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.rememberLazyStaggeredGridState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.lkonlesoft.displayinfo.R
import com.lkonlesoft.displayinfo.view.staggeredHeader
import kotlin.math.atan2
import kotlin.math.sqrt

data class AccelSample(
    val x: Float,
    val y: Float,
    val z: Float
)

@Composable
fun AccelerometerTestScreen(
    paddingValues: PaddingValues
) {
    val context = LocalContext.current
    val layoutDirection = LocalLayoutDirection.current
    val state = rememberLazyStaggeredGridState()
    val width = LocalWindowInfo.current.containerDpSize.width
    val sensorManager = remember { context.getSystemService(Context.SENSOR_SERVICE) as SensorManager }
    val accelerometer = remember { sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) }
    var rawX by remember { mutableFloatStateOf(0f) }
    var rawY by remember { mutableFloatStateOf(0f) }
    var rawZ by remember { mutableFloatStateOf(0f) }
    var offsetX by remember { mutableFloatStateOf(0f) }
    var offsetY by remember { mutableFloatStateOf(0f) }
    var offsetZ by remember { mutableFloatStateOf(0f) }
    var minX by remember { mutableFloatStateOf(Float.MAX_VALUE) }
    var maxX by remember { mutableFloatStateOf(-Float.MAX_VALUE) }
    var minY by remember { mutableFloatStateOf(Float.MAX_VALUE) }
    var maxY by remember { mutableFloatStateOf(-Float.MAX_VALUE) }
    var minZ by remember { mutableFloatStateOf(Float.MAX_VALUE) }
    var maxZ by remember { mutableFloatStateOf(-Float.MAX_VALUE) }
    val history = remember { mutableStateListOf<AccelSample>() }

    DisposableEffect(accelerometer) {
        if (accelerometer == null) return@DisposableEffect onDispose {}
        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent?) {
                if (event == null || event.sensor.type != Sensor.TYPE_ACCELEROMETER) return

                val x = event.values[0]
                val y = event.values[1]
                val z = event.values[2]

                rawX = x
                rawY = y
                rawZ = z

                val calX = x - offsetX
                val calY = y - offsetY
                val calZ = z - offsetZ

                if (calX < minX) minX = calX
                if (calX > maxX) maxX = calX
                if (calY < minY) minY = calY
                if (calY > maxY) maxY = calY
                if (calZ < minZ) minZ = calZ
                if (calZ > maxZ) maxZ = calZ

                if (history.size >= 60) {
                    history.removeAt(0)
                }
                history.add(AccelSample(calX, calY, calZ))
            }
            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
        }
        sensorManager.registerListener(listener, accelerometer, SensorManager.SENSOR_DELAY_GAME)
        onDispose {
            sensorManager.unregisterListener(listener)
        }
    }
    val calX = rawX - offsetX
    val calY = rawY - offsetY
    val calZ = rawZ - offsetZ
    val magnitude = sqrt(calX * calX + calY * calY + calZ * calZ)
    val gForce = magnitude / SensorManager.GRAVITY_EARTH
    val pitch = (atan2(calY.toDouble(), sqrt((calX * calX + calZ * calZ).toDouble())) * (180.0 / Math.PI)).toFloat()
    val roll = (atan2(-calX.toDouble(), calZ.toDouble()) * (180.0 / Math.PI)).toFloat()
    val isLevel = sqrt(pitch * pitch + roll * roll) < 1.5f

    if (accelerometer == null) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .fillMaxSize()
                .padding(top = paddingValues.calculateTopPadding())
        ) {
            Text(
                text = stringResource(id = R.string.accelerometer_not_found),
                color = MaterialTheme.colorScheme.onErrorContainer,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(16.dp)
                    .background(color = MaterialTheme.colorScheme.errorContainer, shape = RoundedCornerShape(16.dp))
                    .padding(12.dp),
                textAlign = TextAlign.Center
            )
        }
    }
    else {
        LazyVerticalStaggeredGrid(
            state = state,
            columns = StaggeredGridCells.Adaptive(300.dp),
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    top = paddingValues.calculateTopPadding(),
                    start = paddingValues.calculateStartPadding(layoutDirection),
                    end = paddingValues.calculateEndPadding(layoutDirection)
                )
                .padding(horizontal = 20.dp)
                .clip(shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)),
            contentPadding = PaddingValues(bottom = paddingValues.calculateBottomPadding()),
            verticalItemSpacing = 16.dp,
            horizontalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            staggeredHeader {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Spacer(modifier = Modifier.height(24.dp))
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.surfaceBright,
                        shadowElevation = 6.dp,
                        modifier = Modifier.size(200.dp)
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.fillMaxSize(if (width < 600.dp) 1f else 0.7f)
                        ) {
                            Canvas(modifier = Modifier.fillMaxSize()) {
                                val center = Offset(size.width / 2, size.height / 2)
                                val maxRadius = (size.width / 2) - 16.dp.toPx()

                                // Target Outer & Inner Concentric Rings
                                drawCircle(
                                    color = if (isLevel) Color(0xFF4CAF50) else Color.LightGray.copy(alpha = 0.5f),
                                    radius = maxRadius,
                                    style = Stroke(width = 3.dp.toPx())
                                )
                                drawCircle(
                                    color = if (isLevel) Color(0xFF4CAF50) else Color.LightGray.copy(alpha = 0.3f),
                                    radius = maxRadius * 0.4f,
                                    style = Stroke(width = 2.dp.toPx())
                                )

                                // Crosshair lines
                                drawLine(
                                    color = Color.LightGray.copy(alpha = 0.4f),
                                    start = Offset(center.x - maxRadius, center.y),
                                    end = Offset(center.x + maxRadius, center.y),
                                    strokeWidth = 1.dp.toPx()
                                )
                                drawLine(
                                    color = Color.LightGray.copy(alpha = 0.4f),
                                    start = Offset(center.x, center.y - maxRadius),
                                    end = Offset(center.x, center.y + maxRadius),
                                    strokeWidth = 1.dp.toPx()
                                )

                                // Dynamic Bubble
                                val normalizedRoll = (roll / 45f).coerceIn(-1f, 1f)
                                val normalizedPitch = (pitch / 45f).coerceIn(-1f, 1f)

                                val bubbleX = center.x + (normalizedRoll * maxRadius * 0.8f)
                                val bubbleY = center.y - (normalizedPitch * maxRadius * 0.8f)

                                drawCircle(
                                    color = if (isLevel) Color(0xFF4CAF50) else Color(0xFFFF9800),
                                    radius = 18.dp.toPx(),
                                    center = Offset(bubbleX, bubbleY)
                                )
                            }

                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.padding(top = 100.dp)
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (isLevel) Color(0xFF4CAF50) else MaterialTheme.colorScheme.primaryContainer
                                ) {
                                    Text(
                                        text = if (isLevel) stringResource(R.string.level).uppercase() else stringResource(id = R.string.accelerometer_level),
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isLevel) Color.White else MaterialTheme.colorScheme.onPrimaryContainer,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(if (width < 600.dp) 1f else 0.7f),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceBright),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(
                                modifier = Modifier.fillMaxSize().padding(12.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(text = stringResource(R.string.pitch), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "%.1f°".format(pitch),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceBright),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(
                                modifier = Modifier.fillMaxSize().padding(12.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(text = stringResource(R.string.roll), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "%.1f°".format(roll),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceBright),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(
                                modifier = Modifier.fillMaxSize().padding(12.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(text = stringResource(R.string.net_g), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "%.2fg".format(gForce),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
            }
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceBright),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = stringResource(R.string.axis_acceleration),
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        val axes = listOf(
                            Triple(R.string.axis_x, calX, Color(0xFFE53935)),
                            Triple(R.string.axis_y, calY, Color(0xFF43A047)),
                            Triple(R.string.axis_z, calZ, Color(0xFF1E88E5))
                        )

                        axes.forEach { (name, value, color) ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(12.dp)
                                            .clip(CircleShape)
                                            .background(color)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = stringResource(name),
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                                Text(
                                    text = "%+.2f m/s²".format(value),
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = color
                                )
                            }
                        }
                    }
                }
            }
            item {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceBright),
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = stringResource(R.string.live_motion_signals),
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.height(12.dp))

                            Canvas(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(140.dp)
                                    .background(
                                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                                        RoundedCornerShape(12.dp)
                                    )
                                    .padding(12.dp)
                            ) {
                                if (history.size < 2)
                                    return@Canvas
                                val width = size.width
                                val height = size.height
                                val centerY = height / 2f
                                val maxRange = 20f
                                fun drawAxisPath(selector: (AccelSample) -> Float, color: Color) {
                                    val path = Path()
                                    history.forEachIndexed { index, sample ->
                                        val x = index * (width / (history.size - 1).coerceAtLeast(1))
                                        val valY = selector(sample).coerceIn(-maxRange, maxRange)
                                        val y = centerY - (valY / maxRange * centerY)
                                        if (index == 0) path.moveTo(x, y) else path.lineTo(x, y)
                                    }
                                    drawPath(path = path, color = color, style = Stroke(width = 2.dp.toPx()))
                                }
                                // Center zero line
                                drawLine(
                                    color = Color.Gray.copy(alpha = 0.3f),
                                    start = Offset(0f, centerY),
                                    end = Offset(width, centerY),
                                    strokeWidth = 1.dp.toPx()
                                )

                                drawAxisPath({ it.x }, Color(0xFFE53935))
                                drawAxisPath({ it.y }, Color(0xFF43A047))
                                drawAxisPath({ it.z }, Color(0xFF1E88E5))
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Button(
                            onClick = {
                                offsetX = rawX
                                offsetY = rawY
                                offsetZ = rawZ - SensorManager.GRAVITY_EARTH
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(text = stringResource(id = R.string.accelerometer_calibrate))
                        }

                        OutlinedButton(
                            onClick = {
                                offsetX = 0f
                                offsetY = 0f
                                offsetZ = 0f
                                minX = Float.MAX_VALUE
                                maxX = -Float.MAX_VALUE
                                minY = Float.MAX_VALUE
                                maxY = -Float.MAX_VALUE
                                minZ = Float.MAX_VALUE
                                maxZ = -Float.MAX_VALUE
                            },
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(text = stringResource(id = R.string.accelerometer_reset))
                        }
                    }
                }
            }
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceBright),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = stringResource(R.string.hardware),
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        val details = listOf(
                            R.string.name to accelerometer.name,
                            R.string.vendor to accelerometer.vendor,
                            R.string.power to "%.2f mA".format(accelerometer.power),
                            R.string.max_range to "%.2f m/s²".format(accelerometer.maximumRange),
                            R.string.resolution to "%.4f m/s²".format(accelerometer.resolution)
                        )

                        details.forEach { (label, value) ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = stringResource(label),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = value,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
