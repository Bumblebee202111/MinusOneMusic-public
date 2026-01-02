package com.github.bumblebee202111.minusonecloudmusic.ui.friend

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.github.bumblebee202111.minusonecloudmusic.databinding.FragmentFansBinding
import com.github.bumblebee202111.minusonecloudmusic.ui.common.repeatWithViewLifecycle
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch


@AndroidEntryPoint
class FansFragment : Fragment() {

    lateinit var binding:FragmentFansBinding
    val viewModel:FansViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding= FragmentFansBinding.inflate(inflater,container,false).apply {
            lifecycleOwner=viewLifecycleOwner
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val adapter = ProfileAdapter()
        binding.fans.adapter = adapter
        repeatWithViewLifecycle {
            launch {
                viewModel.userFans.collect {
                    adapter.submitList(it)
                }
            }
        }
    }


    companion object {
        @JvmStatic
        fun newInstance()=
            FansFragment()
    }
}