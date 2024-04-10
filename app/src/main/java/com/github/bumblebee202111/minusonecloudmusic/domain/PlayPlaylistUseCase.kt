package com.github.bumblebee202111.minusonecloudmusic.domain

import androidx.media3.common.C
import com.github.bumblebee202111.minusonecloudmusic.coroutines.AppDispatchers.Main
import com.github.bumblebee202111.minusonecloudmusic.coroutines.Dispatcher
import com.github.bumblebee202111.minusonecloudmusic.data.MusicServiceConnection
import com.github.bumblebee202111.minusonecloudmusic.data.database.AppDatabase
import com.github.bumblebee202111.minusonecloudmusic.data.model.AbstractRemoteSong
import com.github.bumblebee202111.minusonecloudmusic.data.model.SimpleRemoteSong
import com.github.bumblebee202111.minusonecloudmusic.data.model.asMediaItem
import com.github.bumblebee202111.minusonecloudmusic.data.repository.PlaylistRepository
import com.github.bumblebee202111.minusonecloudmusic.data.repository.SongRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject


class PlayPlaylistUseCase @Inject constructor(
    private val musicServiceConnection: MusicServiceConnection,
    private val playlistRepository: PlaylistRepository,
    private val songRepository: SongRepository,
    private val appDatabase: AppDatabase,
    @Dispatcher(Main) private val mainDispatcher: CoroutineDispatcher
) {
    private val musicInfoDao = appDatabase.musicInfoDao()

    
    suspend operator fun invoke(
        loadedSongs: List<AbstractRemoteSong>,
        moreSongs: List<SimpleRemoteSong>? = null,
        loadRemaining: (suspend () -> List<AbstractRemoteSong>)? = null,
        songId: Long? = null,
    ) {
        val player = musicServiceConnection.player.value ?: return

        val isSongChanged = musicServiceConnection.currentSongId.value != songId

        val expandedMediaItems =
            loadedSongs.map(AbstractRemoteSong::asMediaItem)

        val startIndex = loadedSongs.indexOfFirst { it.id == songId }.coerceAtLeast(0)

        withContext(mainDispatcher) {
            val startPositionMs =
                if (isSongChanged)
                    C.TIME_UNSET
                else
                    player.currentPosition
            player.run {
                setMediaItems(expandedMediaItems, startIndex, startPositionMs)
                prepare()
                play()
            }
        }

        playlistRepository.clearPlayerPlaylist()
        playlistRepository.addSongsToPlayerPlaylist(loadedSongs)

        if (loadRemaining != null) {
            val remainingSongs = loadRemaining.invoke()
            val mediaItems = remainingSongs.map(AbstractRemoteSong::asMediaItem)
            withContext(mainDispatcher) {
                player.addMediaItems(mediaItems)
            }
            playlistRepository.addSongsToPlayerPlaylist(remainingSongs)
        }
    }


}


