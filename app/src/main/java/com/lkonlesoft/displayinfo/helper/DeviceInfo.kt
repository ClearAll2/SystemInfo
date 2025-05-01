package com.lkonlesoft.displayinfo.helper

data class DeviceInfo(
    val name: Int,
    val value: Any,
    val extra: String = "",
    val type: Int = 0
)
