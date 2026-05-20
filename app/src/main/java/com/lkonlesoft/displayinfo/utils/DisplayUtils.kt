package com.lkonlesoft.displayinfo.utils

import android.content.Context
import android.content.res.Resources
import android.graphics.Rect
import android.hardware.display.DisplayManager
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.window.layout.WindowMetricsCalculator
import com.lkonlesoft.displayinfo.R
import com.lkonlesoft.displayinfo.helper.dc.DeviceInfo
import com.lkonlesoft.displayinfo.helper.dc.DisplayInfo
import kotlin.math.hypot

class DisplayUtils(private val context: Context, private val resources: Resources) {
    private val calculator by lazy { WindowMetricsCalculator.getOrCreate() }
    private val displayManager by lazy { context.getSystemService(Context.DISPLAY_SERVICE) as DisplayManager }
    private val bounds: Rect by lazy { calculator.computeMaximumWindowMetrics(context).bounds }

    private fun getDensity(): Float = resources.displayMetrics.density
    private fun getScreenDpi(): Int = resources.displayMetrics.densityDpi
    private fun getXDpi(): Float = resources.displayMetrics.xdpi
    private fun getYDpi(): Float = resources.displayMetrics.ydpi
    private fun getWidthPx(): Int = bounds.width()
    private fun getHeightPx(): Int = bounds.height()
    private fun getWidthDp(): Int = (getWidthPx() / getDensity()).toInt()
    private fun getHeightDp(): Int = (getHeightPx() / getDensity()).toInt()
    private fun getSmallestDp(): Int = minOf(getWidthDp(), getHeightDp())

    @RequiresApi(Build.VERSION_CODES.R)
    private fun getDisplayRefreshRate(): Int = context.display.refreshRate.toInt()

    fun getAllDisplayDetails(): List<List<DeviceInfo>> {
        val displays = displayManager.getDisplays(null)
        return displays.map { display ->
            val metrics = android.util.DisplayMetrics()
            @Suppress("DEPRECATION")
            display.getRealMetrics(metrics)

            val displayInfo = DisplayInfo(
                size = calculateScreenSizeForDisplay(display),
                height = (metrics.heightPixels / metrics.density).toInt().toString(),
                width = (metrics.widthPixels / metrics.density).toInt().toString(),
                smallestDp = minOf(
                    (metrics.heightPixels / metrics.density).toInt(),
                    (metrics.widthPixels / metrics.density).toInt()
                ).toString(),
                screenDpi = metrics.densityDpi.toString(),
                scaleDensity = "%.2f".format(resources.configuration.fontScale * metrics.density),
                xdpi = "%.2f".format(metrics.xdpi),
                ydpi = "%.2f".format(metrics.ydpi),
                orientation = if (metrics.heightPixels >= metrics.widthPixels) context.getString(R.string.portrait) else context.getString(R.string.landscape),
                heightPx = metrics.heightPixels.toString(),
                widthPx = metrics.widthPixels.toString(),
                touch = if (resources.configuration.touchscreen == 1) context.getString(R.string.no_touch) else context.getString(R.string.finger),
                hasHdr = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) display.isHdr else false,
                hasWCG = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) display.isWideColorGamut else false,
                type = display.name,
                rr = display.refreshRate.toInt().toString(),
                modes = display.supportedModes.joinToString("\n") { "${it.physicalWidth} x ${it.physicalHeight} @ ${it.refreshRate.toInt()} Hz" }
            )
            mapDisplayInfoToDeviceInfoList(displayInfo)
        }
    }


    private fun calculateScreenSizeForDisplay(display: android.view.Display): String {
        val metrics = android.util.DisplayMetrics()
        @Suppress("DEPRECATION")
        display.getRealMetrics(metrics)
        val xDpi = if (metrics.xdpi < 1) metrics.densityDpi.toFloat() else metrics.xdpi
        val yDpi = if (metrics.ydpi < 1) metrics.densityDpi.toFloat() else metrics.ydpi
        return "%.2f".format(
            hypot(
                metrics.widthPixels.toDouble() / xDpi,
                metrics.heightPixels.toDouble() / yDpi
            )
        )
    }

    private fun calculateScreenSizeInInches(): Double {
        val xDpi = if (getXDpi() < 1) getScreenDpi().toFloat() else getXDpi()
        val yDpi = if (getYDpi() < 1) getScreenDpi().toFloat() else getYDpi()
        return hypot(getWidthPx().toDouble() / xDpi, getHeightPx().toDouble() / yDpi)
    }

    private fun mapDisplayInfoToDeviceInfoList(info: DisplayInfo): List<DeviceInfo> {
        return listOf(
            DeviceInfo(R.string.size, info.size, " inches"),
            DeviceInfo(R.string.height_px, info.heightPx),
            DeviceInfo(R.string.width_px, info.widthPx),
            DeviceInfo(R.string.smallest_dp, info.smallestDp),
            DeviceInfo(R.string.screen_dpi, info.screenDpi),
            DeviceInfo(R.string.scale_density, info.scaleDensity),
            DeviceInfo(R.string.xdpi, info.xdpi),
            DeviceInfo(R.string.ydpi, info.ydpi),
            DeviceInfo(R.string.orientation, info.orientation),
            DeviceInfo(R.string.height_dp, info.height),
            DeviceInfo(R.string.width_dp, info.width),
            DeviceInfo(R.string.touch_screen, info.touch),
            DeviceInfo(
                R.string.hdr,
                if (info.hasHdr) context.getString(R.string.supported) else context.getString(R.string.not_supported)
            ),
            DeviceInfo(
                R.string.wcg,
                if (info.hasWCG) context.getString(R.string.supported) else context.getString(R.string.not_supported)
            ),
            DeviceInfo(R.string.display_type, info.type),
            DeviceInfo(R.string.refresh_rate, info.rr, " Hz"),
            DeviceInfo(R.string.capacity, info.modes)
        )
    }

    fun getDashboardData(): List<DeviceInfo> = listOf(
        DeviceInfo(R.string.display_pixels, "${getHeightPx()} x ${getWidthPx()}"),
        DeviceInfo(R.string.size, "%.2f".format(calculateScreenSizeInInches()), "\""),
        DeviceInfo(R.string.smallest_dp, getSmallestDp().toString()),
        DeviceInfo(R.string.xdpi, "%.2f".format(getXDpi())),
        DeviceInfo(R.string.ydpi, "%.2f".format(getYDpi())),
        DeviceInfo(R.string.height_dp, getHeightDp().toString()),
        DeviceInfo(R.string.width_dp, getWidthDp().toString()),
        DeviceInfo(
            R.string.refresh_rate,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) getDisplayRefreshRate()
                .toString() else "60",
            " Hz"
        ),
    )
}
