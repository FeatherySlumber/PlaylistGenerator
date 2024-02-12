package com.example.jerseywooly.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.map
import androidx.work.WorkManager
import com.example.jerseywooly.SearchFileWorker

class LogViewModel(application: Application) : AndroidViewModel(application) {
    private val wm: WorkManager = WorkManager.getInstance(application.applicationContext)
    private val searchFileInfo =
        wm.getWorkInfosForUniqueWorkLiveData(SearchFileWorker.SEARCH_WORK_NAME)
    val searchLog = searchFileInfo.map {
        val wi = it.firstOrNull() ?: return@map null
        return@map if (wi.state.isFinished) null else
            wi.progress.getString(SearchFileWorker.CURRENT_SEARCH_DIR)
    }

    fun cancelWork() {
        wm.cancelUniqueWork(SearchFileWorker.SEARCH_WORK_NAME)
    }
}