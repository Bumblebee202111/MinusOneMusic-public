package com.github.bumblebee202111.minusonecloudmusic.ui.comments

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.viewinterop.AndroidViewBinding
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.github.bumblebee202111.minusonecloudmusic.R
import com.github.bumblebee202111.minusonecloudmusic.databinding.ListItemCommentBinding
import com.github.bumblebee202111.minusonecloudmusic.ui.common.Toolbar

@Composable
fun CommentsScreen(
    threadId: String,
    onNavigateBack: () -> Unit,
    viewModel: CommentsViewModel = hiltViewModel<CommentsViewModel, CommentsViewModel.Factory>(
        creationCallback = { factory -> factory.create(threadId) }
    )
) {
    val comments by viewModel.comments.collectAsStateWithLifecycle(initialValue = emptyList())
    val commentsList = comments ?: emptyList()

    Column(modifier = Modifier.fillMaxSize().systemBarsPadding()) {
        Toolbar(
            title = "评论",
            onBackClick = onNavigateBack
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