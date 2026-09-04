package com.lkonlesoft.displayinfo.utils

import android.content.Context
import android.graphics.ImageFormat
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.hardware.camera2.params.StreamConfigurationMap
import android.os.Build
import com.lkonlesoft.displayinfo.R
import com.lkonlesoft.displayinfo.helper.dc.CameraInfo
import com.lkonlesoft.displayinfo.helper.dc.DeviceInfo
import kotlin.math.sqrt

class CameraUtils (private val context: Context) {
    private val cameraManager by lazy {
        context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
    }

    fun getCameraCount(): DeviceInfo {
        return DeviceInfo(R.string.camera, cameraManager.cameraIdList.size)
    }

    fun getAllData(): List<List<DeviceInfo>> {
        val cameraInfo = getCameraInfo()
        return cameraInfo.map { camera ->
            buildList {
                add(DeviceInfo(R.string.id, camera.id))
                if (camera.physicalCameraIds.isNotEmpty()) {
                    add(DeviceInfo(R.string.physical_camera_id, camera.physicalCameraIds.joinToString(", ")))
                }
                add(DeviceInfo(R.string.lens_facing, camera.lensFacing))
                add(DeviceInfo(R.string.sensor_orientation, camera.sensorOrientation.toString(), context.getString(R.string.unit_degree)))
                add(DeviceInfo(R.string.hardware_level, camera.hardwareLevel))
                add(DeviceInfo(R.string.resolution, context.getString(R.string.format_float_2, camera.megapixels), " " + context.getString(R.string.unit_mp)))
                add(DeviceInfo(R.string.max_aperture, context.getString(R.string.format_aperture, camera.maxAperture)))
                add(DeviceInfo(R.string.focal_length, context.getString(R.string.format_float_2, camera.focalLength), " " + context.getString(R.string.unit_mm)))

                camera.focalLength35mm?.let {
                    add(DeviceInfo(R.string.focal_length_35mm, context.getString(R.string.format_float_2, it), " " + context.getString(R.string.unit_mm)))
                }

                camera.sensorSize?.let {
                    add(DeviceInfo(R.string.sensor_size, it, " " + context.getString(R.string.unit_mm)))
                }

                camera.isoRange?.let {
                    add(DeviceInfo(R.string.iso_sensitivity, it))
                }

                camera.exposureTimeRange?.let {
                    add(DeviceInfo(R.string.exposure_time, it))
                }

                add(DeviceInfo(R.string.has_flash, if (camera.hasFlash) context.getString(R.string.yes) else context.getString(R.string.no)))
                add(DeviceInfo(R.string.max_zoom_ratio, "%.2f".format(camera.maxZoomRatio)))
                add(DeviceInfo(R.string.is_stabilization_supported, if (camera.isVideoStabilizationSupported || camera.opticalStabilizationSupported) context.getString(R.string.supported) else context.getString(R.string.not_supported)))

                val stabModes = buildList {
                    if (camera.videoStabilizationModes.isNotEmpty()) addAll(camera.videoStabilizationModes)
                    if (camera.opticalStabilizationSupported) add(context.getString(R.string.ois))
                }

                add(DeviceInfo(R.string.stabilization_modes,
                    if (stabModes.isNotEmpty()) stabModes.distinct().joinToString(", ")
                    else context.getString(R.string.n_a)
                ))

                if (camera.autoFocusModes.isNotEmpty()) {
                    add(DeviceInfo(R.string.af_modes, camera.autoFocusModes.joinToString(", ")))
                }

                if (camera.videoCapabilities.isNotEmpty()) {
                    add(DeviceInfo(R.string.video_capabilities, camera.videoCapabilities.joinToString(", ")))
                }
            }
        }
    }

