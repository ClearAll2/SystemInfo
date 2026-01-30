package com.lkonlesoft.displayinfo.helper.dc

import android.graphics.Bitmap

data class AppInfo(
    val name: String,
    val packageName: String,
    val icon: Bitmap? = null,
    val versionName: String? = null
)
