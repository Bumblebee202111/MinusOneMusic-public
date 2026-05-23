package com.github.bumblebee202111.minusonecloudmusic.ui.mine

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.dropShadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.graphics.shadow.Shadow
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.layout
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.NavKey
import coil3.compose.AsyncImage
import com.github.bumblebee202111.minusonecloudmusic.R
import com.github.bumblebee202111.minusonecloudmusic.ui.MainActivityViewModel
import com.github.bumblebee202111.minusonecloudmusic.ui.navigation.LocalMusicRoute
import com.github.bumblebee202111.minusonecloudmusic.ui.navigation.MyCollectionRoute
import com.github.bumblebee202111.minusonecloudmusic.ui.navigation.MyFriendRoute
import com.github.bumblebee202111.minusonecloudmusic.ui.navigation.MyPrivateCloudRoute
import com.github.bumblebee202111.minusonecloudmusic.ui.navigation.MyRecentPlayRoute
import com.github.bumblebee202111.minusonecloudmusic.ui.navigation.PhoneCaptchaLoginRoute
import com.github.bumblebee202111.minusonecloudmusic.ui.theme.DolphinTheme
import com.github.bumblebee202111.minusonecloudmusic.utils.imageUrl
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.min
import kotlin.math.roundToInt

private val FOSI = FastOutSlowInInterpolator()


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MineScreen(
    mineViewModel: MineViewModel = hiltViewModel(),
    mainViewModel: MainActivityViewModel = hiltViewModel(),
    onOpenDrawer: () -> Unit,
    onNavigate: (NavKey) -> Unit
) {
    val pagerState = rememberPagerState(pageCount = { 2 })
    val scope = rememberCoroutineScope()

    val loggedInUserProfile by mainViewModel.loggedInUserProfile.collectAsStateWithLifecycle()
    val myProfile by mineViewModel.myProfile.collectAsStateWithLifecycle()

    val density = LocalDensity.current
    val insets = WindowInsets.systemBars
    val topInsetPx = with(density) { insets.getTop(density).toFloat() }
    val topInset = with(density) { topInsetPx.toDp() }

    val actionBarHeight = dimensionResource(id = R.dimen.action_bar_size)
    val toolbarHeightPx = topInsetPx + with(density) { actionBarHeight.toPx() }
    val tabHeightPx = with(density) { 44.dp.toPx() }

    var headerHeightPx by remember { mutableFloatStateOf(0f) }
    var offsetPx by remember { mutableFloatStateOf(0f) }

    val minOffsetPx = if (headerHeightPx > 0) -(headerHeightPx - toolbarHeightPx) else 0f
    val pinnedHeightPx = toolbarHeightPx + tabHeightPx

    val nestedScrollConnection = remember(minOffsetPx) {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                val delta = available.y
                if (delta < 0 && minOffsetPx < 0) {
                    val newOffset = offsetPx + delta
                    val previousOffset = offsetPx
                    offsetPx = newOffset.coerceIn(minOffsetPx, 0f)
                    val consumed = offsetPx - previousOffset
                    return Offset(0f, consumed)
                }
                return Offset.Zero
            }

            override fun onPostScroll(
                consumed: Offset,
                available: Offset,
                source: NestedScrollSource
            ): Offset {
                val delta = available.y
                if (delta > 0 && minOffsetPx < 0) {
                    val newOffset = offsetPx + delta
                    val previousOffset = offsetPx
                    offsetPx = newOffset.coerceIn(minOffsetPx, 0f)
                    val consumedY = offsetPx - previousOffset
                    return Offset(0f, consumedY)
                }
                return Offset.Zero
            }
        }
    }

    val headerDraggableState = rememberDraggableState { delta ->
        val newOffset = offsetPx + delta
        offsetPx = newOffset.coerceIn(minOffsetPx, 0f)
    }

    val onProfileClick: () -> Unit = {
        if (mainViewModel.loggedInUserId.value == null) {
            onNavigate(PhoneCaptchaLoginRoute)
        } else {
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colorResource(id = R.color.colorBackgroundAndroid))
            .nestedScroll(nestedScrollConnection)
    ) {
        AsyncImage(
            model = loggedInUserProfile?.backgroundUrl?.imageUrl(
                thumbnailSize = null,
                quality = 80
            ),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            placeholder = ColorPainter(Color(0xFF4D1414)),
            error = ColorPainter(Color(0xFF4D1414)),
            modifier = Modifier
                .fillMaxWidth()
                .layout { measurable, constraints ->
                    val placeable = measurable.measure(constraints)
                    layout(placeable.width, placeable.height) {
                        placeable.place(0, offsetPx.roundToInt())
                    }
                }
                .height(with(density) { headerHeightPx.toDp() } + 50.dp)
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .draggable(
                    orientation = Orientation.Vertical,
                    state = headerDraggableState
                )
                .layout { measurable, constraints ->
                    val placeable = measurable.measure(constraints)
                    layout(placeable.width, placeable.height) {
                        placeable.place(0, offsetPx.roundToInt())
                    }
                }
                .onGloballyPositioned { coordinates ->
                    headerHeightPx = coordinates.size.height.toFloat()
                }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = topInset + 80.dp, bottom = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(77.3.dp)
                        .dropShadow(
                            shape = CircleShape,
                            shadow = Shadow(
                                radius = 6.dp,
                                color = Color(0x3E000000),
                                offset = DpOffset(0.dp, 1.dp)
                            )
                        )
                        .background(Color.White, CircleShape)
                        .clip(CircleShape)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = onProfileClick
                        )
                ) {
                    AsyncImage(
                        model = loggedInUserProfile?.avatarUrl?.imageUrl(
                            thumbnailSize = 148,
                            quality = 80
                        ),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        placeholder = painterResource(id = R.drawable.fgw),
                        error = painterResource(id = R.drawable.fgw),
                        modifier = Modifier.fillMaxSize()
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = myProfile?.displayName ?: "立即登录",
                    color = Color.White,
                    fontSize = 16.sp,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    modifier = Modifier.clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onProfileClick
                    )
                )

                Spacer(modifier = Modifier.height(20.dp))
                Box(modifier = Modifier.padding(horizontal = 16.dp)) {
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
            }
        }
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxWidth()
                .layout { measurable, constraints ->
                    val listHeight = constraints.maxHeight - pinnedHeightPx.roundToInt()
                    val placeable = measurable.measure(
                        constraints.copy(minHeight = listHeight, maxHeight = listHeight)
                    )
                    layout(placeable.width, placeable.height) {
                        val yPosition = (headerHeightPx + tabHeightPx + offsetPx).roundToInt()
                        placeable.place(0, yPosition)
                    }
                }
                .background(colorResource(id = R.color.colorBackgroundAndroid))
        ) { page ->
            when (page) {
                0 -> MyMusicTabScreen(
                    viewModel = mineViewModel,
                    onNavigate = onNavigate
                )

                else -> Box(Modifier.fillMaxSize())
            }
        }
        val totalScrollRange = abs(minOffsetPx)
        val fraction = if (totalScrollRange > 0) min(abs(offsetPx) / totalScrollRange, 1.0f) else 0f
        val interpolation = FOSI.getInterpolation(fraction)
        val isCollapsed = fraction >= 0.4f
        val maxRadiiDp = 24.dp
        val minRadiiDp = 0.dp
        val radiiDp = maxRadiiDp + (minRadiiDp - maxRadiiDp) * fraction * fraction

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .layout { measurable, constraints ->
                    val placeable = measurable.measure(constraints)
                    layout(placeable.width, placeable.height) {
                        val yPosition = (headerHeightPx + offsetPx).roundToInt()
                        placeable.place(0, yPosition)
                    }
                }
                .height(44.dp)
                .background(
                    color = colorResource(id = R.color.colorBackgroundAndroid),
                    shape = RoundedCornerShape(topStart = radiiDp, topEnd = radiiDp)
                ),
            contentAlignment = Alignment.Center
        ) {
            PrimaryTabRow(
                selectedTabIndex = pagerState.currentPage,
                modifier = Modifier
                    .width(140.dp)
                    .height(44.dp),
                containerColor = Color.Transparent,
                divider = {},
                indicator = {
                    Box(
                        modifier = Modifier
                            .tabIndicatorOffset(pagerState.currentPage, matchContentSize = false)
                            .fillMaxSize(),
                        contentAlignment = Alignment.BottomCenter
                    ) {
                        Box(
                            modifier = Modifier
                                .width(15.dp)
                                .height(3.dp)
                                .background(
                                    color = colorResource(id = R.color.colorPrimary1),
                                    shape = RoundedCornerShape(3.dp)
                                )
                        )
                    }
                }
            ) {
                TAB_TEXTS.forEachIndexed { index, resId ->
                    val selected = pagerState.currentPage == index
                    Tab(
                        selected = selected,
                        onClick = {
                            scope.launch { pagerState.animateScrollToPage(index) }
                        },
                        modifier = Modifier.height(44.dp),
                        interactionSource = remember { MutableInteractionSource() }
                    ) {
                        Text(
                            text = stringResource(id = resId),
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (selected) DolphinTheme.colors.text1 else DolphinTheme.colors.text4
                        )
                    }
                }
            }
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(with(density) { toolbarHeightPx.toDp() })
                .alpha(interpolation)
                .background(colorResource(id = R.color.colorBackgroundAndroid))
        )
        val iconTint = if (isCollapsed) Color.Black else Color.White
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = topInset)
                .height(actionBarHeight)
        ) {
            IconButton(
                onClick = onOpenDrawer,
                modifier = Modifier.align(Alignment.CenterStart)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_menu),
                    contentDescription = null,
                    tint = iconTint
                )
            }
            if (isCollapsed) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = onProfileClick
                        )
                ) {
                    AsyncImage(
                        model = myProfile?.avatarUrl?.imageUrl(thumbnailSize = 100, quality = 80),
                        contentDescription = null,
                        placeholder = painterResource(id = R.drawable.fgw),
                        error = painterResource(id = R.drawable.fgw),
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(25.dp)
                            .clip(CircleShape)
                    )
                    Text(
                        text = myProfile?.displayName ?: "Login",
                        color = DolphinTheme.colors.neutral8_1,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
            }
        }
    }
}

private val TAB_TEXTS = listOf(R.string.title_music, R.string.title_feed)