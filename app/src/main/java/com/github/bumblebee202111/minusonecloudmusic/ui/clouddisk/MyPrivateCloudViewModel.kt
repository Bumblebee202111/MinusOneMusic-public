package com.github.bumblebee202111.minusonecloudmusic.ui.clouddisk

import androidx.lifecycle.viewModelScope
import androidx.paging.cachedIn
import androidx.paging.map
import com.github.bumblebee202111.minusonecloudmusic.data.MusicServiceConnection
import com.github.bumblebee202111.minusonecloudmusic.data.model.RemoteSong
import com.github.bumblebee202111.minusonecloudmusic.data.repository.LoggedInUserDataRepository
import com.github.bumblebee202111.minusonecloudmusic.data.repository.LoginRepository
import com.github.bumblebee202111.minusonecloudmusic.domain.GetPagedPlaylistSongItemsUseCase
import com.github.bumblebee202111.minusonecloudmusic.domain.PlayPlaylistUseCase
import com.github.bumblebee202111.minusonecloudmusic.ui.common.AbstractPlaylistViewModel
import com.github.bumblebee202111.minusonecloudmusic.utils.stateInUi
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import javax.inject.Inject

@HiltViewModel
class MyPrivateCloudViewModel @Inject constructor(
    private val loginRepository: LoginRepository,
    private val loggedInUserDataRepository: LoggedInUserDataRepository,
    private val getPagedPlaylistSongItemsUseCase: GetPagedPlaylistSongItemsUseCase,
    private val musicServiceConnection: MusicServiceConnection,
    playPlaylistUseCase: PlayPlaylistUseCase
) : AbstractPlaylistViewModel<RemoteSong>(playPlaylistUseCase) {

    val player = musicServiceConnection.player

    private val isLoggedIn = loginRepository.isLoggedIn

    private val cloudSongsCountAndPagingData = loggedInUserDataRepository.getCloudSongsPagingData1()

    @OptIn(ExperimentalCoroutinesApi::class)
    val cloudSongsCount = isLoggedIn.flatMapLatest { isLoggedIn ->
        if (isLoggedIn) {
            cloudSongsCountAndPagingData.first.map { it.data }
        } else {
            emptyFlow()
        }
    }.stateInUi(0)

    override val loadedSongs: MutableList<RemoteSong> = mutableListOf()

    @OptIn(ExperimentalCoroutinesApi::class)
    val songUiItemsPagingData = isLoggedIn.flatMapLatest { isLoggedIn ->
        if (isLoggedIn) {
            getPagedPlaylistSongItemsUseCase(cloudSongsCountAndPagingData.second.map {
                it.map { song ->
                    loadedSongs += song
                    song
                }
            }.cachedIn(viewModelScope))
                .cachedIn(viewModelScope)
        } else {
            emptyFlow()
        }
    }

    override val loadRemainingSongs: suspend () -> List<RemoteSong> ={
       loggedInUserDataRepository.getAllCloudSongs(loadedSongs.size)
    }
}