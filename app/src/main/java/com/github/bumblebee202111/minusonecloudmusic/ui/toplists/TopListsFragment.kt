package com.github.bumblebee202111.minusonecloudmusic.ui.toplists

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidViewBinding
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.github.bumblebee202111.minusonecloudmusic.R
import com.github.bumblebee202111.minusonecloudmusic.databinding.FragmentTopListsBinding
import com.github.bumblebee202111.minusonecloudmusic.databinding.ListItemBillboardBinding
import com.github.bumblebee202111.minusonecloudmusic.databinding.ListItemBillboardGroupBinding
import com.github.bumblebee202111.minusonecloudmusic.ui.common.loadImage
import com.github.bumblebee202111.minusonecloudmusic.ui.navigation.NavigationManager
import com.github.bumblebee202111.minusonecloudmusic.ui.navigation.PlaylistRoute
import com.github.bumblebee202111.minusonecloudmusic.ui.playlist.PlaylistFragment
import com.github.bumblebee202111.minusonecloudmusic.ui.theme.DolphinTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class TopListsFragment : Fragment() {

    companion object {
        fun newInstance() = TopListsFragment()
    }

    private lateinit var binding: FragmentTopListsBinding
    private val viewModel: TopListsViewModel by viewModels()

    @Inject
    lateinit var navigationManager: NavigationManager

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentTopListsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.toolbar.setNavigationOnClickListener {
            navigationManager.goBack()
        }

        binding.root.findViewById<ComposeView>(R.id.billboard_group_list)?.apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                DolphinTheme {
                    val topLists by viewModel.topLists.collectAsStateWithLifecycle(initialValue = emptyList())

                    LazyColumn {
                        items(topLists ?: emptyList()) { billboardGroup ->
                            AndroidViewBinding(ListItemBillboardGroupBinding::inflate) {
                                this.billboardCategory.text = billboardGroup.name
                                this.billboardCategory.paint.isFakeBoldText = true

                                val billboardsList = billboardGroup.billboards
                                if (billboardsList.isNotEmpty()) {
                                    this.root.findViewById<View>(R.id.billboards).isVisible = true
                                    this.root.findViewById<ComposeView>(R.id.billboards).apply {
                                        setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
                                        setContent {
                                            DolphinTheme {
                                                LazyRow(
                                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                                ) {
                                                    items(billboardsList) { billboard ->
                                                        AndroidViewBinding(ListItemBillboardBinding::inflate) {
                                                            this.billboard = billboard
                                                            this.billboardCover.loadImage(billboard.coverImgUrl, thumbnailSize = 137, quality = 80)

                                                            this.root.setOnClickListener {
                                                                if (billboard.isMusicPlaylist) {
                                                                    navigationManager.navigate(
                                                                        PlaylistRoute(
                                                                            playlistId = billboard.id,
                                                                            playlistCreatorId = PlaylistFragment.ARG_VALUE_PLAYLIST_CREATOR_ID_UNKNOWN
                                                                        )
                                                                    )
                                                                }
                                                            }
                                                            executePendingBindings()
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                } else {
                                    this.root.findViewById<View>(R.id.billboards).isGone = true
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}