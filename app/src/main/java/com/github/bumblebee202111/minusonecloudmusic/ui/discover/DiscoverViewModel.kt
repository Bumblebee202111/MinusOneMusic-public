@file:OptIn(UnstableApi::class)

package com.github.bumblebee202111.minusonecloudmusic.ui.discover

import androidx.annotation.OptIn
import androidx.media3.common.util.UnstableApi
import com.github.bumblebee202111.minusonecloudmusic.coroutines.ApplicationScope
import com.github.bumblebee202111.minusonecloudmusic.data.MusicServiceConnection
import com.github.bumblebee202111.minusonecloudmusic.data.model.RemoteSong
import com.github.bumblebee202111.minusonecloudmusic.data.repository.DiscoverRepository
import com.github.bumblebee202111.minusonecloudmusic.data.repository.PlaylistRepository
import com.github.bumblebee202111.minusonecloudmusic.data.repository.PlaylistRepository.Companion.TOP_LIST_ID
import com.github.bumblebee202111.minusonecloudmusic.domain.MapSongsFlowToUiItemsUseCase
import com.github.bumblebee202111.minusonecloudmusic.domain.PlayPlaylistUseCase
import com.github.bumblebee202111.minusonecloudmusic.ui.common.AbstractPlaylistViewModel
import com.github.bumblebee202111.minusonecloudmusic.utils.stateInUi
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import javax.inject.Inject

@HiltViewModel
class DiscoverViewModel @Inject constructor(
    private val playlistRepository: PlaylistRepository,
    private val discoverRepository: DiscoverRepository,
    private val musicServiceConnection: MusicServiceConnection,
    private val playPlaylistUseCase: PlayPlaylistUseCase,
    @ApplicationScope private val coroutineScope: CoroutineScope,
    private val mapSongsFlowToUiItemsUseCase: MapSongsFlowToUiItemsUseCase
) : AbstractPlaylistViewModel<RemoteSong>(playPlaylistUseCase) {
    override val loadedSongs: List<RemoteSong>
        get() = playlist.value?.expandedSongs ?: emptyList()


    private val playlist =
        playlistRepository.getPlaylistDetail(TOP_LIST_ID).map { it.data }.flowOn(Dispatchers.IO)
            .stateInUi()

    val blocks = discoverRepository.getDiscoverBlocks().map { it.data }.stateInUi()
}