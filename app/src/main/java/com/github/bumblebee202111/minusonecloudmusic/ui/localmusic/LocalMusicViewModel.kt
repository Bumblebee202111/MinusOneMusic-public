package com.github.bumblebee202111.minusonecloudmusic.ui.localmusic

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.bumblebee202111.minusonecloudmusic.model.LocalSong
import com.github.bumblebee202111.minusonecloudmusic.data.repository.SongRepository
import com.github.bumblebee202111.minusonecloudmusic.domain.MapSongsFlowToUiItemsUseCase
import com.github.bumblebee202111.minusonecloudmusic.domain.PlayPlaylistUseCase
import com.github.bumblebee202111.minusonecloudmusic.ui.common.PlaylistPlaybackHandler
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import javax.inject.Inject

@HiltViewModel
class LocalMusicViewModel @Inject constructor(
    private val songRepository: SongRepository,
    playPlaylistUseCase: PlayPlaylistUseCase,
    mapSongsFlowToUiItemsUseCase: MapSongsFlowToUiItemsUseCase
) : ViewModel() {
    val songs = MutableStateFlow<List<LocalSong>?>(null)

    val songItems = mapSongsFlowToUiItemsUseCase(songs)

    private val playbackHandler = PlaylistPlaybackHandler(
        playPlaylistUseCase = playPlaylistUseCase,
        scope = viewModelScope,
        getLoadedSongs = { songs.value ?: emptyList() }
    )

    fun onPermissionGranted() {
        songs.value = songRepository.getLocalSongs()
    }
    fun onSongItemClick(startIndex: Int) = playbackHandler.onSongItemClick(startIndex)
    fun playAll() = playbackHandler.playAll()
}