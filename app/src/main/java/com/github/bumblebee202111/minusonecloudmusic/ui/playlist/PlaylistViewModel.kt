package com.github.bumblebee202111.minusonecloudmusic.ui.playlist

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.map
import com.github.bumblebee202111.minusonecloudmusic.data.model.PlaylistDetail
import com.github.bumblebee202111.minusonecloudmusic.data.model.RemoteSong
import com.github.bumblebee202111.minusonecloudmusic.data.model.SimpleRemoteSong
import com.github.bumblebee202111.minusonecloudmusic.data.repository.LoginRepository
import com.github.bumblebee202111.minusonecloudmusic.data.repository.PlaylistRepository
import com.github.bumblebee202111.minusonecloudmusic.data.repository.SongRepository
import com.github.bumblebee202111.minusonecloudmusic.domain.MapSongPagingDataFlowToUiItemsUseCase
import com.github.bumblebee202111.minusonecloudmusic.domain.PlayPlaylistUseCase
import com.github.bumblebee202111.minusonecloudmusic.ui.common.PlaylistPlaybackHandler
import com.github.bumblebee202111.minusonecloudmusic.utils.stateInUi
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class PlaylistViewModel @Inject constructor(
    private val loginRepository: LoginRepository,
    savedStateHandle: SavedStateHandle,
    private val playlistRepository: PlaylistRepository,
    private val songRepository: SongRepository,
    playPlaylistUseCase: PlayPlaylistUseCase,
    private val getPlaylistSongItemsUseCase: MapSongPagingDataFlowToUiItemsUseCase
) :
    ViewModel() {


    private val args = PlaylistFragmentArgs.fromSavedStateHandle(savedStateHandle)
    private val playlistId = args.playlistId
    private val creatorId = args.playlistCreatorId
    private val isMyPL = args.isMyPL

    private val _playlistDetail = MutableStateFlow<PlaylistDetail?>(null)
    val playlistDetail get() = _playlistDetail.stateInUi()

    private val _songs = MutableStateFlow<PagingData<SongItemUiModel>?>(null)
    val playlistSongs get() = _songs.filterNotNull().cachedIn(viewModelScope)

    val loadedSongs = mutableListOf<RemoteSong>()

    init {
        viewModelScope.launch {
            loginRepository.loggedInUserId.collect { loggedInUserId ->
                val (playlistDetailResultFlow, pagingDataFlow) =
                    if (isMyPL || loggedInUserId == creatorId)
                        playlistRepository.getMyPlaylistDetailAndPagingData(playlistId)
                    else
                        playlistRepository.getPlaylistDetailAndPagingData(playlistId)
                loadedSongs.clear()
                playlistDetailResultFlow.collect {
                    _playlistDetail.value = it.data
                }
                getPlaylistSongItemsUseCase(
                    pagingDataFlow.map {
                        it.map { song ->
                            loadedSongs += song
                            song
                        }
                    }.cachedIn(viewModelScope)
                ).cachedIn(viewModelScope).collect {
                    _songs.value = it
                }

            }
        }
    }

    private val playbackHandler = PlaylistPlaybackHandler(
        playPlaylistUseCase = playPlaylistUseCase,
        scope = viewModelScope,
        getLoadedSongs = { loadedSongs },
        loadRemainingSongs = {
            val allSongs = playlistDetail.value?.allSongs
            if (allSongs != null) {
                songRepository.getSongDetails(allSongs.map(SimpleRemoteSong::id))
            }
            emptyList()
        }
    )
    fun onSongItemClick(startIndex: Int) = playbackHandler.onSongItemClick(startIndex)
    fun playAll() = playbackHandler.playAll()
}
