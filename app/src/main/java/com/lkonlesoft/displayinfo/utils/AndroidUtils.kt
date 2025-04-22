package com.lkonlesoft.displayinfo.utils

import android.os.Build
import androidx.annotation.RequiresApi
import com.lkonlesoft.displayinfo.helper.getKernelVersion

object AndroidUtils {
    fun getAndroidVersion(): String {
        return Build.VERSION.RELEASE ?: "Unknown"
    }

    fun getApiLevel(): Int {
        return Build.VERSION.SDK_INT
    }

    @RequiresApi(Build.VERSION_CODES.M)
    fun getSecurityPatch(): String {
        return Build.VERSION.SECURITY_PATCH ?: "Unknown"
    }

    fun getSdkName(): String {
        return try {
            Build.VERSION_CODES::class.java.fields
                .firstOrNull { it.getInt(null) == Build.VERSION.SDK_INT }
                ?.name ?: "Unknown"
        } catch (e: Exception) {
            "Unavailable"
        }
    }

    fun getId(): String {
        return Build.ID ?: "Unknown"
    }

    fun getDisplay(): String {
        return Build.DISPLAY ?: "Unknown"
    }

    fun getIncremental(): String {
        return Build.VERSION.INCREMENTAL ?: "Unknown"
    }

    fun getCodename(): String {
        return Build.VERSION.CODENAME ?: "Unknown"
    }

    fun getType(): String {
        return Build.TYPE ?: "Unknown"
    }

    fun getTags(): String {
        return Build.TAGS ?: "Unknown"
    }

    fun getFingerprint(): String {
        return Build.FINGERPRINT ?: "Unknown"
    }

    fun getHost(): String {
        return Build.HOST ?: "Unknown"
    }

    fun getKernel(): String {
        return getKernelVersion() ?: "Unknown"
    }

    fun getHardware(): String {
        return Build.HARDWARE ?: "Unknown"
    }

    fun getBoard(): String {
        return Build.BOARD ?: "Unknown"
    }

    fun getBootloader(): String {
        return Build.BOOTLOADER ?: "Unknown"
    }
}