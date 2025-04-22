package com.github.bumblebee202111.minusonecloudmusic.ui.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.forEach
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.media3.common.util.UnstableApi
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.setupWithNavController
import com.github.bumblebee202111.minusonecloudmusic.R
import com.github.bumblebee202111.minusonecloudmusic.databinding.FragmentMainBinding
import com.github.bumblebee202111.minusonecloudmusic.ui.common.MiniPlayerBarController
import com.github.bumblebee202111.minusonecloudmusic.ui.common.PlaylistDialogController
import com.github.bumblebee202111.minusonecloudmusic.ui.common.doOnApplyWindowInsets
import com.github.bumblebee202111.minusonecloudmusic.ui.common.repeatWithViewLifecycle
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigation.NavigationView
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainFragment : Fragment() {

    private lateinit var binding: FragmentMainBinding
    private lateinit var playlistDialogController: PlaylistDialogController
    private lateinit var miniPlayerBarController: MiniPlayerBarController

    private val mainViewModel by viewModels<MainViewModel>()

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
        playlistDialogController = PlaylistDialogController(parentFragmentManager)
        miniPlayerBarController = MiniPlayerBarController(
            view = view,
            navController = findNavController(),
            playlistDialogController = playlistDialogController
        )


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
            when (val itemId = menuItem.itemId) {
                R.id.nav_inbox -> {
                    findNavController().navigate(itemId)
                }

                R.id.logout -> {
                    mainViewModel.onLogout()
                }
            }

            menuItem.isChecked = true
            drawerLayout.close()
            true
        }

        binding.root.doOnApplyWindowInsets { _, insets, _ ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            bottomNavView.run {
                layoutParams.height =
                    resources.getDimensionPixelSize(R.dimen.bottom_nav_view_height) + systemBars.bottom
                isVisible = true
                requestLayout()
            }
        }

        repeatWithViewLifecycle {
            launch {
                mainViewModel.player.collect(miniPlayerBarController::setPlayer)
            }
            launch {
                mainViewModel.isLoggedIn().collect {
                    navView.menu.findItem(R.id.logout).isEnabled = it
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
        findNavController().navigate(destinationId)
    }

    override fun onStop() {
        miniPlayerBarController.onStop()
        super.onStop()
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