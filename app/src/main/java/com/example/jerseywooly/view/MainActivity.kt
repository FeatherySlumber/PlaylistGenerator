package com.example.jerseywooly.view

import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.FloatRange
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.gestures.ScrollableState
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import java.io.File

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Surface {
                NavControl()
            }
        }
    }
}

enum class ScreenType {
    Main,
    Directory,
    Config,
    SaveDirectory,
    StartDirectory,
    MusicList
}

@Composable
fun NavControl(
    navController: NavHostController = rememberNavController(),
    startDestination: String = ScreenType.Main.name
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(ScreenType.Main.name) {
            MainView(navController)
        }
        composable(ScreenType.Directory.name) {
            DirectoryView(navController)
        }
        composable(ScreenType.Config.name) {
            ConfigView(navController)
        }
        composable(ScreenType.SaveDirectory.name) {
            SelectSaveDirectoryView(navController)
        }
        composable(ScreenType.MusicList.name) {
            MusicListView(navController)
        }
        composable(ScreenType.StartDirectory.name) {
            SelectStartDirectoryView(navController)
        }
    }
}

@Composable
fun RequestPermission() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        RequestManageStoragePermissionOnStart()
    }
    RequestPermissionOnStart(android.Manifest.permission.READ_EXTERNAL_STORAGE)
    RequestPermissionOnStart(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        //  RequestPermissionOnStart(android.Manifest.permission.READ_MEDIA_AUDIO)
        RequestPermissionOnStart(android.Manifest.permission.POST_NOTIFICATIONS)
    }
}

@Composable
fun RequestPermissionOnStart(permission: String) {
    val context = LocalContext.current
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) {
        if (!it) {
            Toast.makeText(context, "アプリを使用できない場合があります\n設定より許可してください", Toast.LENGTH_LONG)
                .show()
        }
        Log.v("tag", it.toString())
    }
    val lifecycleObserver = remember {
        LifecycleEventObserver { _, e ->
            if (e == Lifecycle.Event.ON_START) {
                if (ContextCompat.checkSelfPermission(context, permission)
                    == PackageManager.PERMISSION_DENIED
                ) {
                    launcher.launch(permission)
                }
            }
        }
    }
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    DisposableEffect(lifecycle, lifecycleObserver) {
        lifecycle.addObserver(lifecycleObserver)
        onDispose {
            lifecycle.removeObserver(lifecycleObserver)
        }
    }
}

@SuppressLint("NewApi")
@Composable
fun RequestManageStoragePermissionOnStart(file: File? = null) {
    val context = LocalContext.current
    val lifecycleObserver = remember {
        LifecycleEventObserver { _, e ->
            if (e == Lifecycle.Event.ON_START) {
                val isntESM = if (file != null) {
                    !Environment.isExternalStorageManager(file)
                } else {
                    !Environment.isExternalStorageManager()
                }
                if (isntESM) {
                    val uri = Uri.parse("package:${context.packageName}")
                    val intent =
                        Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION, uri)
                    context.startActivity(intent)
                }
            }
        }
    }
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    DisposableEffect(lifecycle, lifecycleObserver) {
        lifecycle.addObserver(lifecycleObserver)
        onDispose {
            lifecycle.removeObserver(lifecycleObserver)
        }
    }
}

fun Modifier.scrollFadingEdge(
    type: Orientation,
    state: ScrollableState,
    color: Color = Color.White,
    @FloatRange(from = 0.0, to = 0.5) shadowRatio: Float = 0.3f,
    isReverse: Boolean = false
): Modifier = drawWithContent {
    drawContent()
    val start = arrayOf(0f to color, shadowRatio to Color.Transparent)
    val end = arrayOf(1 - shadowRatio to Color.Transparent, 1f to color)
    drawRect(
        brush = when (type) {
            Orientation.Vertical -> when {
                state.canScrollBackward && state.canScrollForward ->
                    Brush.verticalGradient(*start, *end)

                state.canScrollBackward -> Brush.verticalGradient(colorStops = if (isReverse) end else start)

                state.canScrollForward -> Brush.verticalGradient(colorStops = if (isReverse) start else end)

                else -> SolidColor(Color.Transparent)
            }

            Orientation.Horizontal -> when {
                state.canScrollBackward && state.canScrollForward ->
                    Brush.horizontalGradient(*start, *end)

                state.canScrollBackward -> Brush.horizontalGradient(colorStops = if (isReverse) end else start)

                state.canScrollForward -> Brush.horizontalGradient(colorStops = if (isReverse) start else end)

                else -> SolidColor(Color.Transparent)
            }
        }
    )
}

enum class Orientation {
    Horizontal,
    Vertical
}

@Preview
@Composable
fun PreviewMessageCard() {
    NavControl()
}
