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

data class CpuCoreState(
    val coreIndex: Int,
    val currentFreqMhz: Int,
    val minFreqMhz: Int,
    val maxFreqMhz: Int,
    val usagePercent: Float,
    val governor: String,
    val isOffline: Boolean
)

data class CpuSnapshot(
    val overallUsagePercent: Float,
    val activeCoreCount: Int,
    val totalCoreCount: Int,
    val coreStates: List<CpuCoreState>
)

class SocUtils(private val context: Context) {

    private val path = "/sys/devices/system/cpu/"
    private var previousCpuStats: List<LongArray>? = null

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

    /*fun getGlEsVersion(): String{
        return activityManager.deviceConfigurationInfo.glEsVersion
    }*/

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
                    minFreq = if (min > 0) "$min MHz" else context.getString(R.string.unknown),
                    maxFreq = if (max > 0) "$max MHz" else context.getString(R.string.unknown),
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

    fun getCpuSnapshot(): CpuSnapshot {
        val numCores = getNumberOfCores()
        val frequencies = getAllCpuFrequencies()
        val governors = getAllGovernors()

        val currentStats = readProcStat()
        val coreUsages = mutableListOf<Float>()
        val coreStates = mutableListOf<CpuCoreState>()

        for (i in 0 until numCores) {
            val curFreq = frequencies.getOrElse(i) { -1 }
            val gov = governors.getOrElse(i) { context.getString(R.string.unknown) }
            val (minFreq, maxFreq) = getMinMaxFreq(i)
            val isOffline = curFreq <= 0

            val usage = if (isOffline) {
                0f
            } else if (currentStats != null && previousCpuStats != null && i + 1 < currentStats.size && i + 1 < previousCpuStats!!.size) {
                calculateUsageFromStat(previousCpuStats!![i + 1], currentStats[i + 1])
            } else {
                if (maxFreq > minFreq && maxFreq > 0) {
                    ((curFreq - minFreq).toFloat() / (maxFreq - minFreq).toFloat() * 100f).coerceIn(0f, 100f)
                } else if (maxFreq > 0) {
                    (curFreq.toFloat() / maxFreq.toFloat() * 100f).coerceIn(0f, 100f)
                } else {
                    15f
                }
            }

            coreUsages.add(usage)
            coreStates.add(
                CpuCoreState(
                    coreIndex = i,
                    currentFreqMhz = curFreq,
                    minFreqMhz = minFreq.toInt(),
                    maxFreqMhz = maxFreq.toInt(),
                    usagePercent = usage,
                    governor = gov,
                    isOffline = isOffline
                )
            )
        }

        if (currentStats != null) {
            previousCpuStats = currentStats
        }

        val overallUsage = if (currentStats != null && previousCpuStats != null && currentStats.isNotEmpty() && previousCpuStats!!.isNotEmpty()) {
            calculateUsageFromStat(previousCpuStats!![0], currentStats[0])
        } else if (coreUsages.isNotEmpty()) {
            coreUsages.average().toFloat()
        } else {
            0f
        }

        val activeCount = coreStates.count { !it.isOffline }

        return CpuSnapshot(
            overallUsagePercent = overallUsage.coerceIn(0f, 100f),
            activeCoreCount = activeCount,
            totalCoreCount = numCores,
            coreStates = coreStates
        )
    }

    private fun readProcStat(): List<LongArray>? {
        return try {
            val file = File("/proc/stat")
            if (!file.exists()) return null
            val lines = file.readLines()
            val result = mutableListOf<LongArray>()
            for (line in lines) {
                if (line.startsWith("cpu")) {
                    val tokens = line.split(Regex("\\s+")).filter { it.isNotBlank() }
                    if (tokens.size >= 8) {
                        val user = tokens[1].toLongOrNull() ?: 0L
                        val nice = tokens[2].toLongOrNull() ?: 0L
                        val system = tokens[3].toLongOrNull() ?: 0L
                        val idle = tokens[4].toLongOrNull() ?: 0L
                        val iowait = tokens.getOrNull(5)?.toLongOrNull() ?: 0L
                        val irq = tokens.getOrNull(6)?.toLongOrNull() ?: 0L
                        val softirq = tokens.getOrNull(7)?.toLongOrNull() ?: 0L
                        val steal = tokens.getOrNull(8)?.toLongOrNull() ?: 0L

                        val idleTotal = idle + iowait
                        val total = user + nice + system + idleTotal + irq + softirq + steal
                        result.add(longArrayOf(total, idleTotal))
                    }
                }
            }
            result.ifEmpty { null }
        } catch (_: Exception) {
            null
        }
    }

    private fun calculateUsageFromStat(prev: LongArray, curr: LongArray): Float {
        val totalDelta = curr[0] - prev[0]
        val idleDelta = curr[1] - prev[1]
        if (totalDelta <= 0L) return 0f
        val activeDelta = totalDelta - idleDelta
        return ((activeDelta.toFloat() / totalDelta.toFloat()) * 100f).coerceIn(0f, 100f)
    }

}