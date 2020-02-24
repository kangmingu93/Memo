package com.line.mymemo.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.line.mymemo.R
import kotlinx.android.synthetic.main.layout_image_preview.view.*

class ImageRecyclerAdapter : RecyclerView.Adapter<ImageRecyclerAdapter.ViewHolder>() {

    private lateinit var context: Context
    private val imageList : ArrayList<String> = ArrayList()

    interface ItemClick { fun onClick(view: View, position: Int) }
    var itemClick: ItemClick? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        context = parent.context
        val view = LayoutInflater.from(parent.context).inflate(R.layout.layout_image_preview, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(imageList[position], context)
        if (itemClick != null) holder.itemView.setOnClickListener { v -> itemClick?.onClick(v, position) }
    }

    override fun getItemCount(): Int = imageList.size

    // 전체 조회
    fun getAllData(): ArrayList<String> {
        return imageList
    }

    // 단일 조회
    fun getData(position: Int): String {
        return imageList[position]
    }

    // 단일 입력
    fun addData(newData: String) {
        imageList.add(newData)
        notifyDataSetChanged()
    }

    // 단일 삭제
    fun removeData(position: Int) {
        imageList.removeAt(position)
        notifyDataSetChanged()
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        fun bind(image: String, context: Context) {
            Glide.with(context)
                .load(image)
                .thumbnail(0.1f)
                .error(R.drawable.ic_no_photo)
                .into(itemView.image_view_thumnail)
        }

    }
}