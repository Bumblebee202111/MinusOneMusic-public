package com.github.bumblebee202111.minusonecloudmusic

import android.content.ComponentName
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toDrawable
import androidx.core.graphics.toColorInt
import androidx.core.view.GravityCompat
import androidx.core.view.forEach
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.ui.NavDisplay
import com.github.bumblebee202111.minusonecloudmusic.data.MusicServiceConnection
import com.github.bumblebee202111.minusonecloudmusic.service.PlaybackService
import com.github.bumblebee202111.minusonecloudmusic.system.isPackageInstalled
import com.github.bumblebee202111.minusonecloudmusic.ui.MainActivityViewModel
import com.github.bumblebee202111.minusonecloudmusic.ui.common.BottomNavigationIconsUtils
import com.github.bumblebee202111.minusonecloudmusic.ui.common.DolphinToast
import com.github.bumblebee202111.minusonecloudmusic.ui.common.MainDrawerContent
import com.github.bumblebee202111.minusonecloudmusic.ui.common.MiniPlayerBarView
import com.github.bumblebee202111.minusonecloudmusic.ui.common.ToastManager
import com.github.bumblebee202111.minusonecloudmusic.ui.common.UiText
import com.github.bumblebee202111.minusonecloudmusic.ui.navigation.createAppEntryProvider
import com.github.bumblebee202111.minusonecloudmusic.ui.navigation.DailyRecommendRoute
import com.github.bumblebee202111.minusonecloudmusic.ui.navigation.DeepLinkRegistry
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
import com.github.bumblebee202111.minusonecloudmusic.ui.navigation.rememberNavigationState
import com.github.bumblebee202111.minusonecloudmusic.ui.navigation.toEntries
import com.github.bumblebee202111.minusonecloudmusic.ui.playerhistory.PlayerHistoryDialogFragment
import com.github.bumblebee202111.minusonecloudmusic.ui.theme.DolphinTheme
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.MoreExecutors
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import javax.inject.Inject


