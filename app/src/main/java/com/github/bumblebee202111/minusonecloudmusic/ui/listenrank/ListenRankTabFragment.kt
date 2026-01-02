package com.github.bumblebee202111.minusonecloudmusic.ui.listenrank

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.github.bumblebee202111.minusonecloudmusic.databinding.FragmentListenRankTabBinding
import com.github.bumblebee202111.minusonecloudmusic.ui.common.repeatWithViewLifecycle
import com.github.bumblebee202111.minusonecloudmusic.ui.common.SongWithPositionAdapter
import com.github.bumblebee202111.minusonecloudmusic.ui.listenrank.ListenRankFragment.Companion.PLAY_RECORDS_TAB_INDEX_ALL_DATA
import com.github.bumblebee202111.minusonecloudmusic.ui.listenrank.ListenRankFragment.Companion.PLAY_RECORDS_TAB_INDEX_WEEK_DATA
import com.github.bumblebee202111.minusonecloudmusic.ui.listenrank.ListenRankFragment.Companion.PlayRecordsTabIndex
import kotlinx.coroutines.launch

class ListenRankTabFragment : Fragment() {
    companion object {
        const val ARG_TAB_INDEX = "tab_index"
        fun newInstance(@PlayRecordsTabIndex index: Int) =
            ListenRankTabFragment().apply {
                arguments = Bundle().apply {
                    putInt(ARG_TAB_INDEX, index)
                }
            }
    }

    lateinit var binding: FragmentListenRankTabBinding
    private val viewModel: ListenRankViewModel by viewModels(::requireParentFragment)

    @PlayRecordsTabIndex
    private var tabIndex: Int =
        PLAY_RECORDS_TAB_INDEX_WEEK_DATA

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            tabIndex = it.getInt(ARG_TAB_INDEX)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentListenRankTabBinding.inflate(inflater, container, false).apply {

        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val adapter = SongWithPositionAdapter {
            when (tabIndex) {
                PLAY_RECORDS_TAB_INDEX_WEEK_DATA -> {
                    viewModel.onWeekRecordClick(it)
                }

                PLAY_RECORDS_TAB_INDEX_ALL_DATA -> {
                    viewModel.onAllRecordClick(it)
                }
            }
        }
        binding.apply {
            playlistActions.setOnClickListener {
                when (tabIndex) {
                    PLAY_RECORDS_TAB_INDEX_WEEK_DATA -> {
                        viewModel.playAllWeekRecords()
                    }

                    PLAY_RECORDS_TAB_INDEX_ALL_DATA -> {
                        viewModel.playAllAllRecords()
                    }
                }
            }

            list.adapter = adapter

            val playRecordsFlow = when (tabIndex) {
                PLAY_RECORDS_TAB_INDEX_WEEK_DATA -> viewModel.weekRecordsUiState
                PLAY_RECORDS_TAB_INDEX_ALL_DATA -> viewModel.allRecordsUiState
                else -> throw IllegalArgumentException()
            }
            repeatWithViewLifecycle {
                launch {
                    playRecordsFlow.collect { playRecords ->
                        setPlayRecords(playRecords)
                        adapter.submitList(playRecords)
                    }
                }
            }
        }


    }

}