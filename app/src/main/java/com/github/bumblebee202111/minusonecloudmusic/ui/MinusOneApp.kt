package com.github.bumblebee202111.minusonecloudmusic.ui

import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import androidx.activity.SystemBarStyle
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toDrawable
import androidx.core.graphics.toColorInt
import androidx.core.view.GravityCompat
import androidx.core.view.forEach
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.ui.NavDisplay
import com.github.bumblebee202111.minusonecloudmusic.R
import com.github.bumblebee202111.minusonecloudmusic.ui.common.BottomNavigationIconsUtils
import com.github.bumblebee202111.minusonecloudmusic.ui.common.DolphinToast
import com.github.bumblebee202111.minusonecloudmusic.ui.common.MainDrawerContent
import com.github.bumblebee202111.minusonecloudmusic.ui.common.MiniBarContainer
import com.github.bumblebee202111.minusonecloudmusic.ui.common.ToastManager
import com.github.bumblebee202111.minusonecloudmusic.ui.navigation.DailyRecommendRoute
import com.github.bumblebee202111.minusonecloudmusic.ui.navigation.DiscoverRoute
import com.github.bumblebee202111.minusonecloudmusic.ui.navigation.FriendTracksRoute
import com.github.bumblebee202111.minusonecloudmusic.ui.navigation.ListenRankRoute
import com.github.bumblebee202111.minusonecloudmusic.ui.navigation.LocalMusicRoute
import com.github.bumblebee202111.minusonecloudmusic.ui.navigation.MineRoute
import com.github.bumblebee202111.minusonecloudmusic.ui.navigation.MyPrivateCloudRoute
import com.github.bumblebee202111.minusonecloudmusic.ui.navigation.MyRecentPlayRoute
import com.github.bumblebee202111.minusonecloudmusic.ui.navigation.NavigationManager
import com.github.bumblebee202111.minusonecloudmusic.ui.navigation.Navigator
import com.github.bumblebee202111.minusonecloudmusic.ui.navigation.NowPlayingRoute
import com.github.bumblebee202111.minusonecloudmusic.ui.navigation.PlaylistRoute
import com.github.bumblebee202111.minusonecloudmusic.ui.navigation.PlaylistV4Route
import com.github.bumblebee202111.minusonecloudmusic.ui.navigation.SearchRoute
import com.github.bumblebee202111.minusonecloudmusic.ui.navigation.V6PlaylistRoute
import com.github.bumblebee202111.minusonecloudmusic.ui.navigation.createAppEntryProvider
import com.github.bumblebee202111.minusonecloudmusic.ui.navigation.rememberNavigationState
import com.github.bumblebee202111.minusonecloudmusic.ui.navigation.toEntries
import com.github.bumblebee202111.minusonecloudmusic.ui.playerhistory.PlayerListDialog2
import com.github.bumblebee202111.minusonecloudmusic.ui.theme.DolphinTheme
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

private val bottomNavMap = mapOf(
    R.id.nav_main to DiscoverRoute,
    R.id.nav_track to FriendTracksRoute,
    R.id.nav_mine to MineRoute
)

private val nonTopLevelMiniBarRoutes = setOf(
    DailyRecommendRoute::class,
    ListenRankRoute::class,
    LocalMusicRoute::class,
    MyPrivateCloudRoute::class,
    MyRecentPlayRoute::class,
    PlaylistRoute::class,
    PlaylistV4Route::class,
    V6PlaylistRoute::class,
    SearchRoute::class
)
private data class NavigationTabUiState(
    val tabCode: String,
    val tabTitle: String,
    val route: NavKey
)
private val textOnlyTabs = listOf(
    NavigationTabUiState(tabCode = "main", tabTitle = "首页", route = DiscoverRoute),
    NavigationTabUiState(tabCode = "follow", tabTitle = "关注", route = FriendTracksRoute),
    NavigationTabUiState(tabCode = "mine", tabTitle = "我的", route = MineRoute)
)
val LocalMiniPlayerBarHeight = compositionLocalOf { 0.dp }

