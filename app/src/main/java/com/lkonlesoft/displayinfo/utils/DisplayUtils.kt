package com.lkonlesoft.displayinfo.utils

import android.content.Context
import android.content.res.Resources
import android.graphics.Point
import android.graphics.Rect
import android.hardware.display.DisplayManager
import android.media.MediaDrm
import android.os.Build
import android.util.Base64
import android.view.WindowManager
import androidx.annotation.RequiresApi
import androidx.window.layout.WindowMetricsCalculator
import com.lkonlesoft.displayinfo.R
import com.lkonlesoft.displayinfo.helper.DeviceInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.UUID
import kotlin.math.hypot

class DisplayUtils (private val context: Context, private val resources: Resources) {
    private val calculator = WindowMetricsCalculator.getOrCreate()
    private val windowManager by lazy {
        context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
    }
    private val displayManager by lazy {
        context.getSystemService(Context.DISPLAY_SERVICE) as DisplayManager
    }
    private val maxWindowMetrics by lazy {
        calculator.computeMaximumWindowMetrics(context)
    }
    private val bounds: Rect by lazy {
        maxWindowMetrics.bounds
    }

    fun getSmallestDp(): Int{
        return minOf(getWidthDp(), getHeightDp())
    }

    fun getDensity(): Float{
        return resources.displayMetrics.density
    }

    fun getScreenDpi(): Int{
        return resources.displayMetrics.densityDpi
    }

    fun getScaleDensity(): Float {
        return context.resources.configuration.fontScale * getDensity()
    }

    fun getXDpi(): Float{
        return resources.displayMetrics.xdpi
    }

    fun getYDpi(): Float{
        return resources.displayMetrics.ydpi
    }

    fun getOrientation(): Int{
        return resources.configuration.orientation
    }

    fun getHeightPx(): Int{
        return bounds.height()
    }

    fun getWidthPx(): Int{
        return bounds.width()
    }

    fun getHeightDp(): Int{
        return (getHeightPx() / getDensity()).toInt()
    }

    fun getWidthDp(): Int{
        return (getWidthPx() / getDensity()).toInt()
    }

    fun getTouchScreen(): Int{
        return resources.configuration.touchscreen
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun getIsHdr(): Boolean{
        return resources.configuration.isScreenHdr
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun getIsScreenWideColorGamut(): Boolean{
        return resources.configuration.isScreenWideColorGamut
    }

    @RequiresApi(Build.VERSION_CODES.R)
    fun getDisPlayType(): String{
        return context.display.name
    }

    @RequiresApi(Build.VERSION_CODES.R)
    fun getDisplayRefreshRate(): Int{
        return context.display.refreshRate.toInt()
    }


    suspend fun getWidevineInfo(): Map<String, String> = withContext(Dispatchers.IO) {
        val widevineUUID = UUID.fromString("edef8ba9-79d6-4ace-a3c8-27dcd51d21ed")
        val info = mutableMapOf<String, String>()

        try {
            val mediaDrm = MediaDrm(widevineUUID)

            val customProps = listOf(
                "vendor",
                "version",
                "securityLevel",
                "algorithms",
                "hdcpLevel",
                "maxHdcpLevel",
                "usageReportingSupport",
                "maxNumberOfSessions",
                "numberOfOpenSessions",
                "systemId"
            )

            for (prop in customProps) {
                val value = try {
                    mediaDrm.getPropertyString(prop)
                } catch (_: Exception) {
                    "Unavailable"
                }
                info[prop] = value
            }

            val uniqueId = try {
                val bytes = mediaDrm.getPropertyByteArray("deviceUniqueId")
                Base64.encodeToString(bytes, Base64.NO_WRAP)
            } catch (_: Exception) {
                "Unavailable"
            }

            info["deviceUniqueId"] = uniqueId

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                mediaDrm.close()
            } else {
                @Suppress("DEPRECATION")
                mediaDrm.release()
            }

        } catch (e: Exception) {
            info["error"] = e.message ?: "Error accessing MediaDrm"
        }

        info
    }

    suspend fun getClearKeyInfo(): Map<String, String> = withContext(Dispatchers.IO) {
        val clearKeyUUID = UUID.fromString("e2719d58-a985-b3c9-781a-b030af78d30e")
        val info = mutableMapOf<String, String>()

        try {
            val mediaDrm = MediaDrm(clearKeyUUID)

            val customProps = listOf(
                "vendor",
                "version"
            )

            for (prop in customProps) {
                val value = try {
                    mediaDrm.getPropertyString(prop)
                } catch (_: Exception) {
                    "Unavailable"
                }
                info[prop] = value
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                mediaDrm.close()
            } else {
                @Suppress("DEPRECATION")
                mediaDrm.release()
            }

        } catch (e: Exception) {
            info["error"] = e.message ?: "Error accessing MediaDrm"
        }

        info
    }

    fun calculateScreenSizeInInches(): Double {
        val widthPixels = getWidthPx()
        val heightPixels = getHeightPx()
        val xDpi = getXDpi()
        val yDpi = getYDpi()
        val widthInches = widthPixels.toDouble() / xDpi
        val heightInches = heightPixels.toDouble() / yDpi
        return hypot(widthInches, heightInches)
    }

    fun calculateScreenSizeInInches2(): Double {
        val displayMetrics = resources.displayMetrics
        val (width, height) = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val bounds = bounds
            Pair(bounds.width(), bounds.height())
        } else {
            val size = Point()
            @Suppress("DEPRECATION")
            windowManager.defaultDisplay.getRealSize(size)
            Pair(size.x, size.y)
        }
        val widthInInches = width.toDouble() / displayMetrics.xdpi
        val heightInInches = height.toDouble() / displayMetrics.ydpi
        // Calculate diagonal using hypot to avoid overflow/underflow
        return hypot(widthInInches, heightInInches)
    }

