package com.github.bumblebee202111.minusonecloudmusic.ui.playerhistory

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.rememberNestedScrollInteropConnection
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.paging.compose.LazyPagingItems
import com.github.bumblebee202111.minusonecloudmusic.model.SongItemUiModel
import com.github.bumblebee202111.minusonecloudmusic.ui.common.PlayingMark
import com.github.bumblebee202111.minusonecloudmusic.ui.theme.DolphinTheme
@Composable
fun PagedPlayerSongList(
    songs: LazyPagingItems<SongItemUiModel>,
    onItemClick: (song: SongItemUiModel, position: Int) -> Unit,
    modifier: Modifier = Modifier,
    state: LazyListState = rememberLazyListState()
) {
    LazyColumn(
        modifier = modifier.nestedScroll(rememberNestedScrollInteropConnection()),
        state = state
    ) {
        items(count = songs.itemCount) { index ->
            val song = songs[index]
            if (song != null) {
                PlayerSongItem(
                    song = song,
                    onClick = { onItemClick(song, index) }
                )
            }
        }
    }
}

@Composable
fun PlayerSongItem(
    song: SongItemUiModel,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isCurrent = song.isCurrentSong
    val bgColor = if (isCurrent) DolphinTheme.colors.text7 else Color.Transparent
    val songNameColor = if (isCurrent) DolphinTheme.colors.primary1 else DolphinTheme.colors.text2
    val artistColor = if (isCurrent) DolphinTheme.colors.primary1 else DolphinTheme.colors.text4

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(48.dp)
            .background(bgColor)
            .clickable(onClick = onClick)
            .padding(start = 19.dp, end = 11.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (isCurrent) {
            PlayingMark(
                isBeingPlayed = song.isBeingPlayed,
                modifier = Modifier.padding(end = 4.dp)
            )
        }
        Text(
            text = buildAnnotatedString {
                withStyle(
                    SpanStyle(
                        fontSize = 16.sp,
                        color = songNameColor
                    )
                ) {
                    append(song.name ?: "Unknown")
                }
                withStyle(
                    SpanStyle(
                        fontSize = 13.sp,
                        color = artistColor
                    )
                ) {
                    append(" · ")
                    append(song.artists.filterNotNull().joinToString("/"))
                }
            },
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )
    }
}