package com.github.bumblebee202111.minusonecloudmusic.ui.mine

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidViewBinding
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.github.bumblebee202111.minusonecloudmusic.databinding.ListItemUserPlaylistChartsBinding
import com.github.bumblebee202111.minusonecloudmusic.databinding.ListItemUserPlaylistNormalBinding
import com.github.bumblebee202111.minusonecloudmusic.ui.theme.DolphinTheme

@Composable
fun UserPlaylistTabScreen(
    viewModel: MineViewModel,
    category: UserPlaylistTab,
    onItemClick: (UserPlaylistItem) -> Unit
) {
    val allTabsData by viewModel.myPlaylistTabs.collectAsStateWithLifecycle(initialValue = null)
    val items = allTabsData?.get(category) ?: emptyList()

    DolphinTheme {
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(
                items = items,
                key = { item ->
                    when (item) {
                        is NormalPlaylistItem -> item.playlist.id
                        is UserChartsItem -> "charts_${item.userId}"
                    }
                }
            ) { item ->
                when (item) {
                    is NormalPlaylistItem -> {
                        NormalPlaylistItemView(item) { onItemClick(item) }
                    }
                    is UserChartsItem -> {
                        UserChartsItemView(item) { onItemClick(item) }
                    }
                }
            }
        }
    }
}

@Composable
private fun NormalPlaylistItemView(
    item: NormalPlaylistItem,
    onClick: () -> Unit
) {
    AndroidViewBinding(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 5.dp),
        factory =  ListItemUserPlaylistNormalBinding::inflate,
        update = {
            playlist = item.playlist
        }
    )
}

@Composable
private fun UserChartsItemView(
    item: UserChartsItem,
    onClick: () -> Unit
) {
    AndroidViewBinding(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 5.dp),
        factory = ListItemUserPlaylistChartsBinding::inflate,
        update = {
            listenSongs = item.listenSongs
        }
    )
}