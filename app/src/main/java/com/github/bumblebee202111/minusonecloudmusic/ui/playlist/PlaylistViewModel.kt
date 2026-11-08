package com.github.bumblebee202111.minusonecloudmusic.ui.playlist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.cachedIn
import com.github.bumblebee202111.minusonecloudmusic.data.AppResult
import com.github.bumblebee202111.minusonecloudmusic.data.repository.LoginRepository
import com.github.bumblebee202111.minusonecloudmusic.data.repository.PlaylistRepository
import com.github.bumblebee202111.minusonecloudmusic.data.repository.SongRepository
import com.github.bumblebee202111.minusonecloudmusic.domain.MapSongPagingDataFlowToUiItemsUseCase
import com.github.bumblebee202111.minusonecloudmusic.domain.PlayPlaylistUseCase
import com.github.bumblebee202111.minusonecloudmusic.model.RemoteSong
import com.github.bumblebee202111.minusonecloudmusic.model.SimpleRemoteSong
import com.github.bumblebee202111.minusonecloudmusic.ui.common.PlaylistPlaybackHandler
import com.github.bumblebee202111.minusonecloudmusic.utils.stateInUi
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.shareIn
import java.util.concurrent.CopyOnWriteArrayList

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel(assistedFactory = PlaylistViewModel.Factory::class)
class PlaylistViewModel @AssistedInject constructor(
    @Assisted val playlistId: Long,
    @Assisted("creatorId") val creatorId: Long?,
    @Assisted("shouldFetchV6") val shouldFetchV6: Boolean?,
    private val loginRepository: LoginRepository,
    private val playlistRepository: PlaylistRepository,
    private val songRepository: SongRepository,
    playPlaylistUseCase: PlayPlaylistUseCase,
    private val getPlaylistSongItemsUseCase: MapSongPagingDataFlowToUiItemsUseCase
) : ViewModel() {

    private val loadedSongs = CopyOnWriteArrayList<RemoteSong>()

    private val onPageLoaded: (Boolean, List<RemoteSong>) -> Unit = { isInitialPage, items ->
        if (isInitialPage) {
            loadedSongs.clear()
        }
        loadedSongs.addAll(items)
    }

    private val detailAndPagingFlows = loginRepository.loggedInUserId
        .map { loggedInUserId ->
            shouldFetchV6 ?: (loggedInUserId != null && loggedInUserId == creatorId)
        }
        .distinctUntilChanged()
        .map { useV6 ->
            loadedSongs.clear()
            if (useV6) {
                playlistRepository.getMyPlaylistDetailAndPagingData(playlistId, onPageLoaded)
            } else {
                playlistRepository.getPlaylistDetailAndPagingData(playlistId, onPageLoaded)
            }
        }
        .shareIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), replay = 1)

    val playlistDetail = detailAndPagingFlows
        .flatMapLatest { (detailResultFlow, _) ->
            detailResultFlow.map { it.data }
        }
        .stateInUi()

    private val basePagingDataFlow = detailAndPagingFlows
        .flatMapLatest { (_, pagingDataFlow) ->
            pagingDataFlow
        }
        .cachedIn(viewModelScope)

    val playlistSongs = getPlaylistSongItemsUseCase(basePagingDataFlow)

    private val playbackHandler = PlaylistPlaybackHandler(
        playPlaylistUseCase = playPlaylistUseCase,
        scope = viewModelScope,
        getLoadedSongs = { loadedSongs },
        loadRemainingSongs = { loadedCount ->
            val remainingSongIds = playlistDetail.value?.allSongs
                ?.drop(loadedCount)
                ?.map(SimpleRemoteSong::id)
                .orEmpty()
            if (remainingSongIds.isNotEmpty()) {
                val result =
                    songRepository.getSongDetails(remainingSongIds).first {
                        it !is AppResult.Loading
                    }

                when (result) {
                    is AppResult.Success -> result.data
                    is AppResult.Error -> emptyList()
                    else -> emptyList()
                }
            } else {
                emptyList()
            }
        }
    )

    fun onSongItemClick(startIndex: Int) = playbackHandler.onSongItemClick(startIndex)
    fun playAll() = playbackHandler.playAll()

    @AssistedFactory
    interface Factory {
        fun create(
            playlistId: Long,
            @Assisted("creatorId") creatorId: Long?,
            @Assisted("shouldFetchV6") shouldFetchV6: Boolean?
        ): PlaylistViewModel
    }
}