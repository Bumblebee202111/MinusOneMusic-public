package com.github.bumblebee202111.minusonecloudmusic.ui.recentplay

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.bumblebee202111.minusonecloudmusic.coroutines.AppDispatchers
import com.github.bumblebee202111.minusonecloudmusic.coroutines.Dispatcher
import com.github.bumblebee202111.minusonecloudmusic.data.model.MyRecentMusicData
import com.github.bumblebee202111.minusonecloudmusic.data.repository.UserRepository
import com.github.bumblebee202111.minusonecloudmusic.domain.MapSongsFlowToUiItemsUseCase
import com.github.bumblebee202111.minusonecloudmusic.domain.PlayPlaylistUseCase
import com.github.bumblebee202111.minusonecloudmusic.ui.common.PlaylistPlaybackHandler
import com.github.bumblebee202111.minusonecloudmusic.utils.stateInUi
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import javax.inject.Inject

@HiltViewModel
class MyRecentPlayViewModel @Inject constructor(
    userRepository: UserRepository,
    mapSongsFlowToUiItemsUseCase: MapSongsFlowToUiItemsUseCase,
    playPlaylistUseCase: PlayPlaylistUseCase,
    @Dispatcher(AppDispatchers.IO) private val ioDispatcher: CoroutineDispatcher
) : ViewModel() {
    private val recentPlaySongs = userRepository.getRecentPlayMusic().map {
        it.data?.map(
            MyRecentMusicData::musicInfo
        )
    }.flowOn(ioDispatcher).stateInUi()

    val recentPlaySongUiList = mapSongsFlowToUiItemsUseCase(recentPlaySongs)
        .stateInUi()

    private val playbackHandler = PlaylistPlaybackHandler(
        playPlaylistUseCase = playPlaylistUseCase,
        scope = viewModelScope,
        getLoadedSongs = { recentPlaySongs.value ?: emptyList() },
        loadRemainingSongs = null
    )

    fun onSongItemClick(startIndex: Int) = playbackHandler.onSongItemClick(startIndex)
    fun playAll() = playbackHandler.playAll()
}