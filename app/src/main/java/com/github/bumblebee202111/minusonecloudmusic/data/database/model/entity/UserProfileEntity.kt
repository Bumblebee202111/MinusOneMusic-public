package com.github.bumblebee202111.minusonecloudmusic.data.database.model.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.github.bumblebee202111.minusonecloudmusic.data.model.UserProfile

@Entity("user_profiles")
class UserProfileEntity(
    @PrimaryKey
    @ColumnInfo("user_id")
    val userId: Long,
    @ColumnInfo("display_name")
    val displayName: String,
    @ColumnInfo("avatar_url")
    val avatarUrl:String,
    @ColumnInfo("background_url")
    val backgroundUrl:String,
)
fun UserProfileEntity.asExternalModel(): UserProfile = UserProfile(userId, displayName, avatarUrl,backgroundUrl)