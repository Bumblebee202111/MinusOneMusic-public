package com.github.bumblebee202111.minusonecloudmusic.ui.mine

import android.graphics.Color
import android.graphics.drawable.PaintDrawable
import android.view.View
import android.view.ViewGroup
import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidViewBinding
import androidx.core.content.ContextCompat
import androidx.core.graphics.ColorUtils
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.navigation3.runtime.NavKey
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.github.bumblebee202111.minusonecloudmusic.MainActivity
import com.github.bumblebee202111.minusonecloudmusic.R
import com.github.bumblebee202111.minusonecloudmusic.databinding.FragmentMineBinding
import com.github.bumblebee202111.minusonecloudmusic.ui.MainActivityViewModel
import com.github.bumblebee202111.minusonecloudmusic.ui.navigation.LocalMusicRoute
import com.github.bumblebee202111.minusonecloudmusic.ui.navigation.MyCollectionRoute
import com.github.bumblebee202111.minusonecloudmusic.ui.navigation.MyFriendRoute
import com.github.bumblebee202111.minusonecloudmusic.ui.navigation.MyPrivateCloudRoute
import com.github.bumblebee202111.minusonecloudmusic.ui.navigation.MyRecentPlayRoute
import com.github.bumblebee202111.minusonecloudmusic.ui.navigation.PhoneCaptchaLoginRoute
import com.google.android.material.divider.MaterialDividerItemDecoration
import com.google.android.material.tabs.TabLayoutMediator
import kotlin.math.abs
import kotlin.math.min


@Composable
fun MineScreen(
    mineViewModel: MineViewModel = hiltViewModel(LocalActivity.current as MainActivity),
    mainViewModel: MainActivityViewModel = hiltViewModel(LocalActivity.current as MainActivity),
    onOpenDrawer: () -> Unit,
    onNavigate: (NavKey) -> Unit
) {
    val context = LocalContext.current
    val fragmentManager = (context as? FragmentActivity)?.supportFragmentManager
        ?: throw IllegalStateException("MineScreen must be hosted in a FragmentActivity")
    val lifecycle = (context as? androidx.activity.ComponentActivity)?.lifecycle
        ?: throw IllegalStateException("Lifecycle not found")

    val lifecycleOwner = LocalLifecycleOwner.current

    AndroidViewBinding(FragmentMineBinding::inflate, modifier = Modifier.fillMaxSize()) {
        this.lifecycleOwner = lifecycleOwner
        viewModel = mainViewModel
        this.mineViewModel = mineViewModel

        topAppBar.setNavigationOnClickListener { onOpenDrawer() }

        ViewCompat.setOnApplyWindowInsetsListener(appBarLayout) { _, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            val currentStatusBarHeight = systemBars.top
            (topAppBar.layoutParams as ViewGroup.MarginLayoutParams).topMargin = currentStatusBarHeight
            toolbarBackground.layoutParams.height =
                (currentStatusBarHeight + root.resources.getDimensionPixelSize(R.dimen.action_bar_size))
            toolbarBackground.requestLayout()
            WindowInsetsCompat.CONSUMED
        }

        val dragonBallAdapter = DragonBallAdapter(MineDragonBall.PINNED_DRAGON_BALLS) { ball ->
            when (ball.code) {
                MineDragonBall.TYPE_LOCAL_MUSIC -> onNavigate(LocalMusicRoute)
                MineDragonBall.TYPE_CLOUD_DISK -> onNavigate(MyPrivateCloudRoute)
                MineDragonBall.TYPE_RECENT_PLAY -> onNavigate(MyRecentPlayRoute)
                MineDragonBall.TYPE_FOLLOW -> onNavigate(MyFriendRoute)
                MineDragonBall.TYPE_COLLECTION -> onNavigate(MyCollectionRoute)
            }
        }

        dragonBallArea.rvDragonBalls.apply {
            adapter = dragonBallAdapter
            addItemDecoration(
                MaterialDividerItemDecoration(context, DividerItemDecoration.HORIZONTAL).apply {
                    setDividerThicknessResource(context, R.dimen.dragon_ball_spacing)
                    isLastItemDecorated = false
                }
            )
            setHasFixedSize(true)
        }

        appBarLayout.addOnOffsetChangedListener { appBarLayout, verticalOffset ->
            val totalScrollRange = appBarLayout.totalScrollRange
            val fraction = min(abs(verticalOffset).toFloat() / totalScrollRange, 1.0F)
            val interpolation = FastOutSlowInInterpolator().getInterpolation(fraction)

            toolbarBackground.alpha = interpolation

            val isCollapsed = fraction >= 0.4F
            val iconColor = if (isCollapsed) R.color.black else R.color.white
            topAppBar.setNavigationIconTint(context.getColor(iconColor))
            smallView.isVisible = isCollapsed

            val maxRadii = 24F
            val minRadii = 0F
            val radii = maxRadii + fraction * (minRadii - maxRadii) * fraction
            val bgColor =
                ContextCompat.getColor(context, R.color.colorBackgroundAndroid)
            tabLayoutContainer.background = PaintDrawable(bgColor).apply {
                setCornerRadii(floatArrayOf(radii, radii, radii, radii, 0F, 0F, 0F, 0F))
            }
        }

        if (viewPager.adapter == null) {
            viewPager.adapter = MyMusicPagerAdapter(fragmentManager, lifecycle)

            val themeColor = root.resources.getColor(R.color.themeColor, null)
            val selectedTabIndicatorColor = ColorUtils.setAlphaComponent(
                themeColor,
                (Color.alpha(themeColor) * 1)
            )
            tabLayout.setSelectedTabIndicatorColor(selectedTabIndicatorColor)

            TabLayoutMediator(tabLayout, viewPager) { tab, position ->
                tab.setText(TAB_TEXTS[position])
            }.attach()

            val profileOnClickListener = View.OnClickListener {
                if (mainViewModel.loggedInUserId.value == null) {
                    onNavigate(PhoneCaptchaLoginRoute)
                } else {
                }
            }
            displayName.setOnClickListener(profileOnClickListener)
            smallNameTv.setOnClickListener(profileOnClickListener)
            smallAvatar.setOnClickListener(profileOnClickListener)
        }
    }
}

private class MyMusicPagerAdapter(
    fragmentManager: FragmentManager,
    lifecycle: Lifecycle
) : FragmentStateAdapter(fragmentManager, lifecycle) {
    override fun getItemCount() = 2
    override fun createFragment(position: Int) = when (position) {
        0 -> MyMusicTabFragment.newInstance()
        else -> MyFeedTabFragment.newInstance()
    }
}

private val TAB_TEXTS = listOf(R.string.title_music, R.string.title_feed)