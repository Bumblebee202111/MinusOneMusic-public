package com.github.bumblebee202111.minusonecloudmusic.data.repository

import com.github.bumblebee202111.minusonecloudmusic.data.Result
import com.github.bumblebee202111.minusonecloudmusic.data.model.MainPageBillboardRowGroup
import com.github.bumblebee202111.minusonecloudmusic.data.model.PlaylistDetail
import com.github.bumblebee202111.minusonecloudmusic.data.network.NetworkDataSource
import com.github.bumblebee202111.minusonecloudmusic.data.network.model.music.NetworkBillboardGroup
import com.github.bumblebee202111.minusonecloudmusic.data.network.model.music.asExternalModel
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PlaylistRepository @Inject constructor(
    private val networkDataSource: NetworkDataSource,
) {

    fun getPlaylistDetail(id: Long): Flow<Result<PlaylistDetail?>> = apiResultFlow(
        fetcher = { networkDataSource.getPlaylistDetail(id) },
        successMapper = {
            it.playlist.asExternalModel()
        }
    )

    fun getMyPlaylistDetail(id: Long): Flow<Result<PlaylistDetail?>> = apiResultFlow(
        fetcher = { networkDataSource.getMyPlaylistDetail(id) },
        successMapper = {
            with(it){
                Pair(playlist,privileges).asExternalModel()
            }
        }
    )

    fun getHotTracks() = NotImplementedError()


    fun getTopLists(): Flow<Result<List<MainPageBillboardRowGroup>?>> = apiResultFlow(
        fetcher = { networkDataSource.getToplistDetail() }
    ) { data: List<NetworkBillboardGroup> ->
        data.map(transform = NetworkBillboardGroup::asExternalModel)
    }


    companion object{
        const val TOP_LIST_ID:Long =3778678
    }
}

