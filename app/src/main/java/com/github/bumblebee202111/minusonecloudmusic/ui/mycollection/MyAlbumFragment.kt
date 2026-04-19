package com.github.bumblebee202111.minusonecloudmusic.ui.mycollection

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.viewinterop.AndroidViewBinding
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.github.bumblebee202111.minusonecloudmusic.R
import com.github.bumblebee202111.minusonecloudmusic.databinding.FragmentMyAlbumBinding
import com.github.bumblebee202111.minusonecloudmusic.databinding.ListItemMyAlbumBinding
import com.github.bumblebee202111.minusonecloudmusic.ui.common.loadImage
import com.github.bumblebee202111.minusonecloudmusic.ui.theme.DolphinTheme
import dagger.hilt.android.AndroidEntryPoint
@AndroidEntryPoint
class MyAlbumFragment : Fragment() {

    private var _binding: FragmentMyAlbumBinding? = null
    private val binding get() = _binding!!
    private val viewModel: MyAlbumViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMyAlbumBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.root.findViewById<ComposeView>(R.id.album_list).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                DolphinTheme {
                    val albums by viewModel.myAlbums.collectAsStateWithLifecycle(initialValue = emptyList())
                    
                    LazyColumn {
                        items(albums ?: emptyList()) { album ->
                            AndroidViewBinding(ListItemMyAlbumBinding::inflate) {
                                this.album = album
                                this.albumCover.loadImage(album.art)
                                executePendingBindings()
                            }
                        }
                    }
                }
            }
        }
    }

    companion object {
        @JvmStatic
        fun newInstance() = MyAlbumFragment()
    }
}