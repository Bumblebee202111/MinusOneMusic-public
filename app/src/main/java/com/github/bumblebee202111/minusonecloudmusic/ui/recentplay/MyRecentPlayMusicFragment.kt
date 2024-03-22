package com.github.bumblebee202111.minusonecloudmusic.ui.recentplay

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.github.bumblebee202111.minusonecloudmusic.databinding.FragmentMyRecentPlayMusicBinding
import com.github.bumblebee202111.minusonecloudmusic.ui.common.repeatWithViewLifecycle
import kotlinx.coroutines.launch

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class MyRecentPlayMusicFragment : Fragment() {

    private val myRecentPlayViewModel:MyRecentPlayViewModel by viewModels( { requireParentFragment() })
    private lateinit var binding: FragmentMyRecentPlayMusicBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {

        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding=FragmentMyRecentPlayMusicBinding.inflate(inflater,container,false).apply {
            lifecycleOwner=viewLifecycleOwner
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val adapter=RecentPlaySongAdapter {

        }
        binding.list.adapter=adapter

        repeatWithViewLifecycle {
            launch {
                myRecentPlayViewModel.recentPlaySongs.collect{
                    adapter.submitList(it)
                }
            }
        }

    }
    companion object {
        @JvmStatic
        fun newInstance() =
            MyRecentPlayMusicFragment().apply {
                arguments = Bundle().apply {
                }
            }
    }
}