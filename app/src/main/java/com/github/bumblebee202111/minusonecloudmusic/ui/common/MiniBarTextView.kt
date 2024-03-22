package com.github.bumblebee202111.minusonecloudmusic.ui.common

import android.content.Context
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.TextUtils
import android.text.style.AbsoluteSizeSpan
import android.text.style.ForegroundColorSpan
import android.util.AttributeSet
import androidx.annotation.ColorInt
import androidx.appcompat.widget.AppCompatTextView
import com.github.bumblebee202111.minusonecloudmusic.R

class MiniBarTextView @JvmOverloads constructor(
    context: Context,
    attributeSet: AttributeSet?,
    i: Int = 0
) : AppCompatTextView(context, attributeSet, i) {
    
    private var title: String? = null
    private var artist: String? = null

    private fun createSpannableString(size: Int, source: String?, @ColorInt color: Int): SpannableString {
        if (source.isNullOrEmpty()) {
            return SpannableString("")
        }
        val spannableString = SpannableString(source)
        val absoluteSizeSpan = AbsoluteSizeSpan(size, true)
        val foregroundColorSpan = ForegroundColorSpan(color)
        spannableString.setSpan(absoluteSizeSpan, 0, source.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        spannableString.setSpan(foregroundColorSpan, 0, source.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        return spannableString
    }

    private fun updateForTitleAndArtistChange() {
        text = SpannableStringBuilder().apply {
            append(createSpannableString(14, title, context.getColor(R.color.colorText2)))
            append(createSpannableString(14, " - $artist", context.getColor(R.color.colorText3_1)))
        }
    }

    fun setTitleAndArtist(title: String?, artist: String?) {
        this.artist = artist
        this.title = title
        updateForTitleAndArtistChange()
    }

    init {
        ellipsize = TextUtils.TruncateAt.END
    }
}