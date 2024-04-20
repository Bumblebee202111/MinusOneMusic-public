package com.github.bumblebee202111.minusonecloudmusic.domain

import androidx.media3.common.C
import androidx.media3.common.MediaItem
import com.github.bumblebee202111.minusonecloudmusic.coroutines.AppDispatchers.Main
import com.github.bumblebee202111.minusonecloudmusic.coroutines.Dispatcher
import com.github.bumblebee202111.minusonecloudmusic.data.MusicServiceConnection
import com.github.bumblebee202111.minusonecloudmusic.data.model.AbstractRemoteSong
import com.github.bumblebee202111.minusonecloudmusic.data.model.AbstractSong
import com.github.bumblebee202111.minusonecloudmusic.data.model.LocalSong
import com.github.bumblebee202111.minusonecloudmusic.data.model.asMediaItem
import com.github.bumblebee202111.minusonecloudmusic.data.repository.PlaylistRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject


class PlayPlaylistUseCase @Inject constructor(
    private val musicServiceConnection: MusicServiceConnection,
    private val playlistRepository: PlaylistRepository,
    @Dispatcher(Main) private val mainDispatcher: CoroutineDispatcher
) {

    
    suspend operator fun invoke(
        loadedSongs: List<AbstractSong>,
        loadRemainingSongs: (suspend () -> List<AbstractSong>)? = null,
        startIndex: Int = 0,
    ) {
        val player = musicServiceConnection.player.value ?: return

        val isSongChanged =
            musicServiceConnection.currentMediaId.value != loadedSongs[startIndex].mediaId

        val loadedMediaItems =
            loadedSongs.map(::mapSongToMediaItem)

        val startPositionMs =
            if (isSongChanged)
                C.TIME_UNSET
            else
                player.currentPosition

        withContext(mainDispatcher) {
            player.run {
                setMediaItems(loadedMediaItems, startIndex, startPositionMs)
                prepare()
                play()
            }
        }

        playlistRepository.clearPlayerPlaylist()
        playlistRepository.addSongsToPlayerPlaylist(loadedSongs)

        if (loadRemainingSongs != null) {
            val remainingSongs = loadRemainingSongs()
            val mediaItems = remainingSongs.map(::mapSongToMediaItem)
            withContext(mainDispatcher) {
                player.addMediaItems(mediaItems)
            }
            playlistRepository.addSongsToPlayerPlaylist(remainingSongs)
        }

    }

    private fun mapSongToMediaItem(song: AbstractSong): MediaItem {
        return when (song) {
            is LocalSong -> song.asMediaItem()
            is AbstractRemoteSong -> song.asMediaItem()
        }

    }

}


