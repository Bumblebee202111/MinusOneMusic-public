package com.github.bumblebee202111.minusonecloudmusic

import android.content.ComponentName
import android.os.Bundle
import android.os.Handler
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import androidx.navigation.NavController
import androidx.navigation.findNavController
import com.github.bumblebee202111.minusonecloudmusic.data.MusicServiceConnection
import com.github.bumblebee202111.minusonecloudmusic.data.repository.LoginRepository
import com.github.bumblebee202111.minusonecloudmusic.data.repository.PlaylistRepository
import com.github.bumblebee202111.minusonecloudmusic.databinding.ActivityMainBinding
import com.github.bumblebee202111.minusonecloudmusic.service.PlaybackService
import com.github.bumblebee202111.minusonecloudmusic.ui.MainActivityViewModel
import com.github.bumblebee202111.minusonecloudmusic.utils.isPackageInstalled
import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.MoreExecutors
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject


@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private lateinit var navController: NavController

    @Inject
    lateinit var musicServiceConnection: MusicServiceConnection

    private val mainActivityViewModel: MainActivityViewModel by viewModels()

    @Inject
    lateinit var loginRepository: LoginRepository

    @Inject
    lateinit var playlistRepository: PlaylistRepository
    private lateinit var mediaControllerFuture: ListenableFuture<MediaController>
    private val mediaController: MediaController?
        get() = if (mediaControllerFuture.isDone) mediaControllerFuture.get() else null

    override fun onStart() {
        super.onStart()
        initializeController()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        navController = findNavController(R.id.nav_host_fragment_content_main)

        if (!ensureOfficialNcmAppInstalled()) {
            return
        }

        mainActivityViewModel.registerAnonymousOrRefreshExisting()

    }

    override fun onStop() {
        musicServiceConnection.close()
        releaseController()
        super.onStop()
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
        val isOfficialNcmAppInstalled = packageManager.isPackageInstalled("com.netease.cloudmusic")
        if (!isOfficialNcmAppInstalled) {
            Toast.makeText(
                this,
                "The official NCM app is not installed. Finishing myself.",
                Toast.LENGTH_LONG
            ).show()
            val finishInMs = 3500L
            Handler(mainLooper).postDelayed(::finish, finishInMs)
        }
        return isOfficialNcmAppInstalled
    }

    companion object {
        init {
            System.loadLibrary("minusonecloudmusic")
        }
    }
}