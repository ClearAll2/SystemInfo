package com.lkonlesoft.displayinfo.view

import android.os.Build
import android.os.Bundle
import android.view.OrientationEventListener
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.lkonlesoft.displayinfo.ui.theme.ScreenInfoTheme
import com.lkonlesoft.displayinfo.view.components.ChargingScreen

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