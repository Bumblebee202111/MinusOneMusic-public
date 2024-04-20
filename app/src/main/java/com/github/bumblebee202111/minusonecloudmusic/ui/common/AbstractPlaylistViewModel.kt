package com.github.bumblebee202111.minusonecloudmusic.ui.common

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.bumblebee202111.minusonecloudmusic.data.model.AbstractSong
import com.github.bumblebee202111.minusonecloudmusic.domain.PlayPlaylistUseCase
import kotlinx.coroutines.launch

abstract class AbstractPlaylistViewModel<out SongType : AbstractSong>(private val playPlaylistUseCase: PlayPlaylistUseCase) :
    ViewModel() {

    protected abstract val loadedSongs: List<SongType>

    fun onSongItemClick(startIndex: Int) {
        viewModelScope.launch {
            playPlaylistUseCase(
                loadedSongs = loadedSongs,
                loadRemainingSongs = loadRemainingSongs,
                startIndex = startIndex
            )
        }
    }

    fun playAll() {
        viewModelScope.launch {
            playPlaylistUseCase(loadedSongs = loadedSongs, loadRemainingSongs = loadRemainingSongs)
        }
    }

    protected open val loadRemainingSongs:(suspend ()->List<SongType>)? = null
}