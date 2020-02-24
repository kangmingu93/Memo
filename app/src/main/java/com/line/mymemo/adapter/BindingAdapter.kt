package com.line.mymemo.adapter

import android.view.View
import android.widget.ImageView
import androidx.cardview.widget.CardView
import androidx.databinding.BindingAdapter
import com.bumptech.glide.Glide
import com.line.mymemo.R
import com.line.mymemo.entity.ImageEntity

@BindingAdapter("setImage")
fun bindImageFormUrl (view: ImageView, images: List<ImageEntity>?) {

    if (images != null && images.isNotEmpty()) {
        Glide.with(view.context)
            .load(images[0].path)
            .thumbnail(0.1f)
            .error(R.drawable.ic_no_photo)
            .into(view)
        (view.parent as CardView).visibility = View.VISIBLE
    } else {
        (view.parent as CardView).visibility = View.GONE
    }

}