package com.github.bumblebee202111.minusonecloudmusic.ui.friend

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
import com.github.bumblebee202111.minusonecloudmusic.databinding.FragmentFollowBinding
import com.github.bumblebee202111.minusonecloudmusic.databinding.ListItemUserFollowBinding
import com.github.bumblebee202111.minusonecloudmusic.ui.theme.DolphinTheme
import dagger.hilt.android.AndroidEntryPoint
@AndroidEntryPoint
class FollowFragment : Fragment() {

    lateinit var binding:FragmentFollowBinding
    val viewModel:FollowViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding= FragmentFollowBinding.inflate(inflater,container,false).apply {
            lifecycleOwner=viewLifecycleOwner
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.root.findViewById<ComposeView>(R.id.following_users)?.apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                DolphinTheme {
                    val follows by viewModel.userFollows.collectAsStateWithLifecycle(initialValue = emptyList())
                    
                    LazyColumn {
                        items(follows ?: emptyList()) { user ->
                            AndroidViewBinding(ListItemUserFollowBinding::inflate) {
                                this.followingUser = user
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
        fun newInstance() = FollowFragment()
    }
}