package com.github.bumblebee202111.minusonecloudmusic.ui.friend

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.github.bumblebee202111.minusonecloudmusic.databinding.FragmentFollowBinding
import com.github.bumblebee202111.minusonecloudmusic.ui.common.repeatWithViewLifecycle
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch


@AndroidEntryPoint
class FollowFragment : Fragment() {

    lateinit var binding:FragmentFollowBinding
    val viewModel:FollowViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding= FragmentFollowBinding.inflate(inflater,container,false).apply {
            lifecycleOwner=viewLifecycleOwner
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val adapter = ProfileAdapter()
        binding.followingUsers.adapter = adapter
        repeatWithViewLifecycle {
            launch {
                viewModel.userFollows.collect {
                    adapter.submitList(it)
                }
            }

        }
    }

    companion object {
        @JvmStatic
        fun newInstance()=
            FollowFragment()
    }
}