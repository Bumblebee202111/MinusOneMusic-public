package com.github.bumblebee202111.minusonecloudmusic.data.database.model.populated

import androidx.room.Embedded
import androidx.room.Relation
import com.github.bumblebee202111.minusonecloudmusic.data.database.model.entity.UserDetailEntity
import com.github.bumblebee202111.minusonecloudmusic.data.database.model.entity.UserProfileEntity
import com.github.bumblebee202111.minusonecloudmusic.data.database.model.entity.asExternalModel
import com.github.bumblebee202111.minusonecloudmusic.data.model.UserDetail

data class PopulatedUserDetail(
    @Embedded
    val entity: UserDetailEntity,
    @Relation(
        parentColumn = "user_id",
        entityColumn = "user_id"
    )
    val userProfile: UserProfileEntity
)

fun PopulatedUserDetail.asExternalModel(): UserDetail =UserDetail(entity.listenSongs,userProfile.asExternalModel())