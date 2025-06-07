package com.lkonlesoft.displayinfo.helper

data class SimInfo (
    val slot: Int,
    val carrierName: String,
    val displayName: String,
    val countryIso: String,
    val iccId: String,
    val subscriptionId: Int
)