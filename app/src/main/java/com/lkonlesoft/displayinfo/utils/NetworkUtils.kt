package com.lkonlesoft.displayinfo.utils

import android.Manifest
import android.content.Context
import android.content.Context.CONNECTIVITY_SERVICE
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.LinkProperties
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.net.wifi.ScanResult
import android.net.wifi.WifiInfo
import android.net.wifi.WifiManager
import android.os.Build
import android.telephony.SubscriptionManager
import android.telephony.TelephonyManager
import androidx.annotation.RequiresApi
import androidx.annotation.RequiresPermission
import androidx.core.app.ActivityCompat
import com.lkonlesoft.displayinfo.R
import com.lkonlesoft.displayinfo.helper.dc.DeviceInfo
import com.lkonlesoft.displayinfo.helper.dc.NetworkInfo
import com.lkonlesoft.displayinfo.helper.dc.SimInfo
import com.lkonlesoft.displayinfo.helper.dc.WifiConnectionInfo
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.coroutines.resume

class NetworkUtils(private val context: Context) {

    private val connectivityManager by lazy {
        context.getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager
    }

    private val telephonyManager by lazy {
        context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
    }

    private val wifiManager by lazy {
        context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
    }

    fun getDetailsInfo(): List<DeviceInfo>{
        val netInfo = getNetInfo()
        return listOf(
            DeviceInfo(R.string.interfaces, netInfo?.interfaces ?: context.getString(R.string.n_a)),
            DeviceInfo(R.string.ip_address, netInfo?.ip ?: context.getString(R.string.n_a)),
            DeviceInfo(R.string.domain, netInfo?.domain ?: context.getString(R.string.n_a)),
            DeviceInfo(R.string.dns, netInfo?.dnsServer?.replace("/", "") ?: context.getString(R.string.n_a)),
            DeviceInfo(R.string.dhcp_server, netInfo?.dhcpServer ?: context.getString(R.string.n_a)),
            DeviceInfo(R.string.is_private_dns_on, if (netInfo?.isPrivateDNSActive == true) context.getString(R.string.enabled) else context.getString(R.string.disabled)),
            DeviceInfo(R.string.private_dns_server, netInfo?.privateDNS ?: context.getString(R.string.n_a)),
            DeviceInfo(R.string.wake_on_lan_sp, if (netInfo?.wakeOnLanSupported == true) context.getString(R.string.supported) else context.getString(R.string.not_supported)),
        )
    }

    fun getDashboardData(): List<DeviceInfo>{
        val netInfo = getNetInfo()
        return listOf(
            DeviceInfo(R.string.interfaces, netInfo?.interfaces ?: context.getString(R.string.n_a)),
            DeviceInfo(R.string.ip_address, netInfo?.ip ?: context.getString(R.string.n_a)),
            DeviceInfo(R.string.dns, netInfo?.dnsServer?.replace("/", "") ?: context.getString(R.string.n_a)),
            DeviceInfo(R.string.dhcp_server, netInfo?.dhcpServer ?: context.getString(R.string.n_a))
        )
    }

    suspend fun getWifiDetails(): List<DeviceInfo> {
        // Try legacy sync API first for speed
        val legacy = getWifiInfoLegacy()

        val wifiInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            // On Android 12+, legacy might be redacted. Try callback if legacy is null or missing SSID.
            if (legacy == null || legacy.ssid == "<unknown ssid>") {
                withTimeoutOrNull(1000L) { getWifiInfo() } ?: legacy
            } else {
                legacy
            }
        } else {
            legacy
        }

