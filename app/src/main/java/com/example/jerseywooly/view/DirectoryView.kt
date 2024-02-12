package com.example.jerseywooly.view

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AddCircle
import androidx.compose.material.icons.rounded.Cancel
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconToggleButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.jerseywooly.viewmodel.DirectoryViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DirectoryView(navController: NavController) {
    Scaffold(topBar = {
        TopAppBar(
            title = { Text("フォルダ選択") },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.primary,
                titleContentColor = MaterialTheme.colorScheme.onPrimary
            )
        )
    }) {
        DirectoryViewContent(it, navController)
    }
}

@Composable
fun DirectoryViewContent(paddingValues: PaddingValues, navController: NavController, viewModel: DirectoryViewModel = viewModel()) {
    Column(
        Modifier
            .fillMaxSize()
            .padding(paddingValues)
    ) {
        val directory by viewModel.directory.collectAsState()
        Text(
            directory.path,
            Modifier
                .background(MaterialTheme.colorScheme.primary)
                .fillMaxWidth()
                .padding(ButtonDefaults.TextButtonContentPadding),
            color = MaterialTheme.colorScheme.onPrimary,
            fontSize = MaterialTheme.typography.bodyLarge.fontSize
        )
        val directories by viewModel.directories.collectAsState()
        val state = rememberLazyListState()
        LazyColumn(
            Modifier
                .weight(1f)
                .fillMaxWidth()
                .scrollFadingEdge(
                    Orientation.Vertical,
                    state,
                    MaterialTheme.colorScheme.surface.copy(alpha = 0.6f)
                ),
            state
        ) {
            items(directories) { dir ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(IntrinsicSize.Min),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val scrollState = rememberScrollState()
                    Text(text = dir.displayName,
                        fontSize = 16.sp,
                        maxLines = 1,
                        modifier = Modifier
                            .clickable { viewModel.changeDirectory(dir.value) }
                            .weight(1f)
                            .fillMaxHeight()
                            .wrapContentHeight()
                            .padding(ButtonDefaults.TextButtonContentPadding)
                            .horizontalScroll(scrollState)
                            .scrollFadingEdge(
                                Orientation.Horizontal,
                                scrollState,
                                shadowRatio = 0.2f
                            )
                    )
                    val scope = rememberCoroutineScope()
                    val checked = dir.contained.isContained
                    val enabled = dir.contained != DirectoryViewModel.DirContain.Parent
                    IconToggleButton(checked = checked, enabled = enabled, onCheckedChange = {
                        scope.launch {
                            if (it) {
                                viewModel.selectDirectory(dir.value)
                            } else {
                                viewModel.deleteDirectory(dir.value)
                            }
                        }
                    }) {
                        Icon(
                            imageVector = if (dir.contained.isContained) Icons.Rounded.Cancel else Icons.Rounded.AddCircle,
                            contentDescription = if (dir.contained.isContained) "Cancel" else "Add",
                            tint = if(enabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                        )
                    }
                }
                Divider(color = MaterialTheme.colorScheme.outline)
            }
        }
        BarBottom(navController)
    }
}