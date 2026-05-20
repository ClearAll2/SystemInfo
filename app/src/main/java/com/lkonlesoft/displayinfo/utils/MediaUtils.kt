package com.lkonlesoft.displayinfo.utils

import android.content.Context
import android.content.pm.PackageManager
import android.media.AudioManager
import android.media.MediaCodecList
import android.media.MediaDrm
import android.os.Build
import android.util.Base64
import com.lkonlesoft.displayinfo.R
import com.lkonlesoft.displayinfo.helper.dc.DeviceInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.UUID

class MediaUtils(private val context: Context) {

    private val audioManager by lazy {
        context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    }

    val packageManager: PackageManager by lazy {
        context.packageManager
    }

    suspend fun getWidevineInfo(): List<DeviceInfo> = withContext(Dispatchers.IO) {
        val widevineUUID = UUID.fromString(context.getString(R.string.widevineUUID))
        val info = mutableMapOf<String, String>()
        val widevineInfo = mutableListOf<DeviceInfo>()
        val customProps = mapOf(
            "vendor" to R.string.vendor,
            "version" to R.string.version,
            "securityLevel" to R.string.security_level,
            "algorithms" to R.string.algorithms,
            "maxNumberOfSessions" to R.string.maxNumberOfSessions,
            "numberOfOpenSessions" to R.string.numberOfOpenSessions,
            "systemId" to R.string.systemId,
            "deviceUniqueId" to R.string.device_uid
        )
        try {
            val mediaDrm = MediaDrm(widevineUUID)

            for (prop in customProps) {
                val value = try {
                    mediaDrm.getPropertyString(prop.key)
                } catch (_: Exception) {
                    "Unavailable"
                }
                info[prop.key] = value
            }

            val uniqueId = try {
                val bytes = mediaDrm.getPropertyByteArray("deviceUniqueId")
                Base64.encodeToString(bytes, Base64.NO_WRAP)
            } catch (_: Exception) {
                "Unavailable"
            }

            info["deviceUniqueId"] = uniqueId

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                mediaDrm.close()
            } else {
                @Suppress("DEPRECATION")
                mediaDrm.release()
            }

        } catch (e: Exception) {
            info["error"] = e.message ?: "Error accessing MediaDrm"
        }

        info.entries.forEach {
            widevineInfo.add(
                DeviceInfo(
                    name = customProps.getValue(it.key),
                    it.value
                )
            )
        }
        widevineInfo
    }

    suspend fun getClearKeyInfo(): List<DeviceInfo> =
        withContext(Dispatchers.IO) {
            val clearKeyUUID = UUID.fromString(context.getString(R.string.clearKeyUUID))
            val info = mutableMapOf<String, String>()
            val clearKeyInfo = mutableListOf<DeviceInfo>()
            val customProps = mapOf(
                "vendor" to R.string.vendor,
                "version" to R.string.version
            )
            try {
                val mediaDrm = MediaDrm(clearKeyUUID)

                for (prop in customProps) {
                    val value = try {
                        mediaDrm.getPropertyString(prop.key)
                    } catch (_: Exception) {
                        "Unavailable"
                    }
                    info[prop.key] = value
                }

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    mediaDrm.close()
                } else {
                    @Suppress("DEPRECATION")
                    mediaDrm.release()
                }

            } catch (e: Exception) {
                info["error"] = e.message ?: "Error accessing MediaDrm"
            }

            info.entries.forEach {
                clearKeyInfo.add(
                    DeviceInfo(
                        name = customProps.getValue(it.key),
                        it.value
                    )
                )
            }
            clearKeyInfo
        }

    fun getAudioFeatures(): List<DeviceInfo> {
        val lowLatencyAudio = if (hasLowLatencyAudio()) context.getString(R.string.supported) else context.getString(R.string.not_supported)
        val proAudio = if (hasProAudio()) context.getString(R.string.supported) else context.getString(R.string.not_supported)
        val hasMidi = if (hasMidi()) context.getString(R.string.supported) else context.getString(R.string.not_supported)
        val unprocessedAudioSource = if (hasUnprocessedSupport()) context.getString(R.string.supported) else context.getString(R.string.not_supported)
        return listOf(
            DeviceInfo(R.string.low_latency_audio, lowLatencyAudio),
            DeviceInfo(R.string.pro_audio_support, proAudio),
            DeviceInfo(R.string.midi_support, hasMidi),
            DeviceInfo(R.string.unprocessed_audio_source, unprocessedAudioSource)
        )
    }

    fun getMediaCodecs(): List<DeviceInfo> {
        return listOf(
            DeviceInfo(R.string.video_codecs, getSupportedVideoCodecs().joinToString(", ")),
            DeviceInfo(R.string.audio_codecs, getSupportedAudioCodecs().joinToString(", ")),
        )
    }

    private fun hasLowLatencyAudio(): Boolean {
        return packageManager.hasSystemFeature(PackageManager.FEATURE_AUDIO_LOW_LATENCY)
    }

    private fun hasProAudio(): Boolean {
        return packageManager.hasSystemFeature(PackageManager.FEATURE_AUDIO_PRO)
    }

    private fun hasMidi(): Boolean {
        return packageManager.hasSystemFeature("android.software.midi")
    }

    private fun hasUnprocessedSupport(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            audioManager.getProperty(AudioManager.PROPERTY_SUPPORT_AUDIO_SOURCE_UNPROCESSED)
                ?.toBoolean() ?: false
        } else {
            false
        }
    }

    private fun getSupportedAudioCodecs(): List<String> {
        val codecs = mutableListOf<String>()
        val mediaCodecList = MediaCodecList(MediaCodecList.REGULAR_CODECS)

        for (info in mediaCodecList.codecInfos) {
            if (!info.isEncoder) continue // or include both, depending on needs

            for (type in info.supportedTypes) {
                if (type.startsWith("audio/")) {
                    val name = type.removePrefix("audio/").uppercase()
                    if (name !in codecs) codecs.add(name)
                }
            }
        }
        return codecs.sorted()
    }

    fun getSupportedVideoCodecs(): List<String> {
        val codecs = mutableListOf<String>()
        val mediaCodecList = MediaCodecList(MediaCodecList.REGULAR_CODECS)

        for (info in mediaCodecList.codecInfos) {
            for (type in info.supportedTypes) {
                if (type.startsWith("video/")) {
                    val name = when {
                        type.contains("av01") -> "AV1"
                        type.contains("vp9") -> "VP9"
                        type.contains("vp8") -> "VP8"
                        type.contains("hevc") || type.contains("h265") -> "H.265/HEVC"
                        type.contains("avc") || type.contains("h264") -> "H.264/AVC"
                        else -> type.removePrefix("video/").uppercase()
                    }
                    if (name !in codecs) codecs.add(name)
                }
            }
        }
        return codecs.sorted()
    }

}