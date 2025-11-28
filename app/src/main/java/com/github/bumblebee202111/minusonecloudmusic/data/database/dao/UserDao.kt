package com.github.bumblebee202111.minusonecloudmusic.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.github.bumblebee202111.minusonecloudmusic.data.database.model.entity.UserDetailEntity
import com.github.bumblebee202111.minusonecloudmusic.data.database.model.entity.UserPlaylistEntity
import com.github.bumblebee202111.minusonecloudmusic.data.database.model.entity.UserProfileEntity
import com.github.bumblebee202111.minusonecloudmusic.data.database.model.populated.PopulatedUserDetail
import com.github.bumblebee202111.minusonecloudmusic.data.database.model.populated.PopulatedUserPlaylist
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {

    @Query("SELECT * FROM user_profiles WHERE user_id = :userId")
    fun observeUserProfile(userId:Long): Flow<UserProfileEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUserProfile(user: UserProfileEntity)

    @Transaction
    @Query("SELECT * FROM user_details WHERE user_id = :userId")
    fun observeUserDetail(userId:Long): Flow<PopulatedUserDetail?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUserDetail(user: UserDetailEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUserPlaylists(userPlaylists:List<UserPlaylistEntity>)

    @Transaction
    @Query("SELECT * FROM user_playlists WHERE user_id = :userId")
    fun observeUserPlaylists(userId:Long): Flow<List<PopulatedUserPlaylist>>

    @Query("DELETE FROM user_playlists WHERE user_id = :userId")
    suspend fun deleteUserPlaylists(userId:Long)

}