package com.lkonlesoft.displayinfo.`object`

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.ui.graphics.Color
import com.lkonlesoft.displayinfo.R
import com.lkonlesoft.displayinfo.ui.theme.AndroidColor
import com.lkonlesoft.displayinfo.ui.theme.AppsColor
import com.lkonlesoft.displayinfo.ui.theme.BatteryColor
import com.lkonlesoft.displayinfo.ui.theme.CameraColor
import com.lkonlesoft.displayinfo.ui.theme.ConnectivityColor
import com.lkonlesoft.displayinfo.ui.theme.DisplayColor
import com.lkonlesoft.displayinfo.ui.theme.MemoryColor
import com.lkonlesoft.displayinfo.ui.theme.NetworkColor
import com.lkonlesoft.displayinfo.ui.theme.Pink80
import com.lkonlesoft.displayinfo.ui.theme.SoCColor
import com.lkonlesoft.displayinfo.ui.theme.StorageColor
import com.lkonlesoft.displayinfo.ui.theme.SystemColor

sealed class NavigationItem (@param:StringRes val name: Int, val route: String, @param:DrawableRes val icon: Int, val color: Color = Color.Transparent){
    data object Home: NavigationItem(name = R.string.home, route = "home", R.drawable.outline_info_24)
    data object About: NavigationItem(name = R.string.about, route = "about", R.drawable.outline_info_24)
    data object System: NavigationItem(name = R.string.system, route = "system", R.drawable.manufacturing_24px,
        SystemColor
    )
    data object Android: NavigationItem(name = R.string.android, route = "android", R.drawable.outline_android_24,
        AndroidColor
    )
    data object Display: NavigationItem(name = R.string.display, route = "display", R.drawable.mobile_text_24px,
        DisplayColor
    )
    data object Battery: NavigationItem(name = R.string.battery, route = "battery", R.drawable.battery_android_4_24px,
        BatteryColor
    )
    data object Memory: NavigationItem(name = R.string.memory, route = "memory", R.drawable.memory_24px,
        MemoryColor
    )
    data object Storage: NavigationItem(name = R.string.storage, route = "storage", R.drawable.outline_storage_24,
        StorageColor
    )
    data object Network: NavigationItem(name = R.string.network, route = "network", R.drawable.android_cell_4_bar_24px,
        NetworkColor
    )
    data object SOC: NavigationItem(name = R.string.soc, route = "soc", R.drawable.developer_board_24px,
        SoCColor
    )
    data object Camera: NavigationItem(name = R.string.camera, route = "camera", R.drawable.camera_24px,
        CameraColor
    )
    data object Connectivity: NavigationItem(name = R.string.connectivity, route = "connectivity", R.drawable.bluetooth_connected_24px,
        ConnectivityColor
    )
    data object Settings: NavigationItem(name = R.string.settings, route = "settings", R.drawable.rounded_settings_24)
    data object Apps: NavigationItem(name = R.string.apps, route = "apps", R.drawable.outline_apps_24,
        AppsColor
    )
    data object Media: NavigationItem(name = R.string.media, route = "media", R.drawable.slideshow_24px,
        Pink80
    )
}
