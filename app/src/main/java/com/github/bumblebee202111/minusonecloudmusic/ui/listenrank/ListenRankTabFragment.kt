package com.github.bumblebee202111.minusonecloudmusic.ui.listenrank

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.github.bumblebee202111.minusonecloudmusic.databinding.FragmentListenRankTabBinding
import com.github.bumblebee202111.minusonecloudmusic.ui.common.SongWithPositionList
import com.github.bumblebee202111.minusonecloudmusic.ui.listenrank.ListenRankFragment.Companion.PLAY_RECORDS_TAB_INDEX_ALL_DATA
import com.github.bumblebee202111.minusonecloudmusic.ui.listenrank.ListenRankFragment.Companion.PLAY_RECORDS_TAB_INDEX_WEEK_DATA
import com.github.bumblebee202111.minusonecloudmusic.ui.listenrank.ListenRankFragment.Companion.PlayRecordsTabIndex

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

            val playRecordsFlow = when (tabIndex) {
                PLAY_RECORDS_TAB_INDEX_WEEK_DATA -> viewModel.weekRecordsUiState
                PLAY_RECORDS_TAB_INDEX_ALL_DATA -> viewModel.allRecordsUiState
                else -> throw IllegalArgumentException()
            }

            list.setContent {
                val playRecords by playRecordsFlow.collectAsStateWithLifecycle(initialValue = emptyList())
                
                LaunchedEffect(playRecords) {
                    setPlayRecords(playRecords)
                }

                SongWithPositionList(
                    songs = playRecords ?: emptyList(),
                    onItemClick = { position ->
                        when (tabIndex) {
                            PLAY_RECORDS_TAB_INDEX_WEEK_DATA -> {
                                viewModel.onWeekRecordClick(position)
                            }

                            PLAY_RECORDS_TAB_INDEX_ALL_DATA -> {
                                viewModel.onAllRecordClick(position)
                            }
                        }
                    }
                )
            }
        }
    }

}