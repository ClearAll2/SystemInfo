package com.lkonlesoft.displayinfo.view.components

import android.app.Activity
import android.app.KeyguardManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.res.Configuration
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Calendar
import kotlin.math.min
import kotlin.math.sin
import kotlin.time.Duration.Companion.milliseconds

private val TopHalfShape = object : Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline {
        return Outline.Rectangle(
            Rect(
                left = 0f,
                top = 0f,
                right = size.width,
                bottom = size.height / 2f
            )
        )
    }
}

private val BottomHalfShape = object : Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline {
        return Outline.Rectangle(
            Rect(
                left = 0f,
                top = size.height / 2f,
                right = size.width,
                bottom = size.height
            )
        )
    }
}

@Composable
fun FlipClockScreen(
    onDismiss: () -> Unit,
    onLockScreenDismiss: () -> Unit
) {
    val context = LocalContext.current
    val view = LocalView.current
    val window = (view.context as? Activity)?.window
    val configuration = LocalConfiguration.current
    val isPortrait = configuration.orientation == Configuration.ORIENTATION_PORTRAIT
    val locale = remember(configuration) { configuration.locales[0] }

    var isLocked by remember { mutableStateOf(false) }

    val initialCalendar = Calendar.getInstance()
    val initialIs24Hour = android.text.format.DateFormat.is24HourFormat(context)
    val initialHourInt = if (initialIs24Hour) {
        initialCalendar.get(Calendar.HOUR_OF_DAY)
    } else {
        val h = initialCalendar.get(Calendar.HOUR)
        if (h == 0) 12 else h
    }

    var hourStr by remember {
        mutableStateOf(String.format(locale, "%02d", initialHourInt))
    }
    var minuteStr by remember {
        mutableStateOf(String.format(locale, "%02d", initialCalendar.get(Calendar.MINUTE)))
    }
    var amPmStr by remember {
        mutableStateOf(
            if (initialIs24Hour) "" else if (initialCalendar.get(Calendar.AM_PM) == Calendar.AM) "AM" else "PM"
        )
    }
    var dateStr by remember {
        mutableStateOf(SimpleDateFormat("EEEE, MMMM d", locale).format(initialCalendar.time).uppercase(locale))
    }

    var burnInShiftX by remember { mutableStateOf(0.dp) }
    var burnInShiftY by remember { mutableStateOf(0.dp) }

    val dismissAction: () -> Unit = {
        if (isLocked) {
            onLockScreenDismiss()
        } else {
            onDismiss()
        }
    }

    // Hide system bars & keep screen awake / listen for system events
    DisposableEffect(Unit) {
        val intentFilter = IntentFilter().apply {
            addAction(Intent.ACTION_SCREEN_ON)
            addAction(Intent.ACTION_SCREEN_OFF)
            addAction(Intent.ACTION_USER_PRESENT)
            addAction(Intent.ACTION_POWER_DISCONNECTED)
        }

        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                val km = context?.getSystemService(Context.KEYGUARD_SERVICE) as? KeyguardManager

                when (intent?.action) {
                    Intent.ACTION_POWER_DISCONNECTED -> dismissAction()
                    Intent.ACTION_SCREEN_OFF -> isLocked = true
                    Intent.ACTION_USER_PRESENT -> isLocked = false
                    else -> {
                        isLocked = km?.isKeyguardLocked == true
                    }
                }
            }
        }

        context.registerReceiver(receiver, intentFilter)

        window?.let { win ->
            WindowInsetsControllerCompat(win, view).let { controller ->
                controller.hide(WindowInsetsCompat.Type.systemBars())
                controller.systemBarsBehavior =
                    WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            }
        }

        onDispose {
            try {
                context.unregisterReceiver(receiver)
            } catch (_: Exception) {
            }
            window?.let { win ->
                WindowInsetsControllerCompat(win, view).show(WindowInsetsCompat.Type.systemBars())
            }
        }
    }

    // Time updating & anti-burn-in shift
    LaunchedEffect(Unit) {
        var minuteCount = 0L
        val dateFormat = SimpleDateFormat("EEEE, MMMM d", locale)

        while (true) {
            val calendar = Calendar.getInstance()
            val is24Hour = android.text.format.DateFormat.is24HourFormat(context)

            val hourInt = if (is24Hour) {
                calendar.get(Calendar.HOUR_OF_DAY)
            } else {
                val h = calendar.get(Calendar.HOUR)
                if (h == 0) 12 else h
            }

            hourStr = String.format(locale, "%02d", hourInt)
            minuteStr = String.format(locale, "%02d", calendar.get(Calendar.MINUTE))
            amPmStr = if (is24Hour) "" else if (calendar.get(Calendar.AM_PM) == Calendar.AM) "AM" else "PM"
            dateStr = dateFormat.format(calendar.time).uppercase(locale)

            // Anti-burn-in shift every ~2 minutes
            if (minuteCount % 2L == 0L) {
                burnInShiftX = (sin(minuteCount.toDouble()) * 4).dp
                burnInShiftY = (sin(minuteCount.toDouble() * 0.7) * 4).dp
            }
            minuteCount++

            // Delay until next minute alignment
            val delayMs = 60_000L - (System.currentTimeMillis() % 60_000L) + 20L
            delay(delayMs.milliseconds)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .clickable { dismissAction() }
            .offset {
                IntOffset(
                    x = burnInShiftX.toPx().toInt(),
                    y = burnInShiftY.toPx().toInt()
                )
            },
        contentAlignment = Alignment.Center
    ) {
        FlipClockContent(
            hourStr = hourStr,
            minuteStr = minuteStr,
            amPmStr = amPmStr,
            dateStr = dateStr,
            isPortrait = isPortrait
        )
    }
}

