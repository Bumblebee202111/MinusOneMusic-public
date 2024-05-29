package com.github.bumblebee202111.minusonecloudmusic.data.repository

import com.github.bumblebee202111.minusonecloudmusic.data.datasource.NetworkDataSource
import com.github.bumblebee202111.minusonecloudmusic.data.network.model.NetworkDiscoveryBlockCursor
import com.github.bumblebee202111.minusonecloudmusic.data.network.model.NetworkDiscoveryBlockData
import com.github.bumblebee202111.minusonecloudmusic.data.network.model.NetworkDiscoveryBlockData.DiscoveryBlock.Companion.BLOCK_CODE_MGC_PLAYLIST
import com.github.bumblebee202111.minusonecloudmusic.data.network.model.NetworkDiscoveryBlockData.DiscoveryBlock.Companion.BLOCK_CODE_OLD_DRAGON_BALL
import com.github.bumblebee202111.minusonecloudmusic.data.network.model.NetworkDiscoveryBlockData.DiscoveryBlock.Companion.BLOCK_CODE_PLAYLIST_RCMD
import com.github.bumblebee202111.minusonecloudmusic.data.network.model.NetworkDiscoveryBlockData.DiscoveryBlock.Companion.BLOCK_CODE_TOPLIST
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
                        BLOCK_CODE_OLD_DRAGON_BALL,
                        BLOCK_CODE_MGC_PLAYLIST,
                        BLOCK_CODE_PLAYLIST_RCMD,
                        BLOCK_CODE_TOPLIST,
                    ),
                    offset = 0
                )
            )
        )
    },
        mapSuccess = { blockData ->
            val blocks = blockData.blocks
            blocks.mapNotNull { block ->
                when (block) {
                    is NetworkDiscoveryBlockData.DiscoveryBlock.DragonBallBlock -> block.asExternalModel()
                    is NetworkDiscoveryBlockData.DiscoveryBlock.PlaylistBlock -> {
                        when (block.blockCode) {
                            BLOCK_CODE_MGC_PLAYLIST -> {
                                val originalBlock = block.asExternalModel()
                                val mainPrivateRadar =
                                    blocks.filterIsInstance<NetworkDiscoveryBlockData.DiscoveryBlock.PlaylistBlock>()
                                        .firstOrNull {
                                            it.blockCode == BLOCK_CODE_PLAYLIST_RCMD
                                        }?.asExternalModel()?.playlists?.firstOrNull { playlist ->
                                            playlist.title.contains(
                                                NetworkDiscoveryBlockData.DiscoveryBlock.PlaylistBlock.KEYWORD_MAIN_PRIVATE_RADAR
                                            )
                                        }
                                if (mainPrivateRadar == null) originalBlock else
                                    originalBlock.copy(
                                        title = originalBlock.title,
                                        playlists = mutableListOf(mainPrivateRadar) + originalBlock.playlists
                                    )
                            }

                            else -> {
                                null
                            }
                        }
                    }

                    is NetworkDiscoveryBlockData.DiscoveryBlock.TopListBlock -> block.asExternalModel()
                    is NetworkDiscoveryBlockData.DiscoveryBlock.UnKnownBlock -> null
                }
            }
        })
}