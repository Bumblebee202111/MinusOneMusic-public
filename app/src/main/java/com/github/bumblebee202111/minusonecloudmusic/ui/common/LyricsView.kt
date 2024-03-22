package com.github.bumblebee202111.minusonecloudmusic.ui.common

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Paint.ANTI_ALIAS_FLAG
import android.util.AttributeSet
import android.util.Log
import android.view.View
import com.github.bumblebee202111.minusonecloudmusic.data.model.LyricsEntry

class LyricsView(context: Context, attrs: AttributeSet) : View(context, attrs) {

    private val textColor: Int = Color.WHITE
    private var textHeight: Float = 60F
    private var position: Long = 0
    private var currentLine: LyricsLine? = null
    private var lyricsLines: List<LyricsLine>? = null
    private val textPaint = Paint(ANTI_ALIAS_FLAG).apply {
        color = Color.GRAY
        textAlign = Paint.Align.CENTER
        textSize=textHeight
    }
    private val currentLyricsLineTextPaint = Paint(ANTI_ALIAS_FLAG).apply {
        color=Color.parseColor("#DDFFFFFF")
        textAlign = Paint.Align.CENTER
        textSize=textHeight
    }
    private var lyricsEntries: List<LyricsEntry>? = null

    fun setLyrics(lyrics: List<LyricsEntry>?) {
        if (this.lyricsEntries == lyrics) return
        this.lyricsEntries = lyrics

        val pairs =
            lyrics?.flatMap { entry -> entry.times.map { time -> Pair(time, entry.lyrics) } }
                ?.sortedBy { it.first }
        lyricsLines = pairs?.mapIndexed { index, pair ->
            val dur =
                if (index < pairs.size - 1) pairs[index + 1].first - pairs[index].first else Long.MAX_VALUE - pair.first
            LyricsLine(index, pair.first, pair.second, dur)
        }

        update()
    }

    fun setPosition(position: Long) {
        if (this.position == position) {
            return
        }

        this.position = position

        if (this.lyricsLines == null)
            return

        val currentLine = getLyricsLineAt(position)
        val prevLine = this.currentLine
        this.currentLine = currentLine

        if (lyricsLines.isNullOrEmpty())
            return

        if (prevLine == null) {
            invalidate()
        } else if (prevLine.time <= position && prevLine.time + prevLine.dur >= position) {
            Log.d("LyricsView", "ignore")
            return
        } else {
            invalidate()
        }
    }

    override fun onDraw(canvas: Canvas) {
        canvas.save()
        val currentLine = currentLine
        val lyricsLines = lyricsLines
        if (currentLine != null && lyricsLines != null) {

            val height = height
            val width = width
            canvas.translate(0F, height / 2-(currentLine.lineNumber+1) * textHeight)
            val currentLineHeight = textHeight
            val x = width / 2F
            var y = 0F
            for (l in 0 until currentLine.lineNumber) {
                y += textHeight
                canvas.drawText(
                    lyricsLines[l].lyrics,
                    x,
                    y,
                    textPaint
                )
            }
            y += textHeight

            canvas.drawText(
                currentLine.lyrics,
                x,
                y,
                currentLyricsLineTextPaint
            )


            for (l in currentLine.lineNumber + 1 until lyricsLines.size) {
                y += textHeight
                canvas.drawText(
                    lyricsLines[l].lyrics,
                    x,
                    y,
                    textPaint
                )
            }


        }


        canvas.restore()
    }

    private fun getLyricsLineAt(position: Long): LyricsLine? {
        val lyricsLines = lyricsLines
            ?: throw RuntimeException("lyricsLines must be set before getLyricsLineAt() is called")
        return lyricsLines.lastOrNull { it.time <= position } ?: lyricsLines.firstOrNull()
    }


    private fun update() {
        invalidate()
    }

    private class LyricsLine(
        val lineNumber: Int,
        val time: Long,
        val lyrics: String,
        val dur: Long,
    )
}