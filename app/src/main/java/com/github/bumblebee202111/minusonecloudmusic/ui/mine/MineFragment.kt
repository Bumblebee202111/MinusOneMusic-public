package com.github.bumblebee202111.minusonecloudmusic.ui.mine

import android.graphics.Color
import android.graphics.drawable.PaintDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.graphics.ColorUtils
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.github.bumblebee202111.minusonecloudmusic.MainActivity
import com.github.bumblebee202111.minusonecloudmusic.R
import com.github.bumblebee202111.minusonecloudmusic.databinding.FragmentMineBinding
import com.github.bumblebee202111.minusonecloudmusic.ui.MainActivityViewModel
import com.github.bumblebee202111.minusonecloudmusic.ui.common.statusBarHeight
import com.github.bumblebee202111.minusonecloudmusic.ui.mine.MineDragonBall.Companion.PINNED_DRAGON_BALLS
import com.github.bumblebee202111.minusonecloudmusic.ui.mine.MineDragonBall.Companion.TYPE_CLOUD_DISK
import com.github.bumblebee202111.minusonecloudmusic.ui.mine.MineDragonBall.Companion.TYPE_COLLECTION
import com.github.bumblebee202111.minusonecloudmusic.ui.mine.MineDragonBall.Companion.TYPE_FOLLOW
import com.github.bumblebee202111.minusonecloudmusic.ui.mine.MineDragonBall.Companion.TYPE_LOCAL_MUSIC
import com.github.bumblebee202111.minusonecloudmusic.ui.mine.MineDragonBall.Companion.TYPE_RECENT_PLAY
import com.github.bumblebee202111.minusonecloudmusic.ui.navigation.LocalMusicRoute
import com.github.bumblebee202111.minusonecloudmusic.ui.navigation.MyCollectionRoute
import com.github.bumblebee202111.minusonecloudmusic.ui.navigation.MyFriendRoute
import com.github.bumblebee202111.minusonecloudmusic.ui.navigation.MyPrivateCloudRoute
import com.github.bumblebee202111.minusonecloudmusic.ui.navigation.MyRecentPlayRoute
import com.github.bumblebee202111.minusonecloudmusic.ui.navigation.NavigationManager
import com.github.bumblebee202111.minusonecloudmusic.ui.navigation.PhoneCaptchaLoginRoute
import com.google.android.material.divider.MaterialDividerItemDecoration
import com.google.android.material.tabs.TabLayoutMediator
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlin.math.abs
import kotlin.math.min



@AndroidEntryPoint
class MineFragment : Fragment() {

    private lateinit var binding: FragmentMineBinding
    private val mineViewModel: MineViewModel by viewModels()
    private val mainActivityViewModel: MainActivityViewModel by activityViewModels()

    @Inject
    lateinit var navigationManager: NavigationManager

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentMineBinding.inflate(inflater, container, false).apply {
            lifecycleOwner = viewLifecycleOwner
            viewModel = this@MineFragment.mainActivityViewModel
            mineViewModel = this@MineFragment.mineViewModel
        }
        val root = binding.root

        binding.topAppBar.setNavigationOnClickListener {
             (activity as? MainActivity)?.openDrawer()
       }
        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        ViewCompat.setOnApplyWindowInsetsListener(binding.appBarLayout) { _, insets ->
            (binding.topAppBar.layoutParams as ViewGroup.MarginLayoutParams).topMargin =
                insets.getInsets(WindowInsetsCompat.Type.systemBars()).top
            WindowInsetsCompat.CONSUMED
        }

        val displayName = binding.displayName
        val smallNameTv = binding.smallNameTv
        val smallAvatar = binding.smallAvatar

