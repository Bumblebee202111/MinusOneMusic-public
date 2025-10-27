@file:OptIn(UnstableApi::class)

package com.github.bumblebee202111.minusonecloudmusic.ui.dailyrecommend

import android.graphics.Typeface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.OptIn
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.media3.common.util.UnstableApi
import androidx.navigation.fragment.findNavController
import com.github.bumblebee202111.minusonecloudmusic.databinding.FragmentDailyRecommendBinding
import com.github.bumblebee202111.minusonecloudmusic.ui.common.PlaylistFragmentUIHelper
import com.github.bumblebee202111.minusonecloudmusic.ui.common.repeatWithViewLifecycle
import com.github.bumblebee202111.minusonecloudmusic.ui.common.SongWithAlbumAdapter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch


@AndroidEntryPoint
class DailyRecommendFragment : Fragment() {

    companion object {
        fun newInstance() = DailyRecommendFragment()
    }

    private lateinit var binding: FragmentDailyRecommendBinding
    val viewModel: DailyRecommendViewModel by viewModels()
    private lateinit var uiHelper: PlaylistFragmentUIHelper

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentDailyRecommendBinding.inflate(inflater, container, false).apply {
            viewModel = this@DailyRecommendFragment.viewModel
            lifecycleOwner = viewLifecycleOwner
        }
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        uiHelper = PlaylistFragmentUIHelper(
            view = view,
            playAllAction = viewModel::playAll
        )

        binding.toolbar.setNavigationOnClickListener {
            findNavController().popBackStack()
        }

        val dailyRecommendList = binding.dailyRecommendList
        val adapter = SongWithAlbumAdapter(viewModel::onSongItemClick)
        dailyRecommendList.adapter = adapter

        val typeface = Typeface.createFromAsset(requireContext().assets, "bamboo.ttf")
        binding.tvPendantDayRecommendDateInfo.typeface = typeface
        binding.tvPendantMonthText.typeface = typeface

        repeatWithViewLifecycle {
            launch {
                viewModel.songItems.collect {
                    adapter.submitList(it)
                }
            }
        }
    }

}
