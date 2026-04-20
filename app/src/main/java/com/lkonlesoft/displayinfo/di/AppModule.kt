package com.lkonlesoft.displayinfo.di

import com.lkonlesoft.displayinfo.helper.SettingsManager
import com.lkonlesoft.displayinfo.viewmodel.SettingsViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val appModule = module {
    single { SettingsManager.getInstance(androidContext()) }
    viewModelOf(::SettingsViewModel)
}