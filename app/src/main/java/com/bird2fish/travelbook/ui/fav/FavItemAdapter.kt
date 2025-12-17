package com.bird2fish.travelbook.ui.fav

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bird2fish.travelbook.R
import com.bird2fish.travelbook.core.FavLocation

class FavItemAdapter(private val dataList: List<FavLocation>) : RecyclerView.Adapter<FavItemAdapter.FavViewHolder>() {

    private var fragment : favFragment? = null


    fun setView(view : favFragment?){
        this.fragment = view
    }

    // 创建 ViewHolder
    inner class FavViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        // ViewHolder 中的视图元素，例如 TextView、ImageView 等
        val imgIcon :ImageView = itemView.findViewById(R.id.img_fav_icon)
        val tvUid : TextView =  itemView.findViewById(R.id.tv_fav_uid)
        val tvNick: TextView  = itemView.findViewById(R.id.tv_nick)
        val tvDate : TextView = itemView.findViewById(R.id.tv_fav_date)
        val tvTitle : TextView = itemView.findViewById(R.id.tv_fav_title)
        val tvDes : TextView = itemView.findViewById(R.id.tv_fav_des)
        val tvDelete :TextView = itemView.findViewById(R.id.tv_fav_share)
        val tvCopy :TextView = itemView.findViewById(R.id.tv_fav_copy)
        var index: Int = 0

        init {
            // 在构造函数中为整个 ViewHolder 的根视图设置点击事件
            itemView.setOnClickListener {
                // 处理点击事件
                if (fragment != null){
                    fragment!!.onClickItem(index)
                }
            }
        }

    }

    // 创建 ViewHolder
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FavViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.fav_list_item, parent, false)
        return FavViewHolder(itemView)
    }


    // 绑定数据到 ViewHolder
    override fun onBindViewHolder(holder: FavViewHolder, position: Int) {
        val item = dataList[position]
        holder.index = position

        holder.tvUid.setText(item!!.uId)
        holder.tvUid.visibility = View.INVISIBLE
        holder.tvNick.setText(item!!.uNick)
        holder.tvDate.setText(item!!.tmStr)
        holder.tvTitle.setText(item!!.title)
        holder.tvDes.setText(item!!.des)

        holder.tvDelete.tag = position

        // 可以添加其他逻辑...
        holder.tvDelete.setOnClickListener{
            if (fragment != null){
                fragment!!.onClickItemShare(holder.tvDelete.tag as Int)
            }
        }

        holder.tvCopy.setOnClickListener{
            if (fragment != null){
                fragment!!.onClickCopy(holder.tvDelete.tag as Int)
            }
        }


    }

    // 返回数据项数量
    override fun getItemCount(): Int {
        return dataList.size
    }

    // 其他方法，例如添加删除项的方法，用于与 ItemTouchHelper 配合实现左滑删除
    // ...

}