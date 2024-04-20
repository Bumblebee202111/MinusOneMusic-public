package com.github.bumblebee202111.minusonecloudmusic.domain

import androidx.paging.PagingData
import androidx.paging.map
import com.github.bumblebee202111.minusonecloudmusic.data.MusicServiceConnection
import com.github.bumblebee202111.minusonecloudmusic.data.model.AbstractSong
import com.github.bumblebee202111.minusonecloudmusic.ui.playlist.PlaylistSongItemUiModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import javax.inject.Inject

class GetPlaylistSongItemsUseCase @Inject constructor(private val musicServiceConnection: MusicServiceConnection) {
    operator fun invoke(songsFlow: Flow<List<AbstractSong>?>): Flow<List<PlaylistSongItemUiModel>?> {
        return combine(
            songsFlow,
            musicServiceConnection.currentMediaId,
            musicServiceConnection.isPlaying
        ) { songs, currentSongId, isPlaying ->
            songs?.map { song ->
                createPlaylistSongItemUiModel(song, currentSongId, isPlaying)
            }
        }
    }
}

class GetPagedPlaylistSongItemsUseCase @Inject constructor(private val musicServiceConnection: MusicServiceConnection) {
    operator fun invoke(songsFlow: Flow<PagingData<out AbstractSong>>): Flow<PagingData<PlaylistSongItemUiModel>> {
        return combine(
            songsFlow,
            musicServiceConnection.currentMediaId,
            musicServiceConnection.isPlaying
        ) { songs, currentMediaId, isPlaying ->
            songs.map { song ->
                createPlaylistSongItemUiModel(song, currentMediaId, isPlaying)
            }
        }
    }
}

private fun createPlaylistSongItemUiModel(
    song: AbstractSong,
    currentMediaId: String?,
    isPlaying: Boolean,
) = PlaylistSongItemUiModel(
    song, song.mediaId == currentMediaId,
    isBeingPlayed = song.mediaId == currentMediaId && isPlaying
)