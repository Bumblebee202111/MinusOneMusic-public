package com.github.bumblebee202111.minusonecloudmusic.data.repository

import com.github.bumblebee202111.minusonecloudmusic.data.AppResult
import com.github.bumblebee202111.minusonecloudmusic.data.network.NcmEapiService
import com.github.bumblebee202111.minusonecloudmusic.data.network.model.container.toDiscoverBlock
import com.github.bumblebee202111.minusonecloudmusic.data.network.util.NcmClientInfoProvider
import com.github.bumblebee202111.minusonecloudmusic.model.DiscoverBlock
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DiscoverRepository @Inject constructor(
    private val ncmEapiService: NcmEapiService,
    private val ncmClientInfoProvider: NcmClientInfoProvider
) {

    fun fetchDiscoverPage(isPullToRefresh: Boolean = false): Flow<AppResult<List<DiscoverBlock>>> {
        val desiredBlocks = listOf(
            "PAGE_RECOMMEND_DAILY_RECOMMEND",
            "PAGE_RECOMMEND_RANK",
            "PAGE_RECOMMEND_RADAR"
        )

        return apiResultFlow(
            fetch = {
                val (width, height) = ncmClientInfoProvider.getScreenSizeDp()
                val blockOrderJson = desiredBlocks.joinToString(separator = ",", prefix = "[", postfix = "]") { "\"$it\"" }

                val now = System.currentTimeMillis()
                val dateFormat = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault())
                val currentClientTime = dateFormat.format(java.util.Date(now))

                val action = if (isPullToRefresh) "pull" else "init"
                val extJsonStr = "{\"refreshAction\":\"$action\"}"

                ncmEapiService.showPageLinkResources(
                    refresh = isPullToRefresh,
                    widthDp = width,
                    heightDp = height,
                    blockCodeOrderList = blockOrderJson,
                    extJson = extJsonStr,
                    reqTimeStamp = now,
                    clientTime = currentClientTime,
                )
            },
            mapSuccess = { data ->
                data.blocks.filter { it.positionCode in desiredBlocks }.mapNotNull { it.toDiscoverBlock() }
            }
        )
    }
}