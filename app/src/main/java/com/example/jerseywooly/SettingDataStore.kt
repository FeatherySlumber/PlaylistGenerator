package com.example.jerseywooly

import android.content.Context
import android.os.Environment
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.File
import java.io.IOException

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class SettingDataStore(private val ctx: Context) {
    companion object{
        private const val defaultUAD = false
        private val defaultStartDirectory = Environment.getRootDirectory()

        private val SAVE_DIRECTORY = stringPreferencesKey("save_directory")
        private val START_DIRECTORY = stringPreferencesKey("start_directory")
        private val USE_ABSOLUTE_PATH = booleanPreferencesKey("use_absolute_path")
        private val ALLOW_DUPLICATE = booleanPreferencesKey("allow_duplicate")
    }

    val defaultUseAbsoluteDirectory = defaultUAD
    val defaultStartDirectoryPath : String
        get() = defaultStartDirectory.absolutePath

    // ファイルが保存できるディレクトリ、またはnullを返す
    val saveDirectoryPath : Flow<String?> = ctx.dataStore.data
        .catch { exception ->
            if (exception is IOException) emit(emptyPreferences()) else throw exception
        }.map {
            var result : String? = null
            val sd = it[SAVE_DIRECTORY]
            if (!sd.isNullOrBlank()) {
                val f = File(sd)
                if (f.exists() && f.isDirectory && f.canWrite()) {
                    result = sd
                }
            }
            result
        }

    // エラーが起きなければtrue
    suspend fun trySetSaveDirectory(path : String) : Boolean {
        return try{
            ctx.dataStore.edit { it[SAVE_DIRECTORY] = path }
            true
        }catch (e : IOException){
            false
        }
    }

    // ファイルピッカー開始時の初期ディレクトリ
    val startDirectoryPath : Flow<String> = ctx.dataStore.data
        .catch { exception ->
            if (exception is IOException) emit(emptyPreferences()) else throw exception
        }.map {
            var result = defaultStartDirectoryPath
            val sd = it[START_DIRECTORY]
            if (!sd.isNullOrBlank()) {
                val f = File(sd)
                if (f.exists() && f.isDirectory) {
                    result = sd
                }
            }
            result
        }

    // エラーが起きなければtrue
    suspend fun trySetStartDirectory(path : String) : Boolean {
        return try{
            ctx.dataStore.edit { it[START_DIRECTORY] = path }
            true
        }catch (e : IOException){
            false
        }
    }

    val useAbsolutePath : Flow<Boolean> = ctx.dataStore.data
        .catch {exception ->
            if (exception is IOException) emit(emptyPreferences()) else throw exception
        }.map{
            it[USE_ABSOLUTE_PATH] ?: defaultUseAbsoluteDirectory
        }

    suspend fun trySetUseAbsolutePath(bool : Boolean) : Boolean {
        return try{
            ctx.dataStore.edit { it[USE_ABSOLUTE_PATH] = bool }
            true
        }catch (e : IOException){
            false
        }
    }

    /*
    val allowDuplicate : Flow<Boolean> = ctx.dataStore.data
        .catch {exception ->
            if(exception is IOException) emit(emptyPreferences()) else throw exception
        }.map{
            it[ALLOW_DUPLICATE] ?: false
        }

    suspend fun trySetAllowDuplicate(bool : Boolean) : Boolean {
        return try{
            ctx.dataStore.edit { it[ALLOW_DUPLICATE] = bool }
            true
        }catch (e : IOException){
            false
        }
    }
     */
}