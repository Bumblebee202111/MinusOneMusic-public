package com.github.bumblebee202111.minusonecloudmusic.ui.mine

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.github.bumblebee202111.minusonecloudmusic.MobileNavigationDirections
import com.github.bumblebee202111.minusonecloudmusic.databinding.FragmentMyPlaylistCategoryBinding
import com.github.bumblebee202111.minusonecloudmusic.ui.common.mainNavController
import com.github.bumblebee202111.minusonecloudmusic.ui.common.repeatWithViewLifecycle
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class UserPlaylistTabFragment : Fragment() {

    private lateinit var category: UserPlaylistTab
    private lateinit var binding: FragmentMyPlaylistCategoryBinding
    private val mineViewModel: MineViewModel by viewModels( {
        requireParentFragment().requireParentFragment()
    })

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.let {
            category = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                it.getSerializable(ARG_CATEGORY, UserPlaylistTab::class.java)
            } else {
                @Suppress("DEPRECATION")
                it.getSerializable(ARG_CATEGORY)
            } as UserPlaylistTab
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentMyPlaylistCategoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val playlistAdapter = MyPlaylistAdapter { userPlaylistItem ->
            val direction = when (userPlaylistItem) {
                is NormalPlaylistItem -> {
                    MobileNavigationDirections.actionGlobalNavPlaylist(userPlaylistItem.playlist.id,userPlaylistItem.playlist.creatorId?:0)
                }

                is UserChartsItem -> {
                    MobileNavigationDirections.actionGlobalNavListenRank(userPlaylistItem.userId)
                }
            }
            mainNavController.navigate(direction)

        }
        binding.playlists.adapter = playlistAdapter

        repeatWithViewLifecycle {
            mineViewModel.myPlaylistTabs
                .collect {
                    playlistAdapter.submitList(it?.get(category))
                }
        }


    }

    override fun onResume() {
        super.onResume()
        binding.root.requestLayout()
    }

    companion object {
        const val ARG_CATEGORY = "category"

        @JvmStatic
        fun newInstance(category: UserPlaylistTab) =
            UserPlaylistTabFragment().apply {
                arguments = Bundle().apply {
                    putSerializable(ARG_CATEGORY, category)
                }
            }
    }
}