package com.lkonlesoft.displayinfo.utils

import android.os.Build
import androidx.annotation.RequiresApi

object SystemUtils {
    fun getModel(): String {
        return Build.MODEL ?: "Unknown"
    }

    fun getProduct(): String {
        return Build.PRODUCT ?: "Unknown"
    }

    fun getDevice(): String {
        return Build.DEVICE ?: "Unknown"
    }

    fun getBoard(): String {
        return Build.BOARD ?: "Unknown"
    }

    fun getManufacturer(): String {
        return Build.MANUFACTURER ?: "Unknown"
    }

    fun getBrand(): String {
        return Build.BRAND ?: "Unknown"
    }

    @RequiresApi(Build.VERSION_CODES.S)
    fun getSku(): String {
        return Build.SKU
    }

    fun getRadio(): String {
        return Build.getRadioVersion() ?: "Unknown"
    }

    fun getInstructions(): String {
        val supportedABIS = Build.SUPPORTED_ABIS
        return supportedABIS?.joinToString(", ") ?: "Unknown"
    }
}