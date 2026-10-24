package com.lkonlesoft.displayinfo.view.test

import android.content.Context
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.os.Build
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
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
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSliderState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.lkonlesoft.displayinfo.R
import com.lkonlesoft.displayinfo.view.staggeredHeader
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.milliseconds

enum class StrobeMode {
    OFF,
    STROBE_1HZ,
    STROBE_5HZ,
    SOS
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun FlashlightTestScreen(
    paddingValues: PaddingValues
) {
    val context = LocalContext.current
    val layoutDirection = LocalLayoutDirection.current
    val state = rememberLazyStaggeredGridState()
    val cameraManager = remember { context.getSystemService(Context.CAMERA_SERVICE) as CameraManager }
    val lifecycleOwner = LocalLifecycleOwner.current
    val haptics = LocalHapticFeedback.current
    val cameraId = remember {
        try {
            cameraManager.cameraIdList.firstOrNull { id ->
                val characteristics = cameraManager.getCameraCharacteristics(id)
                val hasFlash = characteristics.get(CameraCharacteristics.FLASH_INFO_AVAILABLE) == true
                val facing = characteristics.get(CameraCharacteristics.LENS_FACING)
                hasFlash && facing == CameraCharacteristics.LENS_FACING_BACK
            } ?: cameraManager.cameraIdList.firstOrNull { id ->
                cameraManager.getCameraCharacteristics(id).get(CameraCharacteristics.FLASH_INFO_AVAILABLE) == true
            }
        } catch (_: Exception) {
            null
        }
    }

    val maxStrengthLevel = remember(cameraId) {
        if (cameraId != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            try {
                cameraManager.getCameraCharacteristics(cameraId)
                    .get(CameraCharacteristics.FLASH_INFO_STRENGTH_MAXIMUM_LEVEL) ?: 1
            } catch (_: Exception) {
                1
            }
        } else 1
    }

    var isTorchOn by rememberSaveable { mutableStateOf(false) }
    var strobeMode by rememberSaveable { mutableStateOf(StrobeMode.OFF) }
    var strengthLevel by rememberSaveable { mutableIntStateOf(maxStrengthLevel) }
    val sliderState = rememberSliderState(
        value = strengthLevel.toFloat(),
        steps = 0, trackRange = 1f..maxStrengthLevel.toFloat()
    )

    fun setTorchState(enabled: Boolean, level: Int = strengthLevel) {
        if (cameraId == null) return
        try {
            if (enabled) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && maxStrengthLevel > 1) {
                    cameraManager.turnOnTorchWithStrengthLevel(cameraId, level.coerceIn(1, maxStrengthLevel))
                } else {
                    cameraManager.setTorchMode(cameraId, true)
                }
            } else {
                cameraManager.setTorchMode(cameraId, false)
            }
        } catch (_: Exception) {
            // Camera or torch error
        }
    }

    // Camera Torch Callback
    DisposableEffect(cameraId, lifecycleOwner) {
        val callback = object : CameraManager.TorchCallback() {
            override fun onTorchModeChanged(id: String, enabled: Boolean) {
                if (id == cameraId && strobeMode == StrobeMode.OFF) {
                    isTorchOn = enabled
                }
            }
        }
        try {
            cameraManager.registerTorchCallback(callback, null)
        } catch (_: Exception) {
            // ignore
        }
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_PAUSE) {
                isTorchOn = false
                setTorchState(false)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            try {
                cameraManager.unregisterTorchCallback(callback)
                if (cameraId != null) {
                    cameraManager.setTorchMode(cameraId, false)
                }
            } catch (_: Exception) {
                // ignore
            }
        }
    }
    // Strobe / SOS Coroutine Loop
    LaunchedEffect(strobeMode, isTorchOn, strengthLevel) {
        if (!isTorchOn || strobeMode == StrobeMode.OFF) return@LaunchedEffect

        when (strobeMode) {
            StrobeMode.STROBE_1HZ -> {
                while (true) {
                    setTorchState(true)
                    delay(500.milliseconds)
                    setTorchState(false)
                    delay(500.milliseconds)
                }
            }
            StrobeMode.STROBE_5HZ -> {
                while (true) {
                    setTorchState(true)
                    delay(100.milliseconds)
                    setTorchState(false)
                    delay(100.milliseconds)
                }
            }
            StrobeMode.SOS -> {
                val dot = 150L
                val dash = 450L
                val intraChar = 150L
                val interChar = 450L
                val wordGap = 1200L

                while (true) {
                    // S: . . .
                    repeat(3) {
                        setTorchState(true)
                        delay(dot.milliseconds)
                        setTorchState(false)
                        delay(intraChar.milliseconds)
                    }
                    delay(interChar.milliseconds)

                    // O: - - -
                    repeat(3) {
                        setTorchState(true)
                        delay(dash.milliseconds)
                        setTorchState(false)
                        delay(intraChar.milliseconds)
                    }
                    delay(interChar.milliseconds)

                    // S: . . .
                    repeat(3) {
                        setTorchState(true)
                        delay(dot.milliseconds)
                        setTorchState(false)
                        delay(intraChar.milliseconds)
                    }
                    delay(wordGap.milliseconds)
                }
            }
            StrobeMode.OFF -> {}
        }
    }
    if (cameraId == null) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .fillMaxSize()
                .padding(top = paddingValues.calculateTopPadding())
        ) {
            Text(
                text = stringResource(id = R.string.flashlight_not_available),
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
        val infiniteTransition = rememberInfiniteTransition(label = "glow")
        val glowScale by infiniteTransition.animateFloat(
            initialValue = 1f,
            targetValue = 1.15f,
            animationSpec = infiniteRepeatable(
                animation = tween(1000, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "glow_scale"
        )
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
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.size(180.dp)
                    ) {
                        if (isTorchOn) {
                            Box(
                                modifier = Modifier
                                    .size(160.dp)
                                    .scale(glowScale)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.25f))
                            )
                        }

                        Surface(
                            shape = CircleShape,
                            color = if (isTorchOn) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                            shadowElevation = if (isTorchOn) 12.dp else 2.dp,
                            modifier = Modifier
                                .size(130.dp)
                                .clip(CircleShape)
                                .clickable {
                                    isTorchOn = !isTorchOn
                                    setTorchState(isTorchOn)
                                }
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    painter = painterResource(id = R.drawable.flashlight_on_24px),
                                    contentDescription = "Torch Toggle",
                                    tint = if (isTorchOn) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(52.dp)
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = if (isTorchOn) stringResource(id = R.string.flashlight_on) else stringResource(id = R.string.flashlight_off),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (isTorchOn) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
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
                            text = stringResource(R.string.strobe_mode),
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        val modes = listOf(
                            StrobeMode.OFF to stringResource(id = R.string.flashlight_strobe_off),
                            StrobeMode.STROBE_1HZ to stringResource(id = R.string.flashlight_strobe_1hz),
                            StrobeMode.STROBE_5HZ to stringResource(id = R.string.flashlight_strobe_5hz),
                            StrobeMode.SOS to stringResource(id = R.string.flashlight_strobe_sos)
                        )

                        modes.chunked(2).forEach { rowModes ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                rowModes.forEach { (mode, label) ->
                                    val isSelected = strobeMode == mode
                                    Box(
                                        contentAlignment = Alignment.Center,
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(
                                                if (isSelected) MaterialTheme.colorScheme.primaryContainer
                                                else MaterialTheme.colorScheme.surfaceVariant.copy(
                                                    alpha = 0.5f
                                                )
                                            )
                                            .border(
                                                width = if (isSelected) 1.5.dp else 0.dp,
                                                color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                                                shape = RoundedCornerShape(12.dp)
                                            )
                                            .clickable {
                                                strobeMode = mode
                                                if (mode == StrobeMode.OFF && isTorchOn) {
                                                    setTorchState(true)
                                                }
                                            }
                                            .padding(vertical = 12.dp, horizontal = 8.dp)
                                    ) {
                                        Text(
                                            text = label,
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                            color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                                            textAlign = TextAlign.Center
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && maxStrengthLevel > 1) {
                item {
                    Spacer(modifier = Modifier.height(16.dp))
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
                                    text = stringResource(id = R.string.flashlight_brightness),
                                    style = MaterialTheme.typography.labelLarge,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = "$strengthLevel / $maxStrengthLevel",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Slider(
                                state = sliderState,
                                onValueChange = { newLevel ->
                                    strengthLevel = newLevel.toInt().coerceIn(1, maxStrengthLevel)
                                    sliderState.value = strengthLevel.toFloat()
                                    haptics.performHapticFeedback(HapticFeedbackType.SegmentTick)
                                },
                                onValueChangeFinished = {
                                    if (isTorchOn && strobeMode == StrobeMode.OFF) {
                                        setTorchState(true, strengthLevel)
                                    }
                                }
                            )
                        }
                    }
                }
            }
            staggeredHeader {
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}