package com.github.bumblebee202111.minusonecloudmusic.ui.recentplay

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.github.bumblebee202111.minusonecloudmusic.ui.common.PlaylistPlayAllActions
import com.github.bumblebee202111.minusonecloudmusic.ui.common.SimpleSongList

@Composable
fun MyRecentPlayMusicTabContent(
    viewModel: MyRecentPlayViewModel
) {
    val songs by viewModel.recentPlaySongUiList.collectAsStateWithLifecycle(initialValue = emptyList())

    Column(modifier = Modifier.fillMaxSize()) {
        PlaylistPlayAllActions(
            count = null,
            onClick = { viewModel.playAll() }
        )

        SimpleSongList(
            songs = songs ?: emptyList(),
            onItemClick = viewModel::onSongItemClick,
            modifier = Modifier.weight(1f)
        )
    }
}