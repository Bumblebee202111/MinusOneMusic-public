package com.github.bumblebee202111.minusonecloudmusic.ui.nowplaying

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.bumblebee202111.minusonecloudmusic.data.MusicServiceConnection
import com.github.bumblebee202111.minusonecloudmusic.data.Result
import com.github.bumblebee202111.minusonecloudmusic.data.repository.LoggedInUserDataRepository
import com.github.bumblebee202111.minusonecloudmusic.data.repository.LoginRepository
import com.github.bumblebee202111.minusonecloudmusic.data.repository.SongRepository
import com.github.bumblebee202111.minusonecloudmusic.player.CountUtil
import com.github.bumblebee202111.minusonecloudmusic.utils.stateInUi
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
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
    private val loginRepository: LoginRepository
) : ViewModel() {

    private val currentSongId = musicServiceConnection.currentSongId

    val lyrics = currentSongId.flatMapLatest { songId ->
        if (songId != null) songRepository.getLyrics(songId).map { it.data } else flowOf(null)
    }.stateInUi()

    val player = musicServiceConnection.player.stateInUi()

    private val _liked =   currentSongId.flatMapLatest { songId ->
        if (songId != null) {
            loggedInUserDataRepository.observeSongLiked(songId)
        } else
            flowOf(null)
    }

    private val _likeCountText = currentSongId.flatMapLatest { songId ->
        if (songId != null) {
            songRepository.getSongLikeCountText(songId)
                .map { it.data }
        } else {
            flowOf(null)
        }
    }

    val likeState=_liked.combine(_likeCountText){ like, likeCountText->
        LikeState(like,likeCountText)
    }.stateInUi(LikeState(false,null))

    val commentCountDisplayText = currentSongId.flatMapLatest { songId ->
        if (songId != null) {
            songRepository.getCommentCount(songId)
                .map { commentCount -> commentCount.data?.let { CountUtil.getAbbreviatedCommentCount(it) } }

        } else {
            flowOf(null)
        }
    }.stateInUi()

    val isLoggedIn=loginRepository.isLoggedIn.stateInUi()
    fun onLikeClicked() {
        val songId=currentSongId.value?:return

        viewModelScope.launch {
            val like= _liked.first()?.not()?:return@launch
            loggedInUserDataRepository.likeSong(songId = songId, like = like).collect {}
        }
    }

}

data class LikeState(val like: Boolean?, val likeCountDisplayText: String?) {
}
