package com.github.bumblebee202111.minusonecloudmusic.ui.dailyrecommend

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.github.bumblebee202111.minusonecloudmusic.databinding.FragmentDailyRecommendBinding
import com.github.bumblebee202111.minusonecloudmusic.ui.common.repeatWithViewLifecycle
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch


@AndroidEntryPoint
class DailyRecommendFragment : Fragment() {

    companion object {
        fun newInstance() = DailyRecommendFragment()
    }

    private lateinit var binding: FragmentDailyRecommendBinding
    private val viewModel: DailyRecommendViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentDailyRecommendBinding.inflate(inflater, container, false).apply {
            lifecycleOwner = viewLifecycleOwner
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val dailyRecommendList = binding.dailyRecommendList
        val adapter = DailyRecommendSongAdapter { dailyRecommendSong ->
            viewModel.onSongItemClick(dailyRecommendSong)
        }
        dailyRecommendList.adapter = adapter

        repeatWithViewLifecycle {
            launch {
                viewModel.dailyRecommendSongs.collect {
                    adapter.submitList(it)
                }
            }

        }
    }
}
