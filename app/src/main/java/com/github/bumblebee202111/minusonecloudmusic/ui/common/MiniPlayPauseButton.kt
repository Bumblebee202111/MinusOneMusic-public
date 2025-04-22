package com.github.bumblebee202111.minusonecloudmusic.ui.common

import android.content.Context
import android.graphics.Canvas
import android.graphics.CornerPathEffect
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.util.AttributeSet
import androidx.annotation.ColorInt
import androidx.annotation.FloatRange
import androidx.appcompat.widget.AppCompatImageView
import com.github.bumblebee202111.minusonecloudmusic.R
import kotlin.math.pow
import androidx.core.graphics.withRotation

  
class MiniPlayPauseButton(context: Context, attributeSet: AttributeSet?) :
    AppCompatImageView(context, attributeSet) {
    private val centerSpace: Int
    private val contentSize: Int
    private val lineLength: Int
    private var mAngle = 0f
    private var mIsPlaying = false
    private var mMax: Int
    private val mPaint: Paint
    private val mPausePaint: Paint
    private var mProgress = 0
    private val mRectF: RectF
    private var mStartPosition = 0
    private val mTrianglePaint: Paint
    private val triangleLength: Int
    private val width: Int
    private val widthSize: Int
    private val density:Float
    init {
        val res = context.resources
        val displayMetrics = res.displayMetrics
        density = displayMetrics.density
        widthSize = dpToPx(density,40.0f)
        contentSize =dpToPx(density,23.0f)
        lineLength = dpToPx(density,9.0f)
        centerSpace = dpToPx(density,2.0f)
        triangleLength = dpToPx(density,10.75f)
        width = dpToPx(density,1.33f)
        mPaint = Paint()
        mTrianglePaint = Paint()
        mPausePaint = Paint()
        mRectF = RectF()
        mMax = 100
        mPaint.isAntiAlias = true
        mPaint.strokeCap = Paint.Cap.ROUND
        mPaint.strokeJoin = Paint.Join.MITER
        mPaint.style = Paint.Style.STROKE
        val paint = mPaint
        paint.strokeWidth = if (width % 2 == 0) {
            width.toFloat()
        } else {
            (width + 1).toFloat()
        }
        mTrianglePaint.isAntiAlias = true
        mTrianglePaint.strokeCap = Paint.Cap.SQUARE
        mTrianglePaint.strokeJoin = Paint.Join.ROUND
        mTrianglePaint.style = Paint.Style.FILL
        mTrianglePaint.pathEffect = CornerPathEffect(dpToPx(density,2.2f).toFloat())
        mTrianglePaint.strokeWidth = width.toFloat()
        mPausePaint.isAntiAlias = true
        mPausePaint.strokeCap = Paint.Cap.ROUND
        mPausePaint.strokeJoin = Paint.Join.MITER
        mPausePaint.style = Paint.Style.STROKE
        mPausePaint.strokeWidth = dpToPx(density,2.5f).toFloat()
        mPausePaint.color = innerIconColor
    }

    private fun calculateAngle() {
        val i = mMax
        if (i == 0) {
            mAngle = 0.0f
        } else {
            mAngle = mProgress * 1.0f / i * 360.0f
        }
    }

    private fun drawNormal(canvas: Canvas) {
        val i: Int
        mPaint.color = circleColor
        canvas.drawArc(mRectF, -90.0f, 360.0f, false, mPaint)
        mPaint.color = progressColor
        val i2 = mMax
        i = if (i2 == 0) {
            0
        } else {
            mStartPosition * 360 / i2
        }
        canvas.drawArc(mRectF, (i - 90).toFloat(), mAngle - i, false, mPaint)
        if (mIsPlaying) {
            mPausePaint.color = innerIconColor
            canvas.translate(
                measuredWidth / 2 - centerSpace / 2 - mPausePaint.strokeWidth / 2.0f,
                (measuredHeight / 2 - lineLength / 2).toFloat()
            )
            canvas.drawLine(
                0.0f,
                mPausePaint.strokeWidth / 2.0f,
                0.0f,
                lineLength - mPausePaint.strokeWidth / 2.0f,
                mPausePaint
            )
            canvas.translate(centerSpace + mPausePaint.strokeWidth, 0.0f)
            canvas.drawLine(
                0.0f,
                mPausePaint.strokeWidth / 2.0f,
                0.0f,
                lineLength - mPausePaint.strokeWidth / 2.0f,
                mPausePaint
            )
            return
        }
        mTrianglePaint.color = pausedColor
        C105946c.a(
            canvas,
            measuredWidth / 2.0F,
            measuredHeight / 2.0F,
            triangleLength.toFloat(),
            90.0f,
            dpToPx(density,2.2f).toFloat(),
            mTrianglePaint
        )
    }

    @get:ColorInt
    private val progressColor: Int
        get() = context.getColor(R.color.colorText2_1)

    @get:ColorInt
    val circleColor: Int
        get() = context.getColor(R.color.colorText5_1)

    @get:ColorInt
    val innerIconColor: Int
        get() = context.getColor(R.color.colorText2)
    var max: Int
        get() = mMax
        set(i) {
            mMax = i
            calculateAngle()
            invalidate()
        }

    @get:ColorInt
    val pausedColor: Int
        get() = context.getColor(R.color.colorText2)
    var isPlaying: Boolean
        get() = mIsPlaying
        set(value) {
            mIsPlaying = value
            invalidate()
        }
    override fun onDraw(canvas: Canvas) {
        drawNormal(canvas)
    }
    override fun onMeasure(i: Int, i2: Int) {
        super.onMeasure(MeasureSpec.makeMeasureSpec(widthSize, MeasureSpec.EXACTLY), i2)
    }
    override fun onSizeChanged(i: Int, i2: Int, i3: Int, i4: Int) {
        super.onSizeChanged(i, i2, i3, i4)
        mRectF.set(
            (i - contentSize) / 2 + mPaint.strokeWidth / 2.0f,
            (i2 - contentSize) / 2 + mPaint.strokeWidth / 2.0f,
            i - (i - contentSize) / 2 - mPaint.strokeWidth / 2.0f,
            i2 - (i2 - contentSize) / 2 - mPaint.strokeWidth / 2.0f
        )
    }

    fun setProgress(i: Int) {
        if (i == mProgress) {
            return
        }
        mProgress = i
        calculateAngle()
        invalidate()
    }

    fun setStartPosition(i: Int) {
        mStartPosition = i
    }
    companion object{
        private fun dpToPx(density: Float, dps: Float): Int {
            return (dps * density + 0.5f).toInt()
        }
    }

}
object C105946c {
    
    private val path = Path()

    
    fun a(
        canvas: Canvas,
        f: Float,
        f2: Float,
        f3: Float,
        @FloatRange(from = 0.0, to = 360.0) f4: Float,
        f5: Float,
        paint: Paint
    ) {
        canvas.withRotation(f4, f, f2) {
            val f6 = f - f3 * 0.5f
            val d = 3.0
            val d2 = 0.5
            val pow = d.pow(d2).toFloat() * f3 / 6.0f + f2
            val f7 = f6 + f3
            val pow2 = f2 - f3 * d.pow(d2).toFloat() / 3.0f
            paint.pathEffect = CornerPathEffect(f5)
            path.reset()
            path.moveTo(f6, pow)
            path.lineTo(f7, pow)
            path.lineTo(f, pow2)
            path.lineTo(f6, pow)
            path.close()
            drawPath(path, paint)
        }
    }
}
