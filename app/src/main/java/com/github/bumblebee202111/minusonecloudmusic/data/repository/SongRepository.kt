package com.github.bumblebee202111.minusonecloudmusic.data.repository

import com.github.bumblebee202111.minusonecloudmusic.data.AppResult
import com.github.bumblebee202111.minusonecloudmusic.data.database.AppDatabase
import com.github.bumblebee202111.minusonecloudmusic.data.datasource.MediaStoreDataSource
import com.github.bumblebee202111.minusonecloudmusic.data.datasource.SongDownloadDataSource
import com.github.bumblebee202111.minusonecloudmusic.model.LocalSong
import com.github.bumblebee202111.minusonecloudmusic.model.LyricsEntry
import com.github.bumblebee202111.minusonecloudmusic.model.RemoteSong
import com.github.bumblebee202111.minusonecloudmusic.model.SongIdAndVersion
import com.github.bumblebee202111.minusonecloudmusic.data.network.NcmEapiService
import com.github.bumblebee202111.minusonecloudmusic.data.network.model.ApiResult
import com.github.bumblebee202111.minusonecloudmusic.data.network.model.music.MusicInfoApiModel
import com.github.bumblebee202111.minusonecloudmusic.data.network.model.music.SongLyricsApiModel
import com.github.bumblebee202111.minusonecloudmusic.data.network.model.music.SongPrivilegeApiModel
import com.github.bumblebee202111.minusonecloudmusic.data.network.model.music.toSongDownloadInfo
import com.github.bumblebee202111.minusonecloudmusic.data.network.model.music.toLyricsEntries
import com.github.bumblebee202111.minusonecloudmusic.data.network.model.music.toCommentInfo
import com.github.bumblebee202111.minusonecloudmusic.data.network.model.music.toRemoteSong
import com.github.bumblebee202111.minusonecloudmusic.data.network.model.music.toSongPrivilegePartialEntity
import com.github.bumblebee202111.minusonecloudmusic.data.network.model.resource.NetworkComment
import com.github.bumblebee202111.minusonecloudmusic.data.network.model.resource.toComment
import com.github.bumblebee202111.minusonecloudmusic.data.network.model.user.SongUrlInfo
import com.github.bumblebee202111.minusonecloudmusic.data.network.requestparam.CParamSongInfo
import com.squareup.moshi.JsonAdapter
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SongRepository @Inject constructor(
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
                .map(Pair<MusicInfoApiModel, SongPrivilegeApiModel>::toRemoteSong)
        }
    )

    fun getSongDetails(songIds: List<Long>): Flow<AppResult<List<RemoteSong>>> = apiResultFlow(
        fetch = {
            val songs = mutableListOf<Pair<MusicInfoApiModel, SongPrivilegeApiModel>>()
            var lastSuccessCode = 200
            for (chunk in songIds.chunked(1000)) {
                val cParam = moshiAdapter.toJson(chunk.map { CParamSongInfo(it, 0) })
                when (val result = ncmEapiService.getSongDetails(cParam)) {
                    is ApiResult.Success -> {
                        songs += result.data.songs.zip(result.data.privileges)
                        lastSuccessCode = result.code
                    }

                    is ApiResult.Error -> {
                        return@apiResultFlow ApiResult.Error(
                            result.code,
                            result.message
                        )
                    }

                    is ApiResult.SuccessEmpty -> Unit
                }
            }
            ApiResult.Success(songs, lastSuccessCode)
        },
        mapSuccess = { songPairs ->
            songPairs.map(Pair<MusicInfoApiModel, SongPrivilegeApiModel>::toRemoteSong)
        }
    )

    fun getSongUrls(vararg songIds: Long): Flow<AppResult<List<SongUrlInfo>?>> {
        val songIdsJson = moshiAdapter.toJson(songIds)
        return apiResultFlow(
            fetch = { ncmEapiService.getSongUrlsV1(songIdsJson) }
        ) { data ->
            data
        }
    }

    fun refreshUserRemoteSongs(songIds: List<Long>): Flow<AppResult<Unit>> = apiResultFlow(
        fetch = {
            val privileges = mutableListOf<SongPrivilegeApiModel>()
            var lastSuccessCode = 200
            for (chunk in songIds.chunked(1000)) {
                val songIdsString = moshiAdapter.toJson(chunk)
                when (val result = ncmEapiService.getSongEnhancePrivilege(songIdsString)) {
                    is ApiResult.Success -> {
                        privileges.addAll(result.data)
                        lastSuccessCode = result.code
                    }

                    is ApiResult.Error -> {
                        return@apiResultFlow result
                    }

                    is ApiResult.SuccessEmpty -> Unit
                }
            }
            ApiResult.Success(privileges, lastSuccessCode)
        },
        mapSuccess = { privileges ->
            val privilegeEntities = privileges.map(SongPrivilegeApiModel::toSongPrivilegePartialEntity)
            songDao.upsertPrivileges(privilegeEntities)
        }
    )

    fun getLyrics(songId: Long) =
        apiResultFlow<SongLyricsApiModel, List<LyricsEntry>>(fetch = {
            ncmEapiService.getSongLyrics(
                songId
            )
        }, mapSuccess = SongLyricsApiModel::toLyricsEntries)

    fun getSongLikeCountText(songId: Long) = apiResultFlow(
        fetch = { ncmEapiService.getSongRedCount(songId) },
        mapSuccess = { it.countDesc }
    )


    fun getCommentInfo(songId: Long) = apiResultFlow(
        fetch = { ncmEapiService.getCommentInfoResourceList(moshiAdapter.toJson(listOf(songId))) },
        mapSuccess = { it[0].toCommentInfo() }
    )

    fun getLocalSongs(): List<LocalSong> {
        return mediaStoreDataSource.getMusicResources()
    }

    fun download(song: RemoteSong) = apiResultFlow(
        fetch = { ncmEapiService.getSongEnhanceDownloadUrlV1(id = song.id) },
        mapSuccess = { result ->
            val url = result.toSongDownloadInfo()
            mediaDownloadDataSource.download(song, url) }
    )

    fun getComments(threadId: String) = apiResultFlow(
        fetch = {
            ncmEapiService.getV2ResourceComments(threadId)
        },
        mapSuccess = {
            it.comments.map(NetworkComment::toComment)
        }
    )

}