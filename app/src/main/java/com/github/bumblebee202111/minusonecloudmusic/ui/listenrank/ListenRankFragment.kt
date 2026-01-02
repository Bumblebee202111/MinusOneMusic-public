package com.github.bumblebee202111.minusonecloudmusic.ui.listenrank

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.IntDef
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.github.bumblebee202111.minusonecloudmusic.databinding.FragmentListenRankBinding
import com.google.android.material.tabs.TabLayoutMediator
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class ListenRankFragment : Fragment() {

    companion object {
        fun newInstance() = ListenRankFragment()

        @Retention(AnnotationRetention.SOURCE)
        @IntDef(value = [PLAY_RECORDS_TAB_INDEX_WEEK_DATA, PLAY_RECORDS_TAB_INDEX_ALL_DATA])
        annotation class PlayRecordsTabIndex

        const val PLAY_RECORDS_TAB_INDEX_WEEK_DATA = 0
        const val PLAY_RECORDS_TAB_INDEX_ALL_DATA = 1

    }

    lateinit var binding: FragmentListenRankBinding
    private val viewModel: ListenRankViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentListenRankBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.apply {
            toolbar.setNavigationOnClickListener {
                findNavController().navigateUp()
            }

            pager.adapter = PlayRecordsPagerAdapter()
            TabLayoutMediator(tabLayout, pager) { tab, index ->
                tab.text = when (index) {
                    PLAY_RECORDS_TAB_INDEX_WEEK_DATA -> "最近一周"
                    PLAY_RECORDS_TAB_INDEX_ALL_DATA -> "所有时间"
                    else -> throw IllegalArgumentException()
                }
            }.attach()
        }
    }

    private inner class PlayRecordsPagerAdapter :
        FragmentStateAdapter(childFragmentManager, viewLifecycleOwner.lifecycle) {
        override fun getItemCount() = 2

        override fun createFragment(position: Int) = ListenRankTabFragment.newInstance(position)
    }
}

