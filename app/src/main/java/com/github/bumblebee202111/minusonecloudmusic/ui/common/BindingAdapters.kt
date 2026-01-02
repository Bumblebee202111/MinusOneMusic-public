package com.github.bumblebee202111.minusonecloudmusic.ui.common

import android.graphics.Color
import android.graphics.drawable.Drawable
import android.text.SpannableString
import android.text.Spanned
import android.text.style.AbsoluteSizeSpan
import android.text.style.ForegroundColorSpan
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.StringRes
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.drawable.toDrawable
import androidx.core.graphics.toColorInt
import androidx.databinding.BindingAdapter
import coil3.load
import coil3.request.crossfade
import coil3.request.error
import coil3.request.placeholder
import coil3.request.transformations
import coil3.transform.CircleCropTransformation
import com.github.bumblebee202111.minusonecloudmusic.R
import com.github.bumblebee202111.minusonecloudmusic.utils.imageUrl
import kotlin.math.roundToInt



@BindingAdapter("isGone")
fun View.setIsGone(
    isGone: Boolean
) {
    visibility = if (isGone) View.GONE else View.VISIBLE
}

@BindingAdapter("isFakeBoldText")
fun TextView.bindIsFakeBoldText(isFakeBoldText: Boolean) {
    paint.isFakeBoldText = isFakeBoldText
}

@BindingAdapter(
    value = ["image", "thumbnailSize", "quality", "circleCrop", "placeholder", "crossFadeDuration"],
    requireAll = false
)
fun ImageView.loadImage(
    model: Any?,
    thumbnailSize: Int? = null,
    quality: Int? = null,
    circleCrop: Boolean? = false,
    placeholder: Any? = null,
    crossFadeDuration: Int? = null
) {
    val placeholderDrawable: Drawable? = when (placeholder) {
        is Drawable -> placeholder
        is Int -> try {
            ResourcesCompat.getDrawable(resources,placeholder,null)
        } catch (e: Exception) { null }
        is String -> try {
            placeholder.toColorInt().toDrawable()
        } catch (e: Exception) { null }
        else -> null
    }

    val dataToLoad = if (model is String) {
        model.imageUrl(thumbnailSize = thumbnailSize, quality = quality)
    } else {
        model
    }

    if (dataToLoad == null && placeholderDrawable == null) {
        this.setImageDrawable(null)
        return
    }

    load(dataToLoad) {
        if (placeholderDrawable != null) {
            placeholder(placeholderDrawable)
            error(placeholderDrawable)
        }
        if (circleCrop == true) {
            transformations(CircleCropTransformation())
        }
        if (crossFadeDuration != null) {
            crossfade(crossFadeDuration)
        } else {
            crossfade(true)
        }
    }
}


@BindingAdapter("android:text")
fun setText(view: TextView, @StringRes resId: Int) {
    if (resId == 0) {
        view.text = null
    } else {
        view.setText(resId)
    }
}


@BindingAdapter("artists")
fun TextView.artists(artists: List<String>?) {
    text = artists?.joinToString("/") ?: "Unknown"
}

@BindingAdapter(value = ["songItemSubtitleArtists", "songItemSubtitleAlbum"], requireAll = false)
fun TextView.playlistSongSubtitle(
    songItemSubtitleArtists: List<String>?,
    songItemSubtitleAlbum: String?
) {
    val sb = StringBuilder()
    val artistsString =
        songItemSubtitleArtists?.joinToString("/") ?: "Unknown"
    sb.append(artistsString)

    sb.append(" - ")

    val albumString = songItemSubtitleAlbum ?: "Unknown"
    sb.append(albumString)

    text = sb.toString()
}

@BindingAdapter(
    value = ["playerSongItemTitle", "playerSongItemArtists", "playerSongItemIsCurrentSong"],
    requireAll = false
)
fun TextView.playerSongItem(
    songItemTitle: String?,
    songItemArtists: List<String>?,
    isCurrentSong: Boolean
) {
    val titleString = songItemTitle ?: "Unknown"
    val artistsString = songItemArtists?.joinToString("/") ?: "Unknown"
    val spannableString = SpannableString("$titleString · $artistsString")
    val titleLength = titleString.length
    spannableString.setSpan(
        AbsoluteSizeSpan(ViewUtils.dpToPx(context, 15.0f).roundToInt()),
        0,
        titleLength,
        33
    )
    spannableString.setSpan(
        AbsoluteSizeSpan(ViewUtils.dpToPx(context, 11.0f).roundToInt()),
        titleLength,
        spannableString.length,
        Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
    )
    spannableString.setSpan(
        ForegroundColorSpan(
            if (isCurrentSong) ContextCompat.getColor(
                context,
                R.color.colorPrimary1
            ) else Color.BLACK
        ),
        0,
        titleLength,
        Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
    )
    text = spannableString
    setTextColor(
        if (isCurrentSong) ContextCompat.getColor(
            context,
            R.color.colorPrimary1
        ) else "#7f000000".toColorInt()
    )
}

@BindingAdapter("isCurrentSong")
fun TextView.setIsCurrentSong(isCurrentSong: Boolean) {
    setTextColor(
        if (isCurrentSong) ContextCompat.getColor(
            context,
            R.color.colorPrimary1
        ) else "#CC000000".toColorInt()
    )
}

@BindingAdapter("isCurrentSong")
fun LinearLayout.setIsCurrentSong(isCurrentSong: Boolean) {
    if (isCurrentSong)
        setBackgroundResource(R.color.colorText7)
    else setBackgroundColor(Color.WHITE)
}
