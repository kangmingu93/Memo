package com.line.mymemo.adapter

import android.graphics.PorterDuff
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.viewpager.widget.PagerAdapter
import com.bumptech.glide.Glide
import com.line.mymemo.R
import com.line.mymemo.entity.ImageEntity
import kotlinx.android.synthetic.main.layout_image.view.*

class ViewPagerAdapter(private val isEditable: Boolean) : PagerAdapter() {

    private val images: ArrayList<ImageEntity> = ArrayList()

    interface ItemClick { fun onClick(view: View, position: Int) }
    interface CancelClick { fun onClick(view: View, position: Int) }
    var itemClick: ItemClick? = null
    var cancelClick: CancelClick? = null

    // 리스트 데이터 추가
    fun addImageList(newList: List<ImageEntity>) {
        images.addAll(newList)
        notifyDataSetChanged()
    }

    // 단일 데이터 추가
    fun addImage(newImage: ImageEntity) {
        images.add(newImage)
        notifyDataSetChanged()
    }

    // 단일 데이터 제거
    fun removeImage(position: Int) {
        images.removeAt(position)
        notifyDataSetChanged()
    }

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val inflater = LayoutInflater.from(container.context)
        val view = inflater.inflate(R.layout.layout_image, container, false)

        view.image_view_thumnail.setColorFilter(view.resources.getColor(R.color.colorFilter), PorterDuff.Mode.MULTIPLY)
        Glide.with(view.context)
            .load(images[position].path)
            .thumbnail(0.1f)
            .error(R.drawable.ic_no_photo)
            .into(view.image_view_thumnail)

        if (isEditable) {
            view.image_view_cancel.visibility = View.VISIBLE
            view.image_view_cancel.setOnClickListener { v -> cancelClick?.onClick(v, position) }
        } else {
            view.image_view_thumnail.setOnClickListener { v -> itemClick?.onClick(v, position) }
        }

        container.addView(view)
        return view
    }

    override fun destroyItem(container: ViewGroup, position: Int, obj: Any) {
        container.removeView(obj as View?)
    }

    override fun isViewFromObject(view: View, obj: Any): Boolean {
        return view == obj
    }

    override fun getCount(): Int {
        return images.size
    }

}