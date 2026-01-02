package com.github.bumblebee202111.minusonecloudmusic

import android.content.ComponentName
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.core.net.toUri
import androidx.core.view.GravityCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.forEach
import androidx.core.view.isVisible
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.lifecycleScope
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.ui.NavDisplay
import com.example.nav3recipes.deeplink.basic.util.DeepLinkMatcher
import com.example.nav3recipes.deeplink.basic.util.DeepLinkPattern
import com.example.nav3recipes.deeplink.basic.util.DeepLinkRequest
import com.example.nav3recipes.deeplink.basic.util.KeyDecoder
import com.github.bumblebee202111.minusonecloudmusic.data.MusicServiceConnection
import com.github.bumblebee202111.minusonecloudmusic.databinding.ActivityMainBinding
import com.github.bumblebee202111.minusonecloudmusic.service.PlaybackService
import com.github.bumblebee202111.minusonecloudmusic.system.isPackageInstalled
import com.github.bumblebee202111.minusonecloudmusic.ui.MainActivityViewModel
import com.github.bumblebee202111.minusonecloudmusic.ui.common.BottomNavigationIconsUtils
import com.github.bumblebee202111.minusonecloudmusic.ui.common.DolphinToast
import com.github.bumblebee202111.minusonecloudmusic.ui.common.ToastManager
import com.github.bumblebee202111.minusonecloudmusic.ui.common.UiText
import com.github.bumblebee202111.minusonecloudmusic.ui.common.doOnApplyWindowInsets
import com.github.bumblebee202111.minusonecloudmusic.ui.navigation.AppEntryProvider
import com.github.bumblebee202111.minusonecloudmusic.ui.navigation.DailyRecommendRoute
import com.github.bumblebee202111.minusonecloudmusic.ui.navigation.DiscoverRoute
import com.github.bumblebee202111.minusonecloudmusic.ui.navigation.FriendTracksRoute
import com.github.bumblebee202111.minusonecloudmusic.ui.navigation.InboxRoute
import com.github.bumblebee202111.minusonecloudmusic.ui.navigation.ListenRankRoute
import com.github.bumblebee202111.minusonecloudmusic.ui.navigation.LocalMusicRoute
import com.github.bumblebee202111.minusonecloudmusic.ui.navigation.MineRoute
import com.github.bumblebee202111.minusonecloudmusic.ui.navigation.MyPrivateCloudRoute
import com.github.bumblebee202111.minusonecloudmusic.ui.navigation.MyRecentPlayRoute
import com.github.bumblebee202111.minusonecloudmusic.ui.navigation.NavigationManager
import com.github.bumblebee202111.minusonecloudmusic.ui.navigation.Navigator
import com.github.bumblebee202111.minusonecloudmusic.ui.navigation.NowPlayingRoute
import com.github.bumblebee202111.minusonecloudmusic.ui.navigation.PlaylistRoute
import com.github.bumblebee202111.minusonecloudmusic.ui.navigation.SearchRoute
import com.github.bumblebee202111.minusonecloudmusic.ui.navigation.URL_NOW_PLAYING
import com.github.bumblebee202111.minusonecloudmusic.ui.navigation.rememberNavigationState
import com.github.bumblebee202111.minusonecloudmusic.ui.navigation.toEntries
import com.github.bumblebee202111.minusonecloudmusic.ui.playerhistory.PlayerHistoryDialogFragment
import com.github.bumblebee202111.minusonecloudmusic.ui.theme.DolphinTheme
import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.MoreExecutors
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import javax.inject.Inject


@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    @Inject
    lateinit var musicServiceConnection: MusicServiceConnection

    private val mainActivityViewModel: MainActivityViewModel by viewModels()

    private lateinit var mediaControllerFuture: ListenableFuture<MediaController>
    private val mediaController: MediaController?
        get() = if (mediaControllerFuture.isDone) mediaControllerFuture.get() else null

    private val bottomNavMap = mapOf(
        R.id.nav_wow to DiscoverRoute,
        R.id.nav_user_track to FriendTracksRoute,
        R.id.nav_mine to MineRoute
    )
    private val nonTopLevelMusicRoutes = setOf(
        DailyRecommendRoute::class,
        ListenRankRoute::class,
        LocalMusicRoute::class,
        MyPrivateCloudRoute::class,
        MyRecentPlayRoute::class,
        PlaylistRoute::class,
        SearchRoute::class
    )

    @Inject
    lateinit var toastManager: ToastManager

    private val activityScope = MainScope()

    private val snackbarHostState = SnackbarHostState()

    @Inject
    lateinit var navigationManager: NavigationManager

    internal val deepLinkPatterns: List<DeepLinkPattern<out NavKey>> = listOf(
        DeepLinkPattern(NowPlayingRoute.serializer(), URL_NOW_PLAYING.toUri()),
    )

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

        val deepLinkKey: NavKey? = intent.data?.let { uri ->
            val request = DeepLinkRequest(uri)
            val match = deepLinkPatterns.firstNotNullOfOrNull { pattern ->
                DeepLinkMatcher(request, pattern).match()
            }
            match?.let {
                KeyDecoder(match.args).decodeSerializableValue(match.serializer)
            }
        }

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

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

        binding.toastHost.setContent {
            DolphinTheme {
                SnackbarHost(
                    hostState = snackbarHostState,
                    modifier = Modifier.statusBarsPadding(),
                    snackbar = { snackbarData ->
                        DolphinToast(message = snackbarData.visuals.message)
                    }
                )
            }
        }

        binding.root.doOnApplyWindowInsets { _, insets, _ ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            binding.bottomNavView.run {
                layoutParams.height =
                    resources.getDimensionPixelSize(R.dimen.bottom_nav_view_height) + systemBars.bottom
                requestLayout()
            }
        }

        binding.mainNavHost.setContent {
            DolphinTheme {
                val topLevelRoutes = setOf(DiscoverRoute, FriendTracksRoute, MineRoute)

                val navigationState = rememberNavigationState(
                    startRoute = DiscoverRoute,
                    topLevelRoutes = topLevelRoutes
                )
                val navigator = remember { Navigator(navigationState) }

                LaunchedEffect(deepLinkKey) {
                    if (deepLinkKey != null) {
                        navigator.navigate(deepLinkKey)
                    }
                }

                val currentTopLevel = navigationState.topLevelRoute
                val currentStack = navigationState.backStacks[currentTopLevel]
                val currentKey = currentStack?.lastOrNull() ?: currentTopLevel

                LaunchedEffect(currentKey) {
                    val selectedMenuId = bottomNavMap.entries.find { it.value == currentKey }?.key
                    if (selectedMenuId != null && binding.bottomNavView.selectedItemId != selectedMenuId) {
                        binding.bottomNavView.selectedItemId = selectedMenuId
                    }
                    val isTopLevel = currentKey in topLevelRoutes
                    val isMiniBar = currentKey::class in nonTopLevelMusicRoutes || currentKey is PlaylistRoute
                    binding.bottomNavView.isVisible = isTopLevel
                    binding.divider.isVisible = isTopLevel
                    binding.miniPlayerBar.isVisible = isTopLevel || isMiniBar

                    binding.drawerLayout.setDrawerLockMode(
                        if (isTopLevel) DrawerLayout.LOCK_MODE_UNLOCKED else DrawerLayout.LOCK_MODE_LOCKED_CLOSED
                    )
                }

                DisposableEffect(Unit) {
                    binding.bottomNavView.setOnItemSelectedListener { menuItem ->
                        val route = bottomNavMap[menuItem.itemId]
                        if (route != null) {
                            navigator.navigate(route)
                            true
                        } else {
                            false
                        }
                    }
                    onDispose {
                        binding.bottomNavView.setOnItemSelectedListener(null)
                    }
                }

                SideEffect {
                    val bottomNavigationIcons =
                        BottomNavigationIconsUtils.getBottomNavigationIcons(this@MainActivity)
                    binding.bottomNavView.menu.forEach { menuItem ->
                        bottomNavigationIcons[menuItem.itemId]?.let { icon ->
                            menuItem.icon = icon
                        }
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


                NavDisplay(
                    entries = navigationState.toEntries(AppEntryProvider),
                    onBack = { navigator.goBack() }
                )

            }
        }

        binding.navView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_inbox -> {
                    navigationManager.navigate(InboxRoute)
                }

                R.id.logout -> {
                    mainActivityViewModel.onLogout()
                }
            }

            menuItem.isChecked = true
            binding.drawerLayout.close()
            true
        }

        mainActivityViewModel.registerAnonymousOrRefreshExisting()

        binding.miniPlayerBar.setOnClickListener {
            navigationManager.navigate(NowPlayingRoute)
        }
        binding.miniPlayerBar.setPlaylistButtonListener {
            PlayerHistoryDialogFragment().show(
                supportFragmentManager,
                PlayerHistoryDialogFragment.TAG
            )
        }

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
                    binding.drawerLayout.closeDrawer(GravityCompat.START)
                } else {
                    isEnabled = false
                    onBackPressedDispatcher.onBackPressed()
                    isEnabled = true
                }
            }
        })

        lifecycleScope.launch {
            musicServiceConnection.player.collect {
                binding.miniPlayerBar.player = it
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

        intent.data?.let { uri ->
            val request = DeepLinkRequest(uri)
            val match = deepLinkPatterns.firstNotNullOfOrNull { pattern ->
                DeepLinkMatcher(request, pattern).match()
            }
            val key = match?.let {
                KeyDecoder(match.args).decodeSerializableValue(match.serializer)
            }

            if (key != null) {
                navigationManager.navigate(key)
            }
        }
    }

    fun openDrawer() {
        binding.drawerLayout.open()
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