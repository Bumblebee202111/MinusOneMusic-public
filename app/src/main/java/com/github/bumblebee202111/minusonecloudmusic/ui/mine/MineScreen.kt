package com.github.bumblebee202111.minusonecloudmusic.ui.mine

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.rememberNestedScrollInteropConnection
import androidx.compose.ui.viewinterop.AndroidViewBinding
import androidx.core.content.ContextCompat
import androidx.core.graphics.ColorUtils
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.navigation3.runtime.NavKey
import com.github.bumblebee202111.minusonecloudmusic.R
import com.github.bumblebee202111.minusonecloudmusic.databinding.FragmentMineBinding
import com.github.bumblebee202111.minusonecloudmusic.ui.MainActivityViewModel
import com.github.bumblebee202111.minusonecloudmusic.ui.navigation.LocalMusicRoute
import com.github.bumblebee202111.minusonecloudmusic.ui.navigation.MyCollectionRoute
import com.github.bumblebee202111.minusonecloudmusic.ui.navigation.MyFriendRoute
import com.github.bumblebee202111.minusonecloudmusic.ui.navigation.MyPrivateCloudRoute
import com.github.bumblebee202111.minusonecloudmusic.ui.navigation.MyRecentPlayRoute
import com.github.bumblebee202111.minusonecloudmusic.ui.navigation.PhoneCaptchaLoginRoute
import com.google.android.material.tabs.TabLayout
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.min

private val FOSI = FastOutSlowInInterpolator()


@Composable
fun MineScreen(
    mineViewModel: MineViewModel = hiltViewModel(),
    mainViewModel: MainActivityViewModel = hiltViewModel(),
    onOpenDrawer: () -> Unit,
    onNavigate: (NavKey) -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val pagerState = rememberPagerState(pageCount = { 2 })
    val scope = rememberCoroutineScope()

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

        dragonBallArea.rvDragonBalls.setContent {
            DragonBallRow(
                dragonBalls = MineDragonBall.PINNED_DRAGON_BALLS,
                onItemClick = { ball ->
                    when (ball.code) {
                        MineDragonBall.TYPE_LOCAL_MUSIC -> onNavigate(LocalMusicRoute)
                        MineDragonBall.TYPE_CLOUD_DISK -> onNavigate(MyPrivateCloudRoute)
                        MineDragonBall.TYPE_RECENT_PLAY -> onNavigate(MyRecentPlayRoute)
                        MineDragonBall.TYPE_FOLLOW -> onNavigate(MyFriendRoute)
                        MineDragonBall.TYPE_COLLECTION -> onNavigate(MyCollectionRoute)
                    }
                }
            )
        }

        val tabBgDrawable = GradientDrawable().apply {
            setColor(ContextCompat.getColor(context, R.color.colorBackgroundAndroid))
        }
        tabLayoutContainer.background = tabBgDrawable

        appBarLayout.addOnOffsetChangedListener { appBarLayout, verticalOffset ->
            val totalScrollRange = appBarLayout.totalScrollRange
            val fraction = min(abs(verticalOffset).toFloat() / totalScrollRange, 1.0F)
            val interpolation = FOSI.getInterpolation(fraction)

            toolbarBackground.alpha = interpolation

            val isCollapsed = fraction >= 0.4F
            val iconColor = if (isCollapsed) R.color.black else R.color.white
            topAppBar.setNavigationIconTint(root.context.getColor(iconColor))
            smallView.isVisible = isCollapsed

            val maxRadii = 24F
            val minRadii = 0F
            val radii = maxRadii + fraction * (minRadii - maxRadii) * fraction
            tabBgDrawable.cornerRadii = floatArrayOf(radii, radii, radii, radii, 0F, 0F, 0F, 0F)
        }

        val themeColor = root.resources.getColor(R.color.themeColor, null)
        val selectedTabIndicatorColor = ColorUtils.setAlphaComponent(
            themeColor,
            (Color.alpha(themeColor) * 1)
        )
        tabLayout.setSelectedTabIndicatorColor(selectedTabIndicatorColor)

        if (tabLayout.tabCount != TAB_TEXTS.size) {
            tabLayout.removeAllTabs()
            TAB_TEXTS.forEach {
                tabLayout.addTab(tabLayout.newTab())
            }
        }
        TAB_TEXTS.forEachIndexed { index, resId ->
            tabLayout.getTabAt(index)?.setText(resId)
        }

        tabLayout.clearOnTabSelectedListeners()
        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                tab?.let {
                    if (pagerState.currentPage != it.position) {
                        scope.launch { pagerState.animateScrollToPage(it.position) }
                    }
                }
            }
            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })

        val profileOnClickListener = View.OnClickListener {
            if (mainViewModel.loggedInUserId.value == null) {
                onNavigate(PhoneCaptchaLoginRoute)
            } else {
            }
        }
        displayName.setOnClickListener(profileOnClickListener)
        smallNameTv.setOnClickListener(profileOnClickListener)
        smallAvatar.setOnClickListener(profileOnClickListener)

        composeView.setContent {
            LaunchedEffect(pagerState.currentPage) {
                tabLayout.getTabAt(pagerState.currentPage)?.select()
            }

            val nestedScrollConnection = rememberNestedScrollInteropConnection(LocalView.current)
            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .nestedScroll(nestedScrollConnection)
                    .fillMaxSize()
            ) { page ->
                when (page) {
                    0 -> MyMusicTabScreen(
                        viewModel = mineViewModel,
                        onNavigate = onNavigate
                    )
                    else -> {
                        Box(Modifier.fillMaxSize())
                    }
                }
            }
        }
    }
}

private val TAB_TEXTS = listOf(R.string.title_music, R.string.title_feed)