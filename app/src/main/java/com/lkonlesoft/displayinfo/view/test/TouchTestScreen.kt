package com.lkonlesoft.displayinfo.view.test

import android.app.Activity
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.lkonlesoft.displayinfo.view.test.touch.GridDensity
import com.lkonlesoft.displayinfo.view.test.touch.PointerTrail
import com.lkonlesoft.displayinfo.view.test.touch.TouchPoint
import com.lkonlesoft.displayinfo.view.test.touch.TouchTestCanvas
import com.lkonlesoft.displayinfo.view.test.touch.TouchTestHud
import com.lkonlesoft.displayinfo.view.test.touch.TouchTestMode
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.milliseconds

@Composable
fun TouchTestScreen(
    onExit: () -> Unit = {}
) {
    val context = LocalContext.current
    val view = LocalView.current
    val haptic = LocalHapticFeedback.current
    var mode by remember { mutableStateOf(TouchTestMode.TRAILS_AND_INFO) }
    var density by remember { mutableStateOf(GridDensity.MEDIUM) }
    var isHapticsEnabled by remember { mutableStateOf(false) }
    var showHud by remember { mutableStateOf(true) }
    var isPinned by remember { mutableStateOf(false) }
    var activeTouches by remember { mutableStateOf<List<TouchPoint>>(emptyList()) }
    var trails by remember { mutableStateOf<List<PointerTrail>>(emptyList()) }
    var touchedGridCells by remember { mutableStateOf<Set<Pair<Int, Int>>>(emptySet()) }
    var maxTouches by remember { mutableIntStateOf(0) }
    // Touch Sample Rate (Hz) Tracking
    var lastEventTimestamp by remember { mutableLongStateOf(System.currentTimeMillis()) }
    var sampleRateHz by remember { mutableIntStateOf(0) }

    DisposableEffect(Unit) {
        val window = (context as? Activity)?.window
        val insetsController = window?.let { WindowCompat.getInsetsController(it, view) }
        insetsController?.let {
            it.hide(WindowInsetsCompat.Type.systemBars())
            it.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
        onDispose {
            insetsController?.show(WindowInsetsCompat.Type.systemBars())
        }
    }
    LaunchedEffect(showHud, isPinned, mode, density) {
        if (showHud && !isPinned) {
            delay(3000L.milliseconds)
            showHud = false
        }
    }
    BackHandler {
        when {
            isPinned -> isPinned = false
            !showHud -> showHud = true
            else -> onExit()
        }
    }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        TouchTestCanvas(
            mode = mode,
            density = density,
            activeTouches = activeTouches,
            trails = trails,
            touchedGridCells = touchedGridCells,
            maxTouches = maxTouches,
            sampleRateHz = sampleRateHz,
            onPointerEvent = { newTouches ->
                // Calculate Sample Rate in Hz
                val now = System.currentTimeMillis()
                val deltaMs = (now - lastEventTimestamp).coerceAtLeast(1)
                lastEventTimestamp = now
                if (newTouches.isNotEmpty()) {
                    sampleRateHz = (1000f / deltaMs).toInt().coerceIn(30, 240)
                }

                // Trigger Haptic Feedback on new touch down
                if (isHapticsEnabled && newTouches.size > activeTouches.size) {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                }

                // Update Max Touches
                if (newTouches.size > maxTouches) {
                    maxTouches = newTouches.size
                }

                // Update Trails
                if (newTouches.isNotEmpty()) {
                    val updatedTrails = trails.toMutableList()
                    for (touch in newTouches) {
                        val existingIndex = updatedTrails.indexOfFirst { it.id == touch.id }
                        if (existingIndex >= 0) {
                            val existingTrail = updatedTrails[existingIndex]
                            updatedTrails[existingIndex] = existingTrail.copy(
                                points = existingTrail.points + touch.position
                            )
                        } else {
                            updatedTrails.add(
                                PointerTrail(
                                    id = touch.id,
                                    color = touch.color,
                                    points = listOf(touch.position)
                                )
                            )
                        }
                    }
                    trails = updatedTrails
                }

                // Update Grid Cells if in Grid Mode
                if (mode == TouchTestMode.GRID_COVERAGE && newTouches.isNotEmpty()) {
                    val screenWidth = view.width.toFloat().coerceAtLeast(1f)
                    val screenHeight = view.height.toFloat().coerceAtLeast(1f)
                    val cellWidth = screenWidth / density.cols
                    val cellHeight = screenHeight / density.rows

                    val newCells = touchedGridCells.toMutableSet()
                    for (touch in newTouches) {
                        val col = (touch.position.x / cellWidth).toInt().coerceIn(0, density.cols - 1)
                        val row = (touch.position.y / cellHeight).toInt().coerceIn(0, density.rows - 1)
                        newCells.add(Pair(col, row))
                    }
                    touchedGridCells = newCells
                }

                activeTouches = newTouches
            }
        )
        AnimatedVisibility(
            visible = showHud,
            modifier = Modifier.align(Alignment.BottomCenter),
            enter = fadeIn() + slideInVertically(initialOffsetY = { it }),
            exit = fadeOut() + slideOutVertically(targetOffsetY = { it }),
        ) {
            TouchTestHud(
                isPinned = isPinned,
                mode = mode,
                density = density,
                isHapticsEnabled = isHapticsEnabled,
                onModeChange = { newMode -> mode = newMode },
                onDensityChange = { newDensity ->
                    density = newDensity
                    touchedGridCells = emptySet()
                },
                onToggleHaptics = { isHapticsEnabled = !isHapticsEnabled },
                onTogglePin = { isPinned = !isPinned },
                onClear = {
                    trails = emptyList()
                    touchedGridCells = emptySet()
                    maxTouches = activeTouches.size
                },
                onExit = onExit
            )
        }
    }
}
