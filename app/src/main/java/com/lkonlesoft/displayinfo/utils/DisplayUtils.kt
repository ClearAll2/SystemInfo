package com.lkonlesoft.displayinfo.utils

import android.content.Context
import android.content.res.Resources
import android.media.MediaDrm
import android.os.Build
import android.util.Base64
import android.util.DisplayMetrics
import android.view.WindowManager
import androidx.annotation.RequiresApi
import androidx.window.layout.WindowMetricsCalculator
import com.lkonlesoft.displayinfo.R
import com.lkonlesoft.displayinfo.helper.DeviceInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.UUID
import kotlin.math.sqrt

class DisplayUtils (private val context: Context, private val resources: Resources) {
    fun getSmallestDp(): Int{
        return resources.configuration.smallestScreenWidthDp
    }

    fun getDensity(): Int{
        return resources.displayMetrics.densityDpi
    }

    fun getScaleDensity(): Int{
        return resources.displayMetrics.densityDpi
    }

    fun getXDpi(): Int{
        return resources.displayMetrics.xdpi.toInt()
    }

    fun getYDpi(): Int{
        return resources.displayMetrics.ydpi.toInt()
    }

    fun getOrientation(): Int{
        return resources.configuration.orientation
    }

    fun getHeightPx(): Int{
        return resources.displayMetrics.heightPixels
    }

    fun getWidthPx(): Int{
        return resources.displayMetrics.widthPixels
    }

    fun getHeightDp(): Int{
        return resources.configuration.screenHeightDp
    }

    fun getWidthDp(): Int{
        return resources.configuration.screenWidthDp
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
                "algorithms"
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

            mediaDrm.close()

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

            mediaDrm.close()

        } catch (e: Exception) {
            info["error"] = e.message ?: "Error accessing MediaDrm"
        }

        info
    }

    fun calculateScreenSizeInInches(): Float {
        val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val displayMetrics = DisplayMetrics()

        val widthPixels: Int
        val heightPixels: Int
        val xdpi: Float
        val ydpi: Float

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            // Android 11+ (API 30+)
            val windowMetrics = WindowMetricsCalculator.getOrCreate()
                .computeCurrentWindowMetrics(context)

            val bounds = windowMetrics.bounds
            widthPixels = bounds.width()
            heightPixels = bounds.height()

            val metrics = context.resources.displayMetrics
            xdpi = metrics.xdpi
            ydpi = metrics.ydpi
        } else {
            // Older Android (API < 30)
            @Suppress("DEPRECATION")
            windowManager.defaultDisplay.getMetrics(displayMetrics)

            widthPixels = displayMetrics.widthPixels
            heightPixels = displayMetrics.heightPixels
            xdpi = displayMetrics.xdpi
            ydpi = displayMetrics.ydpi
        }

        val widthInches = widthPixels / xdpi
        val heightInches = heightPixels / ydpi

        return sqrt((widthInches * widthInches + heightInches * heightInches))
    }

    fun getAllData(): List<DeviceInfo>{
        return listOf(
            DeviceInfo(R.string.size, "%.2f".format(calculateScreenSizeInInches()), " inches"),
            DeviceInfo(R.string.height_px, getHeightPx().toString()),
            DeviceInfo(R.string.width_px, getWidthPx().toString()),
            DeviceInfo(R.string.smallest_dp, getSmallestDp().toString()),
            DeviceInfo(R.string.screen_dpi, getDensity().toString()),
            DeviceInfo(R.string.scale_density, getScaleDensity().toString()),
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