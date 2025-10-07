package com.github.bumblebee202111.minusonecloudmusic.data.repository

import com.github.bumblebee202111.minusonecloudmusic.coroutines.ApplicationScope
import com.github.bumblebee202111.minusonecloudmusic.data.Result
import com.github.bumblebee202111.minusonecloudmusic.data.database.AppDatabase
import com.github.bumblebee202111.minusonecloudmusic.data.datasource.MediaStoreDataSource
import com.github.bumblebee202111.minusonecloudmusic.data.datasource.SongDownloadDataSource
import com.github.bumblebee202111.minusonecloudmusic.data.model.LocalSong
import com.github.bumblebee202111.minusonecloudmusic.data.model.LyricsEntry
import com.github.bumblebee202111.minusonecloudmusic.data.model.RemoteSong
import com.github.bumblebee202111.minusonecloudmusic.data.model.SongIdAndVersion
import com.github.bumblebee202111.minusonecloudmusic.data.network.NcmEapiService
import com.github.bumblebee202111.minusonecloudmusic.data.network.model.ApiResult
import com.github.bumblebee202111.minusonecloudmusic.data.network.model.music.MusicInfoApiModel
import com.github.bumblebee202111.minusonecloudmusic.data.network.model.music.SongLyricsApiModel
import com.github.bumblebee202111.minusonecloudmusic.data.network.model.music.SongPrivilegeApiModel
import com.github.bumblebee202111.minusonecloudmusic.data.network.model.music.asExternalModel
import com.github.bumblebee202111.minusonecloudmusic.data.network.model.music.toEntity
import com.github.bumblebee202111.minusonecloudmusic.data.network.model.resource.NetworkComment
import com.github.bumblebee202111.minusonecloudmusic.data.network.model.resource.asExternalModel
import com.github.bumblebee202111.minusonecloudmusic.data.network.model.user.SongUrlInfo
import com.github.bumblebee202111.minusonecloudmusic.data.network.requestparam.CParamSongInfo
import com.squareup.moshi.JsonAdapter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SongRepository @Inject constructor(
    @ApplicationScope private val coroutineScope: CoroutineScope,
    private val ncmEapiService: NcmEapiService,
    private val moshiAdapter: JsonAdapter<Any>,
    private val mediaStoreDataSource: MediaStoreDataSource,
    private val mediaDownloadDataSource: SongDownloadDataSource,
    appDatabase: AppDatabase
) {
    private val songDao = appDatabase.songDao()


    @Suppress("unused")
    fun observeSongDetails(songIdAndVersions: List<SongIdAndVersion>) = apiResultFlow(
        fetch = {
            songIdAndVersions.size.takeIf { it >= 1000 }?.let { size ->
                throw IllegalArgumentException("Size $size exceeds API limit 1000")
            }
            val c = songIdAndVersions.map {
                CParamSongInfo(
                    id = it.songId,
                    version = it.version
                )
            }

            val cJson = moshiAdapter.toJson(c)
            ncmEapiService.getSongDetails(cJson)
        },
        mapSuccess = { result ->
            result.songs.zip(result.privileges)
                .map(Pair<MusicInfoApiModel, SongPrivilegeApiModel>::asExternalModel)
        }
    )

    suspend fun getSongDetails(songIds: List<Long>): List<RemoteSong> {
        val songs = mutableListOf<Pair<MusicInfoApiModel, SongPrivilegeApiModel>>()
        for (chunk in songIds.chunked(1000)) {
            val cParam = moshiAdapter.toJson(chunk.map { CParamSongInfo(it, 0) })
            val result = ncmEapiService.getSongDetails(cParam)
            if (result is ApiResult.ApiSuccessResult) {
                songs += result.data.songs.zip(result.data.privileges)
            } else {
                break
            }
        }
        return songs.map(Pair<MusicInfoApiModel, SongPrivilegeApiModel>::asExternalModel)
    }

    fun getSongUrls(vararg songIds: Long): Flow<Result<List<SongUrlInfo>?>> {
        val songIdsJson = moshiAdapter.toJson(songIds)
        return apiResultFlow(
            fetch = { ncmEapiService.getSongUrlsV1(songIdsJson) }
        ) { data ->
            data
        }
    }

    suspend fun refreshUserRemoteSongs(songIds: List<Long>): kotlin.Result<Unit> {
        for (chunk in songIds.chunked(1000)) {
            val songIdsString = moshiAdapter.toJson(chunk)
            when (val result = ncmEapiService.getSongEnhancePrivilege(songIdsString)) {
                is ApiResult.ApiSuccessResult -> {
                    songDao.upsertPrivileges(result.data.map(SongPrivilegeApiModel::toEntity))
                }

                is ApiResult.ApiErrorResult -> {
                    return kotlin.Result.failure(Exception(result.message))
                }
            }
        }
        return kotlin.Result.success(Unit)
    }

    fun getLyrics(songId: Long) =
        apiResultFlow<SongLyricsApiModel, List<LyricsEntry>>(fetch = {
            ncmEapiService.getSongLyrics(
                songId
            )
        }, mapSuccess = SongLyricsApiModel::asExternalModel)

    fun getSongLikeCountText(songId: Long) = apiResultFlow(
        fetch = { ncmEapiService.getSongRedCount(songId) },
        mapSuccess = { it.countDesc }
    )


    fun getCommentInfo(songId: Long) = apiResultFlow(
        fetch = { ncmEapiService.getCommentInfoResourceList(moshiAdapter.toJson(listOf(songId))) },
        mapSuccess = { it[0].asExternalModel() }
    )

    fun getLocalSongs(): List<LocalSong> {
        return mediaStoreDataSource.getMusicResources()
    }

    fun download(song: RemoteSong) {
        coroutineScope.launch {
            val apiResult = ncmEapiService.getSongEnhanceDownloadUrlV1(id = song.id)
            if (apiResult is ApiResult.ApiSuccessResult) {
                val url = apiResult.data.asExternalModel()
                mediaDownloadDataSource.download(song, url)
            }
        }
    }

    fun getComments(threadId: String) = apiResultFlow(
        fetch = {
            ncmEapiService.getV2ResourceComments(threadId)
        },
        mapSuccess = {
            it.comments.map(NetworkComment::asExternalModel)
        }
    )

}