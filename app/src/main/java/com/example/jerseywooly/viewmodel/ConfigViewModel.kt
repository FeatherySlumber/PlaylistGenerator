package com.example.jerseywooly.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.jerseywooly.SettingDataStore
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ConfigViewModel(application: Application) : AndroidViewModel(application) {
    private val setting = SettingDataStore(application.applicationContext)
    val saveDirectory = setting.saveDirectoryPath
        .stateIn(viewModelScope, SharingStarted.Lazily, null)

    val useAbsolutePath = setting.useAbsolutePath
        .stateIn(viewModelScope, SharingStarted.Lazily, setting.defaultUseAbsoluteDirectory)

    val startDirectoryPath = setting.startDirectoryPath
        .stateIn(viewModelScope, SharingStarted.Lazily, setting.defaultStartDirectoryPath)

    fun setUsePath(value: Boolean) {
        viewModelScope.launch {
            setting.trySetUseAbsolutePath(value)
        }
    }
    /*
    val allowDuplicate = setting.allowDuplicate
        .stateIn(viewModelScope, SharingStarted.Lazily, false)

    fun setAllowDuplicate(value: Boolean){
        viewModelScope.launch {
            setting.trySetAllowDuplicate(value)
        }
    }
     */
}