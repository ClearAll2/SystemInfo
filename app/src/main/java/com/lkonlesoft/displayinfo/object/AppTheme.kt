package com.lkonlesoft.displayinfo.`object`

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.lkonlesoft.displayinfo.R

sealed class AppTheme(@param:StringRes val title: Int, val value: Int, @param:DrawableRes val icon: Int, @param:DrawableRes val checkedIcon: Int) {
    object System : AppTheme(title = R.string.system, value = 0, R.drawable.outlined_brightness_auto_24px, R.drawable.brightness_auto_24px)
    object Dark : AppTheme(title = R.string.dark, value = 1, R.drawable.outline_dark_mode_24, R.drawable.dark_mode_24px)
    object Light : AppTheme(title = R.string.light, value = 2, R.drawable.outline_light_mode_24, R.drawable.light_mode_24px)
}