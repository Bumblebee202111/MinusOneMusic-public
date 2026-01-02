package com.github.bumblebee202111.minusonecloudmusic.data.repository

import com.github.bumblebee202111.minusonecloudmusic.data.network.NcmEapiService
import com.github.bumblebee202111.minusonecloudmusic.data.network.model.search.NetworkSearchComplexSongBlock
import com.github.bumblebee202111.minusonecloudmusic.data.network.model.search.SearchComplexCursorParam
import com.github.bumblebee202111.minusonecloudmusic.data.network.model.search.toRemoteSong
import com.squareup.moshi.JsonAdapter
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SearchRepository @Inject constructor(
    private val ncmEapiService: NcmEapiService,
    private val moshiAdapter: JsonAdapter<Any>
) {
    fun searchComplex(keyword: String) = apiResultFlow(
        fetch = {
            ncmEapiService.searchComplex(
                keyword,
                moshiAdapter.toJson(SearchComplexCursorParam(0))
            )
        },
        mapSuccess = { data ->
            data.blocks.filterIsInstance<NetworkSearchComplexSongBlock>().flatMap(
                NetworkSearchComplexSongBlock::toRemoteSong
            )
        }
    )

    companion object {
        val TARGET_SEARCH_BLOCKS = listOf(NetworkSearchComplexSongBlock.SONG_BLOCK_CODE)
    }

}