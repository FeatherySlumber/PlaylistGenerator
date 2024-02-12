package com.example.jerseywooly.viewmodel

import android.app.Application
import android.os.Build
import android.os.Environment
import android.os.storage.StorageManager
import android.util.Log
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
import kotlinx.coroutines.runBlocking
import java.io.File
import kotlin.io.path.isSameFileAs

class DirectoryViewModel(application: Application) : AndroidViewModel(application) {
        private val setting = SettingDataStore(application.applicationContext)
        private val db: AppDatabase = AppDatabase.getDatabase(application.applicationContext)
        private val selectedDirectories = db.selectedDirectoryDao().getAllDirectoryStream()
            .stateIn(viewModelScope, SharingStarted.Eagerly, listOf())

        private val _directory = MutableStateFlow(File(setting.defaultStartDirectoryPath))
        val directory: StateFlow<File> = _directory

        val directories: StateFlow<List<Directory>> =
            combine(selectedDirectories, directory) { _, dir ->
                val dirs = getDirectories(dir)
                return@combine dirs.ifEmpty { initDirectories() }
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
            db.selectedDirectoryDao().deleteContainDirectory(dir.toPath())
            db.selectedDirectoryDao().insert(SelectedDirectory(directory = dir.toPath()))
        }

        suspend fun deleteDirectory(dir: File) {
            val sd = selectedDirectories.value.find { dir.toPath().isSameFileAs(it.directory) }
            if (sd != null) {
                db.selectedDirectoryDao().deleteDirectory(sd)
            }
        }

        /**
         * 指定されたディレクトリ内のサブディレクトリと親ディレクトリに関する情報を取得する関数。
         * @param dir 対象のディレクトリ
         * @return List<Directory> ディレクトリ情報のリスト
         */
        private fun getDirectories(dir: File): List<Directory> {
            val dirs: MutableList<Directory> = mutableListOf()

            // 親ディレクトリが存在する場合は、親ディレクトリの情報をリストに追加
            val parent = dir.parentFile
            if (parent != null) {
                dirs.add(createDirectory(parent, DirNameType.Parent))
            }

            // サブディレクトリが存在する場合は、それぞれのサブディレクトリの情報をリストに追加
            dirs.addAll(dir.listFiles { ele -> ele.isDirectory }?.map {
                createDirectory(it, DirNameType.DirName)
            } ?: listOf())

            return dirs
        }

        /**
         * デバイス上で利用可能なディレクトリに関する情報を初期化する関数。
         * @return List<Directory> ディレクトリ情報のリスト
         */
        private fun initDirectories(): List<Directory> {
            val dirs: MutableList<Directory> = mutableListOf()

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                // Android 11以降の場合、Storage Volumeを利用してディレクトリ情報を取得
                for (dir in storageManager?.storageVolumes?.mapNotNull { it.directory } ?: listOf()) {
                    dirs.add(createDirectory(dir, DirNameType.Path))
                }
            } else {
                // Android 10以前の場合、標準的なディレクトリをリストに追加
                val root = Environment.getRootDirectory()
                dirs.add(createDirectory(root, DirNameType.Path))
                val data = Environment.getDataDirectory()
                dirs.add(createDirectory(data, DirNameType.Path))
                val download = Environment.getDownloadCacheDirectory()
                dirs.add(createDirectory(download, DirNameType.Path))

                // 外部ストレージがマウントされている場合、SDカードのディレクトリ情報をリストに追加
                if (Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED) {
                    val sdcard = Environment.getExternalStorageDirectory()
                    dirs.add(createDirectory(sdcard, DirNameType.Path))
                }
            }
            return dirs
        }

        /**
         * ディレクトリに関する情報を表すデータクラス。
         * @property value ディレクトリの File オブジェクト
         * @property nameType ディレクトリ名の種類
         * @property contained ディレクトリの含まれる状態
         * @property displayName 表示名（calculated property）
         */
        data class Directory(
            val value: File,
            private val nameType: DirNameType,
            val contained: DirContain
        ) {
            /**
             * 表示名（ディレクトリ名の種類に応じて異なる表示名を設定）
             */
            val displayName: String = when (nameType) {
                DirNameType.Parent -> ".."
                DirNameType.DirName -> value.name
                DirNameType.Path -> value.absolutePath
            }
        }

        /**
         * ディレクトリに関する情報を生成する関数。
         * @param value ディレクトリの File オブジェクト
         * @param nameType ディレクトリ名の種類
         * @return Directory ディレクトリに関する情報を含む Directory オブジェクト
         */
        private fun createDirectory(value: File, nameType: DirNameType): Directory =
            Directory(
                value,
                nameType,
                run{
                    var c = DirContain.UnContained
                    runBlocking {
                        val path = value.toPath().toAbsolutePath()
                        val sd = selectedDirectories.value.firstOrNull { path.startsWith(it.directory) }
                        Log.d(value.name, sd.toString())
                        if (sd != null) {
                            c = if(sd.directory.isSameFileAs(path)){
                                DirContain.Contained
                            }else{
                                DirContain.Parent
                            }
                        }
                    }
                    Log.d(value.name, c.isContained.toString())
                    Log.d(value.name, c.name)
                    c
                }
            )

        /**
         * ディレクトリ名の種類を表す列挙型。
         */
        enum class DirNameType {
            Parent,
            DirName,
            Path
        }

        /**
         * ディレクトリが含まれる状態を表す列挙型。
         */
        enum class DirContain{
            UnContained,
            Contained,
            Parent;
            /**
             * ディレクトリが含まれる状態かどうかを示すプロパティ。
             */
            val isContained: Boolean
                get() = this != UnContained
        }
    }