@Composable
fun MinusOneApp(
    navigationManager: NavigationManager,
    toastManager: ToastManager,
    initialDeepLinkKey: NavKey?,
    onLogout: () -> Unit,
    onUpdateSystemBarStyle: (SystemBarStyle) -> Unit
) {
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    val topLevelRoutes = setOf(DiscoverRoute, FriendTracksRoute, MineRoute)

    val state = rememberNavigationState(
        startRoute = DiscoverRoute,
        topLevelRoutes = topLevelRoutes
    )
    val navigator = remember { Navigator(state) }

    var showPlayerHistory by remember { mutableStateOf(false) }

    val currentKey by remember {
        derivedStateOf {
            val currentTopLevel = state.topLevelRoute
            val currentStack = state.backStacks[currentTopLevel]
            currentStack?.lastOrNull() ?: currentTopLevel
        }
    }

    val isTopLevel by remember {
        derivedStateOf { currentKey in topLevelRoutes }
    }


    val isMiniBarVisible by remember {
        derivedStateOf {
            isTopLevel || currentKey::class in nonTopLevelMiniBarRoutes
        }
    }
    var miniPlayerBarHeight by remember { mutableStateOf(0.dp) }
    val density = LocalDensity.current


    LaunchedEffect(toastManager) {
        toastManager.uiTextEvent.collect { event ->
            event?.let {
                val messageText = it.message.asString(context)
                val result = snackbarHostState.showSnackbar(messageText)
                if (result == SnackbarResult.Dismissed || result == SnackbarResult.ActionPerformed) {
                    toastManager.onMessageShown()
                }
            }
        }
    }

    LaunchedEffect(initialDeepLinkKey) {
        if (initialDeepLinkKey != null) navigator.navigate(initialDeepLinkKey)
    }

    DisposableEffect(currentKey) {
        val style = if (currentKey == NowPlayingRoute || currentKey == DailyRecommendRoute) {
            SystemBarStyle.dark(android.graphics.Color.TRANSPARENT)
        } else {
            SystemBarStyle.auto(android.graphics.Color.TRANSPARENT, android.graphics.Color.TRANSPARENT)
        }
        onUpdateSystemBarStyle(style)
        onDispose {}
    }

    LaunchedEffect(Unit) {
        launch {
            navigationManager.navActions.collect { route ->
                navigator.navigate(route)
            }
        }
        launch {
            navigationManager.backActions.collect {
                navigator.goBack()
            }
        }
    }

    MainDrawerLayout(
        isTopLevel = isTopLevel,
        drawerEvents = navigationManager.drawerEvents,
        onNavigate = { navigator.navigate(it) },
        onLogout = onLogout
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(modifier = Modifier.fillMaxSize()) {
                Box(modifier = Modifier.weight(1f)) {
                    CompositionLocalProvider(LocalMiniPlayerBarHeight provides miniPlayerBarHeight) {
                        NavDisplay(
                            entries = state.toEntries(createAppEntryProvider(navigationManager)),
                            onBack = { navigator.goBack() },
                            modifier = Modifier
                                .fillMaxSize()
                                .clipToBounds()
                        )
                    }
                    if (isMiniBarVisible) {
                        MiniBarContainer(
                            onNavigateToNowPlaying = { navigationManager.navigate(NowPlayingRoute) },
                            onShowPlaylist = { showPlayerHistory = true },
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .fillMaxWidth()
                                .onGloballyPositioned { coordinates ->
                                    miniPlayerBarHeight = with(density) { coordinates.size.height.toDp() }
                                }
                                .then(if (!isTopLevel) Modifier.navigationBarsPadding() else Modifier)
                        )
                    } else {
                        miniPlayerBarHeight = 0.dp
                    }
                }
                if (isTopLevel) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        HorizontalDivider(color = DolphinTheme.colors.text7)

                        val useTextOnlyMode = true
                        if (useTextOnlyMode) {
                            NavigationTabLayout(
                                currentKey = currentKey,
                                onNavigate = { navigator.navigate(it) }
                            )
                        } else {
                            MainBottomNavigation(
                                currentKey = currentKey,
                                onNavigate = { navigator.navigate(it) }
                            )
                        }
                    }
                }
            }
            if (showPlayerHistory) {
                PlayerListDialog2(
                    onDismissRequest = { showPlayerHistory = false }
                )
            }

            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .statusBarsPadding(),
                snackbar = { data -> DolphinToast(data.visuals.message) }
            )
        }
    }
}

