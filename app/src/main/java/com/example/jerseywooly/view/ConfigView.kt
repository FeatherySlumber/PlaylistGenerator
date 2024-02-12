package com.example.jerseywooly.view

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonColors
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.jerseywooly.viewmodel.ConfigViewModel


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConfigView(navController: NavController, viewModel: ConfigViewModel = viewModel()) {
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            TopAppBar(
                title = { Text("設定") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                ),
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.Rounded.ArrowBack,
                            "BackView",
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }
            )
        }) {
        Column(
            modifier = Modifier.padding(it),
        ) {
            Text(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.primary)
                    .padding(ButtonDefaults.TextButtonContentPadding),
                text = "保存先",
                color = MaterialTheme.colorScheme.onPrimary,
                fontSize = 20.sp
            )
            Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.Start) {
                val scrollState = rememberScrollState()
                val saveDir by viewModel.saveDirectory.collectAsState()
                Text(
                    saveDir ?: "未設定",
                    fontSize = MaterialTheme.typography.bodyLarge.fontSize,
                    modifier = Modifier
                        .horizontalScroll(scrollState)
                        .scrollFadingEdge(
                            Orientation.Horizontal,
                            scrollState,
                            shadowRatio = 0.2f
                        )
                        .padding(ButtonDefaults.TextButtonContentPadding)
                )
                Button(
                    onClick = { navController.navigate(ScreenType.SaveDirectory.name) },
                    Modifier
                        .padding(ButtonDefaults.ContentPadding)
                        .fillMaxWidth()
                ) { Text(text = if (saveDir == null) "選択" else "変更") }
            }
            Text(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.primary)
                    .padding(ButtonDefaults.TextButtonContentPadding),
                text = "フォルダ選択時の初期位置",
                color = MaterialTheme.colorScheme.onPrimary,
                fontSize = 20.sp
            )
            Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.Start) {
                val scrollState = rememberScrollState()
                val startDir by viewModel.startDirectoryPath.collectAsState()
                Text(
                    startDir,
                    fontSize = MaterialTheme.typography.bodyLarge.fontSize,
                    modifier = Modifier
                        .horizontalScroll(scrollState)
                        .scrollFadingEdge(
                            Orientation.Horizontal,
                            scrollState,
                            shadowRatio = 0.2f
                        )
                        .padding(ButtonDefaults.TextButtonContentPadding)
                )
                Button(
                    onClick = { navController.navigate(ScreenType.StartDirectory.name) },
                    Modifier
                        .padding(ButtonDefaults.ContentPadding)
                        .fillMaxWidth()
                ) { Text("変更") }
            }

            Text(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.primary)
                    .padding(ButtonDefaults.TextButtonContentPadding),
                text = "プレイリスト形式",
                color = MaterialTheme.colorScheme.onPrimary,
                fontSize = 20.sp
            )
            Column(Modifier.fillMaxWidth()) {
                val useAbsolute by viewModel.useAbsolutePath.collectAsState()
                RadioButtonWith(selected = useAbsolute, onClick = { viewModel.setUsePath(true) }) {
                    Text(
                        "絶対パス",
                        fontSize = MaterialTheme.typography.bodyLarge.fontSize,
                        modifier = Modifier
                            .weight(1f)
                            .padding(ButtonDefaults.TextButtonContentPadding)
                    )
                }
                RadioButtonWith(
                    selected = !useAbsolute,
                    onClick = { viewModel.setUsePath(false) }) {
                    Text(
                        "相対パス",
                        fontSize = MaterialTheme.typography.bodyLarge.fontSize,
                        modifier = Modifier
                            .weight(1f)
                            .padding(ButtonDefaults.TextButtonContentPadding)
                    )
                }
                /*
                Row(
                    Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.primary)
                        .padding(ButtonDefaults.ContentPadding),
                    verticalAlignment = Alignment.CenterVertically){
                    val allowDuplicate by viewModel.allowDuplicate.collectAsState()
                    Text(
                        modifier = Modifier.weight(1f),
                        text = "曲の重複を許可",
                        color = MaterialTheme.colorScheme.onPrimary,
                        fontSize = 20.sp
                    )
                    Checkbox(checked = allowDuplicate, onCheckedChange = viewModel::setAllowDuplicate,
                    colors = CheckboxDefaults.colors(MaterialTheme.colorScheme.onPrimary, MaterialTheme.colorScheme.onPrimary, MaterialTheme.colorScheme.primary))
                }
                 */
            }
        }
    }
}

@Composable
fun RadioButtonWith(
    selected: Boolean,
    onClick: (() -> Unit),
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    colors: RadioButtonColors = RadioButtonDefaults.colors(),
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    content: @Composable RowScope.() -> Unit
) {
    Row(
        Modifier
            .clickable(onClick = onClick)
            .fillMaxWidth()
            .padding(ButtonDefaults.ContentPadding),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(selected, null, modifier, enabled, colors, interactionSource)
        content()
    }
}

@Preview
@Composable
fun PreviewCV() {
    ConfigView(navController = rememberNavController())
}