package com.github.bumblebee202111.minusonecloudmusic.domain

import androidx.media3.common.C
import androidx.media3.common.MediaItem
import com.github.bumblebee202111.minusonecloudmusic.coroutines.AppDispatchers.Main
import com.github.bumblebee202111.minusonecloudmusic.coroutines.Dispatcher
import com.github.bumblebee202111.minusonecloudmusic.data.MusicServiceConnection
import com.github.bumblebee202111.minusonecloudmusic.model.AbstractRemoteSong
import com.github.bumblebee202111.minusonecloudmusic.model.AbstractSong
import com.github.bumblebee202111.minusonecloudmusic.model.LocalSong
import com.github.bumblebee202111.minusonecloudmusic.player.asMediaItem
import com.github.bumblebee202111.minusonecloudmusic.data.repository.PlaylistRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PlayPlaylistUseCase @Inject constructor(
    private val musicServiceConnection: MusicServiceConnection,
    private val playlistRepository: PlaylistRepository,
    @Dispatcher(Main) private val mainDispatcher: CoroutineDispatcher
) {

    
    suspend operator fun invoke(
        loadedSongs: List<AbstractSong>,
        loadRemainingSongs: (suspend (loadedCount: Int) -> List<AbstractSong>)? = null,
        startIndex: Int = if (loadedSongs.isEmpty()) C.INDEX_UNSET else 0,
    ) {
        val loadedSnapshot = loadedSongs.toList()
        if (loadedSnapshot.isEmpty() || (startIndex != C.INDEX_UNSET && startIndex !in loadedSnapshot.indices)) {
            return
        }

        val loadedMediaItems =
            loadedSnapshot.map(::mapSongToMediaItem)

        withContext(mainDispatcher) {
            val player = musicServiceConnection.awaitPlayer()
            val isSongChanged = startIndex == C.INDEX_UNSET ||
                musicServiceConnection.currentMediaId.value != loadedSnapshot[startIndex].mediaId
            val startPositionMs =
                if (isSongChanged)
                    C.TIME_UNSET
                else
                    player.currentPosition

            player.run {
                setMediaItems(loadedMediaItems, startIndex, startPositionMs)
                prepare()
                play()
            }
        }

        playlistRepository.clearPlayerPlaylist()
        playlistRepository.addSongsToPlayerPlaylist(loadedSnapshot)

        if (loadRemainingSongs != null) {
            val remainingSongs = loadRemainingSongs(loadedSnapshot.size)
            if (remainingSongs.isNotEmpty()) {
                val mediaItems = remainingSongs.map(::mapSongToMediaItem)
                withContext(mainDispatcher) {
                    musicServiceConnection.awaitPlayer().addMediaItems(mediaItems)
                }
                playlistRepository.addSongsToPlayerPlaylist(remainingSongs)
            }
        }

    }

    private fun mapSongToMediaItem(song: AbstractSong): MediaItem {
        return when (song) {
            is LocalSong -> song.asMediaItem()
            is AbstractRemoteSong -> song.asMediaItem()
        }

    }

}