package com.example.jerseywooly.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.map
import androidx.lifecycle.viewModelScope
import androidx.work.ExistingWorkPolicy
import androidx.work.WorkManager
import com.example.jerseywooly.SearchFileWorker
import com.example.jerseywooly.db.AppDatabase
import com.example.jerseywooly.db.SelectedDirectory
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlin.io.path.relativeTo

class MusicListViewModel(application: Application) : AndroidViewModel(application) {
    private val db: AppDatabase = AppDatabase.getDatabase(application.applicationContext)
    private val wm: WorkManager = WorkManager.getInstance(application.applicationContext)

    private val searchFileInfo = wm.getWorkInfosForUniqueWorkLiveData(SearchFileWorker.SEARCH_WORK_NAME)
    val isSearchFinished = searchFileInfo.map {
        it.firstOrNull()?.state?.isFinished ?: true
    }

    val musicList = db.selectedDirectoryDao().getAllDirectoryStream()
        .combine(db.musicFileDao().maxId()) { list, _ ->
            list.map { sd ->
                val musicDirs = db.musicDirectoryDao().getContainDirectories(sd.directory)
                val musics = db.musicFileDao().selectInDirectories(musicDirs.map { it.id }).map{ mf ->
                    musicDirs.find{it.id == mf.directoryId }!!.directory.relativeTo(sd.directory) to mf
                }
                return@map sd to musics
            }
        }.stateIn(viewModelScope, SharingStarted.Lazily, listOf())

    fun startSearchAllFile() {
        viewModelScope.launch {
            val sdList = db.selectedDirectoryDao().getAllDirectory()

            wm.beginUniqueWork(
                SearchFileWorker.SEARCH_WORK_NAME, ExistingWorkPolicy.KEEP,
                SearchFileWorker.OneTimeWorkRequest(*(sdList.toTypedArray()))
            )
                .enqueue()
        }
    }

    fun startSearchFile(directory: SelectedDirectory) {
        viewModelScope.launch {
            wm.beginUniqueWork(
                SearchFileWorker.SEARCH_WORK_NAME, ExistingWorkPolicy.KEEP,
                SearchFileWorker.OneTimeWorkRequest(directory)
            )
                .enqueue()
        }
    }

    fun deleteDirectory(directory: SelectedDirectory){
        viewModelScope.launch {
            db.selectedDirectoryDao().deleteDirectory(directory)
        }
    }
}