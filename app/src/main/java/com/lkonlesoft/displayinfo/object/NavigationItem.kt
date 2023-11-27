package com.lkonlesoft.displayinfo.`object`

import com.lkonlesoft.displayinfo.R

sealed class NavigationItem (val route: String, val icon: Int){
    data object Home: NavigationItem(route = "System Info", R.drawable.outline_info_24)
    data object System: NavigationItem(route = "System", R.drawable.outline_settings_24)
    data object Android: NavigationItem(route = "Android", R.drawable.outline_android_24)
    data object Display: NavigationItem(route = "Display", R.drawable.outline_smartphone_24)
    data object Battery: NavigationItem(route = "Battery", R.drawable.outline_battery_4_bar_24)
    data object Memory: NavigationItem(route = "Memory", R.drawable.outline_memory_24)
    data object Network: NavigationItem(route = "Network", R.drawable.outline_network_cell_24)
    data object SOC: NavigationItem(route = "SoC", R.drawable.outline_developer_board_24)
}
