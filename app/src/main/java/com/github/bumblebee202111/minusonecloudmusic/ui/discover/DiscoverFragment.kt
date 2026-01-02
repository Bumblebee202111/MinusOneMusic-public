package com.github.bumblebee202111.minusonecloudmusic.ui.discover

import DiscoverList
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.runtime.getValue
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.recyclerview.widget.DividerItemDecoration
import com.github.bumblebee202111.minusonecloudmusic.MainActivity
import com.github.bumblebee202111.minusonecloudmusic.databinding.FragmentDiscoverBinding
import com.github.bumblebee202111.minusonecloudmusic.model.DiscoverBlock
import com.github.bumblebee202111.minusonecloudmusic.ui.common.repeatWithViewLifecycle
import com.github.bumblebee202111.minusonecloudmusic.ui.navigation.DailyRecommendRoute
import com.github.bumblebee202111.minusonecloudmusic.ui.navigation.NavigationManager
import com.github.bumblebee202111.minusonecloudmusic.ui.navigation.PlaylistRoute
import com.github.bumblebee202111.minusonecloudmusic.ui.navigation.SearchRoute
import com.github.bumblebee202111.minusonecloudmusic.ui.navigation.TopListsRoute
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject
@AndroidEntryPoint
class DiscoverFragment : Fragment() {

    companion object {
        fun newInstance() = DiscoverFragment()
    }

    private lateinit var binding: FragmentDiscoverBinding
    val viewModel: DiscoverViewModel by viewModels()

    @Inject
    lateinit var navigationManager: NavigationManager

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentDiscoverBinding.inflate(inflater, container, false).apply {
            lifecycleOwner = viewLifecycleOwner
        }

        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.topAppBar.apply {
            setNavigationOnClickListener {
                navigationManager.openDrawer()
            }
            setOnMenuItemClickListener {
                navigationManager.navigate(SearchRoute)
                true
            }
        }

        binding.blockList.setContent {
            val blocks by viewModel.blocks.collectAsStateWithLifecycle()

            blocks?.let {
                DiscoverList(
                    blocks = it,
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
