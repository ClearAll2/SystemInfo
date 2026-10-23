package com.lkonlesoft.displayinfo.view.test

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.rememberLazyStaggeredGridState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.lkonlesoft.displayinfo.R
import com.lkonlesoft.displayinfo.view.CommonSwitchOption
import com.lkonlesoft.displayinfo.view.staggeredHeader
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class ProximityLogEvent(
    val time: String,
    val isNear: Boolean,
    val distance: Float
)

@Composable
fun ProximityTestScreen(
    paddingValues: PaddingValues
) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val layoutDirection = LocalLayoutDirection.current
    val state = rememberLazyStaggeredGridState()
    val width = LocalWindowInfo.current.containerDpSize.width
    val sensorManager = remember { context.getSystemService(Context.SENSOR_SERVICE) as SensorManager }
    val proximitySensor = remember { sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY) }
    var distance by remember { mutableFloatStateOf(proximitySensor?.maximumRange ?: 5f) }
    var isNear by remember { mutableStateOf(false) }
    var nearCount by rememberSaveable { mutableIntStateOf(0) }
    var farCount by rememberSaveable { mutableIntStateOf(0) }
    var soundHapticsEnabled by rememberSaveable { mutableStateOf(true) }
    val logs = remember { mutableStateListOf<ProximityLogEvent>() }
    val timeFormat = remember { SimpleDateFormat("HH:mm:ss", Locale.getDefault()) }

    DisposableEffect(proximitySensor) {
        if (proximitySensor == null) return@DisposableEffect onDispose {}
        val maxRange = proximitySensor.maximumRange
        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent?) {
                if (event == null || event.sensor.type != Sensor.TYPE_PROXIMITY) return
                val currentDistance = event.values[0]
                val newlyNear = currentDistance < maxRange && currentDistance < 5f

                if (newlyNear != isNear) {
                    isNear = newlyNear
                    if (newlyNear) {
                        nearCount++
                    } else {
                        farCount++
                    }

                    if (soundHapticsEnabled) {
                        haptic.performHapticFeedback(
                            if (newlyNear) HapticFeedbackType.LongPress else HapticFeedbackType.TextHandleMove
                        )
                    }

                    val logEntry = ProximityLogEvent(
                        time = timeFormat.format(Date()),
                        isNear = newlyNear,
                        distance = currentDistance
                    )
                    if (logs.size >= 20) {
                        logs.removeAt(logs.lastIndex)
                    }
                    logs.add(0, logEntry)
                }

                distance = currentDistance
            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
        }
        sensorManager.registerListener(listener, proximitySensor, SensorManager.SENSOR_DELAY_UI)
        onDispose {
            sensorManager.unregisterListener(listener)
        }
    }

    val boxColor by animateColorAsState(
        targetValue = if (isNear) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
        animationSpec = tween(250),
        label = "prox_color"
    )

    if (proximitySensor == null) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .fillMaxSize()
                .padding(top = paddingValues.calculateTopPadding())
        ) {
            Text(
                text = stringResource(id = R.string.proximity_not_found),
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
                .clip(shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)),
            contentPadding = PaddingValues(bottom = paddingValues.calculateBottomPadding()),
            verticalItemSpacing = 16.dp,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            staggeredHeader {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Spacer(modifier = Modifier.height(24.dp))
                    Surface(
                        shape = RoundedCornerShape(24.dp),
                        color = boxColor,
                        shadowElevation = if (isNear) 12.dp else 4.dp,
                        modifier = Modifier
                            .fillMaxWidth(if (width < 600.dp) 1f else 0.7f)
                            .height(180.dp)
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.fillMaxSize()
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    painter = painterResource(id = R.drawable.radar_24px),
                                    contentDescription = "Proximity Radar",
                                    tint = MaterialTheme.colorScheme.onPrimary,
                                    modifier = Modifier.size(48.dp)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = if (isNear) stringResource(id = R.string.near) else stringResource(id = R.string.far),
                                    style = MaterialTheme.typography.headlineLarge,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MaterialTheme.colorScheme.onPrimary
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = stringResource(id = R.string.distance, distance),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.85f)
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(20.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(if (width < 600.dp) 1f else 0.7f),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceBright),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(
                                modifier = Modifier.fillMaxSize().padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = stringResource(R.string.near),
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.error
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = nearCount.toString(),
                                    style = MaterialTheme.typography.headlineMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.error
                                )
                            }
                        }

                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceBright),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(
                                modifier = Modifier.fillMaxSize().padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = stringResource(R.string.far),
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = farCount.toString(),
                                    style = MaterialTheme.typography.headlineMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
            }
            item {
                CommonSwitchOption(
                    text = R.string.haptic_toggle,
                    subText = -1,
                    extra = "",
                    checked = soundHapticsEnabled,
                    onClick = {
                        soundHapticsEnabled = !soundHapticsEnabled
                    },
                    onSwitch = {
                        soundHapticsEnabled = it
                    },
                    isFirst = true,
                    isLast = true
                )
            }
            if (logs.isNotEmpty()) {
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceBright),
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = stringResource(id = R.string.event_log),
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.height(8.dp))

                            logs.take(5).forEach { log ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = log.time,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.outline
                                    )
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = if (log.isNear) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.primaryContainer
                                    ) {
                                        Text(
                                            text = "${if (log.isNear) "NEAR" else "FAR"} (%.1f cm)".format(log.distance),
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = if (log.isNear) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onPrimaryContainer,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                        )
                                    }
                                }
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
                            text = stringResource(R.string.hardware),
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        val details = listOf(
                            R.string.name to proximitySensor.name,
                            R.string.vendor to proximitySensor.vendor,
                            R.string.power to "%.2f mA".format(proximitySensor.power),
                            R.string.max_range to "%.1f cm".format(proximitySensor.maximumRange)
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
