package com.example.jerseywooly.view

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Folder
import androidx.compose.material.icons.rounded.LibraryMusic
import androidx.compose.material.icons.rounded.QueueMusic
import androidx.compose.material.icons.rounded.StopCircle
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import com.example.jerseywooly.viewmodel.LogViewModel

@Composable
fun BarBottom(navController: NavController){
    NavBottom(navController)
    LogBottom()
}

@Composable
fun LogBottom(viewModel : LogViewModel = viewModel()){
    val logView by viewModel.searchLog.observeAsState()
    logView?.run{
        Row(
            Modifier
                .fillMaxWidth()
                .background(color = MaterialTheme.colorScheme.primary)) {
            Text(this@run, Modifier.weight(1f), MaterialTheme.colorScheme.onPrimary)
            IconButton(onClick = viewModel::cancelWork) {
                Icon(Icons.Rounded.StopCircle, "cancel", tint = MaterialTheme.colorScheme.onPrimary)
            }
        }
    }
}

@Composable
fun NavBottom(navController: NavController) {
    Log.d("screen", navController.currentDestination?.route.toString())
    Row(
        Modifier
            .background(MaterialTheme.colorScheme.primaryContainer)
            .fillMaxWidth()
    ) {
        NavBottomItem(
            Modifier.weight(1f),
            Icons.Rounded.QueueMusic,
            "生成",
            navController,
            ScreenType.Main
        )
        NavBottomItem(
            Modifier.weight(1f),
            Icons.Rounded.Folder,
            "選択",
            navController,
            ScreenType.Directory
        )
        NavBottomItem(
            Modifier.weight(1f),
            Icons.Rounded.LibraryMusic,
            "一覧",
            navController,
            ScreenType.MusicList
        )
    }
}

@Composable
fun NavBottomItem(
    modifier: Modifier = Modifier,
    imageVector: ImageVector,
    text: String,
    navController: NavController,
    type: ScreenType
) {
    Column(
        Modifier
            .clickable {
                navController.navigate(type.name) {
                    popUpTo(navController.graph.findStartDestination().id) {
                        saveState = true
                    }
                    launchSingleTop = true
                    restoreState = true
                }
            }
            .then(modifier)
    ) {
        Divider(
            thickness = 2.dp,
            color = if (navController.currentDestination?.route == type.name) MaterialTheme.colorScheme.onPrimaryContainer else Color.Transparent
        )
        Column(
            Modifier
                .fillMaxWidth()
                .padding(vertical = ButtonDefaults.ContentPadding.calculateTopPadding()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = imageVector,
                null,
                tint = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Text(
                text,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                fontSize = MaterialTheme.typography.labelSmall.fontSize
            )
        }
    }
}
