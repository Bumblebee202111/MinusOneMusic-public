package com.github.bumblebee202111.minusonecloudmusic.ui.navigation

import androidx.appcompat.app.AppCompatActivity
import androidx.compose.ui.platform.LocalContext
import androidx.fragment.compose.AndroidFragment
import androidx.navigation3.runtime.entryProvider
import com.github.bumblebee202111.minusonecloudmusic.ui.clouddisk.MyPrivateCloudScreen
import com.github.bumblebee202111.minusonecloudmusic.ui.comments.CommentsScreen
import com.github.bumblebee202111.minusonecloudmusic.ui.dailyrecommend.DailyRecommendScreen
import com.github.bumblebee202111.minusonecloudmusic.ui.discover.DiscoverScreen
import com.github.bumblebee202111.minusonecloudmusic.ui.friend.MyFriendScreen
import com.github.bumblebee202111.minusonecloudmusic.ui.inbox.InboxScreen
import com.github.bumblebee202111.minusonecloudmusic.ui.listenrank.ListenRankScreen
import com.github.bumblebee202111.minusonecloudmusic.ui.localmusic.LocalMusicScreen
import com.github.bumblebee202111.minusonecloudmusic.ui.login.PhoneCaptchaLoginFragment
import com.github.bumblebee202111.minusonecloudmusic.ui.login.PhonePasswordLoginFragment
import com.github.bumblebee202111.minusonecloudmusic.ui.mine.MineScreen
import com.github.bumblebee202111.minusonecloudmusic.ui.mycollection.MyCollectionScreen
import com.github.bumblebee202111.minusonecloudmusic.ui.nowplaying.NowPlayingScreen
import com.github.bumblebee202111.minusonecloudmusic.ui.playerhistory.PlayerHistoryDialogFragment
import com.github.bumblebee202111.minusonecloudmusic.ui.playlist.PLAYLIST_CREATOR_ID_UNKNOWN
import com.github.bumblebee202111.minusonecloudmusic.ui.playlist.PlaylistScreen
import com.github.bumblebee202111.minusonecloudmusic.ui.recentplay.MyRecentPlayScreen
import com.github.bumblebee202111.minusonecloudmusic.ui.search.SearchScreen
import com.github.bumblebee202111.minusonecloudmusic.ui.settings.SettingsScreen
import com.github.bumblebee202111.minusonecloudmusic.ui.toplists.TopListsScreen
import com.github.bumblebee202111.minusonecloudmusic.ui.usertrack.FriendTracksScreen

fun createAppEntryProvider(navigationManager: NavigationManager) = entryProvider {
    entry<DiscoverRoute> {
        DiscoverScreen(
            onMenuClick = { navigationManager.openDrawer() },
            onSearchClick = { navigationManager.navigate(SearchRoute) }
        )
    }
    entry<FriendTracksRoute> { FriendTracksScreen() }
    entry<MineRoute> {
        MineScreen(
            onOpenDrawer = { navigationManager.openDrawer() },
            onNavigate = { route ->
                navigationManager.navigate(route)
            })
    }
    entry<NowPlayingRoute> {
        val context = LocalContext.current
        NowPlayingScreen(
            onNavigateBack = { navigationManager.goBack() },
            onNavigateToComments = { threadId -> navigationManager.navigate(CommentsRoute(threadId)) },
            onOpenPlaylist = {
                val activity = context as? AppCompatActivity
                activity?.let {
                    PlayerHistoryDialogFragment().show(
                        it.supportFragmentManager,
                        PlayerHistoryDialogFragment.TAG
                    )
                }
            }
        )
    }
    entry<InboxRoute> { InboxScreen() }
    entry<DailyRecommendRoute> {
        DailyRecommendScreen(
            onNavigateBack = { navigationManager.goBack() }
        )
    }
    entry<TopListsRoute> {
        TopListsScreen(
            onNavigateBack = { navigationManager.goBack() },
            onNavigateToPlaylist = { playlistId ->
                navigationManager.navigate(
                    PlaylistRoute(
                        playlistId = playlistId,
                        playlistCreatorId = PLAYLIST_CREATOR_ID_UNKNOWN
                    )
                )
            }
        )
    }
    entry<MyPrivateCloudRoute> {
        MyPrivateCloudScreen(
            onNavigateBack = { navigationManager.goBack() }
        )
    }
    entry<LocalMusicRoute> {
        LocalMusicScreen(
            onNavigateBack = { navigationManager.goBack() }
        )
    }
    entry<MyRecentPlayRoute> { 
        MyRecentPlayScreen(
            onNavigateBack = { navigationManager.goBack() }
        ) 
    }
    entry<MyFriendRoute> { 
        MyFriendScreen(
            onNavigateBack = { navigationManager.goBack() }
        ) 
    }
    entry<MyCollectionRoute> { 
        MyCollectionScreen(
            onNavigateBack = { navigationManager.goBack() }
        ) 
    }
    entry<SettingsRoute> { SettingsScreen() }
    entry<SearchRoute> {
        SearchScreen(
            onNavigateBack = { navigationManager.goBack() }
        )
    }
    entry<PhoneCaptchaLoginRoute> { AndroidFragment<PhoneCaptchaLoginFragment>() }
    entry<PhonePasswordLoginRoute> { AndroidFragment<PhonePasswordLoginFragment>() }
    entry<PlaylistRoute> { route ->
        PlaylistScreen(
            playlistId = route.playlistId,
            creatorId = route.playlistCreatorId,
            isMyPL = route.isMyPL,
            onNavigateBack = { navigationManager.goBack() }
        )
    }
    entry<PlaylistV4Route> { route ->
        PlaylistScreen(
            playlistId = route.id,
            isV6 = false,
            onNavigateBack = { navigationManager.goBack() }
        )
    }
    entry<V6PlaylistRoute> { route ->
        PlaylistScreen(
            playlistId = route.id,
            isV6 = true,
            onNavigateBack = { navigationManager.goBack() }
        )
    }
    entry<CommentsRoute> { route ->
        CommentsScreen(
            threadId = route.threadId,
            onNavigateBack = { navigationManager.goBack() }
        )
    }
    entry<ListenRankRoute> { route ->
        ListenRankScreen(
            userId = route.userId,
            onNavigateBack = { navigationManager.goBack() }
        )
    }
}