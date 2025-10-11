package com.lkonlesoft.displayinfo.helper

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit

class SettingsManager private constructor(context: Context) {

    private val sharedPreferences: SharedPreferences = context.getSharedPreferences("AppSettings", Context.MODE_PRIVATE)

    companion object {
        @Volatile
        private var INSTANCE: SettingsManager? = null

        fun getInstance(context: Context): SettingsManager {
            return INSTANCE ?: synchronized(this) {
                val instance = INSTANCE ?: SettingsManager(context)
                INSTANCE = instance
                instance
            }
        }
    }

    // Save settings (example: save a boolean value)
    fun saveSettingLogic(key: String, value: Boolean) {
        sharedPreferences.edit {
            putBoolean(key, value)
            // or commit() for synchronous saving
        }
    }

    // Retrieve settings (example: retrieve a boolean value)
    fun getSettingLogic(key: String, defaultValue: Boolean = true): Boolean {
        return sharedPreferences.getBoolean(key, defaultValue)
    }

    fun saveSettingsInt(key: String, value: Int) {
        sharedPreferences.edit {
            putInt(key, value)
            // or commit() for synchronous saving
        }
    }

    fun getSettingsInt(key: String, defaultValue: Int = 0): Int {
        return sharedPreferences.getInt(key, defaultValue)
    }

}
