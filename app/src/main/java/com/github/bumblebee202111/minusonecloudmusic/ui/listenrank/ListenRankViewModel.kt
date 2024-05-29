package com.github.bumblebee202111.minusonecloudmusic.ui.listenrank

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.bumblebee202111.minusonecloudmusic.data.MusicServiceConnection
import com.github.bumblebee202111.minusonecloudmusic.data.model.RemoteSong
import com.github.bumblebee202111.minusonecloudmusic.data.repository.PlaylistRepository
import com.github.bumblebee202111.minusonecloudmusic.domain.MapSongsFlowToUiItemsUseCase
import com.github.bumblebee202111.minusonecloudmusic.domain.PlayPlaylistUseCase
import com.github.bumblebee202111.minusonecloudmusic.utils.stateInUi
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ListenRankViewModel @Inject constructor(
    playlistRepository: PlaylistRepository,
    mapSongsFlowToUiItemsUseCase: MapSongsFlowToUiItemsUseCase,
    private val playPlaylistUseCase: PlayPlaylistUseCase,
    musicServiceConnection: MusicServiceConnection,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val userId = ListenRankFragmentArgs.fromSavedStateHandle(savedStateHandle).userId

    val player = musicServiceConnection.player.stateInUi()

    private val playRecords = playlistRepository.playRecords(userId).map { it.data }.stateInUi()

    val weekRecordsUiState =
        mapSongsFlowToUiItemsUseCase(playRecords.map { it?.weekData }).stateInUi()
    val allRecordsUiState =
        mapSongsFlowToUiItemsUseCase(playRecords.map { it?.allData }).stateInUi()

    fun onWeekRecordClick(position: Int) {
        val songs = playRecords.value?.weekData ?: return
        playAll(songs = songs, position = position)
    }

    fun onAllRecordClick(position: Int) {
        val songs = playRecords.value?.allData ?: return
        playAll(songs = songs, position = position)
    }

    fun playAllWeekRecords() {
        val songs = playRecords.value?.weekData ?: return
        playAll(songs)
    }

    fun playAllAllRecords() {
        val songs = playRecords.value?.allData ?: return
        playAll(songs)
    }

    private fun playAll(songs: List<RemoteSong>) {
        viewModelScope.launch {
            playPlaylistUseCase(loadedSongs = songs)
        }
    }

    private fun playAll(songs: List<RemoteSong>, position: Int) {
        viewModelScope.launch {
            playPlaylistUseCase(loadedSongs = songs, startIndex = position)
        }
    }

}