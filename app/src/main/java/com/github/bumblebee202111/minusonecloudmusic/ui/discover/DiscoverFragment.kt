package com.github.bumblebee202111.minusonecloudmusic.ui.discover

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.github.bumblebee202111.minusonecloudmusic.model.DiscoverBlock
import com.github.bumblebee202111.minusonecloudmusic.ui.navigation.DailyRecommendRoute
import com.github.bumblebee202111.minusonecloudmusic.ui.navigation.NavigationManager
import com.github.bumblebee202111.minusonecloudmusic.ui.navigation.PlaylistRoute
import com.github.bumblebee202111.minusonecloudmusic.ui.navigation.SearchRoute
import com.github.bumblebee202111.minusonecloudmusic.ui.navigation.TopListsRoute
import com.github.bumblebee202111.minusonecloudmusic.ui.theme.DolphinTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject


@AndroidEntryPoint
class DiscoverFragment : Fragment() {

    private val viewModel: DiscoverViewModel by viewModels()
    @Inject
    lateinit var navigationManager: NavigationManager

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                DolphinTheme {
                    DiscoverScreen(
                        viewModel = viewModel,
                        onMenuClick = { navigationManager.openDrawer() },
                        onSearchClick = { navigationManager.navigate(SearchRoute) },
                        onDragonBallClick = { type ->
                            when (type) {
                                DiscoverBlock.DragonBalls.DragonBall.Type.SONG_RCMD -> navigationManager.navigate(
                                    DailyRecommendRoute
                                )

                                DiscoverBlock.DragonBalls.DragonBall.Type.RANK_LIST -> navigationManager.navigate(
                                    TopListsRoute
                                )

                                DiscoverBlock.DragonBalls.DragonBall.Type.PRIVATE_FM -> {
                                }

                                DiscoverBlock.DragonBalls.DragonBall.Type.PLAYLIST_COLLECTION -> {
                                }
                            }
                        },
                        onPlaylistClick = ::navigateToPlaylist
                    )
                }
            }
        }
    }

    private fun navigateToPlaylist(playlistId: Long, playlistCreatorId: Long, isMyPL: Boolean) {
        navigationManager.navigate(
            PlaylistRoute(
                playlistId = playlistId,
                playlistCreatorId = playlistCreatorId,
                isMyPL = isMyPL
            )
        )
    }
}
