package com.github.bumblebee202111.minusonecloudmusic.ui.friend

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.github.bumblebee202111.minusonecloudmusic.databinding.FragmentMyFriendBinding
import com.google.android.material.tabs.TabLayoutMediator
import dagger.hilt.android.AndroidEntryPoint
@AndroidEntryPoint
class MyFriendFragment : Fragment() {

    lateinit var binding:FragmentMyFriendBinding
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding= FragmentMyFriendBinding.inflate(inflater,container,false).apply {
            lifecycleOwner=viewLifecycleOwner
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val tabLayout=binding.tabLayout
        val pager=binding.pager.apply {
            adapter=MyFriendFragmentPagerAdapter()
        }
        TabLayoutMediator(tabLayout,pager){ tab, i ->
            when(i){
                0->tab.text="关注"
                1->tab.text="粉丝"
            }
        }.attach()

    }
    companion object {
        @JvmStatic
        fun newInstance()=
            MyFriendFragment()
    }
    private inner class MyFriendFragmentPagerAdapter:FragmentStateAdapter(childFragmentManager,viewLifecycleOwner.lifecycle){
        override fun getItemCount(): Int {
           return 2
        }

        override fun createFragment(position: Int): Fragment {
            return when(position){
                0-> FollowFragment.newInstance()
                1 ->FansFragment.newInstance()
                else -> throw IndexOutOfBoundsException()
            }
        }

    }
}