        val dragonBallAdapter = DragonBallAdapter(PINNED_DRAGON_BALLS) { myMusicDragonBall ->
            when (myMusicDragonBall.code) {
                TYPE_LOCAL_MUSIC -> {
                    navigationManager.navigate(LocalMusicRoute)
                }

                TYPE_CLOUD_DISK -> {
                    navigationManager.navigate(MyPrivateCloudRoute)
                }

                TYPE_RECENT_PLAY -> {
                    navigationManager.navigate(MyRecentPlayRoute)
                }

                TYPE_FOLLOW -> {
                    navigationManager.navigate(MyFriendRoute)
                }

                TYPE_COLLECTION -> {
                    navigationManager.navigate(MyCollectionRoute)
                }
            }
        }


        binding.dragonBallArea.rvDragonBalls.apply {
            adapter = dragonBallAdapter
            addItemDecoration(
                MaterialDividerItemDecoration(
                    requireContext(),
                    DividerItemDecoration.HORIZONTAL
                ).apply {
                    setDividerThicknessResource(context, R.dimen.dragon_ball_spacing)
                    isLastItemDecorated = false
                })
            setHasFixedSize(true)
        }


        val toolbarBackground = binding.toolbarBackground
        val smallInfoView = binding.smallView

        toolbarBackground.layoutParams.height =
            (statusBarHeight + resources.getDimensionPixelSize(R.dimen.action_bar_size))


        binding.appBarLayout.addOnOffsetChangedListener { appBarLayout, verticalOffset ->
            val totalScrollRange = appBarLayout.totalScrollRange
            val fraction = min(abs(verticalOffset).toFloat() / totalScrollRange, 1.0F)

            val timeInterpolator = FastOutSlowInInterpolator()
            val interpolation = timeInterpolator.getInterpolation(fraction)
            toolbarBackground.alpha = 0.0F + interpolation * (1.0F - 0.0F)

            if (fraction >= 0.4F) {
                binding.topAppBar.setNavigationIconTint(requireContext().getColor(R.color.black))
                smallInfoView.isVisible = true
            } else {
                binding.topAppBar.setNavigationIconTint(requireContext().getColor(R.color.white))
                smallInfoView.isVisible = false
            }

            val tabLayoutContainer = binding.tabLayoutContainer
            val maxRadii = 24F
            val minRadii = 0F
            val radii = maxRadii + fraction * (minRadii - maxRadii) * fraction
            val background =
                ContextCompat.getColor(requireContext(), R.color.colorBackgroundAndroid)
            val tabLayoutBackground = PaintDrawable(background).apply {
                setCornerRadii(floatArrayOf(radii, radii, radii, radii, 0F, 0F, 0F, 0F))
            }
            tabLayoutContainer.background = tabLayoutBackground
        }

        val viewPager = binding.viewPager
        viewPager.adapter = MyMusicPagerAdapter()

        val tabLayout = binding.tabLayout

        val themeColor = resources.getColor(R.color.themeColor, null)
        val selectedTabIndicatorColor = ColorUtils.setAlphaComponent(
            themeColor,
            (Color.alpha(themeColor) * 1)
        )
        tabLayout.setSelectedTabIndicatorColor(selectedTabIndicatorColor)

        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.setText(TAB_TEXTS[position])
        }.attach()

        val profileOnClickListener = View.OnClickListener {
            if (mainActivityViewModel.loggedInUserId.value == null) {
                navigationManager.navigate(PhoneCaptchaLoginRoute)
            } else {

            }
        }

        displayName.setOnClickListener(profileOnClickListener)
        smallNameTv.setOnClickListener(profileOnClickListener)
        smallAvatar.setOnClickListener(profileOnClickListener)

    }


    private inner class MyMusicPagerAdapter :
        FragmentStateAdapter(childFragmentManager, viewLifecycleOwner.lifecycle) {
        override fun getItemCount() = 2

        override fun createFragment(position: Int) = when (position) {
            0 -> MyMusicTabFragment.newInstance()
            else -> MyFeedTabFragment.newInstance()
        }
    }

    companion object{
        val TAB_TEXTS= listOf(R.string.title_music,R.string.title_feed)
    }

}
