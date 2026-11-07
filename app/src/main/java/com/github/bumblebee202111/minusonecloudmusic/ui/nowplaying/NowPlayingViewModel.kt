package com.github.bumblebee202111.minusonecloudmusic.ui.nowplaying

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.bumblebee202111.minusonecloudmusic.data.AppResult
import com.github.bumblebee202111.minusonecloudmusic.data.MusicServiceConnection
import com.github.bumblebee202111.minusonecloudmusic.data.repository.LoggedInUserDataRepository
import com.github.bumblebee202111.minusonecloudmusic.data.repository.LoginRepository
import com.github.bumblebee202111.minusonecloudmusic.data.repository.PlaylistRepository
import com.github.bumblebee202111.minusonecloudmusic.data.repository.SongRepository
import com.github.bumblebee202111.minusonecloudmusic.model.RemoteSong
import com.github.bumblebee202111.minusonecloudmusic.player.AudioOutputMonitor
import com.github.bumblebee202111.minusonecloudmusic.player.extractEmbeddedLyrics
import com.github.bumblebee202111.minusonecloudmusic.ui.common.ToastManager
import com.github.bumblebee202111.minusonecloudmusic.ui.mapper.toUiText
import com.github.bumblebee202111.minusonecloudmusic.utils.stateInUi
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.combine
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
    private val audioOutputMonitor: AudioOutputMonitor,
    loginRepository: LoginRepository,
    private val playlistRepository: PlaylistRepository,
    private val toastManager: ToastManager
) : ViewModel() {

    val mediaMetadata = musicServiceConnection.mediaMetadata
    val isPlaying = musicServiceConnection.isPlaying
    val currentPosition = musicServiceConnection.currentPosition
    val bufferedPosition = musicServiceConnection.bufferedPosition
    val duration = musicServiceConnection.duration
    val repeatMode = musicServiceConnection.repeatMode
    val shuffleModeEnabled = musicServiceConnection.shuffleModeEnabled
    val isPlayPauseEnabled = musicServiceConnection.isPlayPauseEnabled
    val canShuffleRepeat = musicServiceConnection.canShuffleRepeat
    val isA2dpPlaying = audioOutputMonitor.isA2dpPlaying.stateInUi(false)

    private val currentMediaId = musicServiceConnection.currentMediaId

    val currentSong = currentMediaId.map { mediaId ->
        mediaId?.let { playlistRepository.playerPlaylistSong(it) }
    }.stateInUi()

    private val currentRemoteSongId = currentSong.map { song ->
        (song as? RemoteSong)?.id
    }.stateInUi()

    val lyrics = currentSong.flatMapLatest { song ->
        if (song == null) return@flatMapLatest flowOf(null)

        musicServiceConnection.rawMetadata.flatMapLatest { metadata ->
            val embeddedLyrics = metadata.extractEmbeddedLyrics()

            if (!embeddedLyrics.isNullOrEmpty()) {
                flowOf(embeddedLyrics)
            } else if (song is RemoteSong) {
                songRepository.getLyrics(song.id).map { result ->
                    result.data
                }
            } else {
                flowOf(null)
            }
        }
    }.stateInUi()

    private val _liked = currentRemoteSongId.flatMapLatest { songId ->
        songId?.let { loggedInUserDataRepository.observeSongLiked(it) } ?: flowOf(null)
    }

    private val _likeCountText = currentRemoteSongId.flatMapLatest { songId ->
        songId?.let { songRepository.getSongLikeCountText(it).map { res -> res.data } } ?: flowOf(null)
    }

    val likeState = _liked.combine(_likeCountText) { like, countText ->
        LikeState(like, countText)
    }.stateInUi(LikeState(like = false, likeCountDisplayText = null))

    val commentInfo = currentRemoteSongId.flatMapLatest { songId ->
        songId?.let { songRepository.getCommentInfo(it).map { res -> res.data } } ?: flowOf(null)
    }.stateInUi()

    val isLoggedIn = loginRepository.isLoggedIn.stateInUi()

    fun onPlayPauseClick() = musicServiceConnection.togglePlayPause()

    fun onPreviousClick() = musicServiceConnection.seekToPrevious()

    fun onNextClick() = musicServiceConnection.seekToNext()

    fun onToggleRepeatShuffleMode() = musicServiceConnection.toggleRepeatShuffleMode()

    fun onSeek(positionMs: Long) = musicServiceConnection.seekTo(positionMs)

    fun onShowVolumeUi() = audioOutputMonitor.showSystemVolumeUi()

    fun onLikeClicked() {
        val songId = currentRemoteSongId.value ?: return
        viewModelScope.launch {
            val isCurrentlyLiked = likeState.value.like ?: false
            loggedInUserDataRepository.likeSong(songId = songId, like = !isCurrentlyLiked).collect { result ->
                if (result is AppResult.Error) {
                    toastManager.showMessage(result.error.toUiText())
                }
            }
        }
    }

    fun onDownloadClick() {
        val song = (currentSong.value as? RemoteSong) ?: return
        viewModelScope.launch {
            songRepository.download(song).collect { result ->
                if (result is AppResult.Error) {
                    toastManager.showMessage(result.error.toUiText())
                }
            }
        }
    }
}

data class LikeState(val like: Boolean?, val likeCountDisplayText: String?)