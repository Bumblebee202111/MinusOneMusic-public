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
import com.github.bumblebee202111.minusonecloudmusic.databinding.FragmentCollectedMvListBinding
import com.github.bumblebee202111.minusonecloudmusic.databinding.ListItemMyCollectedMvBinding
import com.github.bumblebee202111.minusonecloudmusic.ui.theme.DolphinTheme
import dagger.hilt.android.AndroidEntryPoint
@AndroidEntryPoint
class CollectedMvListFragment : Fragment() {

    lateinit var binding: FragmentCollectedMvListBinding
    val viewModel: CollectedMvListViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding= FragmentCollectedMvListBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.root.findViewById<ComposeView>(R.id.mv_list)?.apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                DolphinTheme {
                    val mvs by viewModel.myMvs.collectAsStateWithLifecycle(initialValue = emptyList())
                    
                    LazyColumn {
                        items(mvs ?: emptyList()) { video ->
                            AndroidViewBinding(ListItemMyCollectedMvBinding::inflate) {
                                this.video = video
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
        fun newInstance() = CollectedMvListFragment()
    }
}