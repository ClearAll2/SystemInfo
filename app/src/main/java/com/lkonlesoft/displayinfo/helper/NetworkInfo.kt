package com.lkonlesoft.displayinfo.helper

data class NetworkInfo (
    var ip: String = "N/A",
    var domain: String = "N/A",
    var interfaces: String = "N/A",
    var dnsServer: String = "N/A",
    var isPrivateDNSActive: Boolean = false,
    var privateDNS: String = "N/A",
    var dhcpServer: String = "N/A",
    var wakeOnLanSupported: Boolean = false
)