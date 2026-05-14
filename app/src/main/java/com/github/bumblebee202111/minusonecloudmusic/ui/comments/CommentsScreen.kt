package com.github.bumblebee202111.minusonecloudmusic.ui.comments

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.github.bumblebee202111.minusonecloudmusic.R
import com.github.bumblebee202111.minusonecloudmusic.model.Comment
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
                CommentItem(comment = comment)

                if (index < commentsList.lastIndex) {
                    HorizontalDivider(color = colorResource(id = R.color.lineColor))
                }
            }
        }
    }
}

@Composable
fun CommentItem(comment: Comment) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = comment.displayTime,
            color = colorResource(id = R.color.at8),
            fontSize = 9.sp,
            modifier = Modifier
                .padding(start = 10.dp, end = 10.dp, top = 5.dp)
        )
        Text(
            text = comment.content,
            color = Color(0xFF283248),
            fontSize = 14.sp,
            lineHeight = 19.sp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 10.dp, end = 10.dp, top = 5.dp, bottom = 5.dp)
        )
    }
}