package com.example.jerseywooly.view

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import com.example.jerseywooly.viewmodel.SelectStartDirectoryViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SelectStartDirectoryView(navController: NavController) {
    Scaffold(topBar = {
        TopAppBar(
            title = { Text("フォルダを選択") },
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
            })
    }) {
        SelectDirectoryViewContent<SelectStartDirectoryViewModel>(it, navController)
    }
}
