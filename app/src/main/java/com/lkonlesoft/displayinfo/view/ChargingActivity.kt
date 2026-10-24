package com.lkonlesoft.displayinfo.view

import android.app.Activity
import android.app.KeyguardManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.view.OrientationEventListener
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.lkonlesoft.displayinfo.R
import com.lkonlesoft.displayinfo.ui.theme.BatteryColor
import com.lkonlesoft.displayinfo.ui.theme.ScreenInfoTheme
import com.lkonlesoft.displayinfo.utils.BatteryUtils
import kotlinx.coroutines.delay
import java.text.DateFormat
import java.util.Date
import kotlin.math.sin
import kotlin.time.Duration.Companion.seconds

class ChargingActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
        } else {
            @Suppress("DEPRECATION")
            window.addFlags(
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
            )
        }
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        enableEdgeToEdge()
        setContent {
            ScreenInfoTheme(typographyType = 0) {
                ChargingScreen(
                    onDismiss = {
                        finish()
                    },
                    onLockScreenDismiss = {
                        finishAffinity()
                    }
                )
            }
        }
    }
}

enum class DeviceOrientation {
    Portrait,
    Landscape,
    ReversePortrait,
    ReverseLandscape,
    Unknown
}

@Composable
fun rememberDeviceOrientation(): State<DeviceOrientation> {
    val context = LocalContext.current
    val orientation = remember { mutableStateOf(DeviceOrientation.Unknown) }

    DisposableEffect(Unit) {
        val listener = object : OrientationEventListener(context) {
            override fun onOrientationChanged(angle: Int) {
                // angle is 0..359 degrees (or ORIENTATION_UNKNOWN = -1)
                orientation.value = when (angle) {
                    ORIENTATION_UNKNOWN -> DeviceOrientation.Unknown
                    in 315..359, in 0..44 -> DeviceOrientation.Portrait
                    in 45..134 -> DeviceOrientation.Landscape          // 90°
                    in 135..224 -> DeviceOrientation.ReversePortrait    // 180°
                    in 225..314 -> DeviceOrientation.ReverseLandscape   // 270°
                    else -> DeviceOrientation.Unknown
                }
            }
        }

        if (listener.canDetectOrientation()) {
            listener.enable()
        }

        onDispose {
            listener.disable()
        }
    }

    return orientation
}

