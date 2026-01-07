package com.github.bumblebee202111.minusonecloudmusic.ui.mine

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.github.bumblebee202111.minusonecloudmusic.R
import com.github.bumblebee202111.minusonecloudmusic.databinding.FragmentMyMusicTabBinding
import com.github.bumblebee202111.minusonecloudmusic.ui.common.repeatWithViewLifecycle
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayout.OnTabSelectedListener
import com.google.android.material.tabs.TabLayoutMediator
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MyMusicTabFragment : Fragment() {


    private lateinit var binding: FragmentMyMusicTabBinding
    private val mineViewModel: MineViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentMyMusicTabBinding.inflate(inflater, container, false).apply {
            lifecycleOwner = viewLifecycleOwner
            myMusicViewModel = this@MyMusicTabFragment.mineViewModel
        }
        return binding.root
    }

    @SuppressLint("StringFormatMatches")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        val viewPager = binding.viewPager
        viewPager.adapter = MyMusicPlaylistTabsPagerAdapter()
        val tabLayout = binding.tabLayout

        val tabBadgeTextColor = resources.getColor(R.color.colorText4, null)
        val selectedTabBadgeTextColor = resources.getColor(R.color.colorText1, null)

        TabLayoutMediator(tabLayout, viewPager) { tab, position ->

            tab.setText(TAB_TEXTS[position])

            tab.orCreateBadge.apply {
                backgroundColor = resources.getColor(android.R.color.transparent, null)
                badgeTextColor = if (position == 0) selectedTabBadgeTextColor else tabBadgeTextColor
            }
        }.attach()


        tabLayout.addOnTabSelectedListener(object : OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                tab?.badge?.badgeTextColor = selectedTabBadgeTextColor
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {
                tab?.badge?.badgeTextColor = tabBadgeTextColor
            }

            override fun onTabReselected(tab: TabLayout.Tab?) {
            }

        })

        repeatWithViewLifecycle {
            launch {
                mineViewModel.myPlaylistTabs.collect {
                    it?.let { tabs ->
                        UserPlaylistTab.entries.forEachIndexed { index, userPlaylistTab ->
                            tabLayout.getTabAt(index)?.badge?.number =
                                tabs[userPlaylistTab]?.size ?: 0
                        }
                    }
                }
            }
        }
    }


    private inner class MyMusicPlaylistTabsPagerAdapter :
        FragmentStateAdapter(childFragmentManager, viewLifecycleOwner.lifecycle) {
        override fun getItemCount() =
            UserPlaylistTab.entries.size

        override fun createFragment(position: Int) = UserPlaylistTabFragment.newInstance(
            UserPlaylistTab.entries[position]
        )
    }

    companion object {
        fun newInstance() = MyMusicTabFragment()

        val TAB_TEXTS = listOf(
            R.string.my_music_title_created,
            R.string.title_my_music_tab_collected,
            R.string.title_my_music_tab_albums
        )
    }

}