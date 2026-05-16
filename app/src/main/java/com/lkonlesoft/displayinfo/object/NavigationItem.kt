package com.lkonlesoft.displayinfo.`object`

import androidx.annotation.StringRes
import com.lkonlesoft.displayinfo.R

sealed class NavigationItem (@param:StringRes val name: Int, val route: String, val icon: Int){
    data object Home: NavigationItem(name = R.string.home, route = "home", R.drawable.outline_info_24)
    data object About: NavigationItem(name = R.string.about, route = "about", R.drawable.outline_info_24)
    data object System: NavigationItem(name = R.string.system, route = "system", R.drawable.stacks_24px)
    data object Android: NavigationItem(name = R.string.android, route = "android", R.drawable.outline_android_24)
    data object Display: NavigationItem(name = R.string.display, route = "display", R.drawable.mobile_text_24px)
    data object Battery: NavigationItem(name = R.string.battery, route = "battery", R.drawable.battery_android_4_24px)
    data object Memory: NavigationItem(name = R.string.memory, route = "memory", R.drawable.memory_24px)
    data object Storage: NavigationItem(name = R.string.storage, route = "storage", R.drawable.outline_storage_24)
    data object Network: NavigationItem(name = R.string.network, route = "network", R.drawable.android_cell_4_bar_24px)
    data object SOC: NavigationItem(name = R.string.soc, route = "soc", R.drawable.developer_board_24px)
    data object Camera: NavigationItem(name = R.string.camera, route = "camera", R.drawable.camera_24px)
    data object Connectivity: NavigationItem(name = R.string.connectivity, route = "connectivity", R.drawable.bluetooth_connected_24px)
    data object Settings: NavigationItem(name = R.string.settings, route = "settings", R.drawable.outline_settings_24)
    data object Apps: NavigationItem(name = R.string.apps, route = "apps", R.drawable.outline_apps_24)
}
