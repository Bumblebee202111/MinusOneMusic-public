package com.github.bumblebee202111.minusonecloudmusic.ui.clouddisk

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.cachedIn
import com.github.bumblebee202111.minusonecloudmusic.data.AppResult
import com.github.bumblebee202111.minusonecloudmusic.model.RemoteSong
import com.github.bumblebee202111.minusonecloudmusic.data.repository.LoggedInUserDataRepository
import com.github.bumblebee202111.minusonecloudmusic.data.repository.LoginRepository
import com.github.bumblebee202111.minusonecloudmusic.domain.MapSongPagingDataFlowToUiItemsUseCase
import com.github.bumblebee202111.minusonecloudmusic.domain.PlayPlaylistUseCase
import com.github.bumblebee202111.minusonecloudmusic.ui.common.PlaylistPlaybackHandler
import com.github.bumblebee202111.minusonecloudmusic.ui.common.ToastManager
import com.github.bumblebee202111.minusonecloudmusic.utils.stateInUi
import com.github.bumblebee202111.minusonecloudmusic.ui.mapper.toUiText
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.shareIn
import java.util.concurrent.CopyOnWriteArrayList
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class MyPrivateCloudViewModel @Inject constructor(
    loginRepository: LoginRepository,
    private val loggedInUserDataRepository: LoggedInUserDataRepository,
    private val mapSongPagingDataFlowToUiItemsUseCase: MapSongPagingDataFlowToUiItemsUseCase,
    playPlaylistUseCase: PlayPlaylistUseCase,
    private val toastManager: ToastManager
) : ViewModel() {
    private val isLoggedIn = loginRepository.isLoggedIn

    private val loadedSongs = CopyOnWriteArrayList<RemoteSong>()

    private val cloudSongsCountAndPagingData = isLoggedIn
        .distinctUntilChanged()
        .map { loggedIn ->
            loadedSongs.clear()
            if (loggedIn) {
                loggedInUserDataRepository.getCloudSongsPagingData1(
                    onPageLoaded = { isInitialPage, items ->
                        if (isInitialPage) {
                            loadedSongs.clear()
                        }
                        loadedSongs.addAll(items)
                    }
                )
            } else {
                null
            }
        }
        .shareIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), replay = 1)

    val cloudSongsCount = cloudSongsCountAndPagingData
        .flatMapLatest { pair ->
            pair?.first?.map { it.data } ?: emptyFlow()
        }
        .stateInUi(0)

    private val basePagingDataFlow = cloudSongsCountAndPagingData
        .flatMapLatest { pair ->
            pair?.second ?: emptyFlow()
        }
        .cachedIn(viewModelScope)

    val songUiItemsPagingData = mapSongPagingDataFlowToUiItemsUseCase(basePagingDataFlow)

    private val playbackHandler = PlaylistPlaybackHandler(
        playPlaylistUseCase = playPlaylistUseCase,
        scope = viewModelScope,
        getLoadedSongs = { loadedSongs },
        loadRemainingSongs = { loadedCount ->
            val totalCount = cloudSongsCount.value ?: 0
            if (loadedCount >= totalCount) {
                emptyList()
            } else {
                val result = loggedInUserDataRepository.getAllCloudSongs(start = loadedCount)
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
        }
    )
    fun onSongItemClick(startIndex: Int) = playbackHandler.onSongItemClick(startIndex)
    fun playAll() = playbackHandler.playAll()
}