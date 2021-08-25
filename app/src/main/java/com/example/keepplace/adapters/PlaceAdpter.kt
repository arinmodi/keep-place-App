package com.example.keepplace.adapters

import android.content.Context
import android.net.Uri
import android.view.*
import androidx.recyclerview.widget.RecyclerView
import com.example.keepplace.R
import com.example.keepplace.models.PlaceModel
import kotlinx.android.synthetic.main.place_item.view.*

open class PlaceAdpter(private val context : Context, private var list: ArrayList<PlaceModel>) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var onClickListener : OnClickListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return MyViewHolder( LayoutInflater.from(context).inflate(R.layout.place_item,parent,false) )
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val model = list[position]

        if(holder is MyViewHolder){
            holder.itemView.location_image.setImageURI(Uri.parse(model.image))
            holder.itemView.location_name.setText(model.title);
            holder.itemView.setOnClickListener {
                if(onClickListener != null){
                    onClickListener!!.onClick(position,model)
                }
            }
            holder.itemView.more.setOnClickListener {
                if(onClickListener != null){
                    onClickListener!!.onMoreClick(position,model,it,list)
                }
            }
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }

    interface OnClickListener {
        fun onClick(position: Int, model : PlaceModel)
        fun onMoreClick(position: Int,model: PlaceModel, view : View,list : ArrayList<PlaceModel>)
    }

    fun setOnClickListener(OnClickListener : OnClickListener){
        this.onClickListener = OnClickListener
    }

    private class MyViewHolder(view : View) : RecyclerView.ViewHolder(view)


}