package com.github.bumblebee202111.minusonecloudmusic.ui.playerhistory

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.rememberNestedScrollInteropConnection
import androidx.compose.ui.viewinterop.AndroidViewBinding
import androidx.paging.compose.LazyPagingItems
import com.github.bumblebee202111.minusonecloudmusic.databinding.ListItemPlayerPlaylistSongBinding
import com.github.bumblebee202111.minusonecloudmusic.ui.playlist.SongItemUiModel

@Composable
fun PagedPlayerSongList(
    songs: LazyPagingItems<SongItemUiModel>,
    onItemClick: (song: SongItemUiModel, position: Int) -> Unit,
    modifier: Modifier = Modifier,
    state: LazyListState = rememberLazyListState()
) {
    LazyColumn(
        modifier = modifier.nestedScroll(rememberNestedScrollInteropConnection()),
        state = state
    ) {
        items(count = songs.itemCount) { index ->
            val song = songs[index]
            if (song != null) {
                PlayerSongItem(
                    song = song,
                    position = index + 1,
                    onClick = { onItemClick(song, index) }
                )
            }
        }
    }
}

@Composable
fun PlayerSongItem(
    song: SongItemUiModel,
    position: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    AndroidViewBinding(
        factory = ListItemPlayerPlaylistSongBinding::inflate,
        modifier = modifier
    ) {
        this.song = song
        this.position = position
        root.setOnClickListener { onClick() }
        playingMark.apply {
            if (song.isBeingPlayed)
                playAnimation()
            else
                pauseAnimation()
        }
        executePendingBindings()
    }
}
