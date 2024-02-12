package com.example.jerseywooly.viewmodel

import android.app.Application
import android.os.Build
import android.os.Environment
import android.os.storage.StorageManager
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.jerseywooly.SettingDataStore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import java.io.File

abstract class SelectDirectoryViewModel (application: Application) : AndroidViewModel(application) {
    protected val setting = SettingDataStore(application.applicationContext)
    private val storageManager =
        ContextCompat.getSystemService(application.applicationContext, StorageManager::class.java)

    protected abstract val currentDirectory: MutableStateFlow<File>
    val directory: StateFlow<File>
        get() = currentDirectory

    /**
     * ディレクトリとそのパスのリストを提供する StateFlow プロパティ。
     * @return StateFlow<List<Pair<File, String>>> ディレクトリと絶対パスのペアのリストを表す StateFlow
     */
    open val directories: StateFlow<List<Pair<File, String>>> by lazy{
        currentDirectory.map {
            val dirs: MutableList<Pair<File, String>> = mutableListOf()

            // 親ディレクトリが存在する場合は、親ディレクトリの情報をリストに追加
            val parent = it.parentFile
            if (parent != null) {
                dirs.add(parent to "..")
            }

            // サブディレクトリが存在する場合は、それぞれのサブディレクトリの情報をリストに追加
            dirs.addAll(it.listFiles { f -> f.isDirectory }?.map { f ->
                f to f.name
            } ?: listOf())

            // リストが空の場合は、デバイス上で利用可能なディレクトリを取得
            if (dirs.isEmpty()) {
                directoryList()
            } else {
                dirs
            }
        }.stateIn(viewModelScope, SharingStarted.Lazily, listOf())
    }

    fun changeDirectory(dir: File) {
        if (!dir.isDirectory) return
        currentDirectory.value = dir
    }

    open val enabledDoAction = MutableStateFlow(true).asStateFlow()

    abstract suspend fun doAction(dir : File)

    /**
     * デバイス上で利用可能なディレクトリとその絶対パスのリストを取得する関数。
     * @return List<Pair<File, String>> ディレクトリと絶対パスのペアのリスト
     */
    private fun directoryList() : List<Pair<File, String>> {
        val dirs: MutableList<Pair<File, String>> = mutableListOf()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            // Android 11以降の場合、Storage Volumeを利用してディレクトリ情報を取得
            for (dir in storageManager?.storageVolumes?.mapNotNull { f -> f.directory } ?: listOf()) {
                dirs.add(dir to dir.absolutePath)
            }
        } else {
            // Android 10以前の場合、標準的なディレクトリをリストに追加
            val root = Environment.getRootDirectory()
            dirs.add(root to root.absolutePath)
            val data = Environment.getDataDirectory()
            dirs.add(data to data.absolutePath)
            val download = Environment.getDownloadCacheDirectory()
            dirs.add(download to download.absolutePath)

            // Android 10以前の場合、標準的なディレクトリをリストに追加
            if (Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED) {
                val sdcard = Environment.getExternalStorageDirectory()
                dirs.add(sdcard to sdcard.absolutePath)
            }
        }
        return dirs
    }
}