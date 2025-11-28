package com.github.bumblebee202111.minusonecloudmusic.data.database.model.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity("user_details")
class UserDetailEntity(
    @PrimaryKey
    @ColumnInfo("user_id")
    val userId: Long,
    @ColumnInfo("listen_songs")
    val listenSongs:Long,
)