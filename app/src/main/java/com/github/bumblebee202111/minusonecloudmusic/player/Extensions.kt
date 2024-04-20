package com.github.bumblebee202111.minusonecloudmusic.player

import androidx.core.net.toUri
import com.github.bumblebee202111.minusonecloudmusic.data.model.toAudioId

fun String.isLocalSongUri() = this[0] == 'c'

fun String.mediaIdToIsLocalAndSongId() = if (isLocalSongUri()) {
    Pair(true, this.toUri().toAudioId())
} else {
    Pair(false, this.toLong())
}