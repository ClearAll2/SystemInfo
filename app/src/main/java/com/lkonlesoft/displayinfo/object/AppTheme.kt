package com.lkonlesoft.displayinfo.`object`

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.lkonlesoft.displayinfo.R

sealed class AppTheme(@StringRes val title: Int, val value: Int, @DrawableRes val icon: Int) {
    object System : AppTheme(title = R.string.system, value = 0, R.drawable.outline_brightness_4_24)
    object Dark : AppTheme(title = R.string.dark, value = 1, R.drawable.outline_dark_mode_24)
    object Light : AppTheme(title = R.string.light, value = 2, R.drawable.outline_light_mode_24)
}