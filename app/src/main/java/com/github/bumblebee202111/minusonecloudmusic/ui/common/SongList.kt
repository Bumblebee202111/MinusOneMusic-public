package com.github.bumblebee202111.minusonecloudmusic.ui.common

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.rememberNestedScrollInteropConnection
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.paging.compose.LazyPagingItems
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import coil3.request.error
import coil3.request.placeholder
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.rememberLottieComposition
import com.github.bumblebee202111.minusonecloudmusic.R
import com.github.bumblebee202111.minusonecloudmusic.ui.playlist.SongItemUiModel
import com.github.bumblebee202111.minusonecloudmusic.utils.imageUrl
@Composable
fun PlayingMark(isBeingPlayed: Boolean, modifier: Modifier = Modifier) {
    val composition by rememberLottieComposition(LottieCompositionSpec.Asset("playlist_song_playing/data.json"))
    LottieAnimation(
        composition = composition,
        isPlaying = isBeingPlayed,
        iterations = LottieConstants.IterateForever,
        modifier = modifier.size(20.dp),
        contentScale = ContentScale.Inside
    )
}
@Composable
fun SongItemInfo(
    title: String?,
    artists: List<String?>?,
    album: String?,
    isCurrentSong: Boolean,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier, horizontalAlignment = Alignment.Start) {
        Text(
            text = title.orEmpty(),
            color = if (isCurrentSong) colorResource(id = R.color.colorPrimary1) else Color(0xCC000000),
            fontSize = 15.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        val subtitle = buildString {
            append(artists?.filterNotNull()?.joinToString("/") ?: "Unknown")
            append(" - ")
            append(album ?: "Unknown")
        }
        Text(
            text = subtitle,
            color = Color(0x7F000000),
            fontSize = 12.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun SimpleSongList(
    songs: List<SongItemUiModel>,
    onItemClick: (position: Int) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.nestedScroll(rememberNestedScrollInteropConnection())
    ) {
        itemsIndexed(songs) { index, song ->
            SimpleSongItem(
                song = song,
                onClick = { onItemClick(index) }
            )
        }
    }
}

@Composable
fun SimpleSongItem(
    song: SongItemUiModel,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(60.dp)
            .clickable { onClick() },
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (song.isCurrentSong) {
            PlayingMark(
                isBeingPlayed = song.isBeingPlayed,
                modifier = Modifier.padding(start = 16.dp)
            )
            Spacer(modifier = Modifier.width(15.dp))
        } else {
            Spacer(modifier = Modifier.width(16.dp))
        }

        SongItemInfo(
            title = song.name,
            artists = song.artists,
            album = song.album?.name,
            isCurrentSong = song.isCurrentSong,
            modifier = Modifier
                .weight(1f)
                .padding(end = 16.dp)
        )
    }
}

@Composable
fun SongWithAlbumList(
    songs: List<SongItemUiModel>,
    onItemClick: (position: Int) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.nestedScroll(rememberNestedScrollInteropConnection())
    ) {
        itemsIndexed(songs) { index, song ->
            SongWithAlbumItem(
                song = song,
                onClick = { onItemClick(index) }
            )
        }
    }
}

@Composable
fun SongWithAlbumItem(
    song: SongItemUiModel,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(60.dp)
            .clickable { onClick() },
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (song.isCurrentSong) {
            PlayingMark(
                isBeingPlayed = song.isBeingPlayed,
                modifier = Modifier.padding(start = 26.dp, end = 20.dp)
            )
        } else {
            val imageUrl = remember(song.album?.art) {
                val art = song.album?.art
                if (art is String) art.imageUrl(thumbnailSize = 200) else art
            }
            val placeholderDrawable = remember(context) { ContextCompat.getDrawable(context, R.drawable.doe) }

            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(imageUrl)
                    .crossfade(300)
                    .placeholder(placeholderDrawable)
                    .error(placeholderDrawable)
                    .build(),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .padding(start = 12.dp, end = 10.dp)
                    .size(40.dp)
                    .clip(RoundedCornerShape(4.dp))
            )
        }

        SongItemInfo(
            title = song.name,
            artists = song.artists,
            album = song.album?.name,
            isCurrentSong = song.isCurrentSong,
            modifier = Modifier
                .weight(1f)
                .padding(end = 16.dp)
        )
    }
}

@Composable
fun SongWithPositionList(
    songs: List<SongItemUiModel>,
    onItemClick: (position: Int) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.nestedScroll(rememberNestedScrollInteropConnection())
    ) {
        itemsIndexed(songs) { index, song ->
            SongWithPositionItem(
                song = song,
                position = index + 1,
                onClick = { onItemClick(index) }
            )
        }
    }
}

@Composable
fun PagedSongWithPositionList(
    songs: LazyPagingItems<SongItemUiModel>,
    onItemClick: (position: Int) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.nestedScroll(rememberNestedScrollInteropConnection())
    ) {
        items(count = songs.itemCount) { index ->
            val song = songs[index]
            if (song != null) {
                SongWithPositionItem(
                    song = song,
                    position = index + 1,
                    onClick = { onItemClick(index) }
                )
            }
        }
    }
}

@Composable
fun SongWithPositionItem(
    song: SongItemUiModel,
    position: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(60.dp)
            .clickable { onClick() },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .width(46.dp)
                .fillMaxHeight(),
            contentAlignment = Alignment.Center
        ) {
            if (song.isCurrentSong) {
                PlayingMark(isBeingPlayed = song.isBeingPlayed)
            } else {
                Text(
                    text = position.toString(),
                    color = Color(0x66000000),
                    fontSize = 14.sp
                )
            }
        }

        SongItemInfo(
            title = song.name,
            artists = song.artists,
            album = song.album?.name,
            isCurrentSong = song.isCurrentSong,
            modifier = Modifier
                .weight(1f)
                .padding(end = 16.dp)
        )
    }
}