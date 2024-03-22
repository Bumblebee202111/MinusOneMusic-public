package com.github.bumblebee202111.minusonecloudmusic.ui.toplists

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.github.bumblebee202111.minusonecloudmusic.databinding.FragmentTopListsBinding
import com.github.bumblebee202111.minusonecloudmusic.ui.common.repeatWithViewLifecycle
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class TopListsFragment : Fragment() {

    companion object {
        fun newInstance() = TopListsFragment()
    }

    private val viewModel: TopListsViewModel by viewModels ()
    private lateinit var binding:FragmentTopListsBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding=FragmentTopListsBinding.inflate(inflater,container,false).apply {
            lifecycleOwner=viewLifecycleOwner
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val adapter =BillboardGroupAdapter()
        binding.billboardGroupList.adapter=adapter

        repeatWithViewLifecycle {
            launch {
                viewModel.topLists.collect {
                    adapter.submitList(it)
                }
            }
        }
        }


}