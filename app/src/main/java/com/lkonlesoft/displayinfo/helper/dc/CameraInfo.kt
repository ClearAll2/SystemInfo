package com.lkonlesoft.displayinfo.helper.dc


data class CameraInfo(
    val id: String,
    val lensFacing: String,
    val sensorOrientation: Int?,
    val hardwareLevel: String,
    val megapixels: Double?,
    val maxAperture: Float?,
    val focalLength: Float?,
    val focalLength35mm: Float?,
    val sensorSize: String?,
    val isoRange: String?,
    val exposureTimeRange: String?,
    val hasFlash: Boolean,
    val maxZoomRatio: Float?,
    val isVideoStabilizationSupported: Boolean,
    val videoStabilizationModes: List<String> = emptyList(),
    val opticalStabilizationSupported: Boolean = false,
    val autoFocusModes: List<String> = emptyList(),
    val physicalCameraIds: Set<String> = emptySet(),
    val videoCapabilities: List<String> = emptyList()
)