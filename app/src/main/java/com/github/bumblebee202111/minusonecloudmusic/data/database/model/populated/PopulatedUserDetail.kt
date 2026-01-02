package com.github.bumblebee202111.minusonecloudmusic.data.database.model.populated

import androidx.room.Embedded
import androidx.room.Relation
import com.github.bumblebee202111.minusonecloudmusic.data.database.model.entity.UserDetailEntity
import com.github.bumblebee202111.minusonecloudmusic.data.database.model.entity.UserProfileEntity
import com.github.bumblebee202111.minusonecloudmusic.data.database.model.entity.toUserProfile
import com.github.bumblebee202111.minusonecloudmusic.model.UserDetail

data class PopulatedUserDetail(
    @Embedded
    val entity: UserDetailEntity,
    @Relation(
        parentColumn = "user_id",
        entityColumn = "user_id"
    )
    val userProfile: UserProfileEntity
)

fun PopulatedUserDetail.toUserDetail(): UserDetail =
    UserDetail(entity.listenSongs, userProfile.toUserProfile())