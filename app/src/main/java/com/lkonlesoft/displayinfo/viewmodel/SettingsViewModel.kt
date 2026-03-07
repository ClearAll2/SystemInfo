package com.lkonlesoft.displayinfo.viewmodel

import android.os.Build
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lkonlesoft.displayinfo.helper.SettingsManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SettingsViewModel(private val settingsManager: SettingsManager) : ViewModel() {

    private val _typographyType = MutableStateFlow(0)
    val typographyType = _typographyType.asStateFlow()

    private val _useNewDashboard = MutableStateFlow(true)
    val useNewDashboard = _useNewDashboard.asStateFlow()

    private val _appColor = MutableStateFlow(0)
    val appColor = _appColor.asStateFlow()

    private val _useDynamicColors = MutableStateFlow(true)
    val useDynamicColors = _useDynamicColors.asStateFlow()

    private val _longPressCopy = MutableStateFlow(true)
    val longPressCopy = _longPressCopy.asStateFlow()

    private val _showNotice = MutableStateFlow(true)
    val showNotice = _showNotice.asStateFlow()

    private val _copyTitle = MutableStateFlow(true)
    val copyTitle = _copyTitle.asStateFlow()

    init {
        viewModelScope.launch(Dispatchers.IO) {
            _typographyType.emit(settingsManager.getSettingsInt("typographyType"))
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
            _copyTitle.emit(settingsManager.getSettingLogic("copyTitle"))
            _showNotice.emit(settingsManager.getSettingLogic("showNotice"))
        }
    }

    fun setTypographyType(typographyType: Int) {
        viewModelScope.launch (Dispatchers.IO) {
            settingsManager.saveSettingsInt("typographyType", typographyType)
            _typographyType.value = typographyType
        }
    }

    fun setCopyTitle(copyTitle: Boolean) {
        viewModelScope.launch (Dispatchers.IO) {
            settingsManager.saveSettingLogic("copyTitle", copyTitle)
            _copyTitle.value = copyTitle
        }
    }

    fun setUseNewDashboard(useNewDashboard: Boolean) {
        viewModelScope.launch (Dispatchers.IO) {
            settingsManager.saveSettingLogic("useNewDashboard", useNewDashboard)
            _useNewDashboard.value = useNewDashboard
        }
    }

    fun setAppColor(color: Int) {
        viewModelScope.launch (Dispatchers.IO) {
            settingsManager.saveSettingsInt("appColor", color)
            _appColor.value = color
        }
    }

    fun setUseDynamicColors(useDynamicColors: Boolean) {
        viewModelScope.launch (Dispatchers.IO) {
            settingsManager.saveSettingLogic("useDynamicColors", useDynamicColors)
            _useDynamicColors.value = useDynamicColors
        }
    }

    fun setLongPressCopy(longPressCopy: Boolean) {
        viewModelScope.launch (Dispatchers.IO) {
            settingsManager.saveSettingLogic("longPressCopy", longPressCopy)
            _longPressCopy.value = longPressCopy
        }
    }

    fun setShowNotice(showNotice: Boolean) {
        viewModelScope.launch (Dispatchers.IO) {
            settingsManager.saveSettingLogic("showNotice", showNotice)
            _showNotice.value = showNotice
        }
    }
}
