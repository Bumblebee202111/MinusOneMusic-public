package com.github.bumblebee202111.minusonecloudmusic.ui.clouddisk

import androidx.compose.ui.semantics.error
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.cachedIn
import androidx.paging.map
import com.github.bumblebee202111.minusonecloudmusic.data.AppResult
import com.github.bumblebee202111.minusonecloudmusic.data.model.RemoteSong
import com.github.bumblebee202111.minusonecloudmusic.data.repository.LoggedInUserDataRepository
import com.github.bumblebee202111.minusonecloudmusic.data.repository.LoginRepository
import com.github.bumblebee202111.minusonecloudmusic.domain.MapSongPagingDataFlowToUiItemsUseCase
import com.github.bumblebee202111.minusonecloudmusic.domain.PlayPlaylistUseCase
import com.github.bumblebee202111.minusonecloudmusic.ui.common.PlaylistPlaybackHandler
import com.github.bumblebee202111.minusonecloudmusic.utils.ToastManager
import com.github.bumblebee202111.minusonecloudmusic.utils.stateInUi
import com.github.bumblebee202111.minusonecloudmusic.utils.toUiText
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import javax.inject.Inject

@HiltViewModel
class MyPrivateCloudViewModel @Inject constructor(
    loginRepository: LoginRepository,
    private val loggedInUserDataRepository: LoggedInUserDataRepository,
    private val mapSongPagingDataFlowToUiItemsUseCase: MapSongPagingDataFlowToUiItemsUseCase,
    playPlaylistUseCase: PlayPlaylistUseCase,
    private val toastManager: ToastManager
) : ViewModel() {
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

    val loadedSongs: MutableList<RemoteSong> = mutableListOf()

    @OptIn(ExperimentalCoroutinesApi::class)
    val songUiItemsPagingData = isLoggedIn.flatMapLatest { isLoggedIn ->
        if (isLoggedIn) {
            mapSongPagingDataFlowToUiItemsUseCase(cloudSongsCountAndPagingData.second.map {
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

    private val playbackHandler = PlaylistPlaybackHandler(
        playPlaylistUseCase = playPlaylistUseCase,
        scope = viewModelScope,
        getLoadedSongs = { loadedSongs },
        loadRemainingSongs = {
            val result = loggedInUserDataRepository.getAllCloudSongs(start = loadedSongs.size)
                .first { it !is AppResult.Loading }

            when (result) {
                is AppResult.Success -> {
                    result.data
                }

                is AppResult.Error -> {
                    toastManager.showMessage(result.error.toUiText())
                    emptyList()
                }

                is AppResult.Loading -> emptyList()
            }
        }
    )
    fun onSongItemClick(startIndex: Int) = playbackHandler.onSongItemClick(startIndex)
    fun playAll() = playbackHandler.playAll()
}