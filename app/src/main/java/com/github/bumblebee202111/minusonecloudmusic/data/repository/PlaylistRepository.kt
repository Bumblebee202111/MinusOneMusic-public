package com.github.bumblebee202111.minusonecloudmusic.data.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.map
import androidx.room.withTransaction
import com.github.bumblebee202111.minusonecloudmusic.coroutines.ApplicationScope
import com.github.bumblebee202111.minusonecloudmusic.data.Result
import com.github.bumblebee202111.minusonecloudmusic.data.database.AppDatabase
import com.github.bumblebee202111.minusonecloudmusic.data.database.model.entity.PlayerPlaylistSongEntity
import com.github.bumblebee202111.minusonecloudmusic.data.database.model.entity.asExternalModel
import com.github.bumblebee202111.minusonecloudmusic.data.model.AbstractRemoteSong
import com.github.bumblebee202111.minusonecloudmusic.data.model.MainPageBillboardRowGroup
import com.github.bumblebee202111.minusonecloudmusic.data.model.PlaylistDetail
import com.github.bumblebee202111.minusonecloudmusic.data.model.RemoteSong
import com.github.bumblebee202111.minusonecloudmusic.data.model.SongIdAndVersion
import com.github.bumblebee202111.minusonecloudmusic.data.model.asEntity
import com.github.bumblebee202111.minusonecloudmusic.data.network.NetworkDataSource
import com.github.bumblebee202111.minusonecloudmusic.data.network.model.ApiResult
import com.github.bumblebee202111.minusonecloudmusic.data.network.model.music.NetworkBillboardGroup
import com.github.bumblebee202111.minusonecloudmusic.data.network.model.music.SongDetailsApiModel
import com.github.bumblebee202111.minusonecloudmusic.data.network.model.music.asExternalModel
import com.github.bumblebee202111.minusonecloudmusic.data.network.requestparam.CParamSongInfo
import com.squareup.moshi.JsonAdapter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PlaylistRepository @Inject constructor(
    private val networkDataSource: NetworkDataSource,
    private val appDatabase: AppDatabase,
    private val moshiAdapter: JsonAdapter<Any>,
    @ApplicationScope private val coroutineScope: CoroutineScope
) {
    private val musicInfoDao = appDatabase.musicInfoDao()
    private val playerDao = appDatabase.playerDao()


    fun getPlaylistDetail(id: Long): Flow<Result<PlaylistDetail?>> = apiResultFlow(
        fetch = { networkDataSource.getPlaylistV4Detail(id) },
        mapSuccess = {
            it.playlist.asExternalModel()
        }
    )

    fun getMyPlaylistDetail(id: Long): Flow<Result<PlaylistDetail?>> = apiResultFlow(
        fetch = { networkDataSource.getV6PlaylistDetail(id) },
        mapSuccess = {
            with(it) {
                Pair(playlist, privileges).asExternalModel()
            }
        }
    )

    fun getPlaylistDetailAndPagingData(playlistId: Long): Pair<Flow<Result<PlaylistDetail>>, Flow<PagingData<RemoteSong>>> {
        return apiDetailFlowWithPagingDataFlow(
            coroutineScope = coroutineScope,
            getTotalCount = { playlist.trackCount + playlist.cloudTrackCount },
            initialFetch = { networkDataSource.getPlaylistV4Detail(playlistId) },
            nonInitialFetch = { limit, offset, precondition ->
                val songIdsAndVersions = precondition.playlist.trackIds.drop(offset).take(limit)
                    .map { SongIdAndVersion(it.id, 0) }
                fetchPlaylistSongDetailsFromNetwork(songIdsAndVersions)
            },
            mapInitialFetchToResult = { playlist.asExternalModel() },
            limit = PLAYLIST_PAGE_SIZE,
            getPageDataFromInitialFetch = { playlist.tracks },
            getPageDataFromNonInitialFetch = { songs },
            mapPagingValueToResult = { Pair(this, null).asExternalModel() }
        )
    }

    fun getMyPlaylistDetailAndPagingData(playlistId: Long): Pair<Flow<Result<PlaylistDetail>>, Flow<PagingData<RemoteSong>>> {
        return apiDetailFlowWithPagingDataFlow(
            coroutineScope = coroutineScope,
            getTotalCount = { playlist.trackCount + playlist.cloudTrackCount },
            initialFetch = { networkDataSource.getV6PlaylistDetail(playlistId) },
            nonInitialFetch = { limit, offset, precondition ->
                val songIdsAndVersions = precondition.playlist.trackIds.drop(offset).take(limit)
                    .map { SongIdAndVersion(it.id, 0) }
                fetchPlaylistSongDetailsFromNetwork(songIdsAndVersions)
            },
            mapInitialFetchToResult = { playlist.asExternalModel() },
            limit = PLAYLIST_PAGE_SIZE,
            getPageDataFromInitialFetch = { playlist.tracks },
            getPageDataFromNonInitialFetch = { songs },
            mapPagingValueToResult = { Pair(this, null).asExternalModel() },
        )
    }

    private suspend fun fetchPlaylistSongDetailsFromNetwork(songIds: List<SongIdAndVersion>): ApiResult<SongDetailsApiModel> {
        return networkDataSource.getSongDetails(moshiAdapter.toJson(songIds.map {
            CParamSongInfo(
                id = it.songId,
                version = 0
            )
        }))
    }


    fun getHotTracks() = NotImplementedError()


    fun getTopLists(): Flow<Result<List<MainPageBillboardRowGroup>?>> = apiResultFlow(
        fetch = { networkDataSource.getToplistDetail() }
    ) { data: List<NetworkBillboardGroup> ->
        data.map(transform = NetworkBillboardGroup::asExternalModel)
    }


    suspend fun clearPlayerPlaylist() {
        appDatabase.withTransaction {
            playerDao.deleteAllPlaylistSongs()
        }
    }

    
    suspend fun addSongsToPlayerPlaylist(songs: List<AbstractRemoteSong>) {
        appDatabase.withTransaction {
            val playlistSize = playerDao.getPlaylistSize()
            playerDao.insertPlaylistSongs(songs.mapIndexed { index, song ->
                PlayerPlaylistSongEntity(index + playlistSize, song.id)
            })
            musicInfoDao.insertMusicInfos(songs.map(AbstractRemoteSong::asEntity))
        }
    }

    fun getPlayerPlaylistPagingData(): Flow<PagingData<RemoteSong>> {
        val pagingConfig = PagingConfig(100)
        return Pager(config = pagingConfig) {
            playerDao.populatedPlaylistSongs()
        }.flow.map { populatedList -> populatedList.map { it.asExternalModel() } }
    }

    suspend fun getPlayerPlaylistSongPosition(songId: Long): Int {
        return playerDao.getPlaylistSongPosition(songId)
    }


    companion object {
        const val TOP_LIST_ID: Long = 3778678
        const val PLAYLIST_PAGE_SIZE: Int = 1000
        private val REMOTE_PLAYLIST_PAGING_CONFIG = PagingConfig(
            pageSize = PLAYLIST_PAGE_SIZE,
            prefetchDistance = PLAYLIST_PAGE_SIZE / 10,
            initialLoadSize = PLAYLIST_PAGE_SIZE
        )
    }
}

