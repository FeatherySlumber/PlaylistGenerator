package com.example.jerseywooly.viewmodel

import android.app.Application
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.cancellable
import kotlinx.coroutines.runBlocking
import java.io.File

class SelectStartDirectoryViewModel(application: Application) : SelectDirectoryViewModel(application) {
    override val currentDirectory: MutableStateFlow<File> = run{
        var path: String = setting.defaultStartDirectoryPath
        try{
            runBlocking {
                setting.startDirectoryPath.cancellable().collect {
                    path = it
                    cancel()
                }
            }
        } catch (_: CancellationException) {}
        MutableStateFlow(File(path))
    }

    override suspend fun doAction(dir: File) {
        setting.trySetStartDirectory(dir.absolutePath)
    }
}