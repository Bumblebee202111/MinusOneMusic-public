package com.github.bumblebee202111.minusonecloudmusic.data.database.model.entity

sealed interface AbstractSongEntity

fun AbstractSongEntity.asExternalModel() = when (this) {
    is RemoteSongEntity -> asExternalModel()
    is LocalSongEntity -> asExternalModel()
}