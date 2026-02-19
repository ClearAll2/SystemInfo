package com.lkonlesoft.displayinfo.di

import com.lkonlesoft.displayinfo.helper.SettingsManager
import com.lkonlesoft.displayinfo.viewmodel.SettingsViewModel
import org.koin.android.ext.koin.androidApplication
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
    single { androidContext() }
    single<SettingsManager> { SettingsManager.getInstance(androidApplication()) }
    viewModel { SettingsViewModel(get()) }
}