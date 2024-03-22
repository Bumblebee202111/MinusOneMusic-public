package com.github.bumblebee202111.minusonecloudmusic.ui.nowplaying

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.bumblebee202111.minusonecloudmusic.data.MusicServiceConnection
import com.github.bumblebee202111.minusonecloudmusic.data.repository.SongRepository
import com.github.bumblebee202111.minusonecloudmusic.utils.stateInUi
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject
@HiltViewModel
class NowPlayingViewModel @Inject constructor(
    private val songRepository: SongRepository,
    musicServiceConnection: MusicServiceConnection
) : ViewModel() {
    private val currentSongId = MutableStateFlow<Long?>(null)

    fun setCurrentSongId(newSongId: Long?) {
        currentSongId.value = newSongId
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    val lyrics = currentSongId.flatMapLatest { songId ->
        if (songId != null) songRepository.getLyrics(songId).map { it.data } else emptyFlow()
    }.stateIn(viewModelScope, SharingStarted.Lazily, null)

    val player = musicServiceConnection.player.stateInUi()
}