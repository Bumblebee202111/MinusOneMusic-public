package com.github.bumblebee202111.minusonecloudmusic.ui.comments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.viewinterop.AndroidViewBinding
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.github.bumblebee202111.minusonecloudmusic.R
import com.github.bumblebee202111.minusonecloudmusic.databinding.ListItemCommentBinding
import com.github.bumblebee202111.minusonecloudmusic.ui.common.Toolbar
import com.github.bumblebee202111.minusonecloudmusic.ui.navigation.NavigationManager
import com.github.bumblebee202111.minusonecloudmusic.ui.theme.DolphinTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class CommentsFragment : Fragment() {

    companion object {
        fun newInstance() = CommentsFragment()
    }

    private val viewModel: CommentsViewModel by viewModels()

    @Inject
    lateinit var navigationManager: NavigationManager
    
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                DolphinTheme {
                    val comments by viewModel.comments.collectAsStateWithLifecycle(initialValue = emptyList())
                    val commentsList = comments ?: emptyList()

                    Column(modifier = Modifier.fillMaxSize().systemBarsPadding()) {
                        Toolbar(
                            title = "评论",
                            onBackClick = { navigationManager.goBack() }
                        )

                        LazyColumn(modifier = Modifier.weight(1f)) {
                            itemsIndexed(commentsList) { index, comment ->
                                AndroidViewBinding(ListItemCommentBinding::inflate) {
                                    this.comment = comment
                                    executePendingBindings()
                                }

                                if (index < commentsList.lastIndex) {
                                    HorizontalDivider(color = colorResource(id = R.color.lineColor))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}