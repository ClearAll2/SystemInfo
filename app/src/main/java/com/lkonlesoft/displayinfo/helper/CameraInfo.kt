package com.lkonlesoft.displayinfo.helper

data class CameraInfo(
    val id: String,
    val lensFacing: String,
    val sensorOrientation: Int?,
    val hardwareLevel: String,
    val megapixels: Double?,
    val maxAperture: Float?,
    val focalLength: Float?,
    val physicalCameraIds: Set<String> = emptySet()
)