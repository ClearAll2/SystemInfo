package com.lkonlesoft.displayinfo.helper

import android.content.ClipData
import android.content.ClipDescription
import android.content.ClipboardManager
import android.content.Context
import android.content.pm.PackageManager
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.PersistableBundle
import android.util.Log
import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import androidx.compose.ui.graphics.Color
import androidx.core.content.ContextCompat
import androidx.core.graphics.createBitmap
import androidx.core.graphics.drawable.IconCompat
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
        level > 20 -> Color(0xFFFFC107)
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

fun Context.copyTextToClipboard(
    text: String,
    label: CharSequence = "Copied Text",
    isSensitive: Boolean = false
): Boolean {

    val clipboard = getSystemService(ClipboardManager::class.java)
    return try {
        val clip = ClipData.newPlainText(label, text).apply {
            if (isSensitive && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                description.extras = PersistableBundle().apply {
                    putBoolean(ClipDescription.EXTRA_IS_SENSITIVE, true)
                }
            }
        }
        clipboard.setPrimaryClip(clip)
        true
    } catch (_: Exception) {
        false
    }
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


fun getSystemProperty(key: String): String? {
    return try {
        Runtime.getRuntime()
            .exec("getprop $key")
            .inputStream
            .bufferedReader()
            .use { it.readLine() }
            ?.trim()
    } catch (_: Exception) {
        null
    }
}

fun buildDetailsSubTextSetting(resources: Resources, vararg resIds: Int): String {
    return when (resIds.size) {
        0 -> ""
        1 -> resources.getString(resIds[0])
        2 -> "${resources.getString(resIds[0])} & ${resources.getString(resIds[1])}"
        else -> {
            val lastString = resources.getString(resIds.last())
            // Drop the last element to join the rest with commas
            val remainingStrings = resIds.dropLast(1).joinToString(", ") { resources.getString(it) }
            "$remainingStrings & $lastString" // Added the Oxford comma for grammatical correctness
        }
    }
}

fun createTintedIconCompat(
    context: Context,
    @DrawableRes resId: Int,
    @ColorInt tintColor: Int
): IconCompat {

    val drawable = ContextCompat.getDrawable(context, resId)?.mutate()
        ?: return IconCompat.createWithResource(context, resId) // fallback

    // Force tint with SRC_IN (most reliable across versions)
    drawable.colorFilter = PorterDuffColorFilter(tintColor, PorterDuff.Mode.SRC_IN)

    // Create bitmap with explicit config for better compatibility
    val width = drawable.intrinsicWidth.coerceAtLeast(1)
    val height = drawable.intrinsicHeight.coerceAtLeast(1)

    val bitmap = createBitmap(width, height)

    val canvas = Canvas(bitmap)
    drawable.setBounds(0, 0, width, height)
    drawable.draw(canvas)

    return IconCompat.createWithBitmap(bitmap)
}