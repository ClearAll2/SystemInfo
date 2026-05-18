package com.lkonlesoft.displayinfo.utils

import android.app.ActivityManager
import android.content.Context
import android.content.Context.ACTIVITY_SERVICE
import android.opengl.EGL14
import android.opengl.GLES20
import android.os.Build
import android.os.Handler
import android.os.Looper
import com.lkonlesoft.displayinfo.R
import com.lkonlesoft.displayinfo.helper.dc.ClusterInfo
import com.lkonlesoft.displayinfo.helper.dc.DeviceInfo
import java.io.File
import java.io.RandomAccessFile

class SocUtils(private val context: Context) {

    private val path = "/sys/devices/system/cpu/"

    private val activityManager by lazy {
        context.applicationContext.getSystemService(ACTIVITY_SERVICE) as ActivityManager
    }

    private fun getSupportedABI(): String {
        val supportedABIS = Build.SUPPORTED_ABIS
        return supportedABIS?.joinToString(", ") ?: context.getString(R.string.unknown)
    }

    fun getCpuGovernor(core: Int = 0): String {
        val path = "${path}cpu$core/cpufreq/scaling_governor"
        return try {
            File(path).readText().trim()
        } catch (_: Exception) {
            context.getString(R.string.unknown)
        }
    }

    fun getAllGovernors(): List<String> {
        val coreCount = Runtime.getRuntime().availableProcessors()
        return (0 until coreCount).map { getCpuGovernor(it) }
    }

    fun getCpuClockSpeed(core: Int): Int {
        val path = "${path}cpu$core/cpufreq/scaling_cur_freq"
        return try {
            val file = File(path)
            if (file.exists()) {
                val freqKHz = file.readText().trim().toInt()
                freqKHz / 1000
            } else -1
        } catch (_: Exception) {
            -1
        }
    }

    fun getAllCpuFrequencies(): List<Int> {
        val cpuCount = Runtime.getRuntime().availableProcessors()
        return (0 until cpuCount).map { getCpuClockSpeed(it) }
    }

    fun getGlEsVersion(): String{
        return activityManager.deviceConfigurationInfo.glEsVersion
    }

    fun getNumberOfCores(): Int {
        return Runtime.getRuntime().availableProcessors()
    }

    fun getMinMaxFreq(coreNumber: Int): Pair<Long, Long> {
        val minPath = "${path}cpu$coreNumber/cpufreq/cpuinfo_min_freq"
        val maxPath = "${path}cpu$coreNumber/cpufreq/cpuinfo_max_freq"
        return try {
            val minMhz = RandomAccessFile(minPath, "r").use { it.readLine().toLong() / 1000 }
            val maxMhz = RandomAccessFile(maxPath, "r").use { it.readLine().toLong() / 1000 }
            Pair(minMhz, maxMhz)
        } catch (_: Exception) {
            //Timber.e("getMinMaxFreq() - cannot read file")
            Pair(-1, -1)
        }
    }

    fun getCPUInfo(): List<DeviceInfo>{
        val numCores = getNumberOfCores()
        val governor = getCpuGovernor(0)
        /*val minMaxFrequencies = (0 until numCores).map { getMinMaxFreq(it) }
        val retList = minMaxFrequencies.mapIndexed { index, freq ->
            val minFreq = freq.first
            val maxFreq = freq.second
            DeviceInfo(R.string.core, "${index+1}", if (minFreq != -1L && maxFreq != -1L) "${freq.first} - ${freq.second} MHz" else context.getString(R.string.unknown), 1)
        }*/
        return listOf(
            DeviceInfo(R.string.cores, numCores.toString()),
            DeviceInfo(R.string.governor, governor),
            DeviceInfo(R.string.supported_abi, getSupportedABI())
        ) /*+ retList*/
    }

    fun getCPUUsage(): List<DeviceInfo>{
        val frequencies = getAllCpuFrequencies()
        val retList = frequencies.mapIndexed { index, freq ->
            DeviceInfo(R.string.core, "${index+1}", if (freq != -1) "$freq MHz" else context.getString(R.string.unknown))
        }
        return retList
    }

