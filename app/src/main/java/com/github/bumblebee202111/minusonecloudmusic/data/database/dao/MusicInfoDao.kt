package com.github.bumblebee202111.minusonecloudmusic.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.github.bumblebee202111.minusonecloudmusic.data.database.model.entity.MusicInfoEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MusicInfoDao {

    @Insert(entity = MusicInfoEntity::class, onConflict = OnConflictStrategy.REPLACE)
    fun insertMusicInfos(musicInfos:List<MusicInfoEntity>)

    @Query("SELECT * FROM MusicInfoEntity WHERE id IN (:ids)")
    fun observeMusicInfos(ids:List<Long>): Flow<List<MusicInfoEntity>>
}