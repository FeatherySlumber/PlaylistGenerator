package com.example.jerseywooly.view

import android.widget.Toast
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Send
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.jerseywooly.viewmodel.MainViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainView(
    navController: NavController,
    viewModel: MainViewModel = viewModel()
) {
    Scaffold(topBar = {
        TopAppBar(
            title = { Text("JerseyWooly") },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.primary,
                titleContentColor = MaterialTheme.colorScheme.onPrimary
            ),
            actions = {
                IconButton(onClick = { navController.navigate(ScreenType.Config.name) }) {
                    Icon(
                        imageVector = Icons.Rounded.Settings,
                        "Settings",
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                }
            })
    }) {
        RequestPermission()
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(it),
        ) {
            Row(
                Modifier
                    .fillMaxWidth()
                    .height(IntrinsicSize.Min)
                    .padding(ButtonDefaults.TextButtonContentPadding)
            ) {
                var minute by remember { mutableStateOf(0) }
                val scope = rememberCoroutineScope()
                var height by remember { mutableStateOf(0.dp) }
                val density = LocalDensity.current
                TextField(
                    value = if (minute != 0) minute.toString() else "",
                    onValueChange = { value -> minute = value.toIntOrNull() ?: 0 },
                    modifier = Modifier
                        .weight(1f)
                        .onGloballyPositioned {
                            height = with(density) { it.size.height.toDp() }
                        },
                    label = { Text("プレイリスト生成(分単位)") },
                    keyboardOptions = KeyboardOptions.Default.copy(
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Send
                    ),
                    keyboardActions = KeyboardActions(onSend = {
                        scope.launch {
                            viewModel.makePlayList(minute)
                        }
                    }),
                    singleLine = true,
                    shape = RectangleShape
                )
                Button(
                    onClick = {
                        scope.launch {
                            viewModel.makePlayList(minute)
                        }
                    },
                    modifier = Modifier
                        .height(height)
                        .aspectRatio(1f),
                    enabled = minute > 0,
                    shape = RoundedCornerShape(0, 50, 50, 0),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Icon(imageVector = Icons.Rounded.Send, contentDescription = "生成")
                }
            }
            Column(Modifier.fillMaxWidth()) {
                Row(Modifier.fillMaxWidth()){
                    val plInfo by viewModel.playlistInfo.collectAsState()
                    Text("${plInfo.first}曲", Modifier.weight(1f), textAlign = TextAlign.Center)
                    Text(plInfo.second, Modifier.weight(1f), textAlign = TextAlign.Center)
                }
                Divider(color = MaterialTheme.colorScheme.outline)
            }
            val playlist by viewModel.previewPlaylist.collectAsState()
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                val state = rememberLazyListState()
                val color = MaterialTheme.colorScheme.surface.copy(alpha = 0.6f)
                LazyColumn(
                    Modifier
                        .fillMaxSize()
                        .scrollFadingEdge(Orientation.Vertical, state, color),
                    state = state
                ) {
                    items(playlist) {
                        ConstraintLayout(
                            Modifier
                                .fillMaxWidth()
                                .padding(ButtonDefaults.TextButtonContentPadding)
                        ) {
                            val (title, playTime, artist, dir) = createRefs()
                            val dirScroll = rememberScrollState()
                            val artistScroll = rememberScrollState()
                            Text(it.second.directory.toString(),
                                color = MaterialTheme.colorScheme.onBackground.copy(0.7f),
                                fontSize = MaterialTheme.typography.bodySmall.fontSize,
                                maxLines = 1,
                                modifier = Modifier
                                    .constrainAs(dir) {
                                        width = Dimension.matchParent
                                        top.linkTo(parent.top)
                                    }
                                    .horizontalScroll(dirScroll, reverseScrolling = true)
                                    .scrollFadingEdge(
                                        Orientation.Horizontal,
                                        dirScroll,
                                        shadowRatio = 0.2f,
                                        isReverse = true
                                    ))
                            Text(it.first.title,
                                fontSize = MaterialTheme.typography.bodyLarge.fontSize,
                                modifier = Modifier
                                    .constrainAs(title) {
                                        width = Dimension.matchParent
                                        top.linkTo(dir.bottom)
                                    })
                            Text(it.first.artist,
                                fontSize = MaterialTheme.typography.bodyMedium.fontSize,
                                maxLines = 1,
                                modifier = Modifier
                                    .wrapContentHeight()
                                    .constrainAs(artist) {
                                        width = Dimension.fillToConstraints
                                        top.linkTo(title.bottom)
                                        bottom.linkTo(parent.bottom)
                                        start.linkTo(parent.start)
                                        end.linkTo(playTime.start)
                                    }
                                    .horizontalScroll(artistScroll, reverseScrolling = true)
                                    .scrollFadingEdge(
                                        Orientation.Horizontal,
                                        artistScroll,
                                        shadowRatio = 0.2f,
                                        isReverse = true
                                    ))
                            Text(it.first.playTimeString,
                                fontSize = MaterialTheme.typography.bodyMedium.fontSize,
                                modifier = Modifier
                                    .constrainAs(playTime) {
                                        width = Dimension.wrapContent
                                        top.linkTo(title.bottom)
                                        bottom.linkTo(parent.bottom)
                                        start.linkTo(artist.end)
                                        end.linkTo(parent.end)
                                    })
                        }
                        Divider(color = MaterialTheme.colorScheme.outline)
                    }
                }
            }
            val canSave by viewModel.canSavePlayList.collectAsState()
            val context = LocalContext.current
            Button(
                onClick = {
                    val path = viewModel.savePlayList()
                    if (path != null) {
                        Toast.makeText(context, "保存:${path.toAbsolutePath()}", Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier
                    .padding(ButtonDefaults.ContentPadding)
                    .fillMaxWidth(),
                enabled = playlist.isNotEmpty() && canSave
            ) { Text("保存") }
            BarBottom(navController)
            LaunchedEffect(canSave) {
                if (!canSave) {
                    Toast.makeText(context, "設定から保存先を指定してください", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
