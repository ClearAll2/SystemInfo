package com.lkonlesoft.displayinfo.helper.dc

data class SimInfo (
    val slot: Int,
    val carrierName: String,
    val displayName: String,
    val countryIso: String,
    val iccId: String,
    val subscriptionId: Int,
    val isActive: Boolean = true
)