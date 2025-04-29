package com.lkonlesoft.displayinfo.utils

import android.Manifest
import android.content.Context
import android.content.Context.CONNECTIVITY_SERVICE
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.LinkProperties
import android.net.NetworkCapabilities
import android.os.Build
import android.telephony.TelephonyManager
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import com.lkonlesoft.displayinfo.R
import com.lkonlesoft.displayinfo.helper.NetworkInfo

object NetworkUtils {

    @RequiresApi(Build.VERSION_CODES.N)
    fun getNetwork(context: Context): String {
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
                    return "Requires permission"
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
    fun getNetInfo(context: Context): NetworkInfo? {
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
                netInfo.dhcpServer = link.dhcpServerAddress?.hostAddress.toString()
                netInfo.wakeOnLanSupported = link.isWakeOnLanSupported
            }
            return netInfo
        }
        return null
    }

    @Suppress("DEPRECATION")
    fun getNetworkOldApi(context: Context): String {
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

}