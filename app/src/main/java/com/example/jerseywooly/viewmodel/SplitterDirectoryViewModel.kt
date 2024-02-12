package com.example.jerseywooly.viewmodel

import android.app.Application
import android.os.Build
import android.os.Environment
import android.os.storage.StorageManager
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.jerseywooly.SettingDataStore
import com.example.jerseywooly.db.AppDatabase
import com.example.jerseywooly.db.SelectedDirectory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File
import kotlin.io.path.isSameFileAs

class SplitterDirectoryViewModel(application: Application) : AndroidViewModel(application) {
    private val setting = SettingDataStore(application.applicationContext)
    private val db: AppDatabase = AppDatabase.getDatabase(application.applicationContext)
    val selectedDirectories = db.selectedDirectoryDao().getAllDirectoryStream()
        .stateIn(viewModelScope, SharingStarted.Lazily, listOf())

    private val _directory = MutableStateFlow(File(setting.defaultStartDirectoryPath))
    val directory: StateFlow<File> = _directory

    val directories: StateFlow<List<Directory>> =
        combine(selectedDirectories, directory) { _, dir ->
            val dirs = getDirectories(dir)
            return@combine if (dir.name.isEmpty() && dirs.isEmpty()) initDirectories() else dirs
        }.stateIn(viewModelScope, SharingStarted.Lazily, listOf())

    init {
        viewModelScope.launch {
            val path = setting.startDirectoryPath.firstOrNull() ?: return@launch
            _directory.value = File(path)
        }
    }

    private val storageManager =
        ContextCompat.getSystemService(application.applicationContext, StorageManager::class.java)

    fun changeDirectory(dir: File) {
        if (!dir.isDirectory) return
        _directory.value = dir
    }

    suspend fun selectDirectory(dir: File) {
        if (!dir.isDirectory) return
        db.selectedDirectoryDao().insert(SelectedDirectory(directory = dir.toPath()))
    }

    suspend fun deleteDirectory(dir: File) {
        val sd = selectedDirectories.value.find { it.directory.isSameFileAs(dir.toPath()) }
        if (sd != null) {
            deleteDirectory(sd)
        }
    }

    suspend fun deleteDirectory(sd: SelectedDirectory) {
        db.selectedDirectoryDao().deleteDirectory(sd)
    }

    private fun getDirectories(dir: File): List<Directory> {
        val dirs: MutableList<Directory> = mutableListOf()
        val parent = dir.parentFile
        if (parent != null) {
            dirs.add(createDirectory(parent, DirNameType.Parent))
        }
        dirs.addAll(dir.listFiles { ele -> ele.isDirectory }?.map {
            createDirectory(it, DirNameType.DirName)
        } ?: listOf())

        return dirs
    }

    private fun initDirectories(): List<Directory> {
        val dirs: MutableList<Directory> = mutableListOf()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            for (dir in storageManager?.storageVolumes?.mapNotNull { it.directory } ?: listOf()) {
                dirs.add(createDirectory(dir, DirNameType.Path))
            }
        } else {
            val root = Environment.getRootDirectory()
            dirs.add(createDirectory(root, DirNameType.Path))
            val data = Environment.getDataDirectory()
            dirs.add(createDirectory(data, DirNameType.Path))
            val download = Environment.getDownloadCacheDirectory()
            dirs.add(createDirectory(download, DirNameType.Path))
            if (Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED) {
                val sdcard = Environment.getExternalStorageDirectory()
                dirs.add(createDirectory(sdcard, DirNameType.Path))
            }
        }
        return dirs
    }

    data class Directory(
        val value: File,
        private val nameType: DirNameType,
        val contained: Boolean
    ) {
        val displayName: String = when (nameType) {
            DirNameType.Parent -> ".."
            DirNameType.DirName -> value.name
            DirNameType.Path -> value.absolutePath
        }
    }

    private fun createDirectory(value: File, nameType: DirNameType): Directory =
        Directory(
            value,
            nameType,
            selectedDirectories.value.any { it.directory.isSameFileAs(value.toPath()) })

    enum class DirNameType {
        Parent,
        DirName,
        Path
    }
}