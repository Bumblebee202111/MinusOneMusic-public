package com.github.bumblebee202111.minusonecloudmusic.data.database.model.populated

import androidx.room.ColumnInfo
import androidx.room.Relation
import com.github.bumblebee202111.minusonecloudmusic.data.database.model.entity.MusicInfoEntity
import com.github.bumblebee202111.minusonecloudmusic.data.database.model.entity.asExternalModel
import com.github.bumblebee202111.minusonecloudmusic.data.model.MyRecentMusicData

data class PopulatedMyRecentMusicData(
    @ColumnInfo("play_time")
    val playTime: Long,
    @ColumnInfo( "music_info_id")
    val musicInfoId:Long,
    @Relation(
        parentColumn = "music_info_id",
        entityColumn = "id"
    )
    val musicInfo: MusicInfoEntity
)

fun PopulatedMyRecentMusicData.asExternalModel(): MyRecentMusicData =MyRecentMusicData(playTime,musicInfo.asExternalModel())