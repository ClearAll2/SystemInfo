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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.UUID
import kotlin.math.sqrt

object DisplayUtils {
    fun getSmallestDp(resources: Resources): Int{
        return resources.configuration.smallestScreenWidthDp
    }

    fun getDensity(resources: Resources): Int{
        return resources.displayMetrics.densityDpi
    }

    fun getScaleDensity(resources: Resources): Int{
        return resources.displayMetrics.densityDpi
    }

    fun getXDpi(resources: Resources): Int{
        return resources.displayMetrics.xdpi.toInt()
    }

    fun getYDpi(resources: Resources): Int{
        return resources.displayMetrics.ydpi.toInt()
    }

    fun getOrientation(resources: Resources): Int{
        return resources.configuration.orientation
    }

    fun getHeightPx(resources: Resources): Int{
        return resources.displayMetrics.heightPixels
    }

    fun getWidthPx(resources: Resources): Int{
        return resources.displayMetrics.widthPixels
    }

    fun getHeightDp(resources: Resources): Int{
        return resources.configuration.screenHeightDp
    }

    fun getWidthDp(resources: Resources): Int{
        return resources.configuration.screenWidthDp
    }

    fun getTouchScreen(resources: Resources): Int{
        return resources.configuration.touchscreen
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun getIsHdr(resources: Resources): Boolean{
        return resources.configuration.isScreenHdr
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun getIsScreenWideColorGamut(resources: Resources): Boolean{
        return resources.configuration.isScreenWideColorGamut
    }

    @RequiresApi(Build.VERSION_CODES.R)
    fun getDisPlayType(context: Context): String{
        return context.display.name
    }

    @RequiresApi(Build.VERSION_CODES.R)
    fun getDisplayRefreshRate(context: Context): Int{
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

    fun calculateScreenSizeInInches(context: Context): Float {
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

}