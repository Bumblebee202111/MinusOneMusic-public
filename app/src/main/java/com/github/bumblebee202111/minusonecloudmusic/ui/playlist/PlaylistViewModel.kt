package com.github.bumblebee202111.minusonecloudmusic.ui.playlist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.cachedIn
import androidx.paging.map
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

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel(assistedFactory = PlaylistViewModel.Factory::class)
class PlaylistViewModel @AssistedInject constructor(
    @Assisted val playlistId: Long,
    @Assisted("creatorId") val creatorId: Long?,
    @Assisted("isMyPL") val isMyPL: Boolean?,
    @Assisted("isV6") val isV6: Boolean?,
    private val loginRepository: LoginRepository,
    private val playlistRepository: PlaylistRepository,
    private val songRepository: SongRepository,
    playPlaylistUseCase: PlayPlaylistUseCase,
    private val getPlaylistSongItemsUseCase: MapSongPagingDataFlowToUiItemsUseCase
) : ViewModel() {

    val loadedSongs = mutableListOf<RemoteSong>()

    private val detailAndPagingFlows = loginRepository.loggedInUserId
        .map { loggedInUserId ->
            isV6 ?: (isMyPL == true || loggedInUserId == creatorId)
        }
        .distinctUntilChanged()
        .map { useV6 ->
            loadedSongs.clear()
            if (useV6) {
                playlistRepository.getMyPlaylistDetailAndPagingData(playlistId)
            } else {
                playlistRepository.getPlaylistDetailAndPagingData(playlistId)
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
            pagingDataFlow.map { pagingData ->
                pagingData.map { song ->
                    loadedSongs += song
                    song
                }
            }
        }
        .cachedIn(viewModelScope)

    val playlistSongs = getPlaylistSongItemsUseCase(basePagingDataFlow)

    private val playbackHandler = PlaylistPlaybackHandler(
        playPlaylistUseCase = playPlaylistUseCase,
        scope = viewModelScope,
        getLoadedSongs = { loadedSongs },
        loadRemainingSongs = {
            val allSongs = playlistDetail.value?.allSongs
            if (allSongs != null) {
                val result =
                    songRepository.getSongDetails(allSongs.map(SimpleRemoteSong::id)).first {
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
            @Assisted("isMyPL") isMyPL: Boolean?,
            @Assisted("isV6") isV6: Boolean?
        ): PlaylistViewModel
    }
}