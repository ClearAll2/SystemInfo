package com.lkonlesoft.displayinfo.helper.dc

data class WifiConnectionInfo(
    val ssid: String,
    val linkSpeedMbps: Int,
    val signalStrengthDbm: Int,
    val frequencyMhz: Int,
    val channel: Int,
    val standard: String
)
