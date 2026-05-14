package com.github.bumblebee202111.minusonecloudmusic.ui.mycollection

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
@Composable
fun CollectedMvListScreen(
    viewModel: CollectedMvListViewModel = hiltViewModel()
) {
    val mvs by viewModel.myMvs.collectAsStateWithLifecycle(initialValue = emptyList())
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        items(mvs ?: emptyList()) { video ->
            Text(
                text = video.title,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}