@Composable
private fun MainDrawerLayout(
    isTopLevel: Boolean,
    drawerEvents: Flow<Unit>,
    onNavigate: (NavKey) -> Unit,
    onLogout: () -> Unit,
    content: @Composable () -> Unit
) {
    val drawerLayoutRef = remember { mutableStateOf<DrawerLayout?>(null) }
    var isDrawerOpen by remember { mutableStateOf(false) }

    BackHandler(enabled = isDrawerOpen) {
        drawerLayoutRef.value?.closeDrawer(GravityCompat.START)
    }

    LaunchedEffect(drawerEvents) {
        drawerEvents.collect {
            drawerLayoutRef.value?.openDrawer(GravityCompat.START)
        }
    }
    AndroidView(
        factory = { context ->
            DrawerLayout(context).apply {
                id = View.generateViewId()
                layoutParams = ViewGroup.LayoutParams(MATCH_PARENT, MATCH_PARENT)

                addDrawerListener(object : DrawerLayout.SimpleDrawerListener() {
                    override fun onDrawerOpened(drawerView: View) {
                        isDrawerOpen = true
                    }

                    override fun onDrawerClosed(drawerView: View) {
                        isDrawerOpen = false
                    }

                    override fun onDrawerStateChanged(newState: Int) {
                        isDrawerOpen = isDrawerOpen(GravityCompat.START)
                    }
                })
                val contentView = ComposeView(context).apply {
                    layoutParams = DrawerLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT)
                    setContent {
                        content()
                    }
                }
                addView(contentView)
                val drawerView = ComposeView(context).apply {
                    layoutParams = DrawerLayout.LayoutParams(
                        (context.resources.displayMetrics.widthPixels * 0.84).toInt(),
                        MATCH_PARENT
                    ).apply {
                        gravity = GravityCompat.START
                    }
                    setContent {
                        MainDrawerContent(
                            onNavigate = { route ->
                                closeDrawer(GravityCompat.START)
                                onNavigate(route)
                            },
                            onLogout = {
                                closeDrawer(GravityCompat.START)
                                onLogout()
                            }
                        )
                    }
                }
                addView(drawerView)

                drawerLayoutRef.value = this
            }
        },
        update = { view ->
            val lockMode = if (isTopLevel) {
                DrawerLayout.LOCK_MODE_UNLOCKED
            } else {
                DrawerLayout.LOCK_MODE_LOCKED_CLOSED
            }
            view.setDrawerLockMode(lockMode)
        }
    )
}
@Composable
private fun NavigationTabLayout(
    currentKey: NavKey?,
    onNavigate: (NavKey) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .height(dimensionResource(R.dimen.bottom_nav_view_height))
            .background(Color(0xFFFCFDFF)),
        verticalAlignment = Alignment.Bottom
    ) {
        textOnlyTabs.forEach { tabInfo ->
            val isSelected = currentKey == tabInfo.route
            NavigationTabItem(
                tabInfo = tabInfo,
                isSelected = isSelected,
                onClick = { onNavigate(tabInfo.route) },
                modifier = Modifier.weight(1f)
            )
        }
    }
}
@Composable
private fun NavigationTabItem(
    tabInfo: NavigationTabUiState,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val textColor = if (isSelected) DolphinTheme.colors.text1 else DolphinTheme.colors.text4
    val fontWeight = FontWeight.Medium

    Box(
        modifier = modifier
            .fillMaxHeight()
            .selectable(
                selected = isSelected,
                onClick = onClick,
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ),
        contentAlignment = Alignment.BottomCenter
    ) {
        Row(
            modifier = Modifier.padding(bottom = 17.dp),
            verticalAlignment = Alignment.Top
        ) {
            Text(
                text = tabInfo.tabTitle,
                color = textColor,
                fontSize = 16.sp,
                fontWeight = fontWeight
            )
            
        }
    }
}

@Composable
private fun MainBottomNavigation(
    currentKey: NavKey?,
    onNavigate: (NavKey) -> Unit
) {
    AndroidView(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding(),
        factory = { context ->
            BottomNavigationView(context).apply {
                layoutParams = ViewGroup.LayoutParams(
                    MATCH_PARENT,
                    resources.getDimensionPixelSize(R.dimen.bottom_nav_view_height)
                )
                background = "#fcfdff".toColorInt().toDrawable()
                elevation = 0f
                inflateMenu(R.menu.menu_bottom_nav)
                itemIconTintList =
                    ContextCompat.getColorStateList(context, R.color.bnv_icon_tint)
                itemTextColor =
                    ContextCompat.getColorStateList(context, R.color.bnv_title_color)

                val icons = BottomNavigationIconsUtils.getBottomNavigationIcons(context)
                menu.forEach { item ->
                    icons[item.itemId]?.let { icon ->
                        item.icon = icon
                    }
                }

                setOnItemSelectedListener { menuItem ->
                    val route = bottomNavMap[menuItem.itemId]
                    if (route != null) {
                        onNavigate(route)
                        true
                    } else {
                        false
                    }
                }
            }
        },
        update = { view ->
            val selectedMenuId = bottomNavMap.entries.find { it.value == currentKey }?.key
            if (selectedMenuId != null && view.selectedItemId != selectedMenuId) {
                view.selectedItemId = selectedMenuId
            }
        }
    )
}