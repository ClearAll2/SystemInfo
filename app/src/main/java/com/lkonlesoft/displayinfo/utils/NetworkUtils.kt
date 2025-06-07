package com.lkonlesoft.displayinfo.utils

import android.Manifest
import android.content.Context
import android.content.Context.CONNECTIVITY_SERVICE
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.LinkProperties
import android.net.NetworkCapabilities
import android.os.Build
import android.telephony.SubscriptionManager
import android.telephony.TelephonyManager
import androidx.annotation.RequiresApi
import androidx.annotation.RequiresPermission
import androidx.core.app.ActivityCompat
import com.lkonlesoft.displayinfo.R
import com.lkonlesoft.displayinfo.helper.DeviceInfo
import com.lkonlesoft.displayinfo.helper.NetworkInfo
import com.lkonlesoft.displayinfo.helper.SimInfo
import com.lkonlesoft.displayinfo.helper.hasPermission

class NetworkUtils(private val context: Context) {

    fun getDetailsInfo(): List<DeviceInfo>{
        val netInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) getNetInfo() else null
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
        val netInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) getNetInfo() else null
        return listOf(
            DeviceInfo(R.string.interfaces, netInfo?.interfaces ?: context.getString(R.string.n_a)),
            DeviceInfo(R.string.ip_address, netInfo?.ip ?: context.getString(R.string.n_a)),
            DeviceInfo(R.string.dns, netInfo?.dnsServer?.replace("/", "") ?: context.getString(R.string.n_a)),
            DeviceInfo(R.string.dhcp_server, netInfo?.dhcpServer ?: context.getString(R.string.n_a))
        )
    }

    @RequiresPermission(Manifest.permission.READ_PHONE_STATE)
    @RequiresApi(Build.VERSION_CODES.LOLLIPOP_MR1)
    fun getSimInfo(): List<DeviceInfo> {
        val simInfoList = getDualSimInfo(context)
        if (simInfoList.isNotEmpty()) {
            simInfoList.map { simInfo ->
                return listOf(
                    DeviceInfo(R.string.sim_slot, simInfo.slot),
                    DeviceInfo(R.string.carrier_name, simInfo.carrierName),
                    DeviceInfo(R.string.sim_display_name, simInfo.displayName),
                    DeviceInfo(R.string.country_iso, simInfo.countryIso),
                    DeviceInfo(R.string.icc_id, simInfo.iccId),
                    DeviceInfo(R.string.subscription_id, simInfo.subscriptionId)
                )
            }
        }
        return emptyList()
    }

    @RequiresApi(Build.VERSION_CODES.N)
    fun getNetwork(): String {
        val connectivityManager =
            context.getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager
        val nw = connectivityManager.activeNetwork ?: return "-"
        val actNw = connectivityManager.getNetworkCapabilities(nw) ?: return "-"
        when {
            actNw.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> return "WIFI"
            actNw.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> return "ETHERNET"
            actNw.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> {
                val tm = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
                if (ActivityCompat.checkSelfPermission(
                        context,
                        Manifest.permission.READ_PHONE_STATE
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    return context.getString(R.string.require_permission)
                }
                when (tm.dataNetworkType) {
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
                    else -> return "?"
                }
            }
            else -> return "?"
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    fun getNetInfo(): NetworkInfo? {
        val netInfo = NetworkInfo()
        val connectivityManager = context.getSystemService(CONNECTIVITY_SERVICE)
        if (connectivityManager is ConnectivityManager && connectivityManager.activeNetwork != null) {
            val link: LinkProperties =  connectivityManager.getLinkProperties(connectivityManager.activeNetwork) as LinkProperties
            netInfo.ip = link.linkAddresses.joinToString("\n")
            netInfo.domain = if (link.domains != null) link.domains.toString() else context.getString(R.string.n_a)
            netInfo.interfaces = link.interfaceName.toString()
            netInfo.dnsServer = link.dnsServers.joinToString("\n")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                netInfo.isPrivateDNSActive = link.isPrivateDnsActive
                netInfo.privateDNS = if (link.privateDnsServerName != null) link.privateDnsServerName.toString() else context.getString(R.string.n_a)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                netInfo.dhcpServer = if (link.dhcpServerAddress?.hostAddress != null) link.dhcpServerAddress?.hostAddress.toString() else context.getString(R.string.n_a)
                netInfo.wakeOnLanSupported = link.isWakeOnLanSupported
            }
            return netInfo
        }
        return null
    }

    @Suppress("DEPRECATION")
    fun getNetworkOldApi(): String {
        // ConnectionManager instance
        val mConnectivityManager = context.getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager
        val mInfo = mConnectivityManager.activeNetworkInfo

        // If not connected, "-" will be displayed
        if ((mInfo == null) || !mInfo.isConnected) return "-"

        // If Connected to Wifi
        if (mInfo.type == ConnectivityManager.TYPE_WIFI) return "WIFI"

        // If Connected to Mobile
        if (mInfo.type == ConnectivityManager.TYPE_MOBILE) {
            return when (mInfo.subtype) {
                TelephonyManager.NETWORK_TYPE_GPRS, TelephonyManager.NETWORK_TYPE_EDGE, TelephonyManager.NETWORK_TYPE_CDMA, TelephonyManager.NETWORK_TYPE_1xRTT, TelephonyManager.NETWORK_TYPE_IDEN, TelephonyManager.NETWORK_TYPE_GSM -> "2G"
                TelephonyManager.NETWORK_TYPE_UMTS, TelephonyManager.NETWORK_TYPE_EVDO_0, TelephonyManager.NETWORK_TYPE_EVDO_A, TelephonyManager.NETWORK_TYPE_HSDPA, TelephonyManager.NETWORK_TYPE_HSUPA, TelephonyManager.NETWORK_TYPE_HSPA, TelephonyManager.NETWORK_TYPE_EVDO_B, TelephonyManager.NETWORK_TYPE_EHRPD, TelephonyManager.NETWORK_TYPE_HSPAP, TelephonyManager.NETWORK_TYPE_TD_SCDMA -> "3G"
                TelephonyManager.NETWORK_TYPE_LTE, TelephonyManager.NETWORK_TYPE_IWLAN, 19 -> "4G"
                TelephonyManager.NETWORK_TYPE_NR -> "5G"
                else -> "?"
            }
        }
        return "?"
    }

    @RequiresPermission(Manifest.permission.READ_PHONE_STATE)
    @RequiresApi(Build.VERSION_CODES.LOLLIPOP_MR1)
    fun getDualSimInfo(context: Context): List<SimInfo> {
        val simInfoList = mutableListOf<SimInfo>()
        if (context.hasPermission(Manifest.permission.READ_PHONE_STATE)) {
            val subscriptionManager =
                context.getSystemService(Context.TELEPHONY_SUBSCRIPTION_SERVICE) as SubscriptionManager
            val activeSims = subscriptionManager.activeSubscriptionInfoList

            activeSims?.forEach { info ->
                simInfoList.add(
                    SimInfo(
                        slot = info.simSlotIndex,
                        carrierName = info.carrierName.toString(),
                        displayName = info.displayName.toString(),
                        countryIso = info.countryIso,
                        iccId = info.iccId ?: "N/A",
                        subscriptionId = info.subscriptionId
                    )
                )
            }

            return simInfoList
        }
        return emptyList()
    }


}