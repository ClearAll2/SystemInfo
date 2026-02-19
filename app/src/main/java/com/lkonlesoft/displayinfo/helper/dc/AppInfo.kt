package com.lkonlesoft.displayinfo.helper.dc

import android.graphics.drawable.Drawable

data class AppInfo(
    val name: String,
    val packageName: String,
    val icon: Drawable?,
    val versionName: String?,
    val type: Int
)
