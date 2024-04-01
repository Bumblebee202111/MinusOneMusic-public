package com.github.bumblebee202111.minusonecloudmusic.data.repository

import com.github.bumblebee202111.minusonecloudmusic.data.Result
import com.github.bumblebee202111.minusonecloudmusic.data.model.LyricsEntry
import com.github.bumblebee202111.minusonecloudmusic.data.model.VersionedSongTrackId
import com.github.bumblebee202111.minusonecloudmusic.data.network.NetworkDataSource
import com.github.bumblebee202111.minusonecloudmusic.data.network.model.music.MusicInfoApiModel
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
    fun getSongDetails(versionedSongTrackIds: List<VersionedSongTrackId>) = apiResultFlow(
        fetcher = {
            versionedSongTrackIds.size.takeIf { it >= 1000 }?.let { size ->
                throw IllegalArgumentException("Size $size exceeds API limit 1000")
            }
            val c = versionedSongTrackIds.map {
                CParamSongInfo(
                    id = it.songId,
                    version = it.version
                )
            }

            val cJsonString = moshiAdapter.toJson(c)
            networkDataSource.getSongDetails(cJsonString)
        },
        successMapper = { result ->
            result.songs.zip(result.privileges)
                .map(Pair<MusicInfoApiModel, SongPrivilegeApiModel>::asExternalModel)
        }
    )

    fun getSongUrls(vararg songIds: Long): Flow<Result<List<SongUrlInfo>?>> {
        val songIdsJson = moshiAdapter.toJson(songIds)
        return apiResultFlow(
            fetcher = { networkDataSource.getSongUrlsV1(songIdsJson) }
        ) { data ->
            data
        }
    }

    fun getLyrics(songId: Long) =
        apiResultFlow<SongLyricsApiModel, List<LyricsEntry>>(fetcher = {
            networkDataSource.getSongLyrics(
                songId
            )
        }, successMapper = SongLyricsApiModel::asExternalModel)

    fun getSongLikeCountText(songId: Long) = apiResultFlow(
        fetcher = { networkDataSource.getSongLikeCount(songId) },
        successMapper = { it.countDesc }
    )


    fun getCommentCount(songId: Long) = apiResultFlow(
        fetcher = { networkDataSource.getCommentInfoResourceList(moshiAdapter.toJson(listOf(songId))) },
        successMapper = { it[0].commentCount }
    )

}