package com.github.bumblebee202111.minusonecloudmusic.ui.nowplaying

import androidx.annotation.OptIn
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.Metadata
import androidx.media3.common.Player
import androidx.media3.common.Tracks
import androidx.media3.common.util.UnstableApi
import androidx.media3.extractor.metadata.id3.CommentFrame
import androidx.media3.extractor.metadata.id3.TextInformationFrame
import androidx.media3.extractor.metadata.vorbis.VorbisComment
import com.github.bumblebee202111.minusonecloudmusic.data.AppResult
import com.github.bumblebee202111.minusonecloudmusic.data.MusicServiceConnection
import com.github.bumblebee202111.minusonecloudmusic.data.repository.LoggedInUserDataRepository
import com.github.bumblebee202111.minusonecloudmusic.data.repository.LoginRepository
import com.github.bumblebee202111.minusonecloudmusic.data.repository.PlaylistRepository
import com.github.bumblebee202111.minusonecloudmusic.data.repository.SongRepository
import com.github.bumblebee202111.minusonecloudmusic.model.LyricsEntry
import com.github.bumblebee202111.minusonecloudmusic.model.RemoteSong
import com.github.bumblebee202111.minusonecloudmusic.ui.common.ToastManager
import com.github.bumblebee202111.minusonecloudmusic.ui.mapper.toUiText
import com.github.bumblebee202111.minusonecloudmusic.utils.stateInUi
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject
@HiltViewModel
@OptIn(UnstableApi::class)
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
        mediaId?.let { playlistRepository.playerPlaylistSong(it) }
    }.stateInUi()

    private val currentRemoteSongId = currentSong.map { song ->
        (song as? RemoteSong)?.id
    }.stateInUi()
    private val rawMetadataFlow = player.flatMapLatest { currentPlayer ->
        if (currentPlayer == null) return@flatMapLatest flowOf(null)
        callbackFlow<Metadata?> {
            val listener = object : Player.Listener {

                override fun onTracksChanged(tracks: Tracks) {
                    val metadata = tracks.groups.firstNotNullOfOrNull { group ->
                        (0 until group.length).firstNotNullOfOrNull { i ->
                            group.getTrackFormat(i).metadata
                        }
                    }
                    if (metadata != null) trySend(metadata)
                }

                override fun onMetadata(metadata: Metadata) {
                    trySend(metadata)
                }
            }
            currentPlayer.addListener(listener)
            val initialMetadata = currentPlayer.currentTracks.groups.firstNotNullOfOrNull { group ->
                (0 until group.length).firstNotNullOfOrNull { i -> group.getTrackFormat(i).metadata }
            }
            trySend(initialMetadata)

            awaitClose { currentPlayer.removeListener(listener) }
        }
    }

    val lyrics = currentSong.flatMapLatest { song ->
        if (song == null) return@flatMapLatest flowOf(null)

        rawMetadataFlow.flatMapLatest { metadata ->
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


@androidx.annotation.OptIn(UnstableApi::class)
private fun Metadata?.extractEmbeddedLyrics(): List<LyricsEntry>? {
    if (this == null) return null
    var rawLyrics: String? = null

    for (i in 0 until length()) {
        val entry = get(i)
        if (entry is CommentFrame && (entry.id == "USLT" || entry.description.equals("LYRICS", true))) {
            rawLyrics = entry.text
            break
        }
        if (entry is TextInformationFrame && entry.id == "TXXX" && entry.description?.equals("LYRICS", true) == true) {
            rawLyrics = entry.values.first()
            break
        }
        if (entry is VorbisComment && (entry.key.equals("LYRICS", true) || entry.key.equals("UNSYNCEDLYRICS", true))) {
            rawLyrics = entry.value
            break
        }
    }

    return LyricsEntry.parseLrc(rawLyrics).takeIf { it.isNotEmpty() }
}