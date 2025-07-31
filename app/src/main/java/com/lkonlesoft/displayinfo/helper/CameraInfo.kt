package com.lkonlesoft.displayinfo.helper

import android.hardware.camera2.CameraCharacteristics


data class CameraInfo(
    val id: String,
    val lensFacing: String,
    val sensorOrientation: Int?,
    val hardwareLevel: String,
    val megapixels: Double?,
    val maxAperture: Float?,
    val focalLength: Float?,
    val hasFlash: Boolean,
    val maxZoomRatio: Float?,
    val minZoomRatio: Float?,
    val isVideoStabilizationSupported: Boolean,
    val videoStabilizationModes: List<VideoStabilizationMode> = emptyList(),
    val physicalCameraIds: Set<String> = emptySet()
)

// Enum to represent video stabilization modes
enum class VideoStabilizationMode {
    OFF, // No stabilization
    EIS,  // Digital stabilization (Camera2 API)
    OIS, // Optical Image Stabilization (OIS) for preview
    UNKNOWN;

    companion object {
        fun fromCamera2Mode(mode: Int): VideoStabilizationMode = when (mode) {
            CameraCharacteristics.CONTROL_VIDEO_STABILIZATION_MODE_OFF -> OFF
            CameraCharacteristics.CONTROL_VIDEO_STABILIZATION_MODE_ON -> EIS
            CameraCharacteristics.CONTROL_VIDEO_STABILIZATION_MODE_PREVIEW_STABILIZATION -> OIS
            else -> UNKNOWN
        }
    }
}