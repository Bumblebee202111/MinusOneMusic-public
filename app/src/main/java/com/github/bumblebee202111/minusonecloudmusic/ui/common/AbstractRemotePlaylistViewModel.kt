package com.github.bumblebee202111.minusonecloudmusic.ui.common

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.bumblebee202111.minusonecloudmusic.data.model.AbstractRemoteSong
import com.github.bumblebee202111.minusonecloudmusic.domain.PlayPlaylistUseCase
import kotlinx.coroutines.launch

abstract class AbstractRemotePlaylistViewModel<out SongType : AbstractRemoteSong>(private val playPlaylistUseCase: PlayPlaylistUseCase) :
    ViewModel() {


    protected abstract val loadedSongs: List<SongType>

    fun onSongItemClick(songId: Long) {
        if (loadedSongs.find { it.id == songId } == null) return
        viewModelScope.launch {
            playPlaylistUseCase(
                loadedSongs = loadedSongs,
                loadRemaining = loadRemainingSongs,
                songId = songId
            )
        }
    }

    fun playAll() {
        viewModelScope.launch {
            playPlaylistUseCase(loadedSongs = loadedSongs, loadRemaining =loadRemainingSongs)
        }
    }

    protected open val loadRemainingSongs:(suspend ()->List<SongType>)? = null
}