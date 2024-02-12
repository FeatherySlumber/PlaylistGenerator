package com.example.jerseywooly.view

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Cancel
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.ExpandMore
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.Sync
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.jerseywooly.db.SelectedDirectory
import com.example.jerseywooly.viewmodel.MusicListViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MusicListView(navController: NavController, viewModel: MusicListViewModel = viewModel()) {
    Scaffold(topBar = {
        TopAppBar(
            title = { Text("音楽一覧") },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.primary,
                titleContentColor = MaterialTheme.colorScheme.onPrimary
            ),
            actions = {
                val enabled by viewModel.isSearchFinished.observeAsState()
                Button(
                    viewModel::startSearchAllFile,
                    enabled = enabled ?: true,
                    // border = BorderStroke(1.dp, MaterialTheme.colorScheme.onPrimary),
                    colors = ButtonDefaults.buttonColors(
                        MaterialTheme.colorScheme.primaryContainer,
                        MaterialTheme.colorScheme.onPrimaryContainer
                    )
                ) { Text("全てスキャン") }
            })
    }) {
        Column(Modifier.fillMaxWidth()) {
            MusicListViewContent(Modifier.weight(1f), it)
            BarBottom(navController)
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun MusicListViewContent(
    modifier: Modifier,
    paddingValues: PaddingValues,
    viewModel: MusicListViewModel = viewModel()
) {
    val state = rememberLazyListState()
    MaterialTheme.colorScheme.primaryContainer
    val musicList by viewModel.musicList.collectAsState()
    val enabled by viewModel.isSearchFinished.observeAsState()
    val invisibleList = remember { mutableStateListOf<SelectedDirectory>() }
    LazyColumn(modifier.scrollFadingEdge(Orientation.Vertical, state, MaterialTheme.colorScheme.background.copy(0.6f)), state, paddingValues) {
        for ((dir, file) in musicList) {
            item {
                Row(
                    modifier = Modifier
                        .background(MaterialTheme.colorScheme.primary)
                        .fillMaxWidth()
                        .height(IntrinsicSize.Min)
                        .padding(
                            horizontal = ButtonDefaults.TextButtonContentPadding
                                .calculateStartPadding(LocalLayoutDirection.current)
                        ),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        Modifier
                            .clickable {
                                if (invisibleList.contains(dir)) invisibleList.remove(dir)
                                else invisibleList.add(dir)
                            }
                            .weight(1f), verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (invisibleList.contains(dir)) Icons.Rounded.ChevronRight else Icons.Rounded.ExpandMore,
                            contentDescription = "Expand",
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                        val scrollState = rememberScrollState()
                        Text(
                            dir.directory.toString(),
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .wrapContentHeight()
                                .padding(ButtonDefaults.TextButtonContentPadding)
                                .horizontalScroll(scrollState, reverseScrolling = true)
                                .scrollFadingEdge(
                                    Orientation.Horizontal,
                                    scrollState,
                                    MaterialTheme.colorScheme.primary,
                                    0.2f,
                                    true
                                ),
                            color = MaterialTheme.colorScheme.onPrimary,
                            fontSize = MaterialTheme.typography.bodyLarge.fontSize,
                            maxLines = 1
                        )
                    }
                    var isExpand by remember { mutableStateOf(false) }
                    IconButton(onClick = { isExpand = true }) {
                        Icon(
                            imageVector = Icons.Rounded.MoreVert,
                            contentDescription = "expandMenu",
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                        DropdownMenu(expanded = isExpand, onDismissRequest = { isExpand = false }) {
                            DropdownMenuItem(
                                text = { Text("音楽ファイルスキャン") },
                                onClick = { viewModel.startSearchFile(dir) },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Rounded.Sync,
                                        contentDescription = "スキャン",
                                    )
                                },
                                enabled = enabled ?: true
                            )
                            DropdownMenuItem(
                                text = { Text("選択解除") },
                                onClick = {
                                    viewModel.deleteDirectory(dir)
                                    isExpand = false
                                },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Rounded.Cancel,
                                        contentDescription = "消去",
                                    )
                                },
                                enabled = enabled ?: true
                            )
                        }
                    }
                }
            }
            if (!invisibleList.contains(dir)) {
                if (file.isNotEmpty()) {
                    items(file) {
                        val scrollState = rememberScrollState()
                        Column(Modifier.fillMaxWidth()) {
                            val path = it.first.toString()
                            if(path.isNotBlank()){
                                val dirScroll = rememberScrollState()
                                Text(path,
                                    color = MaterialTheme.colorScheme.onBackground.copy(0.7f),
                                    fontSize = MaterialTheme.typography.bodySmall.fontSize,
                                    maxLines = 1,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .horizontalScroll(dirScroll, reverseScrolling = true)
                                        .scrollFadingEdge(
                                            Orientation.Horizontal,
                                            dirScroll,
                                            shadowRatio = 0.2f,
                                            isReverse = true
                                        ))
                            }

                        }
                        Text(
                            text = it.second.title,
                            fontSize = MaterialTheme.typography.bodyLarge.fontSize,
                            maxLines = 1,
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(scrollState)
                                .scrollFadingEdge(
                                    Orientation.Horizontal,
                                    scrollState,
                                    shadowRatio = 0.2f
                                )
                                .padding(ButtonDefaults.TextButtonContentPadding)
                        )
                        Divider(color = MaterialTheme.colorScheme.outline)
                    }
                } else {
                    if (enabled != false) {
                        item {
                            FlowRow(Modifier.fillMaxWidth()) {
                                Text("音楽ファイルがありません。")
                                Text(
                                    "スキャン",
                                    Modifier.clickable { viewModel.startSearchFile(dir) },
                                    MaterialTheme.colorScheme.primary
                                )
                                Text("、または")
                                Text(
                                    "選択解除",
                                    Modifier.clickable { viewModel.deleteDirectory(dir) },
                                    MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
            }
        }
    }

}