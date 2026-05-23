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
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.paging.compose.LazyPagingItems
import com.github.bumblebee202111.minusonecloudmusic.R
import com.github.bumblebee202111.minusonecloudmusic.ui.common.PlayingMark
import com.github.bumblebee202111.minusonecloudmusic.ui.playlist.SongItemUiModel
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
                    position = index + 1,
                    onClick = { onItemClick(song, index) }
                )
            }
        }
    }
}

@Composable
fun PlayerSongItem(
    song: SongItemUiModel,
    position: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isCurrent = song.isCurrentSong
    val primaryColor = colorResource(id = R.color.colorPrimary1)
    val bgColor = if (isCurrent) DolphinTheme.colors.text7 else Color.White

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(48.dp)
            .background(bgColor)
            .clickable { onClick() }
            .padding(horizontal = 16.dp),
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
                        fontSize = 15.sp,
                        color = if (isCurrent) primaryColor else Color.Black
                    )
                ) {
                    append(song.name ?: "Unknown")
                }
                withStyle(
                    SpanStyle(
                        fontSize = 11.sp,
                        color = if (isCurrent) primaryColor else Color(0x7F000000)
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