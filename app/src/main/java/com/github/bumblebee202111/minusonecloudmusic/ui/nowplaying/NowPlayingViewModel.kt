package com.github.bumblebee202111.minusonecloudmusic.ui.nowplaying

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.bumblebee202111.minusonecloudmusic.data.MusicServiceConnection
import com.github.bumblebee202111.minusonecloudmusic.data.model.RemoteSong
import com.github.bumblebee202111.minusonecloudmusic.data.repository.LoggedInUserDataRepository
import com.github.bumblebee202111.minusonecloudmusic.data.repository.LoginRepository
import com.github.bumblebee202111.minusonecloudmusic.data.repository.PlaylistRepository
import com.github.bumblebee202111.minusonecloudmusic.data.repository.SongRepository
import com.github.bumblebee202111.minusonecloudmusic.player.CountUtil
import com.github.bumblebee202111.minusonecloudmusic.utils.stateInUi
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
    private val musicServiceConnection: MusicServiceConnection,
    private val loginRepository: LoginRepository,
    private val playlistRepository: PlaylistRepository
) : ViewModel() {

    private val currentMediaId = musicServiceConnection.currentMediaId

    val currentSong = currentMediaId.map {
        if (it != null) {
            playlistRepository.playerPlaylistSong(it)
        } else {
            null
        }
    }.stateInUi()
    private val currentRemoteSongId = currentSong.map { currentMediaId ->
        (currentMediaId as? RemoteSong)?.id
    }.stateInUi()

    val lyrics = currentRemoteSongId.flatMapLatest { songId ->
        if (songId != null) songRepository.getLyrics(songId).map { it.data } else flowOf(null)
    }.stateInUi()

    val player = musicServiceConnection.player

    private val _liked = currentRemoteSongId.flatMapLatest { songId ->
        if (songId != null) {
            loggedInUserDataRepository.observeSongLiked(songId)
        } else
            flowOf(null)
    }

    private val _likeCountText = currentRemoteSongId.flatMapLatest { songId ->
        if (songId != null) {
            songRepository.getSongLikeCountText(songId)
                .map { it.data }
        } else {
            flowOf(null)
        }
    }

    val likeState = _liked.combine(_likeCountText) { like, likeCountText ->
        LikeState(like, likeCountText)
    }.stateInUi(LikeState(false, null))

    val commentCountDisplayText = currentRemoteSongId.flatMapLatest { songId ->
        if (songId != null) {
            songRepository.getCommentCount(songId)
                .map { commentCount ->
                    commentCount.data?.let(CountUtil::getAbbreviatedCommentCount)
                }

        } else {
            flowOf(null)
        }
    }.stateInUi()

    val isLoggedIn = loginRepository.isLoggedIn.stateInUi()
    fun onLikeClicked() {
        val songId = currentRemoteSongId.value ?: return
        viewModelScope.launch {
            val like = _liked.first()?.not() ?: return@launch
            loggedInUserDataRepository.likeSong(songId = songId, like = like).collect {}
        }
    }

    fun onDownloadClick() {
        val song = (currentSong.value as? RemoteSong) ?: return
        songRepository.download(song)
    }

}

data class LikeState(val like: Boolean?, val likeCountDisplayText: String?)
