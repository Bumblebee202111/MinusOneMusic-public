package com.github.bumblebee202111.minusonecloudmusic.data.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.map
import com.github.bumblebee202111.minusonecloudmusic.coroutines.ApplicationScope
import com.github.bumblebee202111.minusonecloudmusic.data.Result
import com.github.bumblebee202111.minusonecloudmusic.data.database.AppDatabase
import com.github.bumblebee202111.minusonecloudmusic.data.model.DailyRecommendSong
import com.github.bumblebee202111.minusonecloudmusic.data.model.RemoteSong
import com.github.bumblebee202111.minusonecloudmusic.data.model.Video
import com.github.bumblebee202111.minusonecloudmusic.data.network.NcmEapiService
import com.github.bumblebee202111.minusonecloudmusic.data.network.model.ApiResult
import com.github.bumblebee202111.minusonecloudmusic.data.network.model.mymusic.CloudSongsApiPage
import com.github.bumblebee202111.minusonecloudmusic.data.network.model.mymusic.DailyPageApiData
import com.github.bumblebee202111.minusonecloudmusic.data.network.model.mymusic.SearchViewInfo
import com.github.bumblebee202111.minusonecloudmusic.data.network.model.mymusic.asExternalModel
import com.github.bumblebee202111.minusonecloudmusic.data.pagingsource.CloudSongsPagingSource
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LoggedInUserDataRepository @Inject constructor(
    private val appDatabase: AppDatabase,
    private val ncmEapiService: NcmEapiService,
    @ApplicationScope private val coroutineScope: CoroutineScope
) {
    fun getCloudSongs() = apiResultFlow(
        fetch = { ncmEapiService.getV1Cloud(500, 0) },
        mapSuccess = { it.data.map(CloudSongsApiPage.CloudSongData::asExternalModel) }
    )

    suspend fun getAllCloudSongs(start: Int): List<RemoteSong> {
        val songs = mutableListOf<CloudSongsApiPage.CloudSongData>()
        var offset = start
        while (true) {
            val response = ncmEapiService.getV1Cloud(500, offset)
            if (response is ApiResult.ApiSuccessResult) {
                songs += response.data.data
                offset += 500
                if (!response.data.hasMore) {
                    break
                }
            } else {
                break
            }
        }
        return songs.map { it.asExternalModel() }
    }

    fun getCloudSongsPagingData(): Pair<Flow<Result<Int>>, Flow<PagingData<RemoteSong>>> {
        val d = coroutineScope.async {
            ncmEapiService.getV1Cloud(
                CLOUD_SONG_LIST_PAGE_SIZE, 0
            )
        }
        val pd = Pager(
            PagingConfig(CLOUD_SONG_LIST_PAGE_SIZE)
        ) {
            CloudSongsPagingSource(d, CLOUD_SONG_LIST_PAGE_SIZE) { offset, limit ->
                ncmEapiService.getV1Cloud(
                    offset,
                    limit
                )
            }
        }.flow.map { it.map(CloudSongsApiPage.CloudSongData::asExternalModel) }
        return Pair(apiResultFlow(
            fetch = { d.await() },
            mapSuccess = { it.count }
        ), pd)
    }

    fun getCloudSongsPagingData1(): Pair<Flow<Result<Int>>, Flow<PagingData<RemoteSong>>> =
        apiDetailFlowWithPagingDataFlow(
            coroutineScope = coroutineScope,
            limit = CLOUD_SONG_LIST_PAGE_SIZE,
            initialFetch = { limit ->
                ncmEapiService.getV1Cloud(
                    limit = limit
                )
            },
            mapInitialFetchToResult = { this.count },
            getTotalCount = { count },
            nonInitialFetch = { limit, offset, _ ->
                ncmEapiService.getV1Cloud(limit, offset)

            },
            getPageDataFromInitialFetch = { data },
            getPageDataFromNonInitialFetch = { data },
            mapPagingValueToResult = {
                asExternalModel()
            }
        )

    fun getDailyRecommendSongs(): Flow<Result<List<DailyRecommendSong>>> =
        apiResultFlow<DailyPageApiData, List<DailyRecommendSong>>(
            fetch = { ncmEapiService.getV3DiscoveryRecommendSongs() },
            mapSuccess = DailyPageApiData::asExternalModel
        )

    fun getDailyRecommendBanner() = apiResultFlow(
        fetch = {
            ncmEapiService.getResourceExposureConfigs(
                resourcePosition = "DAILY_SONG",
                resourceId = "1",
                source = "dailyrecommend"
            )
        },
        mapSuccess = {
            it.first { resExposureConfig -> resExposureConfig.resourceType == "dailySongBanner" }.imgUrl!!
        }

    )

    fun getMyAlbums(limit: Int, offset: Int) =
        apiResultFlow(fetch = { ncmEapiService.getAlbumSublist(limit, offset) },
            mapSuccess = { result ->
                return@apiResultFlow result.data.map { it.asExternalModel() }

            })

    fun getMyMvs(limit: Int): Flow<Result<List<Video>?>> =
        apiResultFlow(fetch = { ncmEapiService.getMlogMyCollectByTime(limit) }
        ) { data ->
            data.feeds?.map(SearchViewInfo::asExternalModel) ?: emptyList()

        }


    private val myLikedSongs: MutableStateFlow<Set<Long>?> = MutableStateFlow(null)
    suspend fun refreshMyLikedSongs() {
        val starMusicIds = ncmEapiService.getStarMusicIds()
        if (starMusicIds is ApiResult.ApiSuccessResult) {
            myLikedSongs.value = starMusicIds.data.ids.toMutableSet()
        }
    }

    suspend fun clearMyLikedSongs() {
        myLikedSongs.value = emptySet()
    }

    fun observeSongLiked(songId: Long) = myLikedSongs.map { it?.contains(songId) ?: false }
    private fun updateSongLiked(songId: Long, like: Boolean) {
        val oldMyLikedSongs = myLikedSongs.value ?: return
        myLikedSongs.value = oldMyLikedSongs.toMutableSet().also {
            if (like)
                it += songId
            else {
                it -= songId
            }
        }
    }

    fun likeSong(songId: Long, like: Boolean) = apiResultFlow(
        fetch = { ncmEapiService.likeSong(like, songId) },
        mapSuccess = {
            updateSongLiked(songId, like)
            it.playlistId
        }
    )

    companion object {
        const val CLOUD_SONG_LIST_PAGE_SIZE: Int = 500
    }
}