package com.github.bumblebee202111.minusonecloudmusic.data.repository

import com.github.bumblebee202111.minusonecloudmusic.data.Result
import com.github.bumblebee202111.minusonecloudmusic.data.model.LyricsEntry
import com.github.bumblebee202111.minusonecloudmusic.data.model.RemoteSong
import com.github.bumblebee202111.minusonecloudmusic.data.model.SongIdAndVersion
import com.github.bumblebee202111.minusonecloudmusic.data.network.NetworkDataSource
import com.github.bumblebee202111.minusonecloudmusic.data.network.model.ApiResult
import com.github.bumblebee202111.minusonecloudmusic.data.network.model.music.MusicInfoApiModel
import com.github.bumblebee202111.minusonecloudmusic.data.network.model.music.SongDetailsApiModel
import com.github.bumblebee202111.minusonecloudmusic.data.network.model.music.SongLyricsApiModel
import com.github.bumblebee202111.minusonecloudmusic.data.network.model.music.SongPrivilegeApiModel
import com.github.bumblebee202111.minusonecloudmusic.data.network.model.music.asExternalModel
import com.github.bumblebee202111.minusonecloudmusic.data.network.model.user.SongUrlInfo
import com.github.bumblebee202111.minusonecloudmusic.data.network.requestparam.CParamSongInfo
import com.squareup.moshi.JsonAdapter
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SongRepository @Inject constructor(
    private val networkDataSource: NetworkDataSource,
    private val moshiAdapter: JsonAdapter<Any>
) {

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
            networkDataSource.getSongDetails(cJson)
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
            val result = networkDataSource.getSongDetails(cParam)
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
            fetch = { networkDataSource.getSongUrlsV1(songIdsJson) }
        ) { data ->
            data
        }
    }

    fun getLyrics(songId: Long) =
        apiResultFlow<SongLyricsApiModel, List<LyricsEntry>>(fetch = {
            networkDataSource.getSongLyrics(
                songId
            )
        }, mapSuccess = SongLyricsApiModel::asExternalModel)

    fun getSongLikeCountText(songId: Long) = apiResultFlow(
        fetch = { networkDataSource.getSongLikeCount(songId) },
        mapSuccess = { it.countDesc }
    )


    fun getCommentCount(songId: Long) = apiResultFlow(
        fetch = { networkDataSource.getCommentInfoResourceList(moshiAdapter.toJson(listOf(songId))) },
        mapSuccess = { it[0].commentCount }
    )

}