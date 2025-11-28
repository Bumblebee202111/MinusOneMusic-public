package com.github.bumblebee202111.minusonecloudmusic.data.database.model.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class MyRecentMusicDataEntity(
    @ColumnInfo("play_time")
    val playTime: Long,
    @PrimaryKey
    @ColumnInfo("music_info_id")
    val musicInfoId:Long
)
