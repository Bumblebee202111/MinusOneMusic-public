package com.github.bumblebee202111.minusonecloudmusic.ui.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import androidx.core.view.forEach
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.MediaController
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.setupWithNavController
import com.github.bumblebee202111.minusonecloudmusic.R
import com.github.bumblebee202111.minusonecloudmusic.databinding.FragmentMainBinding
import com.github.bumblebee202111.minusonecloudmusic.ui.MainActivityViewModel
import com.github.bumblebee202111.minusonecloudmusic.ui.common.MiniPlayerBarView
import com.github.bumblebee202111.minusonecloudmusic.ui.common.repeatWithViewLifecycle
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigation.NavigationView
import com.google.common.util.concurrent.ListenableFuture
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainFragment : Fragment() {

    private lateinit var binding: FragmentMainBinding
    private val mainViewModel by viewModels<MainViewModel>()
    private val mainActivityViewModel by activityViewModels<MainActivityViewModel>()
    private lateinit var navController: NavController
    private lateinit var miniPlayerBarView: MiniPlayerBarView
    private lateinit var mediaControllerFuture: ListenableFuture<MediaController>
    private val mediaController: MediaController?
        get() = if (mediaControllerFuture.isDone) mediaControllerFuture.get() else null

    lateinit var playPauseButton: ImageButton
    lateinit var artworkView: ImageView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentMainBinding.inflate(inflater, container, false)

        return binding.root
    }

    @UnstableApi
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        navController = findNavController()


        val bottomNavView: BottomNavigationView = binding.bottomNavView
        val bottomNavHostFragment =
            childFragmentManager.findFragmentById(R.id.nav_host_fragment_content_bnv) as NavHostFragment
        val bottomNavController = bottomNavHostFragment.navController
        bottomNavView.setupWithNavController(bottomNavController)
        val bottomNavigationIcons =
            BottomNavigationIconsUtils.getBottomNavigationIcons(requireContext())
        bottomNavView.menu.forEach { menuItem ->
            bottomNavigationIcons[menuItem.itemId]?.let { icon ->
                menuItem.icon = icon
            }
        }

        val drawerLayout = binding.drawerLayout
        val navView: NavigationView = binding.navView
        navView.setNavigationItemSelectedListener { menuItem ->
            when (val itemId=menuItem.itemId) {
                R.id.nav_inbox -> {
                    navController.navigate(itemId)
                }
                R.id.logout->{
                    mainViewModel.onLogout()
                }
            }

            menuItem.isChecked = true
            drawerLayout.close()
            true
        }

        miniPlayerBarView = binding.miniPlayerView

        miniPlayerBarView.setOnClickListener {
            navController.navigate(R.id.nav_now_playing)
        }

        repeatWithViewLifecycle {
            launch {
                mainViewModel.player.collect {
                    miniPlayerBarView.player = it
                }
            }
            launch {
                mainViewModel.isLoggedIn().collect {
                    navView.menu.findItem(R.id.logout).isEnabled=it
                }
            }

        }


    }


    private fun showWelcomeMessage() {
    }

    fun openDrawerLayout() {
        binding.drawerLayout.open()
    }

    fun navigateTo(destinationId: Int) {
        navController.navigate(destinationId)
    }


    @UnstableApi
    override fun onStop() {
        super.onStop()
        miniPlayerBarView.player = null
    }


    companion object {

        @JvmStatic
        fun newInstance() =
            MainFragment().apply {
                arguments = Bundle().apply {
                }
            }
    }
}