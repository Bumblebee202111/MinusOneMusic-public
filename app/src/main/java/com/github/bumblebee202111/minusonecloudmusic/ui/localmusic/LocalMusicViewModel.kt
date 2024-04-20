package com.github.bumblebee202111.minusonecloudmusic.ui.localmusic

import com.github.bumblebee202111.minusonecloudmusic.data.MusicServiceConnection
import com.github.bumblebee202111.minusonecloudmusic.data.model.LocalSong
import com.github.bumblebee202111.minusonecloudmusic.data.repository.SongRepository
import com.github.bumblebee202111.minusonecloudmusic.domain.GetPlaylistSongItemsUseCase
import com.github.bumblebee202111.minusonecloudmusic.domain.PlayPlaylistUseCase
import com.github.bumblebee202111.minusonecloudmusic.ui.common.AbstractPlaylistViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import javax.inject.Inject

@HiltViewModel
class LocalMusicViewModel @Inject constructor(
    musicServiceConnection: MusicServiceConnection,
    private val songRepository: SongRepository,
    playPlaylistUseCase: PlayPlaylistUseCase,
    getPlaylistSongItemsUseCase: GetPlaylistSongItemsUseCase
) : AbstractPlaylistViewModel<LocalSong>(playPlaylistUseCase) {

    val player = musicServiceConnection.player

    val songs = MutableStateFlow<List<LocalSong>?>(null)

    val songItems = getPlaylistSongItemsUseCase(songs)

    override val loadedSongs: List<LocalSong>
        get() = songs.value ?: emptyList()

    fun onPermissionGranted() {
        songs.value = songRepository.getLocalSongs()
    }
}