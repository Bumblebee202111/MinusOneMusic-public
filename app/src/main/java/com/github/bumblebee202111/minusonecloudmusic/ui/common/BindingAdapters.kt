package com.github.bumblebee202111.minusonecloudmusic.ui.common

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.databinding.BindingAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.github.bumblebee202111.minusonecloudmusic.data.model.Billboard
import com.github.bumblebee202111.minusonecloudmusic.ui.toplists.BillboardAdapter

@BindingAdapter(value = ["imageUrl","circleCrop","placeholder"], requireAll = false)
fun bindImageUrl(view: ImageView, imageUrl: String?, circleCrop:Boolean?=false, placeholder: Drawable?=null) {

        when(imageUrl){
            null->{
                Glide.with(view.context)
                    .load(placeholder).run {
                        if(circleCrop == true)
                            optionalTransform(CircleCrop())
                        else this
                    }
                    .into(view)
            }
            else->{
                Glide.with(view.context)
                    .load(imageUrl).placeholder(placeholder).run {
                        if(circleCrop == true)
                            return@run optionalTransform(CircleCrop())
                        this
                    }
                    .into(view)
            }

    }
}

@BindingAdapter(value = ["imageUrl","circleCrop","placeholder"], requireAll = false)
fun bindImageUrl(view: ImageView, imageUrl: String?, circleCrop:Boolean?=false, placeholder: String?=null) {
 bindImageUrl(view, imageUrl, circleCrop, placeholder?.let { ColorDrawable(Color.parseColor(it)) })
}


@BindingAdapter("billboards")
fun bindBillboards(view:RecyclerView,billboards:List<Billboard>?){
    if(!billboards.isNullOrEmpty()){
        view.isVisible=true
        val adapter=(view.adapter as? BillboardAdapter?:BillboardAdapter())
        view.adapter=adapter
        adapter.submitList(billboards)
    }else{
        view.isGone=true
    }
}

@BindingAdapter("artists")
fun bindArtists(view:TextView,artists:List<String>?){
    val artistsString=artists?.joinToString("/")?:"Unknown"
    view.text=artistsString
}
