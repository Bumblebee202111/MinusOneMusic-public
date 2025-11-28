package com.github.bumblebee202111.minusonecloudmusic.ui.recentplay

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.github.bumblebee202111.minusonecloudmusic.databinding.FragmentMyRecentPlayBinding
import com.github.bumblebee202111.minusonecloudmusic.ui.common.repeatWithViewLifecycle
import com.google.android.material.tabs.TabLayoutMediator
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch


@AndroidEntryPoint
class MyRecentPlayFragment : Fragment() {

    companion object {
        fun newInstance() = MyRecentPlayFragment()
    }

    private val viewModel: MyRecentPlayViewModel by viewModels()
    lateinit var binding:FragmentMyRecentPlayBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding= FragmentMyRecentPlayBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val viewPager=binding.viewPager
        viewPager.adapter=MyRecentPlayPagerAdapter()
        val tabLayout = binding.tabLayout

        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.text = when(position){
                0-> "歌曲"
                1->"歌单"
                2->"专辑"
                3->"视频"
                else-> error("")
            }
        }.attach()

        val badge=binding.tabLayout.getTabAt(0)!!.orCreateBadge

        repeatWithViewLifecycle {
            launch {
                viewModel.recentPlaySongUiList.collect {
                    badge.number = it?.size ?: 0
                }

            }
        }
        }

    private inner class MyRecentPlayPagerAdapter: FragmentStateAdapter(childFragmentManager,viewLifecycleOwner.lifecycle) {
        override fun getItemCount(): Int {
            return 4
        }

        override fun createFragment(position: Int): Fragment {
           return when(position){
                0->MyRecentPlayMusicFragment.newInstance()
                else->MyRecentPlayMusicFragment.newInstance()

            }
        }

    }
}