    @Suppress("DEPRECATION")
    private fun getDisplayCapacity(): List<String> {
        val displays = displayManager.displays
        val displayCapacities = mutableListOf<String>()
        for (display in displays) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                val supportedModes = display.supportedModes
                for (mode in supportedModes) {
                    val width = mode.physicalWidth
                    val height = mode.physicalHeight
                    val refreshRate = mode.refreshRate.toInt()
                    displayCapacities.add("$width x $height @ $refreshRate Hz")
                }
            }
            else {
                val displayWidth = display.width
                val displayHeight = display.height
                val displayRefreshRate = display.refreshRate.toInt()
                displayCapacities.add("$displayWidth x $displayHeight @ $displayRefreshRate Hz")
            }
        }
        return displayCapacities
    }

    fun getAllData(): List<DeviceInfo>{
        return listOf(
            DeviceInfo(R.string.size, "%.2f".format(calculateScreenSizeInInches()), " inches"),
            DeviceInfo(R.string.height_px, getHeightPx().toString()),
            DeviceInfo(R.string.width_px, getWidthPx().toString()),
            DeviceInfo(R.string.smallest_dp, getSmallestDp().toString()),
            DeviceInfo(R.string.screen_dpi, getScreenDpi().toString()),
            DeviceInfo(R.string.scale_density, "%.2f".format(getScaleDensity())),
            DeviceInfo(R.string.xdpi, getXDpi().toString()),
            DeviceInfo(R.string.ydpi, getYDpi().toString()),
            DeviceInfo(R.string.orientation, if (getOrientation() == 1) context.getString(R.string.portrait) else context.getString(R.string.landscape)),
            DeviceInfo(R.string.height_dp, getHeightDp().toString()),
            DeviceInfo(R.string.width_dp, getWidthDp().toString()),
            DeviceInfo(R.string.touch_screen, if (getTouchScreen() == 1) context.getString(R.string.no_touch) else context.getString(R.string.finger)),
            DeviceInfo(R.string.hdr, if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && getIsHdr()) resources.getString(R.string.supported) else context.getString(R.string.not_supported)),
            DeviceInfo(R.string.wcg, if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && getIsScreenWideColorGamut()) resources.getString(R.string.supported) else context.getString(R.string.not_supported)),
            DeviceInfo(R.string.display_type, if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) getDisPlayType() else context.getString(R.string.unknown)),
            DeviceInfo(R.string.refresh_rate, if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) getDisplayRefreshRate().toString() else 60, " Hz"),
            DeviceInfo(R.string.capacity, getDisplayCapacity().joinToString("\n")),
        )
    }

    fun getDashboardData(): List<DeviceInfo>{
        return listOf(
            DeviceInfo(R.string.display_pixels, "${getHeightPx()} x ${getWidthPx()}"),
            DeviceInfo(R.string.size, "%.2f".format(calculateScreenSizeInInches()), "\""),
            DeviceInfo(R.string.smallest_dp, getSmallestDp().toString()),
            DeviceInfo(R.string.xdpi, getXDpi().toString()),
            DeviceInfo(R.string.ydpi, getYDpi().toString()),
            DeviceInfo(R.string.height_dp, getHeightDp().toString()),
            DeviceInfo(R.string.width_dp, getWidthDp().toString()),
            DeviceInfo(R.string.refresh_rate, if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) getDisplayRefreshRate().toString() else 60, " Hz"),
        )
    }

}