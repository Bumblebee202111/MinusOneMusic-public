package com.github.bumblebee202111.minusonecloudmusic.ui.localmusic

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.github.bumblebee202111.minusonecloudmusic.data.mediastore.MediaStoreUtils
import com.github.bumblebee202111.minusonecloudmusic.databinding.FragmentLocalMusicBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class LocalMusicFragment : Fragment() {

    companion object {
        fun newInstance() = LocalMusicFragment()
    }

    private lateinit var viewModel: LocalMusicViewModel
    private lateinit var binding: FragmentLocalMusicBinding
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentLocalMusicBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val adapter = LocalSongAdapter {}
        binding.list.adapter = adapter
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            adapter.submitList(MediaStoreUtils.getMusicResources(requireContext()))
        }
    }


}