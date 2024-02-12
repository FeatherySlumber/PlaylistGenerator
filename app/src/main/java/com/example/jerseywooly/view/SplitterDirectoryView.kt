package com.example.jerseywooly.view

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DragHandle
import androidx.compose.material.icons.rounded.AddCircle
import androidx.compose.material.icons.rounded.Cancel
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconToggleButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.times
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.jerseywooly.view.Orientation.Horizontal
import com.example.jerseywooly.view.Orientation.Vertical
import com.example.jerseywooly.viewmodel.SplitterDirectoryViewModel
import kotlinx.coroutines.launch

@Composable
fun SplitterDirectoryViewContent(paddingValues: PaddingValues) {
    // 大きさ管理全般
    BoxWithConstraints(
        Modifier
            .fillMaxSize()
            .padding(paddingValues)
    ) {
        val density = LocalDensity.current
        val dirBoxHeight = remember { mutableStateOf(maxHeight * 0.6f) }
        val hfs = 20.sp
        val hvp = ButtonDefaults.TextButtonContentPadding.calculateTopPadding()
        val splitterHeight = 24.dp
        val oneElementHeaderHeight = with(density) { 2 * hvp + hfs.toDp() }
        val oneElementMaxHeight = maxHeight - (oneElementHeaderHeight * 2 + splitterHeight)
        Column(modifier = Modifier.fillMaxSize()) {
            var dirHeight by dirBoxHeight
            val color = MaterialTheme.colorScheme.onSurface.copy(0.3f)
            val dsScrollState = rememberLazyListState()
            val sdScrollState = rememberLazyListState()
            DirectorySelectBox(
                headerFontSize = hfs, verticalPadding = hvp,
                modifier = Modifier
                    .height(dirHeight)
                    .fillMaxWidth()
                    .scrollFadingEdge(Vertical, dsScrollState, color, 0.08f),
                onHeaderClick = { dirHeight = oneElementMaxHeight }, // タップ時最大表示
                state = dsScrollState
            )
            Splitter(height = splitterHeight, onDrag = { deltaPx ->
                // ドラッグでDirectorySelectBoxのサイズを変更
                dirHeight += with(density) { deltaPx.toDp() }
                if (dirHeight >= oneElementMaxHeight) dirHeight = oneElementMaxHeight
            })
            SelectedDirectoryBox(
                headerFontSize = hfs, verticalPadding = hvp,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .scrollFadingEdge(Vertical, sdScrollState, color, 0.08f),
                onHeaderClick = { dirHeight = 0.dp }, // タップ時最大表示
                state = sdScrollState
            )
        }
    }
}

@Composable
fun DirectorySelectBox(
    modifier: Modifier,
    headerFontSize: TextUnit,
    verticalPadding: Dp,
    onHeaderClick: () -> Unit,
    state: LazyListState,
    viewModel: SplitterDirectoryViewModel = viewModel()
) {
    val current by viewModel.directory.collectAsState()
    ObjectWithHeader(
        headerFontSize = headerFontSize,
        text = current.name,
        verticalPadding = verticalPadding,
        horizontalPadding = ButtonDefaults.TextButtonContentPadding
            .calculateStartPadding(LocalLayoutDirection.current),
        onHeaderClick = onHeaderClick
    ) {
        val directories by viewModel.directories.collectAsState()
        LazyColumn(modifier, state) {
            items(directories) { dir: SplitterDirectoryViewModel.Directory ->
                // RequestManageStoragePermissionOnStart(dir.value)
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
                            .weight(1f)
                            .horizontalScroll(scrollState)
                            .scrollFadingEdge(Horizontal, scrollState, shadowRatio = 0.2f)
                            .clickable { viewModel.changeDirectory(dir.value) }
                            .fillMaxHeight()
                            .wrapContentHeight()
                            .padding(ButtonDefaults.TextButtonContentPadding)
                    )
                    val scope = rememberCoroutineScope()
                    IconToggleButton(checked = dir.contained, onCheckedChange = {
                        scope.launch {
                            if (it) {
                                viewModel.selectDirectory(dir.value)
                            } else {
                                viewModel.deleteDirectory(dir.value)
                            }
                        }
                    }) {
                        Icon(
                            imageVector = if (dir.contained) Icons.Rounded.Cancel else Icons.Rounded.AddCircle,
                            contentDescription = if (dir.contained) "Cancel" else "Add",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                Divider(color = MaterialTheme.colorScheme.outline)
            }
        }

    }
}

@Composable
fun SelectedDirectoryBox(
    modifier: Modifier,
    headerFontSize: TextUnit,
    verticalPadding: Dp,
    onHeaderClick: () -> Unit,
    state: LazyListState,
    viewModel: SplitterDirectoryViewModel = viewModel()
) {
    ObjectWithHeader(
        headerFontSize = headerFontSize,
        text = "選択中のディレクトリ",
        verticalPadding = verticalPadding,
        horizontalPadding = ButtonDefaults.TextButtonContentPadding
            .calculateStartPadding(LocalLayoutDirection.current),
        onHeaderClick = onHeaderClick
    ) {
        val directories by viewModel.selectedDirectories.collectAsState()
        LazyColumn(modifier, state) {
            items(directories) {
                val scrollState = rememberScrollState()
                val scope = rememberCoroutineScope()
                Text(text = it.directory.toString(),
                    fontSize = 16.sp,
                    maxLines = 1,
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(scrollState, reverseScrolling = true)
                        .clickable {
                            scope.launch { viewModel.deleteDirectory(it) }
                        }
                        .padding(ButtonDefaults.TextButtonContentPadding)
                        .scrollFadingEdge(
                            Horizontal,
                            scrollState,
                            isReverse = true
                        )
                )

                Divider(color = MaterialTheme.colorScheme.outline)
            }
        }
    }
}

@Composable
inline fun ObjectWithHeader(
    headerFontSize: TextUnit,
    text: String,
    horizontalPadding: Dp = 0.dp,
    verticalPadding: Dp = 0.dp,
    noinline onHeaderClick: () -> Unit = {},
    content: @Composable () -> Unit
) {
    val height = with(LocalDensity.current) {
        verticalPadding * 2 + headerFontSize.toDp()
    }
    Text(
        text, fontSize = headerFontSize, color = MaterialTheme.colorScheme.onPrimary,
        modifier = Modifier
            .fillMaxWidth()
            .background(color = MaterialTheme.colorScheme.primary)
            .clickable(onClick = onHeaderClick)
            .height(height)
            .wrapContentHeight()
            .padding(horizontalPadding, 0.dp)
    )
    content()
}

@Composable
fun Splitter(height: Dp, onDrag: (deltaPx: Float) -> Unit = {}) {
    Box(
        Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.secondary)
            .draggable(
                orientation = Orientation.Vertical,
                state = rememberDraggableState(onDrag)
            )
            .height(height)
    ) {
        Divider(
            color = MaterialTheme.colorScheme.onSecondary,
            modifier = Modifier.align(alignment = Alignment.Center)
        )

        Icon(
            Icons.Filled.DragHandle,
            "",
            modifier = Modifier
                .align(alignment = Alignment.Center)
                .background(MaterialTheme.colorScheme.secondary),
            tint = MaterialTheme.colorScheme.onSecondary
        )
    }
}
