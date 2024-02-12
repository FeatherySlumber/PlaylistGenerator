package com.example.jerseywooly.view

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.jerseywooly.viewmodel.SelectDirectoryViewModel
import kotlinx.coroutines.launch

@Composable
inline fun <reified T : SelectDirectoryViewModel> SelectDirectoryViewContent(
    paddingValues: PaddingValues,
    navController: NavController,
    viewModel: T = viewModel()
) {
    Column(
        Modifier
            .fillMaxSize()
            .padding(paddingValues)) {
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
                .scrollFadingEdge(Orientation.Vertical, state, MaterialTheme.colorScheme.surface.copy(alpha = 0.6f)),
            state
        ) {
            items(directories) {
                val scrollState = rememberScrollState()
                Text(text = it.second,
                    fontSize = MaterialTheme.typography.bodyLarge.fontSize,
                    maxLines = 1,
                    modifier = Modifier
                        .clickable { viewModel.changeDirectory(it.first) }
                        .fillMaxWidth()
                        .padding(ButtonDefaults.TextButtonContentPadding)
                        .horizontalScroll(scrollState)
                        .scrollFadingEdge(Orientation.Horizontal, scrollState, shadowRatio = 0.2f)
                )
                Divider(color = MaterialTheme.colorScheme.outline)
            }

        }
        val scope = rememberCoroutineScope()
        Button(
            onClick = {
                scope.launch {
                    viewModel.doAction(directory)
                    navController.popBackStack()
                }
            },
            Modifier
                .padding(ButtonDefaults.ContentPadding)
                .fillMaxWidth(),
            enabled = viewModel.enabledDoAction.collectAsState().value
        ) { Text("決定") }
    }
}