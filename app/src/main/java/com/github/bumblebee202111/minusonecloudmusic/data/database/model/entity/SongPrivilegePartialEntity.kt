package com.github.bumblebee202111.minusonecloudmusic.data.database.model.entity

import androidx.room.ColumnInfo

data class SongPrivilegePartialEntity(
    @ColumnInfo("id")
    val songId: Long,
    @ColumnInfo("is_downloadable")
    val isDownloadable: Boolean
)