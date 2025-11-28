package com.github.bumblebee202111.minusonecloudmusic.ui.mycollection

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.github.bumblebee202111.minusonecloudmusic.databinding.FragmentMyCollectionBinding
import com.google.android.material.tabs.TabLayoutMediator
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class MyCollectionFragment : Fragment() {

    lateinit var binding:FragmentMyCollectionBinding
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding= FragmentMyCollectionBinding.inflate(inflater,container,false).apply {
            lifecycleOwner=viewLifecycleOwner
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val tabLayout=binding.tabLayout
        val pager=binding.pager.apply {
            adapter=MyCollectionFragmentPagerAdapter()
        }
        TabLayoutMediator(tabLayout,pager){ tab, i ->
            when(i){
                0->tab.text="专辑"
                1->tab.text="MV"
            }
        }.attach()

    }
    companion object {
        @JvmStatic
        fun newInstance()=
            MyCollectionFragment()
    }
    private inner class MyCollectionFragmentPagerAdapter:FragmentStateAdapter(childFragmentManager,viewLifecycleOwner.lifecycle){
        override fun getItemCount(): Int {
           return 2
        }

        override fun createFragment(position: Int): Fragment {
            return when(position){
                0-> MyAlbumFragment.newInstance()
                1 ->CollectedMvListFragment.newInstance()
                else -> throw IndexOutOfBoundsException()
            }
        }

    }
}
