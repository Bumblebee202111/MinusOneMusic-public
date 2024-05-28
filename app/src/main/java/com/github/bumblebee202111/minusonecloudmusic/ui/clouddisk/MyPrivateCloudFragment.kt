package com.github.bumblebee202111.minusonecloudmusic.ui.clouddisk

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import com.github.bumblebee202111.minusonecloudmusic.databinding.FragmentMyPrivateCloudBinding
import com.github.bumblebee202111.minusonecloudmusic.ui.common.AbstractPlaylistFragment
import com.github.bumblebee202111.minusonecloudmusic.ui.common.repeatWithViewLifecycle
import com.github.bumblebee202111.minusonecloudmusic.ui.common.songadapters.PagedSongWithPositionAdapter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
@AndroidEntryPoint
class MyPrivateCloudFragment : AbstractPlaylistFragment() {

    companion object {
        fun newInstance() = MyPrivateCloudFragment()
    }

    private lateinit var binding:FragmentMyPrivateCloudBinding
    override val viewModel: MyPrivateCloudViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding= FragmentMyPrivateCloudBinding.inflate(inflater,container,false).apply {
            lifecycleOwner=viewLifecycleOwner
            viewModel=this@MyPrivateCloudFragment.viewModel
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
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
                viewModel.player.collect{
                    miniPlayerBar.player=it
                }
            }
        }
    }


}