@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    @Inject
    lateinit var musicServiceConnection: MusicServiceConnection

    private val mainActivityViewModel: MainActivityViewModel by viewModels()

    private lateinit var mediaControllerFuture: ListenableFuture<MediaController>
    private val mediaController: MediaController?
        get() = if (mediaControllerFuture.isDone) mediaControllerFuture.get() else null

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

    @Inject
    lateinit var toastManager: ToastManager

    private val activityScope = MainScope()

    private val snackbarHostState = SnackbarHostState()

    @Inject
    lateinit var navigationManager: NavigationManager

    override fun onStart() {
        super.onStart()
        initializeController()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        if (!BuildConfig.DEBUG && !ensureOfficialNcmAppInstalled()) {
            return
        }
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        val deepLinkKey: NavKey? = DeepLinkRegistry.resolve(intent.data?.toString())

        lifecycleScope.launch {
            toastManager.uiTextEvent.collect { event ->
                event?.let {
                    val messageText = it.message.asString(this@MainActivity)

                    val result = snackbarHostState.showSnackbar(messageText)

                    if (result == SnackbarResult.Dismissed || result == SnackbarResult.ActionPerformed) {
                        toastManager.onMessageShown()
                    }
                }
            }
        }

        setContent {
            val player by musicServiceConnection.player.collectAsStateWithLifecycle()
            DolphinTheme {
                val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
                val scope = rememberCoroutineScope()

                val topLevelRoutes = setOf(DiscoverRoute, FriendTracksRoute, MineRoute)

                val state = rememberNavigationState(
                    startRoute = DiscoverRoute,
                    topLevelRoutes = topLevelRoutes
                )
                val navigator = remember { Navigator(state) }

                LaunchedEffect(Unit) {
                    navigationManager.drawerEvents.collect { drawerState.open() }
                }
                LaunchedEffect(deepLinkKey) {
                    if (deepLinkKey != null) {
                        navigator.navigate(deepLinkKey)
                    }
                }

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

                val drawerLayoutRef = remember { mutableStateOf<DrawerLayout?>(null) }
                var isDrawerOpen by remember { mutableStateOf(false) }

                BackHandler(enabled = isDrawerOpen) {
                    drawerLayoutRef.value?.closeDrawer(GravityCompat.START)
                }

                LaunchedEffect(Unit) {
                    navigationManager.drawerEvents.collect {
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
                                    Box(modifier = Modifier.fillMaxSize()) {
                                        Column(modifier = Modifier.fillMaxSize()) {

                                            NavDisplay(
                                                entries = state.toEntries(createAppEntryProvider(navigationManager)),
                                                onBack = { navigator.goBack() },
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .fillMaxWidth().clipToBounds()
                                            )
                                            if (isMiniBarVisible) {
                                                AndroidView(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    factory = { context ->
                                                        MiniPlayerBarView(context).apply {
                                                            setOnClickListener {
                                                                navigationManager.navigate(
                                                                    NowPlayingRoute
                                                                )
                                                            }
                                                            setPlaylistButtonListener {
                                                                PlayerHistoryDialogFragment().show(
                                                                    supportFragmentManager,
                                                                    PlayerHistoryDialogFragment.TAG
                                                                )
                                                            }
                                                        }
                                                    },
                                                    update = { view ->
                                                        view.player = player
                                                    }
                                                )
                                            }

                                            if (isTopLevel) {
                                                HorizontalDivider(color = DolphinTheme.colors.text7)

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
                                                            background =
                                                                "#fcfdff".toColorInt()
                                                                    .toDrawable()
                                                            elevation = 0f
                                                            inflateMenu(R.menu.menu_bottom_nav)
                                                            itemIconTintList =
                                                                ContextCompat.getColorStateList(
                                                                    context,
                                                                    R.color.bnv_icon_tint
                                                                )
                                                            itemTextColor =
                                                                ContextCompat.getColorStateList(
                                                                    context,
                                                                    R.color.bnv_title_color
                                                                )
                                                            val icons =
                                                                BottomNavigationIconsUtils.getBottomNavigationIcons(
                                                                    context
                                                                )
                                                            menu.forEach { item ->
                                                                icons[item.itemId]?.let { icon ->
                                                                    item.icon = icon
                                                                }
                                                            }

                                                            setOnItemSelectedListener { menuItem ->
                                                                val route =
                                                                    bottomNavMap[menuItem.itemId]
                                                                if (route != null) {
                                                                    navigator.navigate(route)
                                                                    true
                                                                } else {
                                                                    false
                                                                }
                                                            }
                                                        }
                                                    },
                                                    update = { view ->
                                                        val selectedMenuId =
                                                            bottomNavMap.entries.find { it.value == currentKey }?.key
                                                        if (selectedMenuId != null && view.selectedItemId != selectedMenuId) {
                                                            view.selectedItemId = selectedMenuId
                                                        }
                                                    }
                                                )
                                            }
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
                                            scope.launch { drawerState.close() }
                                            navigator.navigate(route)
                                        },
                                        onLogout = {
                                            scope.launch { drawerState.close() }
                                            mainActivityViewModel.onLogout()
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
        }
    }

    override fun onStop() {
        musicServiceConnection.close()
        releaseController()
        super.onStop()
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        this.intent = intent

        val key = DeepLinkRegistry.resolve(intent.data?.toString())
        if (key != null) {
            navigationManager.navigate(key)
        }
    }

    private fun initializeController() {
        mediaControllerFuture = MediaController.Builder(
            this,
            SessionToken(
                this,
                ComponentName(this, PlaybackService::class.java)
            )
        )
            .buildAsync()
        mediaControllerFuture.addListener({
            val mediaController = this.mediaController ?: return@addListener
            musicServiceConnection.connect(mediaController) {
                mediaController.release()
            }
        }, MoreExecutors.directExecutor())
    }

    private fun releaseController() {
        MediaController.releaseFuture(mediaControllerFuture)
    }

    private fun ensureOfficialNcmAppInstalled(): Boolean {
        val isOfficialNcmAppInstalled = packageManager.isPackageInstalled(OFFICIAL_NCM_PACKAGE)
        if (!isOfficialNcmAppInstalled) {
            activityScope.launch {
                toastManager.showMessage(
                    UiText.DynamicString("The official NCM app is not installed. Finishing myself.")
                )
            }
            Handler(mainLooper).postDelayed(::finish, FINISH_DELAY_MS)
        }
        return isOfficialNcmAppInstalled
    }

    companion object {
        init {
            System.loadLibrary("minusonecloudmusic")
        }

        private const val OFFICIAL_NCM_PACKAGE = "com.netease.cloudmusic"
        private const val FINISH_DELAY_MS = 3500L
    }
}