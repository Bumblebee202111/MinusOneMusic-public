package com.github.bumblebee202111.minusonecloudmusic.ui.recentplay

import androidx.lifecycle.ViewModel
import com.github.bumblebee202111.minusonecloudmusic.coroutines.AppDispatchers
import com.github.bumblebee202111.minusonecloudmusic.coroutines.Dispatcher
import com.github.bumblebee202111.minusonecloudmusic.data.MusicServiceConnection
import com.github.bumblebee202111.minusonecloudmusic.data.model.MyRecentMusicData
import com.github.bumblebee202111.minusonecloudmusic.data.model.RemoteSong
import com.github.bumblebee202111.minusonecloudmusic.data.repository.UserRepository
import com.github.bumblebee202111.minusonecloudmusic.domain.GetPlaylistSongItemsUseCase
import com.github.bumblebee202111.minusonecloudmusic.domain.PlayPlaylistUseCase
import com.github.bumblebee202111.minusonecloudmusic.ui.common.AbstractRemotePlaylistViewModel
import com.github.bumblebee202111.minusonecloudmusic.utils.stateInUi
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import javax.inject.Inject

@HiltViewModel
class MyRecentPlayViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val getPlaylistSongItemsUseCase: GetPlaylistSongItemsUseCase,
    private val playPlaylistUseCase: PlayPlaylistUseCase,
    @Dispatcher(AppDispatchers.IO) private val ioDispatcher: CoroutineDispatcher,
    private val musicServiceConnection: MusicServiceConnection
) :
    AbstractRemotePlaylistViewModel<RemoteSong>(playPlaylistUseCase) {
    private val recentPlaySongs = userRepository.getRecentPlayMusic().map {
        it.data?.map(
            MyRecentMusicData::musicInfo
        )
    }.flowOn(ioDispatcher).stateInUi()

    val recentPlaySongUiList = getPlaylistSongItemsUseCase(recentPlaySongs)
        .stateInUi()
    override val loadedSongs: List<RemoteSong>
        get() = recentPlaySongs.value ?: emptyList()

    val player=musicServiceConnection.player.stateInUi()

}