        return wifiInfo?.let { info ->
            listOf(
                DeviceInfo(R.string.ssid, info.ssid),
                DeviceInfo(R.string.link_speed, info.linkSpeedMbps, " Mbps"),
                DeviceInfo(R.string.signal_strength, "${info.signalStrengthDbm} dBm (${rssiToQuality(info.signalStrengthDbm)})"),
                DeviceInfo(R.string.frequency, info.frequencyMhz, " MHz"),
                DeviceInfo(R.string.channel, if (info.channel != -1 ) info.channel else context.getString(R.string.unknown)),
                DeviceInfo(R.string.wifi_standard, info.standard)
            )
        } ?: emptyList()
    }

    fun rssiToQuality(rssi: Int): String = when {
        rssi >= -50 -> context.getString(R.string.excellent)
        rssi >= -60 -> context.getString(R.string.good)
        rssi >= -70 -> context.getString(R.string.fair)
        rssi >= -80 -> context.getString(R.string.weak)
        else -> context.getString(R.string.very_weak)
    }

    fun WifiInfo.wifiStandardToString(): String {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
            return context.getString(R.string.unknown)
        }
        return when (wifiStandard) {
            ScanResult.WIFI_STANDARD_LEGACY -> "802.11a/b/g"
            ScanResult.WIFI_STANDARD_11N   -> "802.11n (WiFi 4)"
            ScanResult.WIFI_STANDARD_11AC  -> "802.11ac (WiFi 5)"
            ScanResult.WIFI_STANDARD_11AX  -> "802.11ax (WiFi 6/6E)"
            ScanResult.WIFI_STANDARD_11AD  -> "802.11ad"
            ScanResult.WIFI_STANDARD_11BE  -> "802.11be (WiFi 7)"  // API 33+
            else -> context.getString(R.string.unknown)
        }
    }

    fun WifiInfo.toWifiConnectionInfo(): WifiConnectionInfo {
        return WifiConnectionInfo(
            ssid = ssid.removeSurrounding("\""),
            linkSpeedMbps = linkSpeed,
            signalStrengthDbm = rssi,
            frequencyMhz = frequency,
            channel = frequencyToChannel(frequency),
            standard = wifiStandardToString()
        )
    }

    @RequiresApi(Build.VERSION_CODES.S)
    suspend fun getWifiInfo(): WifiConnectionInfo? = suspendCancellableCoroutine { continuation ->
        val request = NetworkRequest.Builder()
            .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
            .build()

        val callback = object : ConnectivityManager.NetworkCallback(0) {
            override fun onCapabilitiesChanged(network: Network, capabilities: NetworkCapabilities) {
                val wifiInfo = capabilities.transportInfo as? WifiInfo
                if (continuation.isActive) {
                    continuation.resume(if (wifiManager.isWifiEnabled) wifiInfo?.toWifiConnectionInfo() else null)
                }
                connectivityManager.unregisterNetworkCallback(this)
            }

            override fun onLost(network: Network) {
                if (continuation.isActive) {
                    continuation.resume(null)
                }
                connectivityManager.unregisterNetworkCallback(this)
            }

            override fun onUnavailable() {
                if (continuation.isActive) {
                    continuation.resume(null)
                }
                connectivityManager.unregisterNetworkCallback(this)
            }
        }
        connectivityManager.registerNetworkCallback(request, callback)
        continuation.invokeOnCancellation {
            connectivityManager.unregisterNetworkCallback(callback)
        }
    }

    @Suppress("DEPRECATION")
    fun getWifiInfoLegacy(): WifiConnectionInfo? {
        if (!wifiManager.isWifiEnabled) return null

        val info = wifiManager.connectionInfo ?: return null
        if (info.networkId == -1) return null   // not connected

        return info.toWifiConnectionInfo()
    }


    private fun frequencyToChannel(freq: Int): Int {
        return when (freq) {
            2484 -> 14
            in 2412..2472 -> ((freq - 2412) / 5) + 1
            in 5170..5825 -> ((freq - 5170) / 5) + 34
            in 5945..7105 -> ((freq - 5945) / 5) + 1
            else -> -1
        }
    }


    fun getSimInfo(): List<List<DeviceInfo>> {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED) {
            val simInfoList = getDualSimInfo()
            if (simInfoList.isNotEmpty()) {
                return simInfoList.map  { simInfo ->
                     listOf(
                        DeviceInfo(R.string.sim_slot, simInfo.slot),
                        DeviceInfo(R.string.carrier_name, simInfo.carrierName),
                        DeviceInfo(R.string.sim_display_name, simInfo.displayName),
                        DeviceInfo(R.string.country_iso, simInfo.countryIso),
                        //DeviceInfo(R.string.icc_id, simInfo.iccId),
                        DeviceInfo(R.string.subscription_id, simInfo.subscriptionId),
                        DeviceInfo(R.string.enabled, if (simInfo.isActive) context.getString(R.string.yes) else context.getString(R.string.no))
                    )
                }
            }
        }
        return emptyList()
    }

    @RequiresApi(Build.VERSION_CODES.N)
    fun getNetwork(): String {
        val nw = connectivityManager.activeNetwork ?: return context.getString(R.string.unknown)
        val actNw = connectivityManager.getNetworkCapabilities(nw) ?: return context.getString(R.string.unknown)
        when {
            actNw.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> return context.getString(R.string.wifi)
            actNw.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> return context.getString(R.string.ethernet)
            actNw.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> {
                if (ActivityCompat.checkSelfPermission(
                        context,
                        Manifest.permission.READ_PHONE_STATE
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    return context.getString(R.string.require_permission)
                }
                @Suppress("DEPRECATION")
                when (telephonyManager.dataNetworkType) {
                    TelephonyManager.NETWORK_TYPE_GPRS,
                    TelephonyManager.NETWORK_TYPE_EDGE,
                    TelephonyManager.NETWORK_TYPE_CDMA,
                    TelephonyManager.NETWORK_TYPE_1xRTT,
                    TelephonyManager.NETWORK_TYPE_GSM -> return "2G"
                    TelephonyManager.NETWORK_TYPE_UMTS,
                    TelephonyManager.NETWORK_TYPE_EVDO_0,
                    TelephonyManager.NETWORK_TYPE_EVDO_A,
                    TelephonyManager.NETWORK_TYPE_HSDPA,
                    TelephonyManager.NETWORK_TYPE_HSUPA,
                    TelephonyManager.NETWORK_TYPE_HSPA,
                    TelephonyManager.NETWORK_TYPE_EVDO_B,
                    TelephonyManager.NETWORK_TYPE_EHRPD,
                    TelephonyManager.NETWORK_TYPE_HSPAP,
                    TelephonyManager.NETWORK_TYPE_TD_SCDMA -> return "3G"
                    TelephonyManager.NETWORK_TYPE_LTE,
                    TelephonyManager.NETWORK_TYPE_IWLAN -> return "4G"
                    TelephonyManager.NETWORK_TYPE_NR -> return "5G"
                    else -> return context.getString(R.string.unknown)
                }
            }
            else -> return context.getString(R.string.unknown)
        }
    }

    fun getNetInfo(): NetworkInfo? {
        val activeNetwork = connectivityManager.activeNetwork ?: return null
        val netInfo = NetworkInfo()
        val link: LinkProperties =  connectivityManager.getLinkProperties(activeNetwork) ?: return null
        
        netInfo.ip = link.linkAddresses.joinToString("\n")
        netInfo.domain = if (link.domains != null) link.domains.toString() else context.getString(R.string.n_a)
        netInfo.interfaces = link.interfaceName.toString()
        netInfo.dnsServer = link.dnsServers.joinToString("\n")
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            netInfo.isPrivateDNSActive = link.isPrivateDnsActive
            netInfo.privateDNS = if (link.privateDnsServerName != null) link.privateDnsServerName.toString() else context.getString(R.string.n_a)
        }
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            netInfo.dhcpServer = link.dhcpServerAddress?.hostAddress ?: context.getString(R.string.n_a)
            netInfo.wakeOnLanSupported = link.isWakeOnLanSupported
        }
        
        return netInfo
    }

    @Suppress("DEPRECATION")
    fun getNetworkOldApi(): String {
        // ConnectionManager instance
        val mInfo = connectivityManager.activeNetworkInfo

        // If not connected, "-" will be displayed
        if ((mInfo == null) || !mInfo.isConnected) return "-"

        // If Connected to Wifi
        if (mInfo.type == ConnectivityManager.TYPE_WIFI) return context.getString(R.string.wifi)

        // If Connected to Mobile
        if (mInfo.type == ConnectivityManager.TYPE_MOBILE) {
            return when (mInfo.subtype) {
                TelephonyManager.NETWORK_TYPE_GPRS, TelephonyManager.NETWORK_TYPE_EDGE, TelephonyManager.NETWORK_TYPE_CDMA, TelephonyManager.NETWORK_TYPE_1xRTT, TelephonyManager.NETWORK_TYPE_IDEN, TelephonyManager.NETWORK_TYPE_GSM -> "2G"
                TelephonyManager.NETWORK_TYPE_UMTS, TelephonyManager.NETWORK_TYPE_EVDO_0, TelephonyManager.NETWORK_TYPE_EVDO_A, TelephonyManager.NETWORK_TYPE_HSDPA, TelephonyManager.NETWORK_TYPE_HSUPA, TelephonyManager.NETWORK_TYPE_HSPA, TelephonyManager.NETWORK_TYPE_EVDO_B, TelephonyManager.NETWORK_TYPE_EHRPD, TelephonyManager.NETWORK_TYPE_HSPAP, TelephonyManager.NETWORK_TYPE_TD_SCDMA -> "3G"
                TelephonyManager.NETWORK_TYPE_LTE, TelephonyManager.NETWORK_TYPE_IWLAN, 19 -> "4G"
                TelephonyManager.NETWORK_TYPE_NR -> "5G"
                else -> context.getString(R.string.unknown)
            }
        }
        return context.getString(R.string.unknown)
    }

    @RequiresPermission(Manifest.permission.READ_PHONE_STATE)
    fun getDualSimInfo(): List<SimInfo> {
        val simInfoList = mutableListOf<SimInfo>()
        val subscriptionManager = context.getSystemService(Context.TELEPHONY_SUBSCRIPTION_SERVICE) as SubscriptionManager
        val activeSims = subscriptionManager.activeSubscriptionInfoList
        // Get TelephonyManager to check SIM slot count and states
        val simSlotCount = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            telephonyManager.supportedModemCount
        } else {
            // Fallback for older APIs: assume at least 1 slot, check active subscriptions
            maxOf(1, activeSims?.size ?: 0)
        }
        activeSims?.filterNotNull()?.forEach { info ->
            simInfoList.add(
                SimInfo(
                    slot = info.simSlotIndex,
                    carrierName = info.carrierName?.toString() ?: context.getString(R.string.unknown),
                    displayName = info.displayName?.toString() ?: context.getString(R.string.unknown),
                    countryIso = info.countryIso ?: context.getString(R.string.unknown),
                    iccId = if (!info.iccId.isNullOrEmpty()) info.iccId else context.getString(R.string.unknown),
                    subscriptionId = info.subscriptionId,
                    isActive = true
                )
            )
        }

        // Check for inactive SIM slots (e.g., slots with no active subscription)
        for (slotIndex in 0 until simSlotCount) {
            if (simInfoList.none { it.slot == slotIndex }) {
                // Query SIM state for the slot
                val simState = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    telephonyManager.getSimState(slotIndex)
                } else {
                    telephonyManager.simState // Fallback for single SIM state
                }

                // Consider SIM present if it's in a usable or potentially usable state
                val isSimPresent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    simState == TelephonyManager.SIM_STATE_READY ||
                            simState == TelephonyManager.SIM_STATE_NOT_READY ||
                            simState == TelephonyManager.SIM_STATE_PIN_REQUIRED ||
                            simState == TelephonyManager.SIM_STATE_PUK_REQUIRED ||
                            simState == TelephonyManager.SIM_STATE_CARD_RESTRICTED
                } else {
                    simState == TelephonyManager.SIM_STATE_READY ||
                            simState == TelephonyManager.SIM_STATE_PIN_REQUIRED ||
                            simState == TelephonyManager.SIM_STATE_PUK_REQUIRED
                }
                if (isSimPresent) {
                    simInfoList.add(
                        SimInfo(
                            slot = slotIndex,
                            carrierName = context.getString(R.string.unknown),
                            displayName = "SIM $slotIndex",
                            countryIso = context.getString(R.string.unknown),
                            iccId = context.getString(R.string.unknown),
                            subscriptionId = -1, // No subscription ID for inactive SIM
                            isActive = false
                        )
                    )
                }
            }
        }

        return simInfoList.sortedBy { it.slot }
    }


}