@Composable
private fun FlipClockContent(
    hourStr: String,
    minuteStr: String,
    amPmStr: String,
    dateStr: String,
    isPortrait: Boolean
) {
    BoxWithConstraints(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        val totalWidth = maxWidth
        val totalHeight = maxHeight

        val (tileWidth, tileHeight) = if (isPortrait) {
            val clockWidth = totalWidth * 0.88f
            val calculatedWidth = (clockWidth - 28.dp) / 2f
            val calculatedHeight = calculatedWidth * 1.15f
            calculatedWidth to calculatedHeight
        } else {
            val clockWidth = totalWidth * 0.90f
            val maxAllowedHeight = totalHeight * 0.65f
            val calculatedWidth = (clockWidth - 36.dp) / 2f
            val calculatedHeight = calculatedWidth * 1.15f
            val finalHeight = min(calculatedHeight.value, maxAllowedHeight.value).dp
            val finalWidth = finalHeight / 1.15f
            finalWidth to finalHeight
        }

        val fontSize = (tileHeight.value * 0.62f).sp

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                FlipCardTile(
                    text = hourStr,
                    modifier = Modifier.size(tileWidth, tileHeight),
                    fontSize = fontSize
                )

                // HTC-style Colon Separator
                Column(
                    modifier = Modifier
                        .padding(horizontal = if (isPortrait) 8.dp else 12.dp)
                        .height(tileHeight * 0.4f),
                    verticalArrangement = Arrangement.SpaceEvenly,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(if (isPortrait) 10.dp else 12.dp)
                            .background(Color.White.copy(alpha = 0.85f), CircleShape)
                    )
                    Box(
                        modifier = Modifier
                            .size(if (isPortrait) 10.dp else 12.dp)
                            .background(Color.White.copy(alpha = 0.85f), CircleShape)
                    )
                }

                FlipCardTile(
                    text = minuteStr,
                    modifier = Modifier.size(tileWidth, tileHeight),
                    fontSize = fontSize
                )
            }

            Spacer(modifier = Modifier.height(28.dp))

            // AM/PM & Date Label
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                if (amPmStr.isNotEmpty()) {
                    Text(
                        text = amPmStr,
                        color = Color.White.copy(alpha = 0.95f),
                        fontSize = if (isPortrait) 20.sp else 22.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(14.dp))
                }
                Text(
                    text = dateStr,
                    color = Color.White.copy(alpha = 0.75f),
                    fontSize = if (isPortrait) 15.sp else 17.sp,
                    fontWeight = FontWeight.Medium,
                    letterSpacing = 1.5.sp
                )
            }
        }
    }
}

