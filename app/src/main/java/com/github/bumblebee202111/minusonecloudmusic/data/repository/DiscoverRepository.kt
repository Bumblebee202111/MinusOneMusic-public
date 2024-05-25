package com.github.bumblebee202111.minusonecloudmusic.data.repository

import com.github.bumblebee202111.minusonecloudmusic.data.datasource.NetworkDataSource
import com.github.bumblebee202111.minusonecloudmusic.data.network.model.NetworkDiscoveryBlockCursor
import com.github.bumblebee202111.minusonecloudmusic.data.network.model.NetworkDiscoveryBlockData
import com.github.bumblebee202111.minusonecloudmusic.data.network.model.asExternalModel
import com.squareup.moshi.JsonAdapter
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DiscoverRepository @Inject constructor(
    private val networkDataSource: NetworkDataSource,
    private val moshiAdapter: JsonAdapter<Any>
) {

    fun getDiscoverBlocks() = apiResultFlow(fetch = {
        networkDataSource.getHomeDiscoveryPage(
            moshiAdapter.toJson(
                NetworkDiscoveryBlockCursor(
                    blockCodeOrderList = listOf(
                        "HOMEPAGE_BLOCK_OLD_DRAGON_BALL",
                        "HOMEPAGE_BLOCK_MGC_PLAYLIST",
                        "HOMEPAGE_BLOCK_TOPLIST",
                    ),
                    offset = 0
                )
            )
        )
    },
        mapSuccess = {
            it.blocks.mapNotNull { block ->
                when (block) {
                    is NetworkDiscoveryBlockData.DiscoveryBlock.DragonBallBlock -> block.asExternalModel()
                    is NetworkDiscoveryBlockData.DiscoveryBlock.PlaylistBlock -> block.asExternalModel()
                    is NetworkDiscoveryBlockData.DiscoveryBlock.TopListBlock -> block.asExternalModel()
                    is NetworkDiscoveryBlockData.DiscoveryBlock.UnKnownBlock -> null
                }
            }
        })
}