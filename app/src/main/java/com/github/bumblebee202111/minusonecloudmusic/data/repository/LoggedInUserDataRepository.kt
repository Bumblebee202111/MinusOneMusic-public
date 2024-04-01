package com.github.bumblebee202111.minusonecloudmusic.data.repository

import com.github.bumblebee202111.minusonecloudmusic.coroutines.AppDispatchers.*
import com.github.bumblebee202111.minusonecloudmusic.coroutines.Dispatcher
import com.github.bumblebee202111.minusonecloudmusic.data.Result
import com.github.bumblebee202111.minusonecloudmusic.data.database.AppDatabase
import com.github.bumblebee202111.minusonecloudmusic.data.model.DailyRecommendSong
import com.github.bumblebee202111.minusonecloudmusic.data.model.Video
import com.github.bumblebee202111.minusonecloudmusic.data.network.NetworkDataSource
import com.github.bumblebee202111.minusonecloudmusic.data.network.model.ApiResult
import com.github.bumblebee202111.minusonecloudmusic.data.network.model.mymusic.CloudSongsApiModel
import com.github.bumblebee202111.minusonecloudmusic.data.network.model.mymusic.SearchViewInfo
import com.github.bumblebee202111.minusonecloudmusic.data.network.model.mymusic.asExternalModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LoggedInUserDataRepository @Inject constructor(
    private val appDatabase: AppDatabase,
    private val networkDataSource: NetworkDataSource,
    @Dispatcher(IO) private val ioDispatcher:CoroutineDispatcher
) {
    fun getCloudSongs() = apiResultFlow(
        fetcher = { networkDataSource.getCloudSongs(500, 0) },
        successMapper = { it.data.map(CloudSongsApiModel.CloudSongData::asExternalModel) }
    )

    fun getDailyRecommendSongs(): Flow<Result<List<DailyRecommendSong>>> = apiResultFlow(
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


    private val myLikedSongs:MutableStateFlow<Set<Long>?> = MutableStateFlow(null)
    suspend fun refreshMyLikedSongs(){
            val starMusicIds = networkDataSource.getStarMusicIds()
            if(starMusicIds is ApiResult.ApiSuccessResult){
                myLikedSongs.value =starMusicIds.data.ids.toMutableSet()
            }
    }

    fun observeSongLiked(songId:Long)=myLikedSongs.map { it?.contains(songId)?:false }
    private fun updateSongLiked(songId: Long,like: Boolean){
        val oldMyLikedSongs=myLikedSongs.value?:return
        myLikedSongs.value=oldMyLikedSongs.toMutableSet().also {
            if(like)
                it+=songId
            else{
                it-=songId
            }
        }
    }
    fun likeSong(songId: Long, like: Boolean) = apiResultFlow(
        fetcher = { networkDataSource.likeSong(like,songId) },
        successMapper = {
            updateSongLiked(songId, like)
            it.playlistId
        }
    )
}