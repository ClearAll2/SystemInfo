package com.lkonlesoft.displayinfo.utils

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.SystemClock
import android.provider.Settings
import androidx.annotation.RequiresApi
import com.lkonlesoft.displayinfo.R
import com.lkonlesoft.displayinfo.helper.DeviceInfo
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class SystemUtils(private val context: Context) {

    fun getDeviceData(): List<DeviceInfo>{
        return listOf(
            DeviceInfo(R.string.model, getModel()),
            DeviceInfo(R.string.product, getProduct()),
            DeviceInfo(R.string.device, getDevice()),
            DeviceInfo(R.string.board, getBoard()),
            DeviceInfo(R.string.manufacturer, getManufacturer()),
            DeviceInfo(R.string.brand, getBrand()),
            DeviceInfo(R.string.sku, if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) getSku() else context.getString(R.string.unknown)),
            DeviceInfo(R.string.radio, getRadio()),
            DeviceInfo(R.string.instruction_sets, getInstructions()),
            DeviceInfo(R.string.up_time, getUptime()),
            DeviceInfo(R.string.boot_time, getBootTime()),
        )
    }

    fun getRootData(): List<DeviceInfo>{
        return listOf(
            DeviceInfo(R.string.root, if (isDeviceRooted()) context.getString(R.string.yes) else context.getString(R.string.no)),
            DeviceInfo(R.string.has_magisk, if (isMagiskPresent()) context.getString(R.string.yes) else context.getString(R.string.not_found_or_hidden)),
            DeviceInfo(R.string.has_magisk_properties, if (hasMagiskProperties()) context.getString(R.string.yes) else context.getString(R.string.not_found_or_hidden))
        )
    }

    fun getExtraData(): List<DeviceInfo>{
        return listOf(
            DeviceInfo(R.string.usb_debug, if (isUsbDebuggingEnabled()) context.getString(R.string.enabled) else context.getString(R.string.disabled)),
            DeviceInfo(R.string.treble, if (isTrebleSupported()) context.getString(R.string.supported) else context.getString(R.string.not_supported)),
            DeviceInfo(R.string.seamless_update, if (isSeamlessUpdateSupported()) context.getString(R.string.supported) else context.getString(R.string.not_supported)),
            DeviceInfo(R.string.active_slot, if (isSeamlessUpdateSupported() && getActiveSlot() != context.getString(R.string.unknown)) getActiveSlot() else context.getString(R.string.n_a)),
            DeviceInfo(R.string.device_features, getAllSystemFeatures().joinToString("\n")),
        )
    }

    fun getDashboardData(): List<DeviceInfo>{
        return listOf(
            DeviceInfo(R.string.model, getModel()),
            DeviceInfo(R.string.product, getProduct()),
            DeviceInfo(R.string.device, getDevice()),
            DeviceInfo(R.string.manufacturer, getManufacturer()),
            DeviceInfo(R.string.up_time, getUptime()),
        )
    }


    fun getModel(): String {
        return Build.MODEL ?: context.getString(R.string.unknown)
    }

    fun getProduct(): String {
        return Build.PRODUCT ?: context.getString(R.string.unknown)
    }

    fun getDevice(): String {
        return Build.DEVICE ?: context.getString(R.string.unknown)
    }

    fun getBoard(): String {
        return Build.BOARD ?: context.getString(R.string.unknown)
    }

    fun getManufacturer(): String {
        return Build.MANUFACTURER ?: context.getString(R.string.unknown)
    }

    fun getBrand(): String {
        return Build.BRAND ?: context.getString(R.string.unknown)
    }

    @RequiresApi(Build.VERSION_CODES.S)
    fun getSku(): String {
        return Build.SKU
    }

    fun getRadio(): String {
        return Build.getRadioVersion() ?: context.getString(R.string.unknown)
    }

    fun getInstructions(): String {
        val supportedABIS = Build.SUPPORTED_ABIS
        return supportedABIS?.joinToString(", ") ?: context.getString(R.string.unknown)
    }

    fun getUptime(): String {
        val uptimeMillis = SystemClock.elapsedRealtime()
        val seconds = uptimeMillis / 1000
        val minutes = seconds / 60
        val hours = minutes / 60
        val days = hours / 24

        return "${days}d ${hours % 24}h ${minutes % 60}m"
    }

    fun getBootTime(): String {
        val bootTime = System.currentTimeMillis() - SystemClock.elapsedRealtime()
        val date = Date(bootTime)
        val format = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        return format.format(date)
    }

    fun getAllSystemFeatures(): List<String> {
        val pm = context.packageManager
        return pm.systemAvailableFeatures
            .mapNotNull { it?.name }  // filter out nulls (some features don't have a name)
            .sorted()
    }

    fun getSelinuxStatus(): String {
        return try {
            val process = Runtime.getRuntime().exec("getenforce")
            process.inputStream.bufferedReader().readLine()
        } catch (e: Exception) {
            "Error: ${e.message}"
        }
    }

    fun isUsbDebuggingEnabled(): Boolean {
        return Settings.Global.getInt(
            context.contentResolver,
            Settings.Global.ADB_ENABLED, 0
        ) == 1
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


    fun isTrebleSupported(): Boolean {
        return try {
            val trebleEnabled = getSystemProperty("ro.treble.enabled")
            if (trebleEnabled != null) {
                trebleEnabled.equals("true", ignoreCase = true)
            } else {
                val vendorFingerprint = getSystemProperty("ro.vendor.build.fingerprint")
                !vendorFingerprint.isNullOrEmpty()
            }
        } catch (_: Exception) {
            false
        }
    }

    fun isSeamlessUpdateSupported(): Boolean {
        // Check the legacy A/B update flag.
        val abUpdate = getSystemProperty("ro.build.ab_update")
        if (abUpdate?.equals("true", ignoreCase = true) == true) {
            return true
        }

        // Check for Virtual A/B support, which is common on Android 11+ devices.
        val virtualAb = getSystemProperty("ro.virtual_ab.enabled")
        if (virtualAb?.equals("true", ignoreCase = true) == true) {
            return true
        }

        // Fallback: If an active slot is defined, it suggests the device operates with A/B partitioning.
        val slot = getSystemProperty("ro.boot.slot_suffix")
        return !slot.isNullOrEmpty()

        // If none of the checks are positive, seamless updates likely aren't supported.
    }


    fun getActiveSlot(): String {
        return try {
            getSystemProperty("ro.boot.slot_suffix") ?: "Unknown"
        } catch (_: Exception) {
            context.getString(R.string.unknown)
        }
    }

    fun isDeviceRooted(): Boolean {
        // Check common su binary paths
        val suPaths = listOf(
            "/system/bin/su",
            "/system/xbin/su",
            "/sbin/su",
            "/system/su",
            "/system/bin/.ext/.su",
            "/system/usr/we-need-root/su",
            "/data/local/xbin/su",
            "/data/local/bin/su",
            "/data/local/su",
            "/su/bin/su"
        )

        // Check for su binary existence
        val hasSuBinary = suPaths.any { path ->
            try {
                File(path).exists()
            } catch (_: SecurityException) {
                false // Handle permission denied gracefully
            }
        }

        // Additional root detection checks
        val isTestKeys = Build.TAGS?.contains("test-keys") == true
        val hasSuperuserApk = try {
            File("/system/app/Superuser.apk").exists() || File("/system/app/SuperSU.apk").exists()
        } catch (_: SecurityException) {
            false
        }

        val canExecuteSu = try {
            Runtime.getRuntime().exec(arrayOf("which", "su")).waitFor() == 0
        } catch (_: IOException) {
            false
        } catch (_: SecurityException) {
            false
        }

        return hasSuBinary || isTestKeys || hasSuperuserApk || canExecuteSu
    }

    fun isMagiskPresent(): Boolean {
        val pm = context.packageManager
        return try {
            pm.getPackageInfo("com.topjohnwu.magisk", 0)
            true
        } catch (_: PackageManager.NameNotFoundException) {
            false
        }
    }

    fun hasMagiskProperties(): Boolean {
        return try {
            val props = Runtime.getRuntime().exec("getprop").inputStream.bufferedReader().readText()
            props.contains("magisk") || props.contains("init.magisk")
        } catch (_: IOException) {
            false
        }
    }

}