package com.github.bumblebee202111.minusonecloudmusic.domain

import androidx.paging.PagingData
import androidx.paging.map
import com.github.bumblebee202111.minusonecloudmusic.data.MusicServiceConnection
import com.github.bumblebee202111.minusonecloudmusic.data.model.AbstractRemoteSong
import com.github.bumblebee202111.minusonecloudmusic.ui.playlist.PlaylistSongItemUiModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import javax.inject.Inject

class GetPlaylistSongItemsUseCase @Inject constructor(private val musicServiceConnection: MusicServiceConnection) {
    operator fun invoke(songsFlow: Flow<List<AbstractRemoteSong>?>): Flow<List<PlaylistSongItemUiModel>?> {
        return combine(
            songsFlow,
            musicServiceConnection.currentSongId,
            musicServiceConnection.isPlaying
        ) { songs, currentSongId, isPlaying ->
            songs?.map { song ->
                createPlaylistSongItemUiModel(song, currentSongId, isPlaying)
            }
        }
    }
}

class GetPagedPlaylistSongItemsUseCase @Inject constructor(private val musicServiceConnection: MusicServiceConnection) {
    operator fun invoke(songsFlow: Flow<PagingData<out AbstractRemoteSong>>): Flow<PagingData<PlaylistSongItemUiModel>> {
        return combine(
            songsFlow,
            musicServiceConnection.currentSongId,
            musicServiceConnection.isPlaying
        ) { songs, currentSongId, isPlaying ->
            songs.map { song ->
                createPlaylistSongItemUiModel(song, currentSongId, isPlaying)
            }
        }
    }
}

private fun createPlaylistSongItemUiModel(
    song: AbstractRemoteSong,
    currentSongId: Long?,
    isPlaying: Boolean
) = PlaylistSongItemUiModel(
    song, song.id == currentSongId,
    isBeingPlayed = song.id == currentSongId && isPlaying
)