@Composable
fun ChargingScreen(
    onDismiss: () -> Unit,
    onLockScreenDismiss: () -> Unit
) {
    val context = LocalContext.current
    val view = LocalView.current
    val window = (view.context as Activity).window
    val isPortrait = LocalConfiguration.current.orientation == Configuration.ORIENTATION_PORTRAIT
    var isLocked by remember { mutableStateOf(false) }
    val intentFilter = IntentFilter().apply {
        addAction(Intent.ACTION_SCREEN_ON)
        addAction(Intent.ACTION_SCREEN_OFF)
        addAction(Intent.ACTION_USER_PRESENT)
        addAction(Intent.ACTION_POWER_DISCONNECTED)
    }

    // Periodic updates for time & battery stats
    var refreshKey by remember { mutableIntStateOf(0) }
    var currentTimeString by remember (refreshKey) { mutableStateOf(
        DateFormat.getTimeInstance(DateFormat.SHORT).format(Date())
    ) }
    var burnInShiftX by remember { mutableStateOf(0.dp) }
    var burnInShiftY by remember { mutableStateOf(0.dp) }
    var showExtraInfo by remember { mutableStateOf(false) }
    val batteryUtils = remember(context) { BatteryUtils(context) }
    val chargingState = remember(refreshKey) { batteryUtils.getChargingState() }
    val wirelessStr = stringResource(R.string.wireless)
    val wirelessChargingStr = stringResource(R.string.wireless_charging)
    val fastChargingStr = stringResource(R.string.fast_charging)
    val slowChargingStr = stringResource(R.string.slow_charging)
    val chargingStr = stringResource(R.string.charging)
    val fullyChargedStr = stringResource(R.string.full)
    val chargingLabel = remember(
        chargingState.statusStr,
        chargingState.plugType,
        chargingState.wattage
    ) {
        when {
            chargingState.statusStr == fullyChargedStr -> fullyChargedStr
            chargingState.plugType == wirelessStr -> wirelessChargingStr
            chargingState.wattage >= 15f -> fastChargingStr
            chargingState.wattage in 0.1f..7.5f -> slowChargingStr
            else -> chargingStr
        }
    }
    val accentColor = BatteryColor
    val dismissAction : () -> Unit = {
        if (isLocked)
            onLockScreenDismiss()
        else
            onDismiss()
    }
    // Hide system bars & keep screen awake
    DisposableEffect(Unit) {
        //monitor lock status
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                val km = context?.getSystemService(Context.KEYGUARD_SERVICE) as? KeyguardManager
                    ?: return

                when (intent?.action) {
                    Intent.ACTION_POWER_DISCONNECTED -> dismissAction()
                    Intent.ACTION_SCREEN_OFF -> isLocked = true
                    Intent.ACTION_USER_PRESENT -> isLocked = false
                    else -> isLocked = km.isKeyguardLocked
                }
            }
        }
        context.registerReceiver(receiver, intentFilter)
        WindowInsetsControllerCompat(window, view).let { controller ->
            controller.hide(WindowInsetsCompat.Type.systemBars())
            controller.systemBarsBehavior =
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
        //revert all changes above, reset activity back to normal
        onDispose {
            context.unregisterReceiver(receiver)
            WindowInsetsControllerCompat(window, view).show(WindowInsetsCompat.Type.systemBars())
        }
    }
    LaunchedEffect(Unit) {
        var tickCount = 0L
        while (true) {
            delay(1.seconds)
            tickCount++
            refreshKey++
            // Anti-burn-in shift every ~2 minutes
            if (tickCount % 120 == 0L) {
                val shiftXVal = (sin(tickCount.toDouble()) * 4).dp
                val shiftYVal = (sin(tickCount.toDouble() * 0.7) * 4).dp
                burnInShiftX = shiftXVal
                burnInShiftY = shiftYVal
                tickCount = 0L
            }
        }
    }
    BackHandler {
       dismissAction()
    }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .clickable {
                showExtraInfo = !showExtraInfo
            }
            .offset {
                IntOffset(
                    x = burnInShiftX.toPx().toInt(),
                    y = burnInShiftY.toPx().toInt()
                )
            }
    ) {
        if (!isPortrait) {
            ChargingLandscapeContent(
                level = chargingState.level,
                timeString = currentTimeString,
                chargingLabel = chargingLabel,
                minsTo80 = chargingState.minsTo80,
                minsTo100 = chargingState.minsTo100,
                accentColor = accentColor
            )
        }
        else {
            ChargingPortraitContent(
                level = chargingState.level,
                timeString = currentTimeString,
                chargingLabel = chargingLabel,
                minsTo80 = chargingState.minsTo80,
                minsTo100 = chargingState.minsTo100,
                accentColor = accentColor
            )
        }
        // Overlay tooltip or extra stats when tapped
        AnimatedVisibility(
            visible = showExtraInfo,
            enter = fadeIn() + slideInVertically(
                initialOffsetY = { up -> -up },
                animationSpec = tween(250)
            ),
            exit = fadeOut() + slideOutVertically(
                targetOffsetY = { down -> -down },
                animationSpec = tween(250)
            ),
            modifier = Modifier.align(Alignment.TopCenter)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .padding(top = 48.dp, start = 16.dp, end = 16.dp)
                    .background(Color(0xCC121212), shape = RoundedCornerShape(16.dp))
                    .padding(horizontal = 16.dp, vertical = 10.dp)
                    .clickable {
                        dismissAction()
                    }
            ) {
                if (chargingState.wattage > 0f || chargingState.tempC > 0f) {
                    Text(
                        text = buildString {
                            if (chargingState.wattage > 0f) append("%.1f W".format(chargingState.wattage))
                            if (chargingState.currentMa != -1) append(" • %d mA".format(chargingState.currentMa))
                            if (chargingState.voltageV > 0f) append(" • %.2f V".format(chargingState.voltageV))
                            if (chargingState.tempC > 0f) append(" • %.1f °C".format(chargingState.tempC))
                        },
                        color = Color.White.copy(alpha = 0.9f),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                }
                Text(
                    text = stringResource(R.string.exit_charging_screen_hint),
                    color = Color.White.copy(alpha = 0.6f),
                    fontSize = 12.sp
                )
            }
        }
    }
}

