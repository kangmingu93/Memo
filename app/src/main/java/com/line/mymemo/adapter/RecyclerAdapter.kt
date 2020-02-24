package com.line.mymemo.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.line.mymemo.R
import com.line.mymemo.databinding.LayoutMemoBinding
import com.line.mymemo.entity.MemoWithImageEntity

class RecyclerAdapter : RecyclerView.Adapter<RecyclerAdapter.ViewHolder>() {

    private val items : ArrayList<MemoWithImageEntity> = ArrayList()

    interface ItemClick { fun onClick(view: View, position: Int) }
    var itemClick: ItemClick? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            DataBindingUtil.inflate(
                LayoutInflater.from(parent.context),
                R.layout.layout_memo,
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.apply {
            bind(item)
            itemView.tag = item
            if (itemClick != null) {
                itemView.setOnClickListener {
                    v -> itemClick?.onClick(v, position)
                }
            }
        }
    }

    override fun getItemCount(): Int = items.size

    // 리스트 데이터 추가
    fun addList(newList: List<MemoWithImageEntity>) {
        items.addAll(newList)
        notifyDataSetChanged()
    }

    // 단일 데이터 추가
    fun addItem(newItem: MemoWithImageEntity) {
        items.add(0, newItem)
        notifyDataSetChanged()
    }

    // 단일 데이터 조회
    fun getItem(position: Int): MemoWithImageEntity {
        return items[position]
    }

    // 단일 데이터 제거
    fun removeItem(position: Int) {
        items.removeAt(position)
        notifyDataSetChanged()
    }

    // 단일 데이터 수정
    fun updateItem(position: Int, newItem: MemoWithImageEntity) {
        items[position] = newItem
        notifyDataSetChanged()
    }

    // 데이터 초기화
    fun clear() {
        items.clear()
        notifyDataSetChanged()
    }

    class ViewHolder(private val binding: LayoutMemoBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(temp: MemoWithImageEntity) {
            binding.apply {
                item = temp
            }
        }

    }
}