package com.github.bumblebee202111.minusonecloudmusic.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import com.github.bumblebee202111.minusonecloudmusic.data.database.model.entity.MusicInfoEntity

@Dao
interface MusicInfoDao {

    @Insert(entity = MusicInfoEntity::class, onConflict = OnConflictStrategy.REPLACE)
    fun insertMusicInfos(musicInfos:List<MusicInfoEntity>)
}