@Composable
private fun ChargingPortraitContent(
    level: Int,
    timeString: String,
    chargingLabel: String,
    minsTo80: Int,
    minsTo100: Int,
    accentColor: Color
) {
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val totalHeight = maxHeight
        val rightMargin = 28.dp
        val barTop = totalHeight * 0.12f
        val barBottom = totalHeight * 0.78f
        val barHeight = barBottom - barTop

        // Vertical Progress Line on the Right Edge
        PortraitVerticalProgressBar(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = barTop, end = rightMargin)
                .size(width = 64.dp, height = barHeight),
            level = level,
            minsTo80 = minsTo80,
            minsTo100 = minsTo100,
            accentColor = accentColor
        )

        // Giant Percentage Text at Bottom-Right
        Column(
            horizontalAlignment = Alignment.End,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = totalHeight * 0.22f, end = rightMargin + 24.dp)
        ) {
            Row(
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.End
            ) {
                Text(
                    text = if (level >= 0) "$level" else "--",
                    color = accentColor,
                    fontSize = 110.sp,
                    fontWeight = FontWeight.ExtraLight,
                    lineHeight = 110.sp
                )
                Text(
                    text = "%",
                    color = accentColor,
                    fontSize = 38.sp,
                    fontWeight = FontWeight.Light,
                    modifier = Modifier.padding(bottom = 12.dp, start = 2.dp)
                )
            }
        }

        // Clock & Charging Status at Bottom-Left
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(start = 32.dp, bottom = 48.dp)
        ) {
            Text(
                text = timeString,
                color = Color.White,
                fontSize = 24.sp,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = chargingLabel,
                color = accentColor.copy(alpha = 0.9f),
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun PortraitVerticalProgressBar(
    modifier: Modifier,
    level: Int,
    minsTo80: Int,
    minsTo100: Int,
    accentColor: Color
) {
    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height
        val lineX = width - 8.dp.toPx()
        val strokeWidth = 5.dp.toPx()
        val gap = 6.dp.toPx()

        // 80% split position: 0% at bottom, 100% at top
        val seg1TopY = height * 0.2f + gap / 2f
        val seg2TopY = 0f
        val seg2BottomY = height * 0.2f - gap / 2f

        val trackColor = Color.White.copy(alpha = 0.25f)

        // Draw track segment 1 (0 to 80%)
        drawLine(
            color = trackColor,
            start = Offset(lineX, height),
            end = Offset(lineX, seg1TopY),
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round
        )

        // Draw track segment 2 (80 to 100%)
        drawLine(
            color = trackColor,
            start = Offset(lineX, seg2BottomY),
            end = Offset(lineX, seg2TopY),
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round
        )

        // Draw Filled Progress
        val progressRatio = (level.coerceIn(0, 100) / 100f)
        val currentFillY = height * (1f - progressRatio)

        if (level > 0) {
            // Fill segment 1
            if (currentFillY < height) {
                val endY = currentFillY.coerceAtLeast(seg1TopY)
                drawLine(
                    color = accentColor,
                    start = Offset(lineX, height),
                    end = Offset(lineX, endY),
                    strokeWidth = strokeWidth,
                    cap = StrokeCap.Round
                )
            }
            // Fill segment 2 (above 80%)
            if (currentFillY < seg1TopY) {
                val endY = currentFillY.coerceAtLeast(seg2TopY)
                drawLine(
                    color = accentColor,
                    start = Offset(lineX, seg2BottomY),
                    end = Offset(lineX, endY),
                    strokeWidth = strokeWidth,
                    cap = StrokeCap.Round
                )
            }
        }
    }

    // Overlay text markers at 100% (top) and 80%
    Box(modifier = modifier) {
        // 100% Marker
        Column(
            horizontalAlignment = Alignment.End,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset(x = (-20).dp, y = (-8).dp)
        ) {
            Text(
                text = "100%",
                color = Color.White.copy(alpha = 0.9f),
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
            if (minsTo100 > 0 && level < 100) {
                Text(
                    text = "${minsTo100}m",
                    color = Color.White.copy(alpha = 0.6f),
                    fontSize = 13.sp
                )
            }
        }

        // 80% Marker
        Column(
            horizontalAlignment = Alignment.End,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset(x = (-20).dp, y = 54.dp)
        ) {
            Text(
                text = "80%",
                color = Color.White.copy(alpha = 0.9f),
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
            if (minsTo80 > 0 && level < 80) {
                Text(
                    text = "${minsTo80}m",
                    color = Color.White.copy(alpha = 0.6f),
                    fontSize = 13.sp
                )
            }
        }
    }
}

@Composable
private fun ChargingLandscapeContent(
    level: Int,
    timeString: String,
    chargingLabel: String,
    minsTo80: Int,
    minsTo100: Int,
    accentColor: Color
) {
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val totalWidth = maxWidth
        // Giant Percentage on Left Side
        Row(
            verticalAlignment = Alignment.Bottom,
            modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(start = 48.dp, bottom = 24.dp)
        ) {
            Text(
                text = if (level >= 0) "$level" else "--",
                color = accentColor,
                fontSize = 120.sp,
                fontWeight = FontWeight.ExtraLight,
                lineHeight = 120.sp
            )
            Text(
                text = "%",
                color = accentColor,
                fontSize = 42.sp,
                fontWeight = FontWeight.Light,
                modifier = Modifier.padding(bottom = 16.dp, start = 2.dp)
            )
        }

        // Horizontal Progress Bar across lower-middle
        val barWidth = totalWidth * 0.52f
        val barLeft = totalWidth * 0.40f

        LandscapeHorizontalProgressBar(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .offset(x = barLeft, y = 30.dp)
                .size(width = barWidth, height = 70.dp),
            level = level,
            minsTo80 = minsTo80,
            minsTo100 = minsTo100,
            accentColor = accentColor
        )

        // Center Bottom Clock & Charging Status
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 24.dp)
        ) {
            Text(
                text = timeString,
                color = Color.White,
                fontSize = 22.sp,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = chargingLabel,
                color = accentColor.copy(alpha = 0.9f),
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun LandscapeHorizontalProgressBar(
    modifier: Modifier,
    level: Int,
    minsTo80: Int,
    minsTo100: Int,
    accentColor: Color
) {
    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height
        val lineY = height - 12.dp.toPx()
        val strokeWidth = 5.dp.toPx()
        val gap = 6.dp.toPx()

        // 80% split position: 0% at left, 100% at right
        val seg1LeftX = 0f
        val seg1RightX = width * 0.8f - gap / 2f
        val seg2LeftX = width * 0.8f + gap / 2f

        val trackColor = Color.White.copy(alpha = 0.25f)

        // Draw track segment 1 (0 to 80%)
        drawLine(
            color = trackColor,
            start = Offset(seg1LeftX, lineY),
            end = Offset(seg1RightX, lineY),
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round
        )

        // Draw track segment 2 (80 to 100%)
        drawLine(
            color = trackColor,
            start = Offset(seg2LeftX, lineY),
            end = Offset(width, lineY),
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round
        )

        // Draw Filled Progress
        val progressRatio = (level.coerceIn(0, 100) / 100f)
        val currentFillX = width * progressRatio

        if (level > 0) {
            // Fill segment 1
            if (currentFillX > seg1LeftX) {
                val endX = currentFillX.coerceAtMost(seg1RightX)
                drawLine(
                    color = accentColor,
                    start = Offset(seg1LeftX, lineY),
                    end = Offset(endX, lineY),
                    strokeWidth = strokeWidth,
                    cap = StrokeCap.Round
                )
            }
            // Fill segment 2
            if (currentFillX > seg2LeftX) {
                val endX = currentFillX.coerceAtMost(width)
                drawLine(
                    color = accentColor,
                    start = Offset(seg2LeftX, lineY),
                    end = Offset(endX, lineY),
                    strokeWidth = strokeWidth,
                    cap = StrokeCap.Round
                )
            }
        }
    }

    // Overlay text markers above 80% and 100%
    Box(modifier = modifier) {
        // 80% Marker above bar
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .align(Alignment.TopStart)
                .offset(x = 180.dp, y = 0.dp)
        ) {
            if (minsTo80 > 0 && level < 80) {
                Text(
                    text = "${minsTo80}m",
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 13.sp
                )
            }
            Text(
                text = "80%",
                color = Color.White.copy(alpha = 0.9f),
                fontSize = 17.sp,
                fontWeight = FontWeight.Medium
            )
        }

        // 100% Marker above bar
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset(x = (-10).dp, y = 0.dp)
        ) {
            if (minsTo100 > 0 && level < 100) {
                Text(
                    text = "${minsTo100}m",
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 13.sp
                )
            }
            Text(
                text = "100%",
                color = Color.White.copy(alpha = 0.9f),
                fontSize = 17.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}