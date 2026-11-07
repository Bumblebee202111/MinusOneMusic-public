package com.github.bumblebee202111.minusonecloudmusic.ui.common

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.PaintingStyle
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.media3.common.MediaMetadata
import coil3.compose.AsyncImage
import com.github.bumblebee202111.minusonecloudmusic.R
import com.github.bumblebee202111.minusonecloudmusic.data.MusicServiceConnection
import com.github.bumblebee202111.minusonecloudmusic.ui.theme.DolphinTheme
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
private val PlaceholderColor = Color(0x0D000000)
private val ProgressBgColor = Color.Gray.copy(alpha = 0.15f)

@HiltViewModel
class MiniBarViewModel @Inject constructor(
    private val musicServiceConnection: MusicServiceConnection
) : ViewModel() {
    val isPlaying = musicServiceConnection.isPlaying
    val currentPosition = musicServiceConnection.currentPosition
    val duration = musicServiceConnection.duration
    val mediaMetadata = musicServiceConnection.mediaMetadata

    fun togglePlayPause() = musicServiceConnection.togglePlayPause()
}
@Composable
fun MiniBarContainer(
    onNavigateToNowPlaying: () -> Unit,
    onShowPlaylist: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: MiniBarViewModel = hiltViewModel()
) {
    val isPlaying by viewModel.isPlaying.collectAsStateWithLifecycle()
    val duration by viewModel.duration.collectAsStateWithLifecycle()
    val mediaMetadata by viewModel.mediaMetadata.collectAsStateWithLifecycle()
    val currentPositionState = viewModel.currentPosition.collectAsStateWithLifecycle()

    MiniBarContainer(
        mediaMetadata = mediaMetadata,
        isPlaying = isPlaying,
        progressProvider = {
            if (duration > 0) (currentPositionState.value.toFloat() / duration).coerceIn(0f, 1f) else 0f
        },
        onPlayPauseClick = viewModel::togglePlayPause,
        onNavigateToNowPlaying = onNavigateToNowPlaying,
        onShowPlaylist = onShowPlaylist,
        modifier = modifier
    )
}

