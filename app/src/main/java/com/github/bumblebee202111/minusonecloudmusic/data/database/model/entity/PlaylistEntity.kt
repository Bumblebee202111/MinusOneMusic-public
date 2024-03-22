package com.github.bumblebee202111.minusonecloudmusic.data.database.model.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.github.bumblebee202111.minusonecloudmusic.data.model.Playlist
import com.github.bumblebee202111.minusonecloudmusic.data.model.SpecialType

@Entity("playlists")
data class PlaylistEntity(
    @PrimaryKey
    val id: Long,
    val name: String,
    @ColumnInfo("creator_id")
    val creatorId: Long?,
    @ColumnInfo("cover_image_url")
    val coverImgUrl: String,
    @ColumnInfo("is_private")
    val isPrivate: Boolean,
    @ColumnInfo("track_count")
    val trackCount: Int,
    @ColumnInfo("play_count")
    val playCount: Long,
    @ColumnInfo("special_type")
    val specialType: SpecialType,
    @ColumnInfo("top_list_type")
    val topListType: String?,
    @ColumnInfo("update_frequency")
    val updateFrequency:String?
)

fun PlaylistEntity.asExternalModel():Playlist = Playlist(
    id = id,
    name = name,
    creatorId = creatorId,
    coverImgUrl = coverImgUrl,
    isPrivate = isPrivate,
    trackCount = trackCount,
    playCount = playCount,
    specialType=specialType,
    topListType = topListType,
    updateFrequency=updateFrequency
)