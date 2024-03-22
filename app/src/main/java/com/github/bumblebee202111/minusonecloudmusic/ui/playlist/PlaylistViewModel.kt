package com.github.bumblebee202111.minusonecloudmusic.ui.playlist

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.github.bumblebee202111.minusonecloudmusic.data.MusicServiceConnection
import com.github.bumblebee202111.minusonecloudmusic.data.repository.LoginRepository
import com.github.bumblebee202111.minusonecloudmusic.data.repository.PlaylistRepository
import com.github.bumblebee202111.minusonecloudmusic.utils.stateInUi
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.take
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class PlaylistViewModel @Inject constructor(
    loginRepository: LoginRepository,
    savedStateHandle: SavedStateHandle,
    private val playlistRepository: PlaylistRepository,
    musicServiceConnection: MusicServiceConnection
) :
    ViewModel() {
private val args=PlaylistFragmentArgs.fromSavedStateHandle(savedStateHandle)
    private val playlistId=args.playlistId
private val creatorId=args.playlistCreatorId
    private val playlistDetailFlow =  loginRepository.loggedInUserId.take(1).flatMapLatest {
        if(creatorId==it)
            playlistRepository.getMyPlaylistDetail(playlistId)
        else
        playlistRepository.getPlaylistDetail(playlistId)
    }.map { it.data }
    val playlistDetail = playlistDetailFlow.flowOn(Dispatchers.IO)
        .stateInUi()

    val player=musicServiceConnection.player.stateInUi()

}