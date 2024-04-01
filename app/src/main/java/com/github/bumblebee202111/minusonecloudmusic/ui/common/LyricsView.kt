package com.github.bumblebee202111.minusonecloudmusic.ui.common

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Paint.ANTI_ALIAS_FLAG
import android.text.StaticLayout
import android.text.TextPaint
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.view.animation.DecelerateInterpolator
import android.widget.Scroller
import androidx.core.view.isVisible
import com.github.bumblebee202111.minusonecloudmusic.data.model.LyricsEntry
import kotlin.math.roundToInt

class LyricsView(context: Context, attrs: AttributeSet) : View(context, attrs) {


    private val textColor: Int = Color.WHITE
    private var normalTextSize: Float = ViewUtils.dpToPx(context, 17)
    private var highlightedTextSize: Float = ViewUtils.dpToPx(context, 20)
    private val lineSpacing: Float = ViewUtils.dpToPx(context, 18)
    private var position: Long = 0
    private var highlightedLine: Int? = null
    private var scrollDestination: Int? = null
    private var lyricsLines: List<LyricsLine>? = null
    private val normalTextPaint: TextPaint = TextPaint(ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#AAFFFFFF")
        textAlign = android.graphics.Paint.Align.CENTER
        textSize = normalTextSize
    }
    private val highlightedTextPaint: TextPaint = TextPaint(ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#DDFFFFFF")
        textAlign = Paint.Align.CENTER
        textSize = highlightedTextSize
    }
    private var lyricsEntries: List<LyricsEntry>? = null

    private var scroller: Scroller
    private var isPrepared = false
    private var isScrolling = false
    private lateinit var normalLineLayouts: List<StaticLayout>
    private lateinit var highlightedLineLayouts: List<StaticLayout>
    private var height: Int = 0
    private var width: Int = 0

    init {
        scroller = Scroller(context, DecelerateInterpolator(0.604f))
    }

    fun setLyrics(lyrics: List<LyricsEntry>?) {
        if (this.lyricsEntries == lyrics) return
        this.lyricsEntries = lyrics

        val pairs =
            lyrics?.flatMap { entry -> entry.times.map { time -> Pair(time, entry.lyrics) } }
                ?.sortedBy { it.first }
        lyricsLines = pairs?.mapIndexed { index, pair ->
            val dur =
                if (index < pairs.size - 1) {
                    pairs[index + 1].first - pairs[index].first
                } else {
                    Long.MAX_VALUE - pair.first
                }
            LyricsLine(index, pair.first, pair.second, dur)
        }

        isPrepared = false
        isScrolling = false
        scrollDestination = null
        highlightedLine = null
        update()
    }

    fun setPosition(position: Long) {
        if (this.position == position || lyricsLines == null) {
            return
        }

        this.position = position

        val newLine = findLyricsLineAt(position)

        if (!isPrepared) {
            highlightedLine = newLine
            invalidate()
        } else if (isScrolling) {
            if (newLine != scrollDestination) {
                scroller.forceFinished(true)
                scrollTo(newLine)
            }
        } else if (newLine != highlightedLine) {
            highlightedLine = newLine
            scrollTo(newLine)
        }

    }

    private fun scrollTo(destination: Int?) {
        this.scrollDestination = destination
        val destY = getOffsetY(destination ?: 0)
        val dy = destY - offsetY
        Log.d("LyricsView", "$dy $offsetY ${destination.toString()}")
        isScrolling = true

        scroller.startScroll(0, offsetY.roundToInt(), 0, dy.roundToInt(), 300)
        invalidate()
    }

    
    private fun getOffsetY(line: Int): Float {

        val highlightedLineLayouts = highlightedLineLayouts
        val normalLineLayouts = normalLineLayouts
        var y = height / 2F - lineSpacing * (line - 1)
        for (i in 0 until line) {
            y -= if (highlightedLine == i)
                highlightedLineLayouts[i].height
            else {
                normalLineLayouts[i].height
            }
        }
        y -= if (highlightedLine == line) {
            highlightedLineLayouts[line].height / 2
        } else {
            normalLineLayouts[line].height / 2
        }
        return y
    }

    override fun onDraw(canvas: Canvas) {

        val highlightedLine = highlightedLine
        val lyricsLines = lyricsLines

        this.height = super.getHeight()
        this.width = super.getWidth()

        if (lyricsLines.isNullOrEmpty() || height == 0) return
        if (!isPrepared) {
            prepareForNewLyrics()
        }

        if (!isScrolling) {
            offsetY = getOffsetY(highlightedLine ?: 0)
        }


        val dx = width / 2F
        var dy = offsetY

        for (l in lyricsLines.indices) {
            val line = if (highlightedLine == l) {
                highlightedLineLayouts[l]
            } else {
                normalLineLayouts[l]
            }

            val lineHeight = line.height
            if (dy + lineHeight > 0 && dy < height) {
                with(canvas) {
                    save()
                    translate(dx, dy)
                    line.draw(this)
                    canvas.restore()
                }

            }

            dy += lineHeight + lineSpacing
        }

    }

    private fun findLyricsLineAt(position: Long): Int? {
        val lyricsLines = lyricsLines
            ?: throw RuntimeException("lyricsLines must be set before getLyricsLineAt() is called")
        return lyricsLines.lastOrNull { it.time <= position }?.lineNumber
    }


    private fun prepareForNewLyrics() {
        val lyricsLines = lyricsLines ?: return
        normalLineLayouts = List(lyricsLines.size) { index ->
            StaticLayout.Builder.obtain(
                lyricsLines[index].lyrics,
                0,
                lyricsLines[index].lyrics.length,
                normalTextPaint,
                width
            ).build()
        }.also { normalLineLayouts = it }
        highlightedLineLayouts = List(lyricsLines.size) { index ->
            StaticLayout.Builder.obtain(
                lyricsLines[index].lyrics,
                0,
                lyricsLines[index].lyrics.length,
                highlightedTextPaint,
                width
            ).build()
        }.also { highlightedLineLayouts = it }
        isPrepared = true
    }

    private fun update() {
        invalidate()
    }

    private var offsetY = 0F
    override fun computeScroll() {
        if (lyricsLines == null) return
        if (scroller.computeScrollOffset()) {
            Log.d("LyricsView", "currY: " + scroller.currY)
            offsetY = scroller.currY.toFloat()
            invalidate()
        } else {
            if (isScrolling) {
                isScrolling = false
                highlightedLine = scrollDestination
                invalidate()
            }

        }
    }

    private data class LyricsLine(
        val lineNumber: Int,
        val time: Long,
        val lyrics: String,
        val dur: Long,
    )

}