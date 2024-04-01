@file:OptIn(UnstableApi::class) package com.github.bumblebee202111.minusonecloudmusic.ui.discover

import androidx.annotation.OptIn
import androidx.lifecycle.ViewModel
import androidx.media3.common.C
import androidx.media3.common.util.UnstableApi
import com.github.bumblebee202111.minusonecloudmusic.data.MusicServiceConnection
import com.github.bumblebee202111.minusonecloudmusic.data.model.Song
import com.github.bumblebee202111.minusonecloudmusic.data.model.asMediaItem
import com.github.bumblebee202111.minusonecloudmusic.data.repository.PlaylistRepository
import com.github.bumblebee202111.minusonecloudmusic.data.repository.PlaylistRepository.Companion.TOP_LIST_ID
import com.github.bumblebee202111.minusonecloudmusic.domain.PlaySongsUseCase
import com.github.bumblebee202111.minusonecloudmusic.utils.stateInUi
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import javax.inject.Inject

@HiltViewModel
class DiscoverViewModel @Inject constructor(
    private val playlistRepository: PlaylistRepository,
    private val musicServiceConnection: MusicServiceConnection,
    private val playSongsUseCase: PlaySongsUseCase
) : ViewModel() {
    fun onSongItemClick(song: Song) {
        val songs=songs.value?:return
        playSongsUseCase(songs, song)
    }

    val songs =
        playlistRepository.getPlaylistDetail(TOP_LIST_ID).map { it.data?.songs?.take(3) }
            .flowOn(Dispatchers.IO)
            .stateInUi()

}