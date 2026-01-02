package com.github.bumblebee202111.minusonecloudmusic.ui.navigation

import android.os.Bundle
import androidx.fragment.compose.AndroidFragment
import androidx.navigation3.runtime.entryProvider
import com.github.bumblebee202111.minusonecloudmusic.ui.clouddisk.MyPrivateCloudFragment
import com.github.bumblebee202111.minusonecloudmusic.ui.comments.CommentsFragment
import com.github.bumblebee202111.minusonecloudmusic.ui.dailyrecommend.DailyRecommendFragment
import com.github.bumblebee202111.minusonecloudmusic.ui.discover.DiscoverFragment
import com.github.bumblebee202111.minusonecloudmusic.ui.friend.MyFriendFragment
import com.github.bumblebee202111.minusonecloudmusic.ui.inbox.InboxFragment
import com.github.bumblebee202111.minusonecloudmusic.ui.listenrank.ListenRankFragment
import com.github.bumblebee202111.minusonecloudmusic.ui.localmusic.LocalMusicFragment
import com.github.bumblebee202111.minusonecloudmusic.ui.login.PhoneCaptchaLoginFragment
import com.github.bumblebee202111.minusonecloudmusic.ui.login.PhonePasswordLoginFragment
import com.github.bumblebee202111.minusonecloudmusic.ui.mine.MineFragment
import com.github.bumblebee202111.minusonecloudmusic.ui.mycollection.MyCollectionFragment
import com.github.bumblebee202111.minusonecloudmusic.ui.nowplaying.NowPlayingFragment
import com.github.bumblebee202111.minusonecloudmusic.ui.playlist.PlaylistFragment
import com.github.bumblebee202111.minusonecloudmusic.ui.recentplay.MyRecentPlayFragment
import com.github.bumblebee202111.minusonecloudmusic.ui.search.SearchFragment
import com.github.bumblebee202111.minusonecloudmusic.ui.settings.SettingsFragment
import com.github.bumblebee202111.minusonecloudmusic.ui.toplists.TopListsFragment
import com.github.bumblebee202111.minusonecloudmusic.ui.usertrack.FriendTracksFragment

val AppEntryProvider = entryProvider {
    entry<DiscoverRoute> { AndroidFragment<DiscoverFragment>() }
    entry<FriendTracksRoute> { AndroidFragment<FriendTracksFragment>() }
    entry<MineRoute> { AndroidFragment<MineFragment>() }
    entry<NowPlayingRoute> { AndroidFragment<NowPlayingFragment>() }
    entry<InboxRoute> { AndroidFragment<InboxFragment>() }
    entry<DailyRecommendRoute> { AndroidFragment<DailyRecommendFragment>() }
    entry<TopListsRoute> { AndroidFragment<TopListsFragment>() }
    entry<MyPrivateCloudRoute> { AndroidFragment<MyPrivateCloudFragment>() }
    entry<LocalMusicRoute> { AndroidFragment<LocalMusicFragment>() }
    entry<MyRecentPlayRoute> { AndroidFragment<MyRecentPlayFragment>() }
    entry<MyFriendRoute> { AndroidFragment<MyFriendFragment>() }
    entry<MyCollectionRoute> { AndroidFragment<MyCollectionFragment>() }
    entry<SettingsRoute> { AndroidFragment<SettingsFragment>() }
    entry<SearchRoute> { AndroidFragment<SearchFragment>() }
    entry<PhoneCaptchaLoginRoute> { AndroidFragment<PhoneCaptchaLoginFragment>() }
    entry<PhonePasswordLoginRoute> { AndroidFragment<PhonePasswordLoginFragment>() }
    entry<PlaylistRoute> { route ->
        val args = Bundle().apply {
            putLong("playlistId", route.playlistId)
            putLong("playlistCreatorId", route.playlistCreatorId)
            putBoolean("isMyPL", route.isMyPL)
        }
        AndroidFragment<PlaylistFragment>(arguments = args)
    }
    entry<CommentsRoute> { route ->
        val args = Bundle().apply {
            putString("threadId", route.threadId)
        }
        AndroidFragment<CommentsFragment>(arguments = args)
    }
    entry<ListenRankRoute> { route ->
        val args = Bundle().apply {
            putLong("userId", route.userId)
        }
        AndroidFragment<ListenRankFragment>(arguments = args)
    }
}