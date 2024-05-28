package com.github.bumblebee202111.minusonecloudmusic.ui.recentplay

import com.github.bumblebee202111.minusonecloudmusic.coroutines.AppDispatchers
import com.github.bumblebee202111.minusonecloudmusic.coroutines.Dispatcher
import com.github.bumblebee202111.minusonecloudmusic.data.MusicServiceConnection
import com.github.bumblebee202111.minusonecloudmusic.data.model.MyRecentMusicData
import com.github.bumblebee202111.minusonecloudmusic.data.model.RemoteSong
import com.github.bumblebee202111.minusonecloudmusic.data.repository.UserRepository
import com.github.bumblebee202111.minusonecloudmusic.domain.MapSongsFlowToUiItemsUseCase
import com.github.bumblebee202111.minusonecloudmusic.domain.PlayPlaylistUseCase
import com.github.bumblebee202111.minusonecloudmusic.ui.common.AbstractPlaylistViewModel
import com.github.bumblebee202111.minusonecloudmusic.utils.stateInUi
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import javax.inject.Inject

@HiltViewModel
class MyRecentPlayViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val mapSongsFlowToUiItemsUseCase: MapSongsFlowToUiItemsUseCase,
    private val playPlaylistUseCase: PlayPlaylistUseCase,
    @Dispatcher(AppDispatchers.IO) private val ioDispatcher: CoroutineDispatcher,
    private val musicServiceConnection: MusicServiceConnection
) :
    AbstractPlaylistViewModel<RemoteSong>(playPlaylistUseCase) {
    private val recentPlaySongs = userRepository.getRecentPlayMusic().map {
        it.data?.map(
            MyRecentMusicData::musicInfo
        )
    }.flowOn(ioDispatcher).stateInUi()

    val recentPlaySongUiList = mapSongsFlowToUiItemsUseCase(recentPlaySongs)
        .stateInUi()
    override val loadedSongs: List<RemoteSong>
        get() = recentPlaySongs.value ?: emptyList()

    val player = musicServiceConnection.player

}