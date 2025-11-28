package com.github.bumblebee202111.minusonecloudmusic.ui.discover

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.DividerItemDecoration
import com.github.bumblebee202111.minusonecloudmusic.MainActivity
import com.github.bumblebee202111.minusonecloudmusic.MobileNavigationDirections
import com.github.bumblebee202111.minusonecloudmusic.R
import com.github.bumblebee202111.minusonecloudmusic.model.DiscoverBlock
import com.github.bumblebee202111.minusonecloudmusic.databinding.FragmentDiscoverBinding
import com.github.bumblebee202111.minusonecloudmusic.ui.common.mainNavController
import com.github.bumblebee202111.minusonecloudmusic.ui.common.repeatWithViewLifecycle
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch


@AndroidEntryPoint
class DiscoverFragment : Fragment() {

    companion object {
        fun newInstance() = DiscoverFragment()
    }

    private lateinit var binding: FragmentDiscoverBinding
    val viewModel: DiscoverViewModel by viewModels()

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
                (activity as? MainActivity)?.openDrawer()
            }
            setOnMenuItemClickListener {
                mainNavController.navigate(R.id.nav_search)
                true
            }
        }

        val discoverBlockAdapter = DiscoverBlockAdapter(
            onDragonBallClick = { type ->
                val navController = mainNavController
                when (type) {
                    DiscoverBlock.DragonBalls.DragonBall.Type.SONG_RCMD -> {
                        navController.navigate(R.id.nav_dailyrecommend)
                    }

                    DiscoverBlock.DragonBalls.DragonBall.Type.RANK_LIST -> {
                        navController.navigate(R.id.nav_top_lists)
                }

                    DiscoverBlock.DragonBalls.DragonBall.Type.PRIVATE_FM -> {

                }

                    DiscoverBlock.DragonBalls.DragonBall.Type.PLAYLIST_COLLECTION -> {

                    }
                }

            },
            onPlaylistClick = ::navigateToPlaylist
        )
        binding.blockList.apply {
            adapter = discoverBlockAdapter
            addItemDecoration(
                DiscoverDividerItemDecoration(
                    requireContext(),
                    DividerItemDecoration.VERTICAL
                )
            )

        }

        repeatWithViewLifecycle {
            launch {
                viewModel.blocks.collect {
                    discoverBlockAdapter.submitList(it)
                }
            }
        }
    }

    private fun navigateToPlaylist(playlistId: Long, playlistCreatorId: Long, isMyPL: Boolean) {
        mainNavController.navigate(
            MobileNavigationDirections.actionGlobalNavPlaylist(
                playlistId = playlistId,
                playlistCreatorId = playlistCreatorId,
                isMyPL = isMyPL
            )
        )
    }
}
