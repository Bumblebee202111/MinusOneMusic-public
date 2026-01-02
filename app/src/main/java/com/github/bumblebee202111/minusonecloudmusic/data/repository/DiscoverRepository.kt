package com.github.bumblebee202111.minusonecloudmusic.data.repository

import com.github.bumblebee202111.minusonecloudmusic.data.network.NcmEapiService
import com.github.bumblebee202111.minusonecloudmusic.data.network.model.NetworkDiscoveryBlockCursor
import com.github.bumblebee202111.minusonecloudmusic.data.network.model.NetworkDiscoveryBlockData
import com.github.bumblebee202111.minusonecloudmusic.data.network.model.NetworkDiscoveryBlockData.DiscoveryBlock.Companion.BLOCK_CODE_MGC_PLAYLIST
import com.github.bumblebee202111.minusonecloudmusic.data.network.model.NetworkDiscoveryBlockData.DiscoveryBlock.Companion.BLOCK_CODE_OLD_DRAGON_BALL
import com.github.bumblebee202111.minusonecloudmusic.data.network.model.NetworkDiscoveryBlockData.DiscoveryBlock.Companion.BLOCK_CODE_PLAYLIST_RCMD
import com.github.bumblebee202111.minusonecloudmusic.data.network.model.NetworkDiscoveryBlockData.DiscoveryBlock.Companion.BLOCK_CODE_TOPLIST
import com.github.bumblebee202111.minusonecloudmusic.data.network.model.toDragonBalls
import com.github.bumblebee202111.minusonecloudmusic.data.network.model.toTopLists
import com.github.bumblebee202111.minusonecloudmusic.data.network.model.toPlaylists
import com.squareup.moshi.JsonAdapter
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DiscoverRepository @Inject constructor(
    private val ncmEapiService: NcmEapiService,
    private val moshiAdapter: JsonAdapter<Any>
) {

    fun getDiscoverBlocks() = apiResultFlow(fetch = {
        ncmEapiService.getHomepageBlockPage(
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
                    is NetworkDiscoveryBlockData.DiscoveryBlock.DragonBallBlock -> block.toDragonBalls()
                    is NetworkDiscoveryBlockData.DiscoveryBlock.PlaylistBlock -> {
                        when (block.blockCode) {
                            BLOCK_CODE_MGC_PLAYLIST -> {
                                val originalBlock = block.toPlaylists()
                                val mainPrivateRadar =
                                    blocks.filterIsInstance<NetworkDiscoveryBlockData.DiscoveryBlock.PlaylistBlock>()
                                        .firstOrNull {
                                            it.blockCode == BLOCK_CODE_PLAYLIST_RCMD
                                        }?.toPlaylists()?.playlists?.firstOrNull { playlist ->
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

                    is NetworkDiscoveryBlockData.DiscoveryBlock.TopListBlock -> block.toTopLists()
                    is NetworkDiscoveryBlockData.DiscoveryBlock.UnKnownBlock -> null
                }
            }
        })
}