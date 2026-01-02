package com.github.bumblebee202111.minusonecloudmusic.ui.nowplaying

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.bumblebee202111.minusonecloudmusic.data.AppResult
import com.github.bumblebee202111.minusonecloudmusic.data.MusicServiceConnection
import com.github.bumblebee202111.minusonecloudmusic.model.RemoteSong
import com.github.bumblebee202111.minusonecloudmusic.data.repository.LoggedInUserDataRepository
import com.github.bumblebee202111.minusonecloudmusic.data.repository.LoginRepository
import com.github.bumblebee202111.minusonecloudmusic.data.repository.PlaylistRepository
import com.github.bumblebee202111.minusonecloudmusic.data.repository.SongRepository
import com.github.bumblebee202111.minusonecloudmusic.ui.common.ToastManager
import com.github.bumblebee202111.minusonecloudmusic.utils.stateInUi
import com.github.bumblebee202111.minusonecloudmusic.ui.mapper.toUiText
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject



@HiltViewModel
@OptIn(ExperimentalCoroutinesApi::class)
class NowPlayingViewModel @Inject constructor(
    private val songRepository: SongRepository,
    private val loggedInUserDataRepository: LoggedInUserDataRepository,
    musicServiceConnection: MusicServiceConnection,
    loginRepository: LoginRepository,
    private val playlistRepository: PlaylistRepository,
    private val toastManager: ToastManager
) : ViewModel() {

    val player = musicServiceConnection.player

    private val currentMediaId = musicServiceConnection.currentMediaId

    val currentSong = currentMediaId.map { mediaId ->
        if (mediaId != null) {
            playlistRepository.playerPlaylistSong(mediaId)
        } else {
            null
        }
    }.stateInUi()

    private val currentRemoteSongId = currentSong.map { song ->
        (song as? RemoteSong)?.id
    }.stateInUi()

    val lyrics = currentRemoteSongId.flatMapLatest { songId ->
        if (songId != null) songRepository.getLyrics(songId).map { result -> result.data } else flowOf(null)
    }.stateInUi()

    private val _liked = currentRemoteSongId.flatMapLatest { songId ->
        if (songId != null) {
            loggedInUserDataRepository.observeSongLiked(songId)
        } else
            flowOf(null)
    }

    private val _likeCountText = currentRemoteSongId.flatMapLatest { songId ->
        if (songId != null) {
            songRepository.getSongLikeCountText(songId)
                .map { result -> result.data }
        } else {
            flowOf(null)
        }
    }

    val likeState = _liked.combine(_likeCountText) { like, likeCountText ->
        LikeState(like, likeCountText)
    }.stateInUi(LikeState(like = false, likeCountDisplayText = null))

    val commentInfo = currentRemoteSongId.flatMapLatest { songId ->
        if (songId != null) {
            songRepository.getCommentInfo(songId)
                .map { result -> result.data }

        } else {
            flowOf(null)
        }
    }.stateInUi()

    val isLoggedIn = loginRepository.isLoggedIn.stateInUi()
    fun onLikeClicked() {
        val songId = currentRemoteSongId.value ?: return
        viewModelScope.launch {
            val isCurrentlyLiked = _liked.first() ?: false
            val newLikeState = !isCurrentlyLiked
            loggedInUserDataRepository.likeSong(songId = songId, like = newLikeState).collect { result ->
                if (result is AppResult.Error) {
                    toastManager.showMessage(result.error.toUiText())
                }
            }
        }
    }

    fun onDownloadClick() {
        val song = (currentSong.value as? RemoteSong) ?: return
        viewModelScope.launch {
            songRepository.download(song).collect {result ->
                if(result is AppResult.Error){
                    toastManager.showMessage(result.error.toUiText())
                }
            }
        }
    }

}

data class LikeState(val like: Boolean?, val likeCountDisplayText: String?)
