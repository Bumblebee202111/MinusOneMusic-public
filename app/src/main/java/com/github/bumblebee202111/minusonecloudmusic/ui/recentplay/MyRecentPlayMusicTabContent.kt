package com.github.bumblebee202111.minusonecloudmusic.ui.recentplay

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidViewBinding
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.github.bumblebee202111.minusonecloudmusic.databinding.LayoutMyRecentPlayMusicActionsBinding
import com.github.bumblebee202111.minusonecloudmusic.ui.common.SimpleSongList

@Composable
fun MyRecentPlayMusicTabContent(
    viewModel: MyRecentPlayViewModel
) {
    val songs by viewModel.recentPlaySongUiList.collectAsStateWithLifecycle(initialValue = emptyList())

    Column(modifier = Modifier.fillMaxSize()) {
        AndroidViewBinding(
            factory = LayoutMyRecentPlayMusicActionsBinding::inflate,
            update = {
                playlistActions.setOnClickListener {
                    viewModel.playAll()
                }
            }
        )

        SimpleSongList(
            songs = songs ?: emptyList(),
            onItemClick = viewModel::onSongItemClick,
            modifier = Modifier.weight(1f)
        )
    }
}
