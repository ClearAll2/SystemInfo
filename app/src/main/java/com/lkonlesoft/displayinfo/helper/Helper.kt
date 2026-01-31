package com.lkonlesoft.displayinfo.helper

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.util.Log
import androidx.compose.ui.graphics.Color
import androidx.core.content.ContextCompat
import androidx.core.graphics.createBitmap
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader

fun getKernelVersion(): String? {
    return try {
        val p = Runtime.getRuntime().exec("uname -a")
        val inputStream: InputStream? = if (p.waitFor() == 0) {
            p.inputStream
        }
        else {
            p.errorStream
        }
        val br = BufferedReader(InputStreamReader(inputStream))
        val line = br.readLine()
        Log.i("Kernel Version", line)
        br.close()
        line
    }
    catch (ex: Exception) {
        "ERROR: " + ex.message
    }
}

fun getBatteryLevelColor(level: Long): Color {
    return when {
        level >= 80 -> Color(0xFF4CAF50)
        level >= 30 -> Color(0xFFFFC107)
        else -> Color(0xFFF44336)
    }
}

fun getMemoryLevelColor(level: Long): Color {
    return when {
        level < 40 -> Color(0xFF4CAF50)
        level in 40..80 -> Color(0xFFFFC107)
        else -> Color(0xFFF44336)
    }
}

fun Context.hasPermission(permission: String): Boolean {
    return ContextCompat.checkSelfPermission(
        this,
        permission
    ) == PackageManager.PERMISSION_GRANTED
}

fun Context.copyTextToClipboard(text: String) {
    val clipboard = this.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    val clip = ClipData.newPlainText("Cp", text)
    clipboard.setPrimaryClip(clip)
}

fun Drawable.toBitmap(): Bitmap {
    return if (this is BitmapDrawable && this.bitmap != null) {
        this.bitmap
    } else {
        val bitmap = createBitmap(intrinsicWidth.coerceAtLeast(1), intrinsicHeight.coerceAtLeast(1))
        val canvas = Canvas(bitmap)
        setBounds(0, 0, canvas.width, canvas.height)
        draw(canvas)
        bitmap
    }
}



