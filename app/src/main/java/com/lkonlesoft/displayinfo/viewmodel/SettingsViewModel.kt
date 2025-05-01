package com.lkonlesoft.displayinfo.viewmodel

import android.content.Context
import android.os.Build
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lkonlesoft.displayinfo.helper.SettingsManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SettingsViewModel(context: Context) : ViewModel() {

    // Get singleton instance of SettingsManager
    private val settingsManager = SettingsManager.getInstance(context)

    private val _useNewDashboard = MutableStateFlow(true)
    val useNewDashboard = _useNewDashboard.asStateFlow()

    private val _appColor = MutableStateFlow(0)
    val appColor = _appColor.asStateFlow()

    private val _useDynamicColors = MutableStateFlow(true)
    val useDynamicColors = _useDynamicColors.asStateFlow()

    private val _longPressCopy = MutableStateFlow(true)
    val longPressCopy = _longPressCopy.asStateFlow()

    init {
        viewModelScope.launch(Dispatchers.IO) {
            _useNewDashboard.emit(settingsManager.getSettingLogic("useNewDashboard"))
            _useDynamicColors.emit(settingsManager.getSettingLogic("useDynamicColors"))
            val appColor = settingsManager.getSettingsInt("appColor")
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q && appColor == 0){
                _appColor.emit(2)
            }
            else {
                _appColor.emit(appColor)
            }
            _longPressCopy.emit(settingsManager.getSettingLogic("longPressCopy"))
        }
    }

    fun setUseNewDashboard(useNewDashboard: Boolean) {
        settingsManager.saveSettingLogic("useNewDashboard", useNewDashboard)
        _useNewDashboard.value = useNewDashboard
    }

    fun getUseNewDashboard(): Boolean {
        return settingsManager.getSettingLogic("useNewDashboard")
    }

    fun setAppColor(color: Int) {
        settingsManager.saveSettingsInt("appColor", color)
        _appColor.value = color

    }

    fun getAppColor(): Int {
        return settingsManager.getSettingsInt("appColor")
    }

    fun setUseDynamicColors(useDynamicColors: Boolean) {
        settingsManager.saveSettingLogic("useDynamicColors", useDynamicColors)
        _useDynamicColors.value = useDynamicColors
    }

    fun getUseDynamicColors(): Boolean {
        return settingsManager.getSettingLogic("useDynamicColors")
    }

    fun setLongPressCopy(longPressCopy: Boolean) {
        settingsManager.saveSettingLogic("longPressCopy", longPressCopy)
        _longPressCopy.value = longPressCopy
    }

    fun getLongPressCopy(): Boolean {
        return settingsManager.getSettingLogic("longPressCopy")
    }

}