    fun getCameraInfo() : List<CameraInfo>{
        val infoList = cameraManager.cameraIdList.map { cameraId ->
            val characteristics = cameraManager.getCameraCharacteristics(cameraId)

            val lensFacingValue = characteristics.get(CameraCharacteristics.LENS_FACING)
            val lensFacing = when (lensFacingValue) {
                CameraCharacteristics.LENS_FACING_FRONT -> context.getString(R.string.front)
                CameraCharacteristics.LENS_FACING_BACK -> context.getString(R.string.back)
                CameraCharacteristics.LENS_FACING_EXTERNAL -> context.getString(R.string.external)
                else -> context.getString(R.string.unknown)
            }

            val sensorOrientation = characteristics.get(CameraCharacteristics.SENSOR_ORIENTATION) ?: 0
            val hardwareLevelInt = characteristics.get(CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL)
            val hardwareLevel = when (hardwareLevelInt) {
                CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_LEGACY -> context.getString(R.string.legacy)
                CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_LIMITED -> context.getString(R.string.limited)
                CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_FULL -> context.getString(R.string.full)
                CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_3 -> context.getString(R.string.lv3)
                else -> context.getString(R.string.unknown)
            }

            val pixelArraySize = characteristics.get(CameraCharacteristics.SENSOR_INFO_PIXEL_ARRAY_SIZE)
            val megapixels = pixelArraySize?.let {
                it.width.toLong() * it.height.toLong() / 1_000_000.0
            }

            val physicalSize = characteristics.get(CameraCharacteristics.SENSOR_INFO_PHYSICAL_SIZE)
            val sensorSizeStr = physicalSize?.let { context.getString(R.string.format_sensor_size, it.width, it.height) }

            val isoRange = characteristics.get(CameraCharacteristics.SENSOR_INFO_SENSITIVITY_RANGE)
            val isoRangeStr = isoRange?.let { "${it.lower} - ${it.upper}" }

            val exposureRange = characteristics.get(CameraCharacteristics.SENSOR_INFO_EXPOSURE_TIME_RANGE)
            val exposureRangeStr = exposureRange?.let {
                if (it.upper > 1_000_000_000) {
                     "%.4f - %.2f %s".format(it.lower / 1_000_000_000.0, it.upper / 1_000_000_000.0, context.getString(R.string.unit_s))
                } else {
                     "%.4f - %.2f %s".format(it.lower / 1_000_000.0, it.upper / 1_000_000.0, context.getString(R.string.unit_ms))
                }
            }

            val hasFlash = characteristics.get(CameraCharacteristics.FLASH_INFO_AVAILABLE) ?: false
            val maxZoomRatio = characteristics.get(CameraCharacteristics.SCALER_AVAILABLE_MAX_DIGITAL_ZOOM)

            val videoStabilizationModes = characteristics.get(
                CameraCharacteristics.CONTROL_AVAILABLE_VIDEO_STABILIZATION_MODES
            )?.map { fromVideoStabilizationMode(it) }?.filter { it != context.getString(R.string.unknown) } ?: emptyList()

            val isVideoStabilizationSupported = videoStabilizationModes.isNotEmpty()

            val opticalStabilizationModes = characteristics.get(
                CameraCharacteristics.LENS_INFO_AVAILABLE_OPTICAL_STABILIZATION
            )
            val opticalStabilizationSupported = opticalStabilizationModes?.contains(CameraCharacteristics.LENS_OPTICAL_STABILIZATION_MODE_ON) ?: false

            val afModes = characteristics.get(CameraCharacteristics.CONTROL_AF_AVAILABLE_MODES)
                ?.map { fromAFMode(it) }?.filter { it != context.getString(R.string.unknown) } ?: emptyList()

            val apertures = characteristics.get(CameraCharacteristics.LENS_INFO_AVAILABLE_APERTURES)
            val maxAperture = apertures?.minOrNull()

            val focalLengths = characteristics.get(CameraCharacteristics.LENS_INFO_AVAILABLE_FOCAL_LENGTHS)
            val focalLength = focalLengths?.firstOrNull()

            val focalLength35mm = if (focalLength != null && physicalSize != null) {
                val diagonal = sqrt(physicalSize.width * physicalSize.width + physicalSize.height * physicalSize.height)
                val cropFactor = 43.27f / diagonal
                focalLength * cropFactor
            } else null

            val physicalCameraIds = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                characteristics.physicalCameraIds
            } else {
                emptySet()
            }

            val streamMap = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)
            val videoCaps = streamMap?.let { getVideoCapabilities(it) } ?: emptyList()

            CameraInfo(
                id = cameraId,
                lensFacing = lensFacing,
                sensorOrientation = sensorOrientation,
                hardwareLevel = hardwareLevel,
                megapixels = megapixels,
                maxAperture = maxAperture,
                focalLength = focalLength,
                focalLength35mm = focalLength35mm,
                sensorSize = sensorSizeStr,
                isoRange = isoRangeStr,
                exposureTimeRange = exposureRangeStr,
                hasFlash = hasFlash,
                maxZoomRatio = maxZoomRatio,
                isVideoStabilizationSupported = isVideoStabilizationSupported,
                videoStabilizationModes = videoStabilizationModes,
                opticalStabilizationSupported = opticalStabilizationSupported,
                autoFocusModes = afModes,
                physicalCameraIds = physicalCameraIds,
                videoCapabilities = videoCaps
            )
        }
        return infoList
    }

    private fun fromVideoStabilizationMode(mode: Int): String = when (mode) {
        CameraCharacteristics.CONTROL_VIDEO_STABILIZATION_MODE_OFF -> context.getString(R.string.unknown)
        CameraCharacteristics.CONTROL_VIDEO_STABILIZATION_MODE_ON -> context.getString(R.string.eis)
        CameraCharacteristics.CONTROL_VIDEO_STABILIZATION_MODE_PREVIEW_STABILIZATION -> context.getString(R.string.preview_stabilization)
        else -> context.getString(R.string.unknown)
    }

    private fun fromAFMode(mode: Int): String = when (mode) {
        CameraCharacteristics.CONTROL_AF_MODE_OFF -> context.getString(R.string.af_mode_off)
        CameraCharacteristics.CONTROL_AF_MODE_AUTO -> context.getString(R.string.af_mode_auto)
        CameraCharacteristics.CONTROL_AF_MODE_MACRO -> context.getString(R.string.af_mode_macro)
        CameraCharacteristics.CONTROL_AF_MODE_CONTINUOUS_VIDEO -> context.getString(R.string.af_mode_continuous_video)
        CameraCharacteristics.CONTROL_AF_MODE_CONTINUOUS_PICTURE -> context.getString(R.string.af_mode_continuous_picture)
        CameraCharacteristics.CONTROL_AF_MODE_EDOF -> context.getString(R.string.af_mode_edof)
        else -> context.getString(R.string.unknown)
    }

    private fun getVideoCapabilities(map: StreamConfigurationMap): List<String> = buildList {
        val outputSizes = map.getOutputSizes(ImageFormat.PRIVATE) ?: return@buildList

        val maxRes = outputSizes.maxByOrNull { it.width * it.height }
        maxRes?.let {
             add("${it.width}x${it.height}")
        }

        // Try to find if 4K is supported
        val has4K = outputSizes.any { it.width >= 3840 && it.height >= 2160 }
        if (has4K && maxRes?.width != 3840) add(context.getString(R.string.video_cap_4k))
    }.distinct()
}