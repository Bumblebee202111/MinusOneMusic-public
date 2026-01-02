package com.github.bumblebee202111.minusonecloudmusic.ui.navigation

import androidx.core.net.toUri
import androidx.navigation3.runtime.NavKey
import com.example.nav3recipes.deeplink.basic.util.DeepLinkMatcher
import com.example.nav3recipes.deeplink.basic.util.DeepLinkPattern
import com.example.nav3recipes.deeplink.basic.util.DeepLinkRequest
import com.example.nav3recipes.deeplink.basic.util.KeyDecoder

object DeepLinkRegistry {
    private val patterns: List<DeepLinkPattern<out NavKey>> = listOf(
        DeepLinkPattern(DailyRecommendRoute.serializer(), URL_SONG_RCMD.toUri()),
        DeepLinkPattern(NowPlayingRoute.serializer(), URL_PLAYER.toUri()),
        DeepLinkPattern(PlaylistV4Route.serializer(), URL_PLAYLIST.toUri()),
        DeepLinkPattern(V6PlaylistRoute.serializer(), URL_NM_PLAYLIST.toUri()),
    )

    fun resolve(url: String?): NavKey? {
        if (url.isNullOrBlank()) return null
        val uri = url.toUri()
        val request = DeepLinkRequest(uri)

        val match = patterns.firstNotNullOfOrNull { pattern ->
            DeepLinkMatcher(request, pattern).match()
        }
        
        return match?.let {
            KeyDecoder(match.args).decodeSerializableValue(match.serializer)
        }
    }
}