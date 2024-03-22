package com.github.bumblebee202111.minusonecloudmusic.ui.mycollection

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.github.bumblebee202111.minusonecloudmusic.databinding.FragmentCollectedMvListBinding
import com.github.bumblebee202111.minusonecloudmusic.ui.common.repeatWithViewLifecycle
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
@AndroidEntryPoint
class CollectedMvListFragment : Fragment() {

    lateinit var binding: FragmentCollectedMvListBinding
    val viewModel: CollectedMvListViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding= FragmentCollectedMvListBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val adapter= MyCollectedMvAdapter()
        binding.mvList.adapter=adapter

        repeatWithViewLifecycle {
            launch {
                viewModel.myMvs.collect {
                    adapter.submitList(it)
                }
            }
        }
        }


    companion object {
        @JvmStatic
        fun newInstance()=
            CollectedMvListFragment()
    }
}