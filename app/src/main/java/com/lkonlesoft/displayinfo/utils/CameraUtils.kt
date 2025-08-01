package com.lkonlesoft.displayinfo.utils

import android.content.Context
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.os.Build
import com.lkonlesoft.displayinfo.R
import com.lkonlesoft.displayinfo.helper.CameraInfo
import com.lkonlesoft.displayinfo.helper.DeviceInfo

class CameraUtils (private val context: Context) {
    private val cameraManager by lazy {
        context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
    }

    fun getAllData(): List<List<DeviceInfo>> {
        val cameraInfo = getCameraInfo()
        return cameraInfo.map { camera ->
            listOf(
                DeviceInfo(R.string.id, camera.id),
                DeviceInfo(R.string.lens_facing, camera.lensFacing),
                DeviceInfo(R.string.sensor_orientation, camera.sensorOrientation.toString()),
                DeviceInfo(R.string.hardware_level, camera.hardwareLevel),
                DeviceInfo(R.string.megapixels, camera.megapixels?.toString() ?: context.getString(R.string.unknown)),
                DeviceInfo(R.string.max_aperture, camera.maxAperture?.toString() ?: context.getString(R.string.unknown)),
                DeviceInfo(R.string.focal_length, camera.focalLength?.toString() ?: context.getString(R.string.unknown)),
                DeviceInfo(R.string.has_flash, if (camera.hasFlash) context.getString(R.string.yes) else context.getString(R.string.no)),
                DeviceInfo(R.string.max_zoom_ratio, camera.maxZoomRatio?.toString() ?: context.getString(R.string.unknown)),
                DeviceInfo(R.string.min_zoom_ratio, camera.minZoomRatio?.toString() ?: context.getString(R.string.unknown)),
                DeviceInfo(R.string.is_stabilization_supported, if (camera.isVideoStabilizationSupported) context.getString(R.string.supported) else context.getString(R.string.not_supported)),
                DeviceInfo(R.string.stabilization_modes, camera.videoStabilizationModes.joinToString(", ")),
                if (camera.physicalCameraIds.isNotEmpty())
                    DeviceInfo(R.string.physical_camera_id, camera.physicalCameraIds.joinToString(",")) else
                    DeviceInfo(R.string.physical_camera_id, context.getString(R.string.unknown))
            )

        }
    }

    fun getCameraInfo() : List<CameraInfo>{
        val infoList = cameraManager.cameraIdList.map { cameraId ->
            val characteristics = cameraManager.getCameraCharacteristics(cameraId)
            // Determine which way the camera faces.
            val lensFacingValue = characteristics.get(CameraCharacteristics.LENS_FACING)
            val lensFacing = when (lensFacingValue) {
                CameraCharacteristics.LENS_FACING_FRONT -> context.getString(R.string.front)
                CameraCharacteristics.LENS_FACING_BACK -> context.getString(R.string.back)
                CameraCharacteristics.LENS_FACING_EXTERNAL -> context.getString(R.string.external)
                else -> context.getString(R.string.unknown)
            }
            // Retrieve sensor orientation.
            val sensorOrientation = characteristics.get(CameraCharacteristics.SENSOR_ORIENTATION) ?: 0
            // Determine supported hardware level.
            val hardwareLevelInt = characteristics.get(CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL)
            val hardwareLevel = when (hardwareLevelInt) {
                CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_LEGACY -> context.getString(R.string.legacy)
                CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_LIMITED -> context.getString(R.string.limited)
                CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_FULL -> context.getString(R.string.full)
                CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_3 -> context.getString(R.string.lv3)
                else -> context.getString(R.string.unknown)
            }
            // Compute megapixels from sensor pixel array size.
            val pixelArraySize = characteristics.get(CameraCharacteristics.SENSOR_INFO_PIXEL_ARRAY_SIZE)
            val megapixels = pixelArraySize?.let {
                it.width.toLong() * it.height.toLong() / 1_000_000.0
            }
            val hasFlash = characteristics.get(
                CameraCharacteristics.FLASH_INFO_AVAILABLE
            ) ?: false
            // Zoom capabilities
            val maxZoomRatio = characteristics.get(
                CameraCharacteristics.SCALER_AVAILABLE_MAX_DIGITAL_ZOOM
            )
            val minZoomRatio = if (maxZoomRatio != null) 1.0f else null
            val isVideoStabilizationSupported = characteristics.get(
                CameraCharacteristics.CONTROL_AVAILABLE_VIDEO_STABILIZATION_MODES
            )?.contains(CameraCharacteristics.CONTROL_VIDEO_STABILIZATION_MODE_ON) ?: false
            val videoStabilizationModes = characteristics.get(
                CameraCharacteristics.CONTROL_AVAILABLE_VIDEO_STABILIZATION_MODES
            )?.map { fromCamera2Mode(it) } ?: emptyList()

            // Retrieve available aperture(s) and use the smallest f-number as maximum aperture.
            val apertures = characteristics.get(CameraCharacteristics.LENS_INFO_AVAILABLE_APERTURES)
            val maxAperture = apertures?.minOrNull() // Lower f-number: larger aperture

            // Retrieve focal length, usually the first value is selected.
            val focalLengths = characteristics.get(CameraCharacteristics.LENS_INFO_AVAILABLE_FOCAL_LENGTHS)
            val focalLength = focalLengths?.firstOrNull()
            // Attempt to get physical camera IDs if available (API 28+).
            val physicalCameraIds = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                characteristics.physicalCameraIds
            } else {
                emptySet()
            }
            CameraInfo(
                id = cameraId,
                lensFacing = lensFacing,
                sensorOrientation = sensorOrientation,
                hardwareLevel = hardwareLevel,
                megapixels = megapixels,
                maxAperture = maxAperture,
                focalLength = focalLength,
                hasFlash = hasFlash,
                maxZoomRatio = maxZoomRatio,
                minZoomRatio = minZoomRatio,
                isVideoStabilizationSupported = isVideoStabilizationSupported,
                videoStabilizationModes = videoStabilizationModes,
                physicalCameraIds = physicalCameraIds
            )
        }
        return infoList
    }

    fun fromCamera2Mode(mode: Int): String = when (mode) {
        CameraCharacteristics.CONTROL_VIDEO_STABILIZATION_MODE_ON -> context.getString(R.string.eis)
        CameraCharacteristics.CONTROL_VIDEO_STABILIZATION_MODE_PREVIEW_STABILIZATION -> context.getString(R.string.ois)
        else -> context.getString(R.string.unknown)
    }
}