package com.github.bumblebee202111.minusonecloudmusic.ui.localmusic

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.bumblebee202111.minusonecloudmusic.model.LocalSong
import com.github.bumblebee202111.minusonecloudmusic.data.repository.SongRepository
import com.github.bumblebee202111.minusonecloudmusic.domain.MapSongsFlowToUiItemsUseCase
import com.github.bumblebee202111.minusonecloudmusic.domain.PlayPlaylistUseCase
import com.github.bumblebee202111.minusonecloudmusic.ui.common.PlaylistPlaybackHandler
import com.github.bumblebee202111.minusonecloudmusic.utils.stateInUi
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LocalMusicViewModel @Inject constructor(
    private val songRepository: SongRepository,
    playPlaylistUseCase: PlayPlaylistUseCase,
    mapSongsFlowToUiItemsUseCase: MapSongsFlowToUiItemsUseCase
) : ViewModel() {
    private val songs = MutableStateFlow<List<LocalSong>?>(null)

    val songItems = mapSongsFlowToUiItemsUseCase(songs).stateInUi()

    private val playbackHandler = PlaylistPlaybackHandler(
        playPlaylistUseCase = playPlaylistUseCase,
        scope = viewModelScope,
        getLoadedSongs = { songs.value ?: emptyList() }
    )

    fun onPermissionGranted() {
        viewModelScope.launch {
            songs.value = songRepository.getLocalSongs()
        }
    }
    fun onSongItemClick(startIndex: Int) = playbackHandler.onSongItemClick(startIndex)
    fun playAll() = playbackHandler.playAll()
}