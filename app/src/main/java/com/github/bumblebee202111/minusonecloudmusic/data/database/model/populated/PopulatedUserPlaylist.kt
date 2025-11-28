package com.github.bumblebee202111.minusonecloudmusic.data.database.model.populated

import androidx.room.Embedded
import androidx.room.Relation
import com.github.bumblebee202111.minusonecloudmusic.data.database.model.entity.PlaylistEntity
import com.github.bumblebee202111.minusonecloudmusic.data.database.model.entity.UserPlaylistEntity

data class PopulatedUserPlaylist(
    @Embedded
    val entity: UserPlaylistEntity,
    @Relation(
        parentColumn = "playlist_id",
        entityColumn = "id"
    )
    val playlist: PlaylistEntity
)