@Composable
fun MiniBarContainer(
    mediaMetadata: MediaMetadata?,
    isPlaying: Boolean,
    progressProvider: () -> Float,
    onPlayPauseClick: () -> Unit,
    onNavigateToNowPlaying: () -> Unit,
    onShowPlaylist: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.fillMaxWidth()) {

        val bgColor = DolphinTheme.colors.backgroundWhite
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(59.dp)
                .align(Alignment.BottomCenter)
                .background(
                    Brush.verticalGradient(
                        0.0f to bgColor.copy(alpha = 0f),
                        32f / 59f to bgColor,
                        1.0f to bgColor
                    )
                )
        )
        val context = LocalContext.current
        val shadowDrawable = remember(context) {
            ContextCompat.getDrawable(context, R.drawable.a4o)
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 2.dp)
                .height(72.dp)
                .align(Alignment.BottomCenter)
                .offset(y = 9.dp)
                .drawBehind {
                    shadowDrawable?.let { drawable ->
                        drawable.setBounds(0, 0, size.width.toInt(), size.height.toInt())
                        drawIntoCanvas { canvas ->
                            drawable.draw(canvas.nativeCanvas)
                        }
                    }
                }
        )
        MiniNewStyleRootView(
            progressProvider = progressProvider,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(horizontal = 16.dp)
                .padding(bottom = 5.dp)
                .fillMaxWidth()
                .height(44.dp)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onNavigateToNowPlaying
                )
        ) {
            MiniDiskContainer(mediaMetadata)

            MiniPlayerDiscViewFlipper(mediaMetadata)
            MiniPlayPauseButton(
                isPlaying = isPlaying,
                progressProvider = progressProvider,
                onClick = onPlayPauseClick
            )

            MinPlayListBtnContainer(onClick = onShowPlaylist)
        }
    }
}
@Composable
private fun MiniNewStyleRootView(
    progressProvider: () -> Float,
    modifier: Modifier = Modifier,
    content: @Composable RowScope.() -> Unit
) {
    val progressColor = DolphinTheme.colors.text2_1

    Row(
        modifier = modifier
            .background(
                color = DolphinTheme.colors.backgroundWhite,
                shape = RoundedCornerShape(22.dp)
            )
            .drawBehind {
                val progress = progressProvider()
                val margin = 22.dp.toPx()
                val lineH = 1.dp.toPx()
                val y = size.height - (lineH / 2f)

                drawLine(
                    color = ProgressBgColor,
                    start = Offset(margin, y),
                    end = Offset(size.width - margin, y),
                    strokeWidth = lineH,
                    cap = StrokeCap.Round
                )

                if (progress > 0) {
                    drawLine(
                        color = progressColor,
                        start = Offset(margin, y),
                        end = Offset(margin + (size.width - 2 * margin) * progress, y),
                        strokeWidth = lineH,
                        cap = StrokeCap.Round
                    )
                }
            },
        verticalAlignment = Alignment.CenterVertically,
        content = content
    )
}
@Composable
private fun MiniDiskContainer(
    mediaMetadata: MediaMetadata?,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.size(44.dp),
        contentAlignment = Alignment.Center
    ) {
        AsyncImage(
            model = mediaMetadata?.artworkUri ?: mediaMetadata?.artworkData,
            contentDescription = null,
            placeholder = ColorPainter(PlaceholderColor),
            error = ColorPainter(PlaceholderColor),
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(34.dp)
                .clip(CircleShape)
                .background(PlaceholderColor)
        )
    }
}
@Composable
private fun RowScope.MiniPlayerDiscViewFlipper(
    mediaMetadata: MediaMetadata?,
    modifier: Modifier = Modifier
) {
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
        modifier = modifier
            .weight(1f)
            .padding(start = 4.dp, end = 2.dp)
            .basicMarquee()
    )
}
@Composable
private fun MiniPlayPauseButton(
    isPlaying: Boolean,
    progressProvider: () -> Float,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val progressColor = DolphinTheme.colors.text2_1
    val circleColor = DolphinTheme.colors.text5_1
    val innerIconColor = DolphinTheme.colors.text2
    val playPaint = remember { Paint().apply { style = PaintingStyle.Fill } }

    Canvas(
        modifier = modifier
            .size(width = 36.dp, height = 44.dp)
            .clip(CircleShape)
            .clickable(onClick = onClick)
    ) {
        val progress = progressProvider()
        val contentSize = 23.dp.toPx()
        val strokeW = 1.67.dp.toPx()
        val offsetValX = (size.width - contentSize) / 2f + strokeW / 2f
        val offsetValY = (size.height - contentSize) / 2f + strokeW / 2f
        val topLeftOffset = Offset(offsetValX, offsetValY)
        val arcSize = Size(contentSize - strokeW, contentSize - strokeW)

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
            val lineLength = 8.5.dp.toPx()
            val centerSpace = 2.dp.toPx()
            val pauseStrokeW = 2.5.dp.toPx()
            val pauseCornerRadius = CornerRadius(0.7.dp.toPx())

            val rectWidth = pauseStrokeW
            val rectHeight = lineLength
            val leftRectX = center.x - (centerSpace / 2f) - rectWidth
            val rightRectX = center.x + (centerSpace / 2f)
            val rectY = center.y - (rectHeight / 2f)

            drawRoundRect(
                color = innerIconColor,
                topLeft = Offset(leftRectX, rectY),
                size = Size(rectWidth, rectHeight),
                cornerRadius = pauseCornerRadius
            )
            drawRoundRect(
                color = innerIconColor,
                topLeft = Offset(rightRectX, rectY),
                size = Size(rectWidth, rectHeight),
                cornerRadius = pauseCornerRadius
            )
        } else {
            val triangleLength = 10.dp.toPx()
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
@Composable
private fun MinPlayListBtnContainer(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.ic_playlist),
            contentDescription = "Playlist",
            contentScale = ContentScale.Inside,
            modifier = Modifier
                .size(44.dp)
                .padding(end = 12.dp)
                .clip(CircleShape)
                .clickable(onClick = onClick)
        )
    }
}


@Preview(showBackground = true, backgroundColor = 0xff000000)
@Composable
private fun MiniBarContainerPreview() {
    DolphinTheme {
        Box(modifier = Modifier.padding(16.dp)) {
            MiniBarContainer(
                mediaMetadata = null,
                isPlaying = false,
                progressProvider = { 0.3f },
                onPlayPauseClick = {},
                onNavigateToNowPlaying = {},
                onShowPlaylist = {}
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun MiniBarAssembledPreview() {
    DolphinTheme {
        Box(modifier = Modifier.padding(16.dp)) {
            MiniNewStyleRootView(progressProvider = { 0.3f }) {
                MiniDiskContainer(mediaMetadata = null)
                MiniPlayerDiscViewFlipper(mediaMetadata = null)
                MiniPlayPauseButton(isPlaying = false, progressProvider = { 0.3f }, onClick = {})
                MinPlayListBtnContainer(onClick = {})
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun MiniPlayPauseButtonPreview() {
    DolphinTheme {
        Row(modifier = Modifier.padding(16.dp)) {
            MiniPlayPauseButton(isPlaying = true, progressProvider = { 0.5f }, onClick = {})
            Box(modifier = Modifier.size(16.dp))
            MiniPlayPauseButton(isPlaying = false, progressProvider = { 0.0f }, onClick = {})
        }
    }
}