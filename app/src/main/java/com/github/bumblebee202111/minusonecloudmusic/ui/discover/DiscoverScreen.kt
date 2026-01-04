package com.github.bumblebee202111.minusonecloudmusic.ui.discover

import DiscoverList
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.github.bumblebee202111.minusonecloudmusic.ui.theme.DolphinTheme
@Composable
fun DiscoverScreen(
    viewModel: DiscoverViewModel = hiltViewModel<DiscoverViewModel>(),
    onMenuClick: () -> Unit,
    onSearchClick: () -> Unit
) {
    val blocks by viewModel.blocks.collectAsStateWithLifecycle()

    Scaffold(
        containerColor = DolphinTheme.colors.backgroundAndroid,
        topBar = {
            DiscoverTopBar(
                onMenuClick = onMenuClick,
                onSearchClick = onSearchClick
            )
        }
    ) { innerPadding ->

        val items = blocks?.data ?: emptyList()
        DiscoverList(
            items = items,
            contentPadding = PaddingValues(
                start = innerPadding.calculateStartPadding(LocalLayoutDirection.current),
                top = innerPadding.calculateTopPadding() + 16.dp,
                end = innerPadding.calculateEndPadding(LocalLayoutDirection.current),
                bottom = innerPadding.calculateBottomPadding()
            ),
            onUrlClick = {
                viewModel.onGenericUrlClick(it)
            }
        )
    }
}