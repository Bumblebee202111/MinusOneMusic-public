package com.github.bumblebee202111.minusonecloudmusic.domain

import androidx.paging.PagingData
import androidx.paging.map
import com.github.bumblebee202111.minusonecloudmusic.data.MusicServiceConnection
import com.github.bumblebee202111.minusonecloudmusic.data.model.AbstractSong
import com.github.bumblebee202111.minusonecloudmusic.ui.playlist.SongItemUiModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import javax.inject.Inject

class MapSongsFlowToUiItemsUseCase @Inject constructor(private val musicServiceConnection: MusicServiceConnection) {
    operator fun invoke(songsFlow: Flow<List<AbstractSong>?>): Flow<List<SongItemUiModel>?> {
        return combine(
            songsFlow,
            musicServiceConnection.currentMediaId,
            musicServiceConnection.isPlaying
        ) { songs, currentSongId, isPlaying ->
            songs?.map { song ->
                createSongUiItem(song, currentSongId, isPlaying)
            }
        }
    }
}

class MapSongPagingDataFlowToUiItemsUseCase @Inject constructor(private val musicServiceConnection: MusicServiceConnection) {
    operator fun invoke(songsFlow: Flow<PagingData<out AbstractSong>>): Flow<PagingData<SongItemUiModel>> {
        return combine(
            songsFlow,
            musicServiceConnection.currentMediaId,
            musicServiceConnection.isPlaying
        ) { songs, currentMediaId, isPlaying ->
            songs.map { song ->
                createSongUiItem(song, currentMediaId, isPlaying)
            }
        }
    }
}

private fun createSongUiItem(
    song: AbstractSong,
    currentMediaId: String?,
    isPlaying: Boolean,
) = SongItemUiModel(
    song, song.mediaId == currentMediaId,
    isBeingPlayed = song.mediaId == currentMediaId && isPlaying
)