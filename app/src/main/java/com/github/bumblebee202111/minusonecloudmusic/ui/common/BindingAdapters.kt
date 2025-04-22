package com.github.bumblebee202111.minusonecloudmusic.ui.common

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
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
import androidx.databinding.BindingAdapter
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.github.bumblebee202111.minusonecloudmusic.R
import com.github.bumblebee202111.minusonecloudmusic.utils.imageUrl
import kotlin.math.roundToInt
import androidx.core.graphics.toColorInt
import androidx.core.graphics.drawable.toDrawable



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
fun ImageView.image(
    model: Any?,
    thumbnailSize: Int? = null,
    quality: Int? = null,
    circleCrop: Boolean? = false,
    placeholder: Drawable? = null,
    crossFadeDuration: Int? = null
) {
    if (model == null && placeholder == null) return

    val remoteImageUrl = if (model is String) {
        model.imageUrl(thumbnailSize = thumbnailSize, quality = quality)
    } else {
        model
    }
    var rb = Glide.with(context).load(remoteImageUrl ?: placeholder)
    with(rb) {
        if (model != null && placeholder != null) {
            rb = placeholder(placeholder)
        }
        if (circleCrop == true) {
            rb = optionalCircleCrop()
        }
        if (crossFadeDuration != null) {
            rb = transition(DrawableTransitionOptions.withCrossFade(crossFadeDuration))
        }
        into(this@image)
    }
}

@BindingAdapter(
    value = ["image", "thumbnailSize", "quality", "circleCrop", "placeholder", "crossFadeDuration"],
    requireAll = false
)
fun ImageView.image(
    model: Any?,
    thumbnailSize: Int? = null,
    quality: Int? = null,
    circleCrop: Boolean? = false,
    placeholder: String? = null,
    crossFadeDuration: Int? = null
) {
    image(
        model = model,
        thumbnailSize = thumbnailSize,
        quality = quality,
        circleCrop = circleCrop,
        placeholder = placeholder?.toColorInt()?.toDrawable(),
        crossFadeDuration = crossFadeDuration
    )
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
