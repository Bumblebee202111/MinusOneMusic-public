package com.github.bumblebee202111.minusonecloudmusic.data.repository

import com.github.bumblebee202111.minusonecloudmusic.data.Result
import com.github.bumblebee202111.minusonecloudmusic.data.database.AppDatabase
import com.github.bumblebee202111.minusonecloudmusic.data.model.Video
import com.github.bumblebee202111.minusonecloudmusic.data.network.NetworkDataSource
import com.github.bumblebee202111.minusonecloudmusic.data.network.model.mymusic.CloudSongsApiModel
import com.github.bumblebee202111.minusonecloudmusic.data.network.model.mymusic.SearchViewInfo
import com.github.bumblebee202111.minusonecloudmusic.data.network.model.mymusic.asExternalModel
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LoggedInUserDataRepository @Inject constructor(
    private val appDatabase: AppDatabase,
    private val networkDataSource: NetworkDataSource
) {
    fun getCloudSongs() = apiResultFlow(
        fetcher = { networkDataSource.getCloudSongs(500, 0) },
        successMapper = { it.data.map(CloudSongsApiModel.CloudSongData::asExternalModel) }
    )

    fun getDailyRecommendSongs() = apiResultFlow(
        fetcher = { networkDataSource.getRecommendSongs() }
    ) { data ->
        data.asExternalModel()
    }

    fun getMyAlbums(limit: Int, offset: Int) =
        apiResultFlow(fetcher = { networkDataSource.getAlbumSublist(limit, offset) },
            successMapper = { result ->
                return@apiResultFlow result.data.map { it.asExternalModel() }

            })

    fun getMyMvs(limit: Int): Flow<Result<List<Video>?>> =
        apiResultFlow(fetcher = { networkDataSource.getMlogMyCollectByTime(limit) }
        ) { data ->
            data.feeds?.map(SearchViewInfo::asExternalModel) ?: emptyList()

        }

}