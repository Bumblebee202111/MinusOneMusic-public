package com.github.bumblebee202111.minusonecloudmusic.ui.common

import androidx.lifecycle.viewModelScope
import com.github.bumblebee202111.minusonecloudmusic.model.AbstractSong
import com.github.bumblebee202111.minusonecloudmusic.domain.PlayPlaylistUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
class PlaylistPlaybackHandler<SongType : AbstractSong>(
    private val playPlaylistUseCase: PlayPlaylistUseCase,
    private val scope: CoroutineScope,
    private val getLoadedSongs: () -> List<SongType>,
    private val loadRemainingSongs: (suspend () -> List<SongType>)? = null
) {

    fun onSongItemClick(startIndex: Int) {
        scope.launch {
            playPlaylistUseCase(
                loadedSongs = getLoadedSongs(),
                loadRemainingSongs = loadRemainingSongs,
                startIndex = startIndex
            )
        }
    }

    fun playAll() {
        scope.launch {
            playPlaylistUseCase(
                loadedSongs = getLoadedSongs(),
                loadRemainingSongs = loadRemainingSongs
            )
        }
    }
}