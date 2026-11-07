@file:UnstableApi

package com.github.bumblebee202111.minusonecloudmusic.player

import androidx.core.net.toUri
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.util.UnstableApi
import com.github.bumblebee202111.minusonecloudmusic.model.AbstractRemoteSong
import com.github.bumblebee202111.minusonecloudmusic.model.AbstractSong
import com.github.bumblebee202111.minusonecloudmusic.model.LocalSong
import com.github.bumblebee202111.minusonecloudmusic.utils.imageUrl

fun AbstractSong.asMediaItem(): MediaItem {
    val mediaId = mediaId

    val mediaMetadataBuilder =
        MediaMetadata.Builder()
            .setTitle(name)
            .setArtist(artists.joinToString("/"))
    when (this) {
        is AbstractRemoteSong -> {
            mediaMetadataBuilder.setArtworkUri(
                album?.pictureUrl?.imageUrl(
                    thumbnailSize = 600,
                    quality = 80
                )?.toUri()
            )
        }

        is LocalSong -> {
            mediaMetadataBuilder.setArtworkData(album?.art)
        }
    }

    val mediaMetadata =
        mediaMetadataBuilder
            .build()
    return MediaItem.Builder().setMediaId(mediaId)
        .setUri(mediaId)
        .setMediaMetadata(mediaMetadata)
        .setTag(true)
        .setCustomCacheKey(mediaId)
        .build()
}