package com.github.bumblebee202111.minusonecloudmusic.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.github.bumblebee202111.minusonecloudmusic.data.database.model.entity.MyRecentMusicDataEntity
import com.github.bumblebee202111.minusonecloudmusic.data.database.model.populated.PopulatedMyRecentMusicData
import kotlinx.coroutines.flow.Flow

@Dao
interface RecentPlayDao {

    @Transaction
    @Query("SELECT * FROM MyRecentMusicDataEntity ORDER BY play_time DESC")
    fun observeMyRecentPlayMusic(): Flow<List<PopulatedMyRecentMusicData>>

    @Insert(entity = MyRecentMusicDataEntity::class, onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMyRecentPlayMusic(myRecentMusicData: List<MyRecentMusicDataEntity>)

    @Query("DELETE FROM MyRecentMusicDataEntity")
    suspend fun deleteAllMyRecentPlayMusic()

    @Transaction
    suspend fun deleteAndInsertRecentPlayMusic(myRecentMusicList: List<MyRecentMusicDataEntity>) {
        deleteAllMyRecentPlayMusic()
        insertMyRecentPlayMusic(myRecentMusicList)
    }
}