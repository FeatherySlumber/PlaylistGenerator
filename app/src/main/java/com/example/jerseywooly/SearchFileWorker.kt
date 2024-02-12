package com.example.jerseywooly

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.MediaMetadataRetriever
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.ForegroundInfo
import androidx.work.OneTimeWorkRequest
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.OutOfQuotaPolicy
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.example.jerseywooly.db.AppDatabase
import com.example.jerseywooly.db.MusicDirectory
import com.example.jerseywooly.db.MusicFile
import com.example.jerseywooly.db.SelectedDirectory
import com.example.jerseywooly.view.MainActivity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.io.File
import java.time.Instant

class SearchFileWorker(private val ctx: Context, params: WorkerParameters) :
    CoroutineWorker(ctx, params) {
    private val notificationManager =
        ContextCompat.getSystemService(ctx, NotificationManager::class.java)

    /**
     * バックグラウンドでの作業を実行します。
     * @return Result ワーカーの実行結果
     */
    override suspend fun doWork(): Result {
        val db = AppDatabase.getDatabase(applicationContext)

        // 入力データから検索対象のディレクトリパスリストを取得
        val pathList = inputData.getStringArray(DATA_KEY) ?: arrayOf()
        // val sdList = db.selectedDirectoryDao().getAllDirectory()

        try {
            // searchDirectory(sdList.map { File(it.directory) }).collect { dir ->
            searchDirectory(pathList.map { File(it) }).collect { dir ->
                val start = Instant.now()
                Log.d("tag", dir.path)
                // 検索したディレクトリ内のオーディオファイルを取得
                val audioFiles = dir.listFiles { file ->
                    file.isFile && file.canRead() &&
                            (file.name.endsWith(".mp3", ignoreCase = true) ||
                                    file.name.endsWith(".wav", ignoreCase = true))
                }
                Log.d("audio", audioFiles?.size.toString())
                if (audioFiles?.isEmpty() == false) {
                    val dirId =
                        db.musicDirectoryDao().insert(MusicDirectory(directory = dir.toPath(), lastScannedAt = Instant.now()))
                    setProgressAsync(Data.Builder().putString("currentDir", dir.path).build())

                    val mmr = MediaMetadataRetriever()
                    try {
                        for (file in audioFiles) {
                            file.inputStream().use {
                                mmr.setDataSource(it.fd)

                                val title =
                                    mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE)
                                        ?: file.nameWithoutExtension
                                Log.d("title", title)
                                val artist =
                                    mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST)
                                        ?: "Unknown"
                                val length =
                                    mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
                                        ?.toIntOrNull()
                                val fileName = file.name

                                if (length != null) {
                                    db.musicFileDao().insert(
                                        MusicFile(
                                            title = title,
                                            artist = artist,
                                            length = length,
                                            fileName = fileName,
                                            directoryId = dirId
                                        )
                                    )
                                }
                            }
                        }
                    } catch (e: Exception) {
                        Log.e("err", e.message ?: "")
                        setProgressAsync(Data.Builder().putString(CURRENT_SEARCH_DIR, "Error").build())
                    } finally {
                        mmr.close()
                    }
                }
                db.musicDirectoryDao().deleteContainDirectoriesScannedBefore(dir.toPath().toAbsolutePath(), start)
            }
        } catch (e: Exception) {
            return Result.failure()
        }

        return Result.success()
    }

    companion object {
        const val CHANNEL_ID = "57358"
        const val NOTIFY_ID = 57358
        const val SEARCH_WORK_NAME = "WORKINGJERSEYWOOLY"
        const val CURRENT_SEARCH_DIR = "currentDir"
        private const val DATA_KEY = "dirs"

        fun OneTimeWorkRequest(vararg dirs : SelectedDirectory) : OneTimeWorkRequest {
            val data = Data.Builder()
            data.putStringArray(DATA_KEY, dirs.map { it.directory.toString() }.toTypedArray())

            return OneTimeWorkRequestBuilder<SearchFileWorker>()
                .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
                .setInputData(data.build())
                .build()
        }
    }

    init {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "検索中のファイル",
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "ファイルサーチ中に表示"
        }
        notificationManager?.createNotificationChannel(channel)
    }

    override suspend fun getForegroundInfo(): ForegroundInfo =
        ForegroundInfo(R.string.app_name, createNotification())

    /**
     * ディレクトリの配列をカプセル化
     * 現在のディレクトリを追跡するためのインデックスを維持。
     * ディレクトリの反復処理とトラバーサルの状態を保持するためにsearchDirectoryで使用。
     * @property dirs ディレクトリの配列
     */
    private class DirWithIdx(val dirs: Array<File>) {
        private var idx: Int = 1
        val isLastDirectory: Boolean
            get() = idx >= dirs.size
        val nextDirectory: File
            get() = dirs[idx++]
    }

    /**
     * 選択したディレクトリを非同期で検索し、見つかった各ディレクトリを発行。
     * この関数は、ディレクトリを表す File オブジェクトの Flow を返す。
     * @param selectedDirectories 検索を開始する初期ディレクトリのリスト
     * @return Flow<File> ディレクトリを表す File オブジェクトを発行する Flow
     */
    private suspend fun searchDirectory(selectedDirectories: List<File>): Flow<File> = flow {
        for (path in selectedDirectories) {
            var current: File = path
            val stack = ArrayDeque(listOf(DirWithIdx(arrayOf(current))))
            emit(current)
            while (stack.isNotEmpty()) {
                val directories = current.listFiles { ele -> ele.isDirectory } ?: arrayOf()
                if (directories.isNotEmpty()) {
                    current = directories[0]
                    emit(current)
                    stack.add(DirWithIdx(directories))
                } else {
                    if (stack.last().isLastDirectory) {
                        stack.removeLast()
                    } else {
                        current = stack.last().nextDirectory
                        emit(current)
                    }
                }
            }
        }
    }

    private fun createNotification(): Notification {
        val cancelIntent = WorkManager.getInstance(ctx).createCancelPendingIntent(id)
        val openIntent: PendingIntent = PendingIntent.getActivity(
            ctx, 0,
            Intent(ctx, MainActivity::class.java).apply {
                flags =
                    Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_ONE_SHOT
        )

        return NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setContentTitle("ファイル検索")
            .setSmallIcon(android.R.drawable.ic_menu_search)
            .setContentTitle("検索中...")
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText("現在走査中のディレクトリはアプリ上に表示されます\nタップで表示")
            )
            .setContentIntent(openIntent)
            .addAction(android.R.drawable.ic_menu_close_clear_cancel, "停止", cancelIntent)
            .setOngoing(true)
            .setShowWhen(true)
            .setAutoCancel(false)
            .build()
    }

    private fun sendNotification(): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    applicationContext,
                    android.Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_DENIED
            ) {
                return false
            }
        }
        with(NotificationManagerCompat.from(applicationContext)) {
            notify(NOTIFY_ID, createNotification())
        }
        return true
    }

    private fun deleteNotification() {
        NotificationManagerCompat.from(applicationContext).cancel(NOTIFY_ID)
    }
}
