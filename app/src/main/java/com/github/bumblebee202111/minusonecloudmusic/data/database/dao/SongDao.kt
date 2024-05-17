package com.github.bumblebee202111.minusonecloudmusic.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.github.bumblebee202111.minusonecloudmusic.data.database.model.entity.LocalSongEntity
import com.github.bumblebee202111.minusonecloudmusic.data.database.model.entity.RemoteSongEntity
import com.github.bumblebee202111.minusonecloudmusic.data.database.model.entity.SongPrivilegePartialEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SongDao {

    @Insert(entity = RemoteSongEntity::class, onConflict = OnConflictStrategy.REPLACE)
    fun insertRemoteSongs(songs: List<RemoteSongEntity>)

    @Insert(entity = LocalSongEntity::class, onConflict = OnConflictStrategy.REPLACE)
    fun insertLocalSongs(songs: List<LocalSongEntity>)

    @Query("SELECT * FROM RemoteSongEntity WHERE id IN (:ids)")
    fun observeRemoteSongs(ids: List<Long>): Flow<List<RemoteSongEntity>>

    @Update(entity = RemoteSongEntity::class)
    suspend fun upsertPrivileges(userSongs: List<SongPrivilegePartialEntity>)

}