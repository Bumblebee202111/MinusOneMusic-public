package com.github.bumblebee202111.minusonecloudmusic.ui.common

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.github.bumblebee202111.minusonecloudmusic.model.LyricsEntry
import kotlin.math.pow
import kotlin.math.roundToInt
private val LyricLineSpacing = 28.dp
private val LyricPaddingHorizontal = 56.dp
private val LyricFadingEdgeLength = 50.dp
private const val LYRIC_TIME_OFFSET_MS = 50L
private const val DEFAULT_SCROLL_DURATION_MS = 300
private const val MAX_SCROLL_DURATION_MS = 800L
private val LyricDecelerateEasing = Easing { fraction ->
    1f - (1f - fraction).pow(2f * 0.604f)
}
private val NormalLyricTextStyle = TextStyle(
    color = Color(0x66FFFFFF),
    fontSize = 16.sp,
    textAlign = TextAlign.Center
)
private val HighlightedLyricTextStyle = TextStyle(
    color = Color.White,
    fontSize = 18.sp,
    textAlign = TextAlign.Center
)
@Composable
fun LyricsView(
    lyrics: List<LyricsEntry>?,
    currentPositionProvider: () -> Long,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val density = LocalDensity.current
    val lineSpacingPx = with(density) { LyricLineSpacing.toPx() }
    val paddingLrPx = with(density) { LyricPaddingHorizontal.toPx() }
    val fadingEdgePx = with(density) { LyricFadingEdgeLength.toPx() }

    val textMeasurer = rememberTextMeasurer()
    var canvasSize by remember { mutableStateOf(IntSize.Zero) }

    val lyricsLines = remember(lyrics) {
        val pairs = lyrics
            ?.flatMap { entry -> entry.times.map { time -> time to entry.lyrics } }
            ?.sortedBy { it.first }
        pairs?.mapIndexed { index, pair ->
            val dur = if (index < pairs.size - 1) {
                pairs[index + 1].first - pairs[index].first
            } else {
                Long.MAX_VALUE - pair.first
            }
            LyricsLine(
                lineNumber = index,
                time = pair.first,
                lyrics = pair.second,
                dur = dur
            )
        }
    }

    val currentPositionProviderState by rememberUpdatedState(currentPositionProvider)
    val highlightedLine by remember(lyricsLines) {
        derivedStateOf {
            val position = currentPositionProviderState() + LYRIC_TIME_OFFSET_MS
            lyricsLines?.lastOrNull { it.time <= position }?.lineNumber
        }
    }

    val contentWidth = (canvasSize.width - (paddingLrPx * 2).roundToInt()).coerceAtLeast(0)
    val lineLayouts = remember(lyricsLines, contentWidth, textMeasurer) {
        if (lyricsLines.isNullOrEmpty() || contentWidth <= 0) {
            null
        } else {
            val constraints = Constraints(minWidth = contentWidth, maxWidth = contentWidth)
            LyricsLayouts(
                normal = lyricsLines.map { line ->
                    textMeasurer.measure(
                        text = line.lyrics,
                        style = NormalLyricTextStyle,
                        constraints = constraints
                    )
                },
                highlighted = lyricsLines.map { line ->
                    textMeasurer.measure(
                        text = line.lyrics,
                        style = HighlightedLyricTextStyle,
                        constraints = constraints
                    )
                }
            )
        }
    }

    val offsetYAnimatable = remember(lyricsLines) { Animatable(0f) }
    var isInitialized by remember(lyricsLines) { mutableStateOf(false) }

    LaunchedEffect(highlightedLine, lineLayouts, canvasSize.height) {
        val layouts = lineLayouts ?: return@LaunchedEffect
        val lines = lyricsLines ?: return@LaunchedEffect
        if (canvasSize.height == 0 || lines.isEmpty()) return@LaunchedEffect

        val targetIndex = (highlightedLine ?: 0).coerceIn(lines.indices)
        val targetY = calculateOffsetY(
            line = targetIndex,
            layouts = layouts,
            height = canvasSize.height,
            lineSpacing = lineSpacingPx
        )

        if (!isInitialized) {
            offsetYAnimatable.snapTo(targetY)
            isInitialized = true
        } else {
            val lineDuration = lines.getOrNull(targetIndex)?.dur ?: 0L
            val durationMillis = if (lineDuration > 0L) {
                (lineDuration / 2).coerceAtMost(MAX_SCROLL_DURATION_MS).toInt()
            } else {
                DEFAULT_SCROLL_DURATION_MS
            }
            offsetYAnimatable.animateTo(
                targetValue = targetY,
                animationSpec = tween(
                    durationMillis = durationMillis,
                    easing = LyricDecelerateEasing
                )
            )
        }
    }

    Canvas(
        modifier = modifier
            .onSizeChanged { canvasSize = it }
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
            .graphicsLayer { compositingStrategy = CompositingStrategy.Offscreen }
            .drawWithContent {
                drawContent()
                val fadeHeight = fadingEdgePx.coerceAtMost(size.height / 2f)
                if (fadeHeight > 0f && size.height > 0f) {
                    val topFraction = fadeHeight / size.height
                    val bottomFraction = 1f - topFraction
                    drawRect(
                        brush = Brush.verticalGradient(
                            0f to Color.Transparent,
                            topFraction to Color.Black,
                            bottomFraction to Color.Black,
                            1f to Color.Transparent
                        ),
                        blendMode = BlendMode.DstIn
                    )
                }
            }
    ) {
        val layouts = lineLayouts ?: return@Canvas
        val lines = lyricsLines ?: return@Canvas
        if (lines.isEmpty() || size.height == 0f) return@Canvas

        val currentHighlighted = highlightedLine
        var dy = if (isInitialized) {
            offsetYAnimatable.value
        } else {
            calculateOffsetY(
                line = (currentHighlighted ?: 0).coerceIn(lines.indices),
                layouts = layouts,
                height = size.height.roundToInt(),
                lineSpacing = lineSpacingPx
            )
        }

        for (index in lines.indices) {
            val layoutResult = if (currentHighlighted == index) {
                layouts.highlighted[index]
            } else {
                layouts.normal[index]
            }

            val lineHeight = layoutResult.size.height
            if (dy + lineHeight > 0f && dy < size.height) {
                drawText(
                    textLayoutResult = layoutResult,
                    topLeft = Offset(paddingLrPx, dy)
                )
            }

            dy += lineHeight + lineSpacingPx
        }
    }
}

private fun calculateOffsetY(
    line: Int,
    layouts: LyricsLayouts,
    height: Int,
    lineSpacing: Float
): Float {
    var y = height / 2f - lineSpacing * line
    for (i in 0 until line) {
        y -= layouts.normal[i].size.height
    }
    y -= layouts.highlighted[line].size.height / 2f
    return y
}

private class LyricsLayouts(
    val normal: List<TextLayoutResult>,
    val highlighted: List<TextLayoutResult>
)

private data class LyricsLine(
    val lineNumber: Int,
    val time: Long,
    val lyrics: String,
    val dur: Long
)