package com.github.bumblebee202111.minusonecloudmusic.domain

import androidx.media3.common.C
import com.github.bumblebee202111.minusonecloudmusic.data.MusicServiceConnection
import com.github.bumblebee202111.minusonecloudmusic.data.model.PlayableSong
import com.github.bumblebee202111.minusonecloudmusic.data.model.asMediaItem
import javax.inject.Inject
class PlaySongsUseCase @Inject constructor(private val musicServiceConnection: MusicServiceConnection) {
    operator fun invoke( songs:List<PlayableSong>, song:PlayableSong){
        val player=musicServiceConnection.player.value?:return
        val mediaItems=songs.map(PlayableSong::asMediaItem)
        val startPositionMs =
            if ( player.currentMediaItem?.mediaId == song.id.toString())
                player.currentPosition
            else C.TIME_UNSET
        val startIndex= songs.indexOfFirst { it.id == song.id }
        player.run {
            setMediaItems(mediaItems, startIndex, startPositionMs)
            prepare()
            play()
        }
    }
}