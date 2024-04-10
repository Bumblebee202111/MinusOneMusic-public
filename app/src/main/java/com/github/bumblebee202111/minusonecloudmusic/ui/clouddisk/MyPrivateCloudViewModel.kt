package com.github.bumblebee202111.minusonecloudmusic.ui.clouddisk

import androidx.lifecycle.viewModelScope
import androidx.paging.cachedIn
import androidx.paging.map
import com.github.bumblebee202111.minusonecloudmusic.data.MusicServiceConnection
import com.github.bumblebee202111.minusonecloudmusic.data.model.RemoteSong
import com.github.bumblebee202111.minusonecloudmusic.data.repository.LoggedInUserDataRepository
import com.github.bumblebee202111.minusonecloudmusic.domain.GetPagedPlaylistSongItemsUseCase
import com.github.bumblebee202111.minusonecloudmusic.domain.PlayPlaylistUseCase
import com.github.bumblebee202111.minusonecloudmusic.ui.common.AbstractRemotePlaylistViewModel
import com.github.bumblebee202111.minusonecloudmusic.utils.stateInUi
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.map
import javax.inject.Inject

@HiltViewModel
class MyPrivateCloudViewModel @Inject constructor(
    private val loggedInUserDataRepository: LoggedInUserDataRepository,
    private val getPlaylistSongItemsUseCase: GetPagedPlaylistSongItemsUseCase,
    private val musicServiceConnection: MusicServiceConnection,
    playPlaylistUseCase: PlayPlaylistUseCase
) : AbstractRemotePlaylistViewModel<RemoteSong>(playPlaylistUseCase) {

    val player = musicServiceConnection.player.stateInUi()


    private val cloudSongsCountAndPagingData = loggedInUserDataRepository.getCloudSongsPagingData1()

    val cloudSongsCount= cloudSongsCountAndPagingData.first.map { it.data }.stateInUi(0)

    override val loadedSongs: MutableList<RemoteSong> = mutableListOf()

    val cloudSongsPagingData =
        getPlaylistSongItemsUseCase(cloudSongsCountAndPagingData.second.map {
            it.map { song ->
                loadedSongs += song
                song
            }
        }.cachedIn(viewModelScope))
            .cachedIn(viewModelScope)

    override val loadRemainingSongs: suspend () -> List<RemoteSong> ={
       loggedInUserDataRepository.getAllCloudSongs(loadedSongs.size)
    }
}