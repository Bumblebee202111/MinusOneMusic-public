package com.github.bumblebee202111.minusonecloudmusic.ui.mycollection

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.github.bumblebee202111.minusonecloudmusic.databinding.FragmentMyAlbumBinding
import com.github.bumblebee202111.minusonecloudmusic.ui.common.repeatWithViewLifecycle
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch


@AndroidEntryPoint
class MyAlbumFragment : Fragment() {

    lateinit var binding: FragmentMyAlbumBinding
    val viewModel: MyAlbumViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding= FragmentMyAlbumBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val adapter = MyAlbumAdapter()
        binding.albumList.adapter = adapter

        repeatWithViewLifecycle {
            launch {
                viewModel.myAlbums.collect {
                    adapter.submitList(it)
                }
            }
        }
    }

    companion object {
        @JvmStatic
        fun newInstance()=
            MyAlbumFragment()
    }
}