@Composable
private fun FlipCardTile(
    text: String,
    modifier: Modifier = Modifier,
    cardColor: Color = Color(0xFF1E1E22),
    textColor: Color = Color.White,
    fontSize: TextUnit = 100.sp,
    cornerRadius: Dp = 18.dp
) {
    var oldText by remember { mutableStateOf(text) }
    var currentText by remember { mutableStateOf(text) }
    val animationProgress = remember { Animatable(1f) }

    LaunchedEffect(text) {
        if (text != currentText) {
            oldText = currentText
            currentText = text
            animationProgress.snapTo(0f)
            animationProgress.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 450, easing = FastOutSlowInEasing)
            )
            oldText = currentText
        }
    }

    val progress = animationProgress.value

    Box(
        modifier = modifier
            .shadow(elevation = 12.dp, shape = RoundedCornerShape(cornerRadius))
            .background(cardColor, shape = RoundedCornerShape(cornerRadius))
            .clip(RoundedCornerShape(cornerRadius))
    ) {
        // Base Static Background Layer
        // Top Half shows currentText (uncovered when top flap flips down)
        CardFace(
            text = currentText,
            modifier = Modifier
                .fillMaxSize()
                .clip(TopHalfShape),
            textColor = textColor,
            fontSize = fontSize
        )

        // Bottom Half shows oldText during animation (< 1f), or currentText when resting (== 1f)
        CardFace(
            text = if (progress < 1f) oldText else currentText,
            modifier = Modifier
                .fillMaxSize()
                .clip(BottomHalfShape),
            textColor = textColor,
            fontSize = fontSize
        )

        // Animated 3D Flaps
        if (progress < 0.5f) {
            // Top flap flips DOWN from 0° to -90°
            val rotation = -progress * 180f
            val shadowAlpha = (progress * 2f) * 0.35f

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        rotationX = rotation
                        cameraDistance = 12f * density
                        transformOrigin = TransformOrigin(0.5f, 0.5f)
                    }
                    .clip(TopHalfShape)
            ) {
                CardFace(
                    text = oldText,
                    modifier = Modifier.fillMaxSize(),
                    textColor = textColor,
                    fontSize = fontSize
                )
                if (shadowAlpha > 0f) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = shadowAlpha))
                    )
                }
            }
        } else if (progress < 1f) {
            // Bottom flap flips DOWN from +90° to 0°
            val rotation = (1f - progress) * 180f
            val shadowAlpha = ((1f - progress) * 2f) * 0.35f

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        rotationX = rotation
                        cameraDistance = 12f * density
                        transformOrigin = TransformOrigin(0.5f, 0.5f)
                    }
                    .clip(BottomHalfShape)
            ) {
                CardFace(
                    text = currentText,
                    modifier = Modifier.fillMaxSize(),
                    textColor = textColor,
                    fontSize = fontSize
                )
                if (shadowAlpha > 0f) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = shadowAlpha))
                    )
                }
            }
        }
    }
}

@Composable
private fun CardFace(
    text: String,
    modifier: Modifier = Modifier,
    textColor: Color = Color.White,
    fontSize: TextUnit = 100.sp
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        // Top Half Background
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.5f)
                .align(Alignment.TopCenter)
                .background(Color(0xFF28282D))
        )
        // Bottom Half Background
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.5f)
                .align(Alignment.BottomCenter)
                .background(Color(0xFF1B1B1E))
        )
        // Digit Text centered across the card
        @Suppress("DEPRECATION")
        Text(
            text = text,
            color = textColor,
            fontSize = fontSize,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            style = TextStyle(
                platformStyle = PlatformTextStyle(
                    includeFontPadding = false
                )
            )
        )
        // Center split crease line
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .fillMaxWidth()
                .height(2.dp)
                .background(Color(0xFF0F0F12))
        )
        // Center split bottom highlight edge
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .offset(y = 1.dp)
                .fillMaxWidth()
                .height(0.5.dp)
                .background(Color(0xFF3B3B42))
        )
    }
}
