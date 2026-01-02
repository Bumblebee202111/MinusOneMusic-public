package com.github.bumblebee202111.minusonecloudmusic.ui.comments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.github.bumblebee202111.minusonecloudmusic.R
import com.github.bumblebee202111.minusonecloudmusic.databinding.FragmentCommentsBinding
import com.github.bumblebee202111.minusonecloudmusic.ui.common.repeatWithViewLifecycle
import com.google.android.material.divider.MaterialDividerItemDecoration
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class CommentsFragment : Fragment() {

    companion object {
        fun newInstance() = CommentsFragment()
    }

    lateinit var binding: FragmentCommentsBinding

    private val viewModel: CommentsViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentCommentsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }

        val adapter = CommentsAdapter()
        binding.commentList.apply {
            this.adapter = adapter
            this.addItemDecoration(
                MaterialDividerItemDecoration(
                    context,
                    MaterialDividerItemDecoration.VERTICAL
                ).apply {
                    setDividerColorResource(requireContext(), R.color.lineColor)
                })
        }

        repeatWithViewLifecycle {
            launch {
                viewModel.comments.collect(adapter::submitList)
            }
        }
    }
}