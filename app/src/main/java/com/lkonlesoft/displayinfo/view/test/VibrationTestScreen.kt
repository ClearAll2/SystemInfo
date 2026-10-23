package com.lkonlesoft.displayinfo.view.test

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSliderState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.lkonlesoft.displayinfo.R
import com.lkonlesoft.displayinfo.view.staggeredHeader

@Composable
fun VibrationTestScreen(
    paddingValues: PaddingValues
) {
    val context = LocalContext.current
    val layoutDirection = LocalLayoutDirection.current
    val state = rememberLazyStaggeredGridState()
    val haptics = LocalHapticFeedback.current
    val vibrator = remember {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vibratorManager.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }
    }

    val hasVibrator = remember { vibrator.hasVibrator() }
    val hasAmplitudeControl = remember {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) vibrator.hasAmplitudeControl() else false
    }

    var amplitude by remember { mutableIntStateOf(255) }
    var isContinuousVibrating by remember { mutableStateOf(false) }
    val sliderState = rememberSliderState(
        value = amplitude.toFloat(),
        steps = 0, trackRange = 1f..255f
    )

    fun triggerEffect(effectType: Int) {
        if (!hasVibrator) return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val effect = VibrationEffect.createPredefined(effectType)
            vibrator.vibrate(effect)
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val effect = VibrationEffect.createOneShot(100, amplitude)
            vibrator.vibrate(effect)
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(100)
        }
    }

    fun triggerWaveform(pattern: LongArray, amplitudes: IntArray = intArrayOf()) {
        if (!hasVibrator) return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val effect = if (amplitudes.isNotEmpty() && hasAmplitudeControl) {
                VibrationEffect.createWaveform(pattern, amplitudes, -1)
            } else {
                VibrationEffect.createWaveform(pattern, -1)
            }
            vibrator.vibrate(effect)
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(pattern, -1)
        }
    }

    fun toggleContinuous(enable: Boolean) {
        if (!hasVibrator) return
        if (enable) {
            val timings = longArrayOf(0, 1000)
            val amps = intArrayOf(0, amplitude)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val effect = if (hasAmplitudeControl) {
                    VibrationEffect.createWaveform(timings, amps, 0)
                } else {
                    VibrationEffect.createWaveform(timings, 0)
                }
                vibrator.vibrate(effect)
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(timings, 0)
            }
            isContinuousVibrating = true
        } else {
            vibrator.cancel()
            isContinuousVibrating = false
        }
    }

    fun triggerPrimitive(primitiveId: Int) {
        if (!hasVibrator) return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (vibrator.areAllPrimitivesSupported(primitiveId)) {
                val effect = VibrationEffect.startComposition()
                    .addPrimitive(primitiveId, (amplitude / 255f).coerceIn(0.1f, 1.0f))
                    .compose()
                vibrator.vibrate(effect)
            }
        }
    }
    DisposableEffect(Unit) {
        onDispose {
            vibrator.cancel()
        }
    }

    val continuousColor by animateColorAsState(
        targetValue = if (isContinuousVibrating) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
        animationSpec = tween(300),
        label = "continuous_color"
    )
    if (!hasVibrator) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .fillMaxSize()
                .padding(top = paddingValues.calculateTopPadding())
        ) {
            Text(
                text = stringResource(id = R.string.vibration_not_supported),
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
                    Spacer(modifier = Modifier.height(16.dp))
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.size(160.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = continuousColor,
                            shadowElevation = if (isContinuousVibrating) 12.dp else 4.dp,
                            modifier = Modifier
                                .size(120.dp)
                                .clip(CircleShape)
                                .clickable {
                                    toggleContinuous(!isContinuousVibrating)
                                }
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    painter = painterResource(id = R.drawable.mobile_vibrate_24px),
                                    contentDescription = "Continuous Vibration",
                                    tint = MaterialTheme.colorScheme.onPrimary,
                                    modifier = Modifier.size(48.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = if (isContinuousVibrating) stringResource(R.string.vibrating) else stringResource(id = R.string.vibration_continuous),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (isContinuousVibrating) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                    )

                    Spacer(modifier = Modifier.height(28.dp))
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
                            text = stringResource(R.string.one_shot_patterns),
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        val patterns = buildList {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                                add(stringResource(id = R.string.vibration_pattern_click) to {
                                    triggerEffect(VibrationEffect.EFFECT_CLICK)
                                })
                                add(stringResource(id = R.string.vibration_pattern_heavy) to {
                                    triggerEffect(VibrationEffect.EFFECT_HEAVY_CLICK)
                                })
                                add(stringResource(id = R.string.vibration_pattern_double) to {
                                    triggerEffect(VibrationEffect.EFFECT_DOUBLE_CLICK)
                                })
                            } else {
                                add(stringResource(id = R.string.vibration_pattern_click) to {
                                    triggerWaveform(longArrayOf(0, 50))
                                })
                                add(stringResource(id = R.string.vibration_pattern_heavy) to {
                                    triggerWaveform(longArrayOf(0, 150))
                                })
                                add(stringResource(id = R.string.vibration_pattern_double) to {
                                    triggerWaveform(longArrayOf(0, 50, 80, 50))
                                })
                            }
                            add(stringResource(id = R.string.vibration_pattern_pulse) to {
                                triggerWaveform(
                                    pattern = longArrayOf(0, 100, 100, 100, 100, 200),
                                    amplitudes = intArrayOf(0, 80, 0, 160, 0, 255)
                                )
                            })
                        }

                        patterns.chunked(2).forEach { rowPatterns ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                rowPatterns.forEach { (title, action) ->
                                    Box(
                                        contentAlignment = Alignment.Center,
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
                                            .clickable {
                                                if (isContinuousVibrating) toggleContinuous(false)
                                                action()
                                            }
                                            .padding(vertical = 12.dp, horizontal = 8.dp)
                                    ) {
                                        Text(
                                            text = title,
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Medium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            textAlign = TextAlign.Center
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
            if (hasAmplitudeControl) {
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceBright),
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = stringResource(id = R.string.vibration_amplitude),
                                    style = MaterialTheme.typography.labelLarge,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = "$amplitude / 255",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Slider(state = sliderState ,
                                onValueChange = { newAmp ->
                                    amplitude = newAmp.toInt().coerceIn(1, 255)
                                    sliderState.value = amplitude.toFloat()
                                    if (isContinuousVibrating) {
                                        toggleContinuous(true)
                                    }
                                    haptics.performHapticFeedback(HapticFeedbackType.SegmentTick)
                                }
                            )
                        }
                    }
                }
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                item {
                    val primitives = buildList {
                        add(R.string.click to VibrationEffect.Composition.PRIMITIVE_CLICK)
                        add(R.string.tick to VibrationEffect.Composition.PRIMITIVE_TICK)
                        add(R.string.quick_rise to VibrationEffect.Composition.PRIMITIVE_QUICK_RISE)
                        add(R.string.quick_fall to VibrationEffect.Composition.PRIMITIVE_QUICK_FALL)
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                            add(R.string.thud to VibrationEffect.Composition.PRIMITIVE_THUD)
                            add(R.string.spin to VibrationEffect.Composition.PRIMITIVE_SPIN)
                            add(R.string.slow_rise to VibrationEffect.Composition.PRIMITIVE_SLOW_RISE)
                            add(R.string.low_tick to VibrationEffect.Composition.PRIMITIVE_LOW_TICK)
                        }
                    }

                    val supportedPrimitives = remember {
                        primitives.filter { vibrator.areAllPrimitivesSupported(it.second) }
                    }

                    if (supportedPrimitives.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceBright),
                            shape = RoundedCornerShape(20.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = stringResource(id = R.string.vibration_primitives),
                                    style = MaterialTheme.typography.labelLarge,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Spacer(modifier = Modifier.height(12.dp))

                                supportedPrimitives.chunked(2).forEach { rowPrims ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 4.dp),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        rowPrims.forEach { (name, primId) ->
                                            Box(
                                                contentAlignment = Alignment.Center,
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .clip(RoundedCornerShape(12.dp))
                                                    .border(
                                                        1.dp,
                                                        MaterialTheme.colorScheme.outlineVariant,
                                                        RoundedCornerShape(12.dp)
                                                    )
                                                    .clickable {
                                                        if (isContinuousVibrating) toggleContinuous(false)
                                                        triggerPrimitive(primId)
                                                    }
                                                    .padding(vertical = 10.dp, horizontal = 8.dp)
                                            ) {
                                                Text(
                                                    text = stringResource(id = name),
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    textAlign = TextAlign.Center
                                                )
                                            }
                                        }
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
                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = stringResource(R.string.amplitude_control), style = MaterialTheme.typography.bodyMedium)
                            Text(
                                text = if (hasAmplitudeControl) stringResource(R.string.supported) else stringResource(R.string.not_supported),
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (hasAmplitudeControl) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                            )
                        }

                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                            Spacer(modifier = Modifier.height(6.dp))
                            val hasEnvelope = vibrator.hasAmplitudeControl()
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(text = stringResource(R.string.primitive_composition), style = MaterialTheme.typography.bodyMedium)
                                Text(
                                    text = if (hasEnvelope) stringResource(R.string.supported) else stringResource(R.string.not_supported),
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = if (hasEnvelope) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
