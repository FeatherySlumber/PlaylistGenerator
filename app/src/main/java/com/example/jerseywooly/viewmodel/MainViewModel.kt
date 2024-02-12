package com.example.jerseywooly.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.map
import androidx.lifecycle.viewModelScope
import androidx.work.WorkManager
import com.example.jerseywooly.R
import com.example.jerseywooly.SearchFileWorker
import com.example.jerseywooly.SettingDataStore
import com.example.jerseywooly.db.AppDatabase
import com.example.jerseywooly.db.MusicDirectory
import com.example.jerseywooly.db.MusicFile
import com.example.jerseywooly.milliTimeToString
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import java.io.File
import java.nio.file.Path
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import kotlin.io.path.relativeTo

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val db: AppDatabase = AppDatabase.getDatabase(application.applicationContext)
    private val wm: WorkManager = WorkManager.getInstance(application.applicationContext)
    private val setting = SettingDataStore(application.applicationContext)

    private val useAbsolute =
        setting.useAbsolutePath.stateIn(viewModelScope, SharingStarted.Eagerly, false)
    private val saveDirectory =
        setting.saveDirectoryPath.stateIn(viewModelScope, SharingStarted.Eagerly, null)
    /*
    private val allowDuplicate =
        setting.allowDuplicate.stateIn(viewModelScope, SharingStarted.Eagerly, false)
     */

    private val logText = MutableLiveData("Log")

    private val searchFileInfo = wm.getWorkInfosForUniqueWorkLiveData(SearchFileWorker.SEARCH_WORK_NAME)
    private val searchLog = searchFileInfo.map {
        it.firstOrNull()?.progress?.getString("currentDir") ?: "Log"
    }

    val logView: MediatorLiveData<String> = MediatorLiveData("Log")

    private val _previewPlaylist = MutableStateFlow<List<Pair<MusicFile, MusicDirectory>>>(listOf())
    val previewPlaylist: StateFlow<List<Pair<MusicFile, MusicDirectory>>> = _previewPlaylist

    val playlistInfo = previewPlaylist.map{
        it.count() to milliTimeToString(it.sumOf { (f,_) -> f.length })
    }.stateIn(viewModelScope, SharingStarted.Lazily, 0 to "")

    val canSavePlayList = setting.saveDirectoryPath.map {
        it != null
    }.stateIn(viewModelScope, SharingStarted.Lazily, true)

    init {
        logView.addSource(logText){ logView.postValue(it) }
        logView.addSource(searchLog){ logView.postValue(it)}
    }

    fun setLog(value: String) {
        logText.value = value
    }

    /**
     * 指定された再生時間（分）でプレイリストを生成する関数
     *
     * @param minute 生成するプレイリストの再生時間（分）
     */
    suspend fun makePlayList(minute: Int) {
        var millisecond = minute * 1000 * 60

        // 選択されたディレクトリから関連する音楽ディレクトリを取得
        val musicDirs = mutableListOf<MusicDirectory>()
        for (d in db.selectedDirectoryDao().getAllDirectory()) {
            musicDirs.addAll(db.musicDirectoryDao().getContainDirectories(d.directory))
        }

        val ids = musicDirs.map { d -> d.id }
        val playlist = mutableListOf<Pair<MusicFile, MusicDirectory>>()

        // プレイリストを生成するループ
        while (true) {
            val files = db.musicFileDao().selectInDirectoriesShorterThan(ids, millisecond)
            if (files.isEmpty()) break

            // プレイリストに追加するファイルを選択するループ
            while (true) {
                val file = files.random()
                if (file.length > millisecond) break
                playlist.add(file to musicDirs.first { d -> d.id == file.directoryId })
                millisecond -= file.length
            }
        }
        _previewPlaylist.value = playlist
    }

    private val appName = application.resources.getString(R.string.app_name)

    /**
     * プレイリストを保存する関数
     * @return 保存されたプレイリストの絶対パス。保存に失敗した場合はnullを返します。
     */
    fun savePlayList(): Path? {
        val ua = useAbsolute.value
        val dirPath = saveDirectory.value ?: return null
        val dateTime = ZonedDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))
        val f = File(
            dirPath,
            "${appName}_$dateTime.m3u"
        )
        // ファイルが正常に作成された場合
        if (f.createNewFile()) {
            runBlocking {
                writePlayList(f, ua)
            }
        }
        return f.toPath()
    }

    /**
     * プレイリストをファイルに書き込む関数
     *
     * @param file 書き込むファイル
     * @param useAbsolute 絶対パスを使用するかどうかのフラグ
     */
    private suspend fun writePlayList(file: File, useAbsolute: Boolean) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                file.printWriter(Charsets.UTF_8).use { pw ->
                    for ((mf, md) in previewPlaylist.value) {
                        val path = md.directory.resolve(mf.fileName).run {
                            if (useAbsolute) {
                                this.toAbsolutePath()
                            } else {
                                // 相対パスを取得し、ファイルの親ディレクトリに対して相対化
                                file.toPath().parent.let {
                                    this.relativeTo(it)
                                }
                            }
                        }
                        pw.println(path.toString())
                    }
                }
            }
        }
    }
}

