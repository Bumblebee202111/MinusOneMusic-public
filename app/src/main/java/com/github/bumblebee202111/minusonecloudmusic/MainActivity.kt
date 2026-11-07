package com.github.bumblebee202111.minusonecloudmusic

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation3.runtime.NavKey
import com.github.bumblebee202111.minusonecloudmusic.data.MusicServiceConnection
import com.github.bumblebee202111.minusonecloudmusic.system.isPackageInstalled
import com.github.bumblebee202111.minusonecloudmusic.ui.MainActivityViewModel
import com.github.bumblebee202111.minusonecloudmusic.ui.MinusOneApp
import com.github.bumblebee202111.minusonecloudmusic.ui.common.ToastManager
import com.github.bumblebee202111.minusonecloudmusic.ui.common.UiText
import com.github.bumblebee202111.minusonecloudmusic.ui.navigation.DeepLinkRegistry
import com.github.bumblebee202111.minusonecloudmusic.ui.navigation.NavigationManager
import com.github.bumblebee202111.minusonecloudmusic.ui.theme.DolphinTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    @Inject
    lateinit var musicServiceConnection: MusicServiceConnection

    private val mainActivityViewModel: MainActivityViewModel by viewModels()

    @Inject
    lateinit var toastManager: ToastManager

    @Inject
    lateinit var navigationManager: NavigationManager

    override fun onStart() {
        super.onStart()
        musicServiceConnection.connect()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        checkOfficialNcmAppStatus()

        val initialDeepLinkKey: NavKey? = DeepLinkRegistry.resolve(intent.data?.toString())

        setContent {
            val player by musicServiceConnection.player.collectAsStateWithLifecycle()

            DolphinTheme {
                MinusOneApp(
                    player = player,
                    navigationManager = navigationManager,
                    toastManager = toastManager,
                    initialDeepLinkKey = initialDeepLinkKey,
                    onLogout = mainActivityViewModel::onLogout,
                    onUpdateSystemBarStyle = { style ->
                        this@MainActivity.enableEdgeToEdge(statusBarStyle = style)
                    }
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        this.intent = intent

        val key = DeepLinkRegistry.resolve(intent.data?.toString())
        if (key != null) {
            navigationManager.navigate(key)
        }
    }

    private fun checkOfficialNcmAppStatus() {
        val isOfficialNcmAppInstalled =
            packageManager.isPackageInstalled(OFFICIAL_NCM_PACKAGE)
        if (!isOfficialNcmAppInstalled) {
            lifecycleScope.launch {
                toastManager.showMessage(
                    UiText.StringResource(R.string.disclaimer_unofficial_app)
                )
            }
        }
    }

    companion object {
        init {
            System.loadLibrary("minusonecloudmusic")
        }

        private const val OFFICIAL_NCM_PACKAGE = "com.netease.cloudmusic"
    }
}