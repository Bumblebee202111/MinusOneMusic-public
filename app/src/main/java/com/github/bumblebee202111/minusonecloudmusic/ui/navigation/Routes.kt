package com.github.bumblebee202111.minusonecloudmusic.ui.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable data object DiscoverRoute : NavKey
@Serializable data object FriendTracksRoute : NavKey
@Serializable data object MineRoute : NavKey

@Serializable data object NowPlayingRoute : NavKey
@Serializable data object InboxRoute : NavKey
@Serializable data object DailyRecommendRoute : NavKey
@Serializable data object TopListsRoute : NavKey
@Serializable data object MyPrivateCloudRoute : NavKey
@Serializable data object LocalMusicRoute : NavKey
@Serializable data object MyRecentPlayRoute : NavKey
@Serializable data object MyFriendRoute : NavKey
@Serializable data object MyCollectionRoute : NavKey
@Serializable data object SettingsRoute : NavKey
@Serializable data object SearchRoute : NavKey

@Serializable
data class PlaylistRoute(
    val playlistId: Long,
    val playlistCreatorId: Long,
    val isMyPL: Boolean = false
) : NavKey

@Serializable
data class CommentsRoute(val threadId: String) : NavKey

@Serializable
data class ListenRankRoute(val userId: Long) : NavKey

@Serializable data object PhoneCaptchaLoginRoute : NavKey
@Serializable data object PhonePasswordLoginRoute : NavKey

const val DEEP_LINK_SCHEME = "orpheus"
const val URL_NOW_PLAYING = "$DEEP_LINK_SCHEME: