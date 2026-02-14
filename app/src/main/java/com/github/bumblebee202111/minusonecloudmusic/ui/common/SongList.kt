package com.github.bumblebee202111.minusonecloudmusic.ui.common

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.rememberNestedScrollInteropConnection
import androidx.compose.ui.viewinterop.AndroidViewBinding
import androidx.paging.compose.LazyPagingItems
import com.github.bumblebee202111.minusonecloudmusic.databinding.ListItemNormalSongSimpleBinding
import com.github.bumblebee202111.minusonecloudmusic.databinding.ListItemNormalSongWithAlbumBinding
import com.github.bumblebee202111.minusonecloudmusic.databinding.ListItemNormalSongWithPositionBinding
import com.github.bumblebee202111.minusonecloudmusic.databinding.ListItemPlayerPlaylistSongBinding
import com.github.bumblebee202111.minusonecloudmusic.ui.playlist.SongItemUiModel

@Composable
fun SimpleSongList(
    songs: List<SongItemUiModel>,
    onItemClick: (position: Int) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.nestedScroll(rememberNestedScrollInteropConnection())
    ) {
        itemsIndexed(songs) { index, song ->
            SimpleSongItem(
                song = song,
                onClick = { onItemClick(index) }
            )
        }
    }
}

@Composable
fun SimpleSongItem(
    song: SongItemUiModel,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    AndroidViewBinding(
        factory = ListItemNormalSongSimpleBinding::inflate,
        modifier = modifier
    ) {
        this.song = song
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

@Composable
fun SongWithAlbumList(
    songs: List<SongItemUiModel>,
    onItemClick: (position: Int) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.nestedScroll(rememberNestedScrollInteropConnection())
    ) {
        itemsIndexed(songs) { index, song ->
            SongWithAlbumItem(
                song = song,
                onClick = { onItemClick(index) }
            )
        }
    }
}

@Composable
fun SongWithAlbumItem(
    song: SongItemUiModel,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    AndroidViewBinding(
        factory = ListItemNormalSongWithAlbumBinding::inflate,
        modifier = modifier
    ) {
        this.song = song
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

@Composable
fun SongWithPositionList(
    songs: List<SongItemUiModel>,
    onItemClick: (position: Int) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.nestedScroll(rememberNestedScrollInteropConnection())
    ) {
        itemsIndexed(songs) { index, song ->
            SongWithPositionItem(
                song = song,
                position = index + 1,
                onClick = { onItemClick(index) }
            )
        }
    }
}

@Composable
fun PagedSongWithPositionList(
    songs: LazyPagingItems<SongItemUiModel>,
    onItemClick: (position: Int) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.nestedScroll(rememberNestedScrollInteropConnection())
    ) {
        items(count = songs.itemCount) { index ->
            val song = songs[index]
            if (song != null) {
                SongWithPositionItem(
                    song = song,
                    position = index + 1,
                    onClick = { onItemClick(index) }
                )
            }
        }
    }
}

@Composable
fun SongWithPositionItem(
    song: SongItemUiModel,
    position: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    AndroidViewBinding(
        factory = ListItemNormalSongWithPositionBinding::inflate,
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