    fun getCPUClusterInfo(): List<List<DeviceInfo>> {
        val clusters = mutableListOf<List<Int>>()
        val coreCount = getNumberOfCores()
        val visited = mutableSetOf<Int>()

        for (i in 0 until coreCount) {
            if (i in visited) continue
            val relatedPath = "${path}cpu$i/cpufreq/related_cpus"
            val clusterCores = try {
                val file = File(relatedPath)
                if (file.exists()) {
                    val content = file.readText().trim()
                    if (content.contains("-")) {
                        val parts = content.split("-")
                        (parts[0].toInt()..parts[1].toInt()).toList()
                    } else {
                        content.split(Regex("\\s+")).filter { it.isNotBlank() }.map { it.toInt() }
                    }
                } else listOf(i)
            } catch (_: Exception) {
                listOf(i)
            }
            clusters.add(clusterCores)
            visited.addAll(clusterCores)
        }

        val result = mutableListOf<ClusterInfo>()
        clusters.forEach { cores ->
            val firstCore = cores.first()
            val (min, max) = getMinMaxFreq(firstCore)
            val coresRange = if (cores.size > 1) "${cores.first()+1}-${cores.last()+1}" else "${cores.first()+1}"

            result.add(
                ClusterInfo(
                    minFreq = if (min != -1L) "$min MHz" else context.getString(R.string.unknown),
                    maxFreq = if (max != -1L) "$max MHz" else context.getString(R.string.unknown),
                    cores = coresRange
                )
            )
        }

        return result.map { cluster ->
            listOf(
                DeviceInfo(R.string.min_freq, cluster.minFreq),
                DeviceInfo(R.string.max_freq, cluster.maxFreq),
                DeviceInfo(R.string.cores, cluster.cores),
            )
        }
    }

    //This is AI do it, not me :)
    fun fetchGpuInfoOptimized(onResult: (List<DeviceInfo>) -> Unit) {
        Thread {
            var eglDisplay = EGL14.EGL_NO_DISPLAY
            var eglContext = EGL14.EGL_NO_CONTEXT
            var eglSurface = EGL14.EGL_NO_SURFACE

            try {
                eglDisplay = EGL14.eglGetDisplay(EGL14.EGL_DEFAULT_DISPLAY)
                if (eglDisplay == EGL14.EGL_NO_DISPLAY) return@Thread

                val version = IntArray(2)
                if (!EGL14.eglInitialize(eglDisplay, version, 0, version, 1)) return@Thread

                val configAttribs = intArrayOf(
                    EGL14.EGL_RENDERABLE_TYPE, EGL14.EGL_OPENGL_ES2_BIT,
                    EGL14.EGL_RED_SIZE, 8, EGL14.EGL_GREEN_SIZE, 8, EGL14.EGL_BLUE_SIZE, 8,
                    EGL14.EGL_NONE
                )

                val configs = arrayOfNulls<android.opengl.EGLConfig>(1)
                val numConfigs = IntArray(1)
                EGL14.eglChooseConfig(eglDisplay, configAttribs, 0, configs, 0, 1, numConfigs, 0)
                val eglConfig = configs[0] ?: return@Thread

                val contextAttribs = intArrayOf(EGL14.EGL_CONTEXT_CLIENT_VERSION, 2, EGL14.EGL_NONE)
                eglContext = EGL14.eglCreateContext(eglDisplay, eglConfig, EGL14.EGL_NO_CONTEXT, contextAttribs, 0)
                if (eglContext == EGL14.EGL_NO_CONTEXT) return@Thread

                val surfaceAttribs = intArrayOf(EGL14.EGL_WIDTH, 1, EGL14.EGL_HEIGHT, 1, EGL14.EGL_NONE)
                eglSurface = EGL14.eglCreatePbufferSurface(eglDisplay, eglConfig, surfaceAttribs, 0)
                if (eglSurface == EGL14.EGL_NO_SURFACE) return@Thread

                if (!EGL14.eglMakeCurrent(eglDisplay, eglSurface, eglSurface, eglContext)) return@Thread

                val gpuInfo = listOf(
                    DeviceInfo(R.string.gpu_vendor, GLES20.glGetString(GLES20.GL_VENDOR) ?: context.getString(R.string.unknown)),
                    DeviceInfo(R.string.gpu_renderer, GLES20.glGetString(GLES20.GL_RENDERER) ?: context.getString(R.string.unknown)),
                    DeviceInfo(R.string.gles_version, GLES20.glGetString(GLES20.GL_VERSION) ?: context.getString(R.string.unknown)),
                    DeviceInfo(R.string.extensions, GLES20.glGetString(GLES20.GL_EXTENSIONS)?.replace(" ", "\n") ?: context.getString(R.string.unknown))
                )

                Handler(Looper.getMainLooper()).post {
                    onResult(gpuInfo)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                if (eglDisplay != EGL14.EGL_NO_DISPLAY) {
                    EGL14.eglMakeCurrent(eglDisplay, EGL14.EGL_NO_SURFACE, EGL14.EGL_NO_SURFACE, EGL14.EGL_NO_CONTEXT)
                    if (eglSurface != EGL14.EGL_NO_SURFACE) EGL14.eglDestroySurface(eglDisplay, eglSurface)
                    if (eglContext != EGL14.EGL_NO_CONTEXT) EGL14.eglDestroyContext(eglDisplay, eglContext)
                    EGL14.eglTerminate(eglDisplay)
                }
            }
        }.start()
    }

}