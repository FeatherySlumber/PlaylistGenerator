package com.example.jerseywooly.viewmodel

import android.app.Application
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.async
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.cancellable
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.runBlocking
import java.io.File

class SelectSaveDirectoryViewModel(application: Application) :
    SelectDirectoryViewModel(application) {
    override val currentDirectory: MutableStateFlow<File> = run {
        var path: String = setting.defaultStartDirectoryPath
        runBlocking {
            try {
                async {
                    setting.startDirectoryPath.cancellable().collect {
                        path = it
                        cancel()
                    }
                }.await()
            } catch (_: CancellationException) {}
            try{
                async {
                    setting.saveDirectoryPath.collect {
                        if (it != null) {
                            path = it
                        }
                        cancel()
                    }
                }.await()
            } catch (_: CancellationException) {}
        }
        MutableStateFlow(File(path))
    }

    override val enabledDoAction = currentDirectory.map {
        it.isDirectory && it.canWrite()
    }.stateIn(viewModelScope, SharingStarted.Lazily, false)

    override suspend fun doAction(dir: File) {
        if (dir.isDirectory && dir.canWrite())
            setting.trySetSaveDirectory(dir.absolutePath)
    }
}