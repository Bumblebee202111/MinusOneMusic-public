package com.github.bumblebee202111.minusonecloudmusic

import android.content.ComponentName
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
import androidx.compose.ui.Modifier
import androidx.core.view.GravityCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.forEach
import androidx.core.view.isVisible
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.lifecycleScope
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.github.bumblebee202111.minusonecloudmusic.data.MusicServiceConnection
import com.github.bumblebee202111.minusonecloudmusic.databinding.ActivityMainBinding
import com.github.bumblebee202111.minusonecloudmusic.service.PlaybackService
import com.github.bumblebee202111.minusonecloudmusic.ui.MainActivityViewModel
import com.github.bumblebee202111.minusonecloudmusic.ui.common.BottomNavigationIconsUtils
import com.github.bumblebee202111.minusonecloudmusic.ui.common.DolphinToast
import com.github.bumblebee202111.minusonecloudmusic.ui.common.doOnApplyWindowInsets
import com.github.bumblebee202111.minusonecloudmusic.ui.playerhistory.PlayerHistoryDialogFragment
import com.github.bumblebee202111.minusonecloudmusic.ui.theme.DolphinTheme
import com.github.bumblebee202111.minusonecloudmusic.ui.common.ToastManager
import com.github.bumblebee202111.minusonecloudmusic.system.isPackageInstalled
import com.github.bumblebee202111.minusonecloudmusic.ui.common.UiText
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

    private val topLevelDestinations = setOf(
        R.id.nav_wow,
        R.id.nav_user_track,
        R.id.nav_mine,
    )

    private val miniPlayerBarDestinations = setOf(
        R.id.nav_dailyrecommend,
        R.id.nav_listen_rank,
        R.id.nav_local_music,
        R.id.nav_my_private_cloud,
        R.id.nav_my_recent_play,
        R.id.nav_playlist,
        R.id.nav_search
    )

    @Inject
    lateinit var toastManager: ToastManager

    private val activityScope= MainScope()

    private val snackbarHostState = SnackbarHostState()

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

        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController
        binding.bottomNavView.setupWithNavController(navController)
        val bottomNavigationIcons =
            BottomNavigationIconsUtils.getBottomNavigationIcons(this)
        binding.bottomNavView.menu.forEach { menuItem ->
            bottomNavigationIcons[menuItem.itemId]?.let { icon ->
                menuItem.icon = icon
            }
        }
        navController.addOnDestinationChangedListener { _, destination, _ ->
            val isTopLevelDestination = destination.id in topLevelDestinations
            val isMiniPlayerBarDestination = destination.id in miniPlayerBarDestinations
            binding.bottomNavView.isVisible = isTopLevelDestination
            binding.divider.isVisible = isTopLevelDestination
            binding.miniPlayerBar.isVisible = isTopLevelDestination || isMiniPlayerBarDestination
            binding.drawerLayout.setDrawerLockMode(
                if (isTopLevelDestination) DrawerLayout.LOCK_MODE_UNLOCKED else DrawerLayout.LOCK_MODE_LOCKED_CLOSED
            )
        }
        binding.navView.setNavigationItemSelectedListener { menuItem ->
            when (val itemId = menuItem.itemId) {
                R.id.nav_inbox -> {
                    navController.navigate(itemId)
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
            navController.navigate(R.id.nav_now_playing)
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