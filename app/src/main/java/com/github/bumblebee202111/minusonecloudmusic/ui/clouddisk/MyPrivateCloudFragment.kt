package com.github.bumblebee202111.minusonecloudmusic.ui.clouddisk

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.paging.compose.collectAsLazyPagingItems
import com.github.bumblebee202111.minusonecloudmusic.databinding.FragmentMyPrivateCloudBinding
import com.github.bumblebee202111.minusonecloudmusic.ui.common.PagedSongWithPositionList
import com.github.bumblebee202111.minusonecloudmusic.ui.common.PlaylistFragmentUIHelper
import dagger.hilt.android.AndroidEntryPoint
@AndroidEntryPoint
class MyPrivateCloudFragment : Fragment() {

    companion object {
        fun newInstance() = MyPrivateCloudFragment()
    }

    private lateinit var binding: FragmentMyPrivateCloudBinding
    val viewModel: MyPrivateCloudViewModel by viewModels()
    private lateinit var uiHelper: PlaylistFragmentUIHelper

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentMyPrivateCloudBinding.inflate(inflater, container, false).apply {
            lifecycleOwner = viewLifecycleOwner
            viewModel = this@MyPrivateCloudFragment.viewModel
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        uiHelper = PlaylistFragmentUIHelper(
            view = view,
            playAllAction = viewModel::playAll
        )
        
        binding.privateSongsList.setContent {
            val songs = viewModel.songUiItemsPagingData.collectAsLazyPagingItems()
            PagedSongWithPositionList(
                songs = songs,
                onItemClick = viewModel::onSongItemClick
            )
        }
    }
}