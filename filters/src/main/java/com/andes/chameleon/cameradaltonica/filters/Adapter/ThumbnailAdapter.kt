package com.andes.chameleon.cameradaltonica.filters.Adapter

import android.content.Context
import android.os.Debug
import android.support.v4.content.ContextCompat
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.andes.chameleon.cameradaltonica.filters.Interface.FilterListFragmentListener
import com.andes.chameleon.cameradaltonica.filters.R
import com.zomato.photofilters.utils.ThumbnailItem
import kotlinx.android.synthetic.main.thumbnail_list_item.view.*

class ThumbnailAdapter (private val context: Context,
                        private val thumbnailItemList: List<ThumbnailItem>,
                        private val listener: FilterListFragmentListener): RecyclerView.Adapter<ThumbnailAdapter.MyViewHolder>() {

    private var selectedIndex = 0

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val itemView = LayoutInflater.from(context).inflate(R.layout.thumbnail_list_item, parent, false)
        return MyViewHolder(itemView)
    }

    override fun getItemCount(): Int {
        Log.d("State", thumbnailItemList.size.toString())
        return thumbnailItemList.size
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val thumbNailItem = thumbnailItemList[position]
        holder.thumbNail.setImageBitmap(thumbNailItem.image)
        holder.thumbNail.setOnClickListener{
            listener.onFilterSelected(thumbNailItem.filter)
            selectedIndex = position
            notifyDataSetChanged()
        }

        holder.filterName.text = thumbNailItem.filterName

        if(selectedIndex == position){
            holder.filterName.setTextColor(ContextCompat.getColor(context, R.color.filter_label_selected))
        } else{
            holder.filterName.setTextColor(ContextCompat.getColor(context, R.color.filter_label_normal))
        }


    }

    class MyViewHolder (itemView: View): RecyclerView.ViewHolder(itemView) {
        var thumbNail: ImageView
        var filterName: TextView
        init {
            thumbNail= itemView.thumbnail
            filterName = itemView.filter_name
        }

    }
}