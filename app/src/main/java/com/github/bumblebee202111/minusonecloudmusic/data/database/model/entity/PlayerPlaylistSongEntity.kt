package com.github.bumblebee202111.minusonecloudmusic.data.database.model.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.github.bumblebee202111.minusonecloudmusic.data.database.dao.SongDao
import kotlin.collections.List
import kotlin.collections.chunked
import kotlin.collections.flatMap
import kotlin.collections.forEach
import kotlin.collections.groupBy
import kotlin.collections.map
import kotlin.collections.mutableMapOf
import kotlin.collections.set
import kotlin.collections.sortedBy

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

    val idPositions = mutableMapOf<Pair<Boolean, Long>, Int>()
    forEach {
        with(it) {
            idPositions[Pair(isLocal, id)] = position
        }
    }
    return groupBy(keySelector = PlayerPlaylistSongEntity::isLocal)
        .flatMap { entry ->
            entry.value.chunked(999).flatMap { chunked ->
                val ids = chunked.map(PlayerPlaylistSongEntity::id)
                when (entry.key) {
                    true -> localSongs(ids)
                    false -> remoteSongs(ids)
                }
            }
        }
        .sortedBy {
            with(it) {
                idPositions[
                    when (this) {
                        is LocalSongEntity -> Pair(true, id)
                        is RemoteSongEntity -> Pair(false, id)
                    }
                ]
            }
        }
}