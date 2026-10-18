package com.lkonlesoft.displayinfo.view.test

import android.app.Activity
import androidx.activity.compose.BackHandler
import androidx.annotation.StringRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ButtonGroup
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.lkonlesoft.displayinfo.R
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.milliseconds


enum class GradientType {
    RGB_HORIZONTAL,
    SPECTRUM_RAINBOW,
    GRAYSCALE_RAMP,
    RADIAL_LUMINANCE
}

enum class GridType {
    PRIMARY_BLOCKS,
    MULTI_COLOR_CHECKERED,
    HIGH_DENSITY_MESH
}

enum class DiagnosticType {
    SMPTE_COLOR_BARS,
    CONTRAST_16_STEPS
}

sealed class TestPattern(
    @StringRes val titleRes: Int,
    val detailText: String
) {
    data class Solid(
        @StringRes val nameRes: Int,
        val color: Color,
        val hexCode: String
    ) : TestPattern(nameRes, hexCode)

    data class Gradient(
        @StringRes val nameRes: Int,
        val gradientType: GradientType,
        val label: String
    ) : TestPattern(nameRes, label)

    data class Grid(
        @StringRes val nameRes: Int,
        val rows: Int,
        val cols: Int,
        val gridType: GridType
    ) : TestPattern(nameRes, "${rows}x${cols} Matrix")

    data class Diagnostic(
        @StringRes val nameRes: Int,
        val diagnosticType: DiagnosticType,
        val label: String
    ) : TestPattern(nameRes, label)
}

val defaultColorTestPatterns = listOf(
    // 1. Pure Solid Colors
    TestPattern.Solid(R.string.color_test_red, Color(0xFFFF0000), "#FF0000"),
    TestPattern.Solid(R.string.color_test_green, Color(0xFF00FF00), "#00FF00"),
    TestPattern.Solid(R.string.color_test_blue, Color(0xFF0000FF), "#0000FF"),
    TestPattern.Solid(R.string.color_test_white, Color(0xFFFFFFFF), "#FFFFFF"),
    TestPattern.Solid(R.string.color_test_black, Color(0xFF000000), "#000000"),
    TestPattern.Solid(R.string.color_test_cyan, Color(0xFF00FFFF), "#00FFFF"),
    TestPattern.Solid(R.string.color_test_magenta, Color(0xFFFF00FF), "#FF00FF"),
    TestPattern.Solid(R.string.color_test_yellow, Color(0xFFFFFF00), "#FFFF00"),
    TestPattern.Solid(R.string.color_test_grey_25, Color(0xFF404040), "#404040"),
    TestPattern.Solid(R.string.color_test_grey_50, Color(0xFF808080), "#808080"),
    TestPattern.Solid(R.string.color_test_grey_75, Color(0xFFC0C0C0), "#C0C0C0"),

    // 2. Gradients
    TestPattern.Gradient(R.string.color_test_grad_rgb, GradientType.RGB_HORIZONTAL, "Red -> Green -> Blue"),
    TestPattern.Gradient(R.string.color_test_grad_spectrum, GradientType.SPECTRUM_RAINBOW, "Full Spectrum Rainbow"),
    TestPattern.Gradient(R.string.color_test_grad_grayscale, GradientType.GRAYSCALE_RAMP, "0% Black to 100% White Ramp"),
    TestPattern.Gradient(R.string.color_test_grad_radial, GradientType.RADIAL_LUMINANCE, "Center White to Outer Black"),

    // 3. Multi-Color Grids & Rectangles
    TestPattern.Grid(R.string.color_test_grid_primary, 2, 4, GridType.PRIMARY_BLOCKS),
    TestPattern.Grid(R.string.color_test_grid_density, 8, 8, GridType.MULTI_COLOR_CHECKERED),
    TestPattern.Grid(R.string.color_test_grid_high_density, 16, 16, GridType.HIGH_DENSITY_MESH),

    // 4. Diagnostic Calibration
    TestPattern.Diagnostic(R.string.color_test_smpte, DiagnosticType.SMPTE_COLOR_BARS, "TV Standard Color Bars"),
    TestPattern.Diagnostic(R.string.color_test_contrast_steps, DiagnosticType.CONTRAST_16_STEPS, "16-Level Dynamic Range")
)

@Composable
fun ColorTestScreen(
    onExit: () -> Unit = {}
) {
    val context = LocalContext.current
    val view = LocalView.current
    var currentIndex by remember { mutableIntStateOf(0) }
    var showHud by remember { mutableStateOf(true) }
    var isPinned by remember { mutableStateOf(false) }
    val currentPattern = defaultColorTestPatterns[currentIndex]
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
    LaunchedEffect(currentIndex, showHud, isPinned) {
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
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = {
                        currentIndex = (currentIndex + 1) % defaultColorTestPatterns.size
                    },
                    onDoubleTap = {
                        // Double tap toggles pinning HUD
                        isPinned = !isPinned
                        showHud = true
                    }
                )
            }
    ) {
        ColorTestPatternRenderer(pattern = currentPattern)
        AnimatedVisibility(
            visible = showHud,
            enter = fadeIn() + slideInVertically(initialOffsetY = { it }),
            exit = fadeOut() + slideOutVertically(targetOffsetY = { it }),
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            ColorTestTopHud(
                currentStage = currentIndex + 1,
                totalStages = defaultColorTestPatterns.size,
                pattern = currentPattern,
                isPinned = isPinned,
                onPrevious = {
                    currentIndex = if (currentIndex > 0) currentIndex - 1 else defaultColorTestPatterns.size - 1
                },
                onNext = {
                    currentIndex = (currentIndex + 1) % defaultColorTestPatterns.size
                },
                onTogglePin = {
                    isPinned = !isPinned
                },
                onReset = {
                    currentIndex = 0
                },
                onClose = onExit
            )
        }
    }
}

@Composable
fun ColorTestTopHud(
    currentStage: Int,
    totalStages: Int,
    pattern: TestPattern,
    isPinned: Boolean,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onTogglePin: () -> Unit,
    onReset: () -> Unit,
    onClose: () -> Unit
) {
    val with = LocalWindowInfo.current.containerDpSize.width
    val actionMap = remember {
        mapOf(
            R.drawable.baseline_arrow_back_24 to onPrevious,
            R.drawable.refresh_24px to onReset,
            R.drawable.arrow_forward_24px to onNext,
        )
    }
    val interactionSources = remember { List(actionMap.size) { MutableInteractionSource() } }
    Card(
        modifier = Modifier
            .navigationBarsPadding()
            .padding(bottom = 48.dp, start = 16.dp, end = 16.dp)
            .fillMaxWidth(if (with < 600.dp) 1f else .5f),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.background
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Button(
                    onClick = onClose,
                    shapes = ButtonDefaults.shapes(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                        contentColor = MaterialTheme.colorScheme.onSurface
                    )
                ) {
                    Text(text = stringResource(id = R.string.close))
                }
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = stringResource(pattern.titleRes),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "$currentStage / $totalStages • ${pattern.detailText}",
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                IconButton(
                    shapes = IconButtonDefaults.shapes(),
                    colors = IconButtonDefaults.iconButtonColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHighest),
                    onClick = onTogglePin
                ) {
                    Icon(
                        imageVector = ImageVector.vectorResource(if (isPinned) R.drawable.keep_24px else R.drawable.keep_off_24px),
                        contentDescription = "Toggle Pin HUD"
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            ButtonGroup(
                modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                expandedRatio = 1f,
                overflowIndicator = {}
            ) {
                actionMap.entries.forEachIndexed { index, entry ->
                    customItem(
                        buttonGroupContent = {
                            val contentPadding = ButtonDefaults.ButtonWithIconContentPadding
                            val layoutDirection = LocalLayoutDirection.current
                            Button(
                                shapes = ButtonDefaults.shapes(),
                                onClick = entry.value,
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
                                interactionSource = interactionSources[index],
                                modifier =
                                    Modifier.weight(1f).animateWidth(
                                        interactionSource = interactionSources[index],
                                        compressionLimit =
                                            contentPadding.calculateEndPadding(layoutDirection),
                                    )
                            ) {
                                Icon(
                                    imageVector = ImageVector.vectorResource(entry.key),
                                    contentDescription = "Action",
                                    tint = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                            }
                        },
                        menuContent = {}
                    )
                }
            }
        }
    }
}

@Composable
fun ColorTestPatternRenderer(pattern: TestPattern) {
    when (pattern) {
        is TestPattern.Solid -> {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(pattern.color)
            )
        }
        is TestPattern.Gradient -> {
            val brush = when (pattern.gradientType) {
                GradientType.RGB_HORIZONTAL -> Brush.horizontalGradient(
                    listOf(Color.Red, Color.Green, Color.Blue)
                )

                GradientType.SPECTRUM_RAINBOW -> Brush.horizontalGradient(
                    listOf(
                        Color.Red,
                        Color.Yellow,
                        Color.Green,
                        Color.Cyan,
                        Color.Blue,
                        Color.Magenta,
                        Color.Red
                    )
                )

                GradientType.GRAYSCALE_RAMP -> Brush.verticalGradient(
                    listOf(
                        Color.Black,
                        Color(0xFF202020),
                        Color(0xFF404040),
                        Color(0xFF606060),
                        Color(0xFF808080),
                        Color(0xFFA0A0A0),
                        Color(0xFFC0C0C0),
                        Color(0xFFE0E0E0),
                        Color.White
                    )
                )

                GradientType.RADIAL_LUMINANCE -> Brush.radialGradient(
                    listOf(Color.White, Color(0xFF808080), Color.Black)
                )
            }
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(brush)
            )
        }
        is TestPattern.Grid -> {
            Canvas(modifier = Modifier.fillMaxSize()) {
                drawGridPattern(pattern)
            }
        }
        is TestPattern.Diagnostic -> {
            Canvas(modifier = Modifier.fillMaxSize()) {
                when (pattern.diagnosticType) {
                    DiagnosticType.SMPTE_COLOR_BARS -> drawSmpteColorBars()
                    DiagnosticType.CONTRAST_16_STEPS -> draw16StepContrastChart()
                }
            }
        }
    }
}

private fun DrawScope.drawGridPattern(grid: TestPattern.Grid) {
    val cellWidth = size.width / grid.cols
    val cellHeight = size.height / grid.rows

    val primaryColors = listOf(
        Color(0xFFFF0000), Color(0xFF00FF00), Color(0xFF0000FF), Color(0xFFFFFF00),
        Color(0xFF00FFFF), Color(0xFFFF00FF), Color(0xFFFFFFFF), Color(0xFF000000)
    )

    for (r in 0 until grid.rows) {
        for (c in 0 until grid.cols) {
            val color = when (grid.gridType) {
                GridType.PRIMARY_BLOCKS -> {
                    val index = (r * grid.cols + c) % primaryColors.size
                    primaryColors[index]
                }

                GridType.MULTI_COLOR_CHECKERED -> {
                    val hue = ((r * grid.cols + c) * 360f / (grid.rows * grid.cols)) % 360f
                    Color.hsv(hue, 0.95f, 1.0f)
                }

                GridType.HIGH_DENSITY_MESH -> {
                    val isEven = (r + c) % 2 == 0
                    if (isEven) {
                        val hue = ((r * grid.cols + c) * 360f / (grid.rows * grid.cols)) % 360f
                        Color.hsv(hue, 1.0f, 1.0f)
                    } else {
                        val hue = ((c * grid.rows + r) * 360f / (grid.rows * grid.cols)) % 360f
                        Color.hsv(hue, 0.4f, 0.9f)
                    }
                }
            }

            drawRect(
                color = color,
                topLeft = Offset(c * cellWidth, r * cellHeight),
                size = Size(cellWidth + 1f, cellHeight + 1f)
            )
        }
    }
}

private fun DrawScope.drawSmpteColorBars() {
    val width = size.width
    val height = size.height

    // Standard 75% SMPTE top bars (67% height)
    val topBarsHeight = height * 0.67f
    val smpteTopColors = listOf(
        Color(0xFFC0C0C0), // 75% White
        Color(0xFFC0C000), // Yellow
        Color(0xFF00C0C0), // Cyan
        Color(0xFF00C000), // Green
        Color(0xFFC000C0), // Magenta
        Color(0xFFC00000), // Red
        Color(0xFF0000C0)  // Blue
    )

    val topBarWidth = width / smpteTopColors.size
    smpteTopColors.forEachIndexed { i, color ->
        drawRect(
            color = color,
            topLeft = Offset(i * topBarWidth, 0f),
            size = Size(topBarWidth + 1f, topBarsHeight)
        )
    }

    // Reverse Blue bars (8% height)
    val midBarsHeight = height * 0.08f
    val smpteMidColors = listOf(
        Color(0xFF0000C0), // Blue
        Color(0xFF131313), // Black
        Color(0xFFC000C0), // Magenta
        Color(0xFF131313), // Black
        Color(0xFF00C0C0), // Cyan
        Color(0xFF131313), // Black
        Color(0xFFC0C0C0)  // White
    )

    smpteMidColors.forEachIndexed { i, color ->
        drawRect(
            color = color,
            topLeft = Offset(i * topBarWidth, topBarsHeight),
            size = Size(topBarWidth + 1f, midBarsHeight)
        )
    }

    // Bottom PLUGE & Luminance Signals (25% height)
    val botBarsHeight = height * 0.25f
    val botBarsTop = topBarsHeight + midBarsHeight

    val bottomWidthUnit = width / 7f

    // 1. -I Signal
    drawRect(Color(0xFF00214C), Offset(0f, botBarsTop), Size(bottomWidthUnit * 1.25f, botBarsHeight))
    // 2. 100% White
    drawRect(Color(0xFFFFFFFF), Offset(bottomWidthUnit * 1.25f, botBarsTop), Size(bottomWidthUnit * 1.25f, botBarsHeight))
    // 3. +Q Signal
    drawRect(Color(0xFF32006A), Offset(bottomWidthUnit * 2.5f, botBarsTop), Size(bottomWidthUnit * 1.25f, botBarsHeight))

    // 4. Black / PLUGE area
    val plugeStart = bottomWidthUnit * 3.75f
    val plugeWidth = bottomWidthUnit * 2.25f
    drawRect(Color(0xFF131313), Offset(plugeStart, botBarsTop), Size(plugeWidth, botBarsHeight))

    // PLUGE Sub-stripes (-4%, +4% super-black / dark black)
    val stripeWidth = plugeWidth / 3f
    drawRect(Color(0xFF070707), Offset(plugeStart, botBarsTop), Size(stripeWidth, botBarsHeight))
    drawRect(Color(0xFF131313), Offset(plugeStart + stripeWidth, botBarsTop), Size(stripeWidth, botBarsHeight))
    drawRect(Color(0xFF1F1F1F), Offset(plugeStart + 2 * stripeWidth, botBarsTop), Size(stripeWidth, botBarsHeight))

    // 5. Final Black block
    drawRect(Color(0xFF131313), Offset(bottomWidthUnit * 6f, botBarsTop), Size(bottomWidthUnit, botBarsHeight))
}

private fun DrawScope.draw16StepContrastChart() {
    val steps = 16
    val barHeight = size.height / steps

    for (i in 0 until steps) {
        val intensity = (i * 255 / (steps - 1)).coerceIn(0, 255)
        val color = Color(intensity, intensity, intensity)

        drawRect(
            color = color,
            topLeft = Offset(0f, i * barHeight),
            size = Size(size.width, barHeight + 1f)
        )
    }
}
