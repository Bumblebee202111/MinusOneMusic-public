package com.github.bumblebee202111.minusonecloudmusic.ui.clouddisk

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.github.bumblebee202111.minusonecloudmusic.databinding.FragmentMyPrivateCloudBinding
import com.github.bumblebee202111.minusonecloudmusic.ui.common.MiniPlayerBarController
import com.github.bumblebee202111.minusonecloudmusic.ui.common.PlaylistDialogController
import com.github.bumblebee202111.minusonecloudmusic.ui.common.PlaylistFragmentUIHelper
import com.github.bumblebee202111.minusonecloudmusic.ui.common.repeatWithViewLifecycle
import com.github.bumblebee202111.minusonecloudmusic.ui.common.songadapters.PagedSongWithPositionAdapter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
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
            fragment = this,
            view = view,
            navController = findNavController(),
            playAllAction = viewModel::playAll
        )
        val cloudSongsList = binding.privateSongsList

        val adapter = PagedSongWithPositionAdapter(viewModel::onSongItemClick)
        cloudSongsList.adapter = adapter

        repeatWithViewLifecycle {
            launch {
                viewModel.songUiItemsPagingData.collect {
                    adapter.submitData(it)
                }
            }
            launch {
                viewModel.player.collect {
                    binding.miniPlayerBar.player = it
                }
            }
        }
    }

    override fun onStop() {
        uiHelper.onStop()
        super.onStop()
    }

}