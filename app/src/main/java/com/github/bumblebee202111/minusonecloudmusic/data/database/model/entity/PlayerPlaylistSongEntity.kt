package com.github.bumblebee202111.minusonecloudmusic.data.database.model.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.github.bumblebee202111.minusonecloudmusic.data.database.dao.SongDao

@Entity(tableName = "player_playlist_songs", indices = [Index(value = ["position"], unique = true)])
data class PlayerPlaylistSongEntity(
    @PrimaryKey
    val position: Int,

    @ColumnInfo("media_id")
    val mediaId: String,
    @ColumnInfo("is_local")
    val isLocal: Boolean,
    val id: Long,
)

context (SongDao)
suspend fun PlayerPlaylistSongEntity.populate(): AbstractSongEntity {
    return when (isLocal) {
        true -> localSong(id)
        false -> remoteSong(id)
    }

}

context (SongDao)
suspend fun List<PlayerPlaylistSongEntity>.populate(): List<AbstractSongEntity> {
    return asSequence()
        .groupBy(keySelector = PlayerPlaylistSongEntity::isLocal)
        .map {
            val positions = it.value.map(PlayerPlaylistSongEntity::position)
            val ids = it.value.map(PlayerPlaylistSongEntity::id)
            positions.zip(
                when (it.key) {
                    true -> localSongs(ids)
                    false -> remoteSongs(ids)
                }
            )
        }
        .flatten()
        .sortedBy(Pair<Int, AbstractSongEntity>::first)
        .map(Pair<Int, AbstractSongEntity>::second)
        .toList()
}