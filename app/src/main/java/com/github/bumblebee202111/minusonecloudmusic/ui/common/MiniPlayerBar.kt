package com.github.bumblebee202111.minusonecloudmusic.ui.common

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.PaintingStyle
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.media3.common.C
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.common.util.Util
import coil3.compose.AsyncImage
import com.github.bumblebee202111.minusonecloudmusic.R
import com.github.bumblebee202111.minusonecloudmusic.ui.theme.DolphinTheme
import kotlinx.coroutines.delay

@UnstableApi
@Composable
fun MiniPlayerBar(
    player: Player?,
    onNavigateToNowPlaying: () -> Unit,
    onShowPlaylist: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isPlaying by remember { mutableStateOf(player?.isPlaying == true) }
    var currentPosition by remember { mutableLongStateOf(player?.contentPosition ?: 0L) }
    var duration by remember { mutableLongStateOf(player?.duration?.takeIf { it != C.TIME_UNSET } ?: 0L) }
    var mediaMetadata by remember { mutableStateOf(player?.mediaMetadata) }

    DisposableEffect(player) {
        val listener = object : Player.Listener {
            override fun onEvents(p: Player, events: Player.Events) {
                isPlaying = p.isPlaying
                currentPosition = p.contentPosition
                duration = p.duration.takeIf { it != C.TIME_UNSET } ?: 0L
                mediaMetadata = p.mediaMetadata
            }
        }
        player?.addListener(listener)
        onDispose { player?.removeListener(listener) }
    }

    LaunchedEffect(isPlaying) {
        if (isPlaying) {
            while (true) {
                player?.let { currentPosition = it.contentPosition }
                delay(200)
            }
        }
    }

    val progress = if (duration > 0) (currentPosition.toFloat() / duration.toFloat()).coerceIn(0f, 1f) else 0f

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(46.dp)
            .background(DolphinTheme.colors.backgroundWhite)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onNavigateToNowPlaying
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val placeholderColor = remember { Color(0x0D000000) }
        AsyncImage(
            model = mediaMetadata?.artworkUri ?: mediaMetadata?.artworkData,
            contentDescription = null,
            placeholder = ColorPainter(placeholderColor),
            error = ColorPainter(placeholderColor),
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .padding(start = 16.dp)
                .size(40.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(placeholderColor)
        )
        Text(
            text = buildAnnotatedString {
                withStyle(SpanStyle(color = DolphinTheme.colors.text2, fontSize = 14.sp)) {
                    append(mediaMetadata?.title?.toString() ?: "Unknown")
                }
                withStyle(SpanStyle(color = DolphinTheme.colors.text3_1, fontSize = 14.sp)) {
                    append(" - ${mediaMetadata?.artist?.toString() ?: "Unknown"}")
                }
            },
            maxLines = 1,
            modifier = Modifier
                .weight(1f)
                .padding(start = 4.dp, end = 8.dp)
                .basicMarquee()
        )

        MiniPlayPauseButton(
            isPlaying = isPlaying,
            progress = progress,
            onClick = { player?.let { Util.handlePlayPauseButtonAction(it) } },
            modifier = Modifier
                .padding(end = 2.dp)
                .size(46.dp)
        )

        Image(
            painter = painterResource(id = R.drawable.cpq),
            contentDescription = "Playlist",
            contentScale = ContentScale.Inside,
            modifier = Modifier
                .padding(start = 4.dp)
                .size(46.dp)
                .clip(CircleShape)
                .clickable(onClick = onShowPlaylist)
                .padding(end = 8.dp)
        )
    }
}

@Composable
private fun MiniPlayPauseButton(
    isPlaying: Boolean,
    progress: Float,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val progressColor = DolphinTheme.colors.text2_1
    val circleColor = DolphinTheme.colors.text5_1
    val innerIconColor = DolphinTheme.colors.text2

    val playPaint = remember {
        Paint().apply {
            style = PaintingStyle.Fill
        }
    }

    Canvas(
        modifier = modifier
            .clip(CircleShape)
            .clickable(onClick = onClick)
    ) {
        val contentSize = 23.dp.toPx()
        val strokeW = 1.33.dp.toPx()
        val topLeftOffset = Offset((size.width - contentSize) / 2f, (size.height - contentSize) / 2f)
        val arcSize = Size(contentSize, contentSize)
        drawArc(
            color = circleColor,
            startAngle = -90f,
            sweepAngle = 360f,
            useCenter = false,
            topLeft = topLeftOffset,
            size = arcSize,
            style = Stroke(width = strokeW, cap = StrokeCap.Round)
        )
        drawArc(
            color = progressColor,
            startAngle = -90f,
            sweepAngle = 360f * progress,
            useCenter = false,
            topLeft = topLeftOffset,
            size = arcSize,
            style = Stroke(width = strokeW, cap = StrokeCap.Round)
        )

        val center = Offset(size.width / 2f, size.height / 2f)

        if (isPlaying) {
            val lineLength = 9.dp.toPx()
            val centerSpace = 2.dp.toPx()
            val pauseStrokeW = 2.5.dp.toPx()

            val lineLeftX = center.x - centerSpace / 2f - pauseStrokeW / 2f
            val lineRightX = center.x + centerSpace / 2f + pauseStrokeW / 2f
            val topY = center.y - lineLength / 2f
            val bottomY = center.y + lineLength / 2f

            drawLine(
                color = innerIconColor,
                start = Offset(lineLeftX, topY),
                end = Offset(lineLeftX, bottomY),
                strokeWidth = pauseStrokeW,
                cap = StrokeCap.Round
            )
            drawLine(
                color = innerIconColor,
                start = Offset(lineRightX, topY),
                end = Offset(lineRightX, bottomY),
                strokeWidth = pauseStrokeW,
                cap = StrokeCap.Round
            )
        } else {
            val triangleLength = 10.75.dp.toPx()
            val trianglePath = Path().apply {
                val h = triangleLength * 0.866f
                val leftX = center.x - h / 3f
                val rightX = leftX + h
                val topY = center.y - triangleLength / 2f
                val bottomY = center.y + triangleLength / 2f

                moveTo(leftX, topY)
                lineTo(rightX, center.y)
                lineTo(leftX, bottomY)
                close()
            }

            playPaint.color = innerIconColor
            playPaint.pathEffect = PathEffect.cornerPathEffect(2.2.dp.toPx())

            drawIntoCanvas { canvas ->
                canvas.drawPath(trianglePath, playPaint)
            }
        }
    }
}