package com.bird2fish.travelbook.ui.tracks

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bird2fish.travelbook.R
import com.bird2fish.travelbook.core.TracklistElement

class TrackItemAdapter(private val trackList: List<TracklistElement>) : RecyclerView.Adapter<TrackItemAdapter.TrackViewHolder>() {

    private var fragment : maptrack? = null


    fun setView(view : maptrack?){
        this.fragment = view
    }
    // 创建 ViewHolder
    class TrackViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        // ViewHolder 中的视图元素，例如 TextView、ImageView 等
        val tvName: TextView = itemView.findViewById(R.id.tv_name)
        val tvDate : TextView= itemView.findViewById(R.id.tv_time)
        val tvDur: TextView = itemView.findViewById(R.id.tv_duration)
        val tvlength : TextView= itemView.findViewById(R.id.tv_length)
        val starImg:ImageView =  itemView.findViewById(R.id.img_star)
        //val itemContainer: RelativeLayout = itemView.findViewById(R.id.itemContainer)
        val btnDelete: TextView = itemView.findViewById(R.id.tv_delete)
        var btnShare :TextView = itemView.findViewById(R.id.tv_track_item_share)

        var tvPts :TextView = itemView.findViewById(R.id.tv_track_item_pts)
        var tvEndTime :TextView = itemView.findViewById(R.id.tv_track_item_endtime)

    }

    // 创建 ViewHolder
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TrackViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.track_list_item, parent, false)
        return TrackViewHolder(itemView)
    }


    // 绑定数据到 ViewHolder
    override fun onBindViewHolder(holder: TrackViewHolder, position: Int) {
        val trackItem = trackList[position]
        holder.tvName.text = trackItem.name
        holder.tvDate.text = "日期：" + trackItem.dateString
        holder.tvEndTime.setText("结束：" + trackItem?.endTimeString)

        holder.tvDur.setText("时长：" + trackItem?.durationString)
        holder.tvPts.setText("%d 个轨迹点".format(trackItem?.points))


        var lenStr = ""
        if (trackItem!!.length > 1000.0){

            lenStr = "路程：" + "%.2f 千米".format(trackItem.length / 1000.0)
        }else
        {
            lenStr = "路程：" + "%.0f 米".format(trackItem.length / 1000.0)
        }

        holder.tvlength.setText(lenStr)
        if (trackItem != null && trackItem!!.starred){
            holder.starImg.setImageResource(android.R.drawable.btn_star_big_on)
        }else{
            holder.starImg.setImageResource(android.R.drawable.btn_star_big_off)
        }

        holder.starImg.tag = position
        holder.starImg.setOnClickListener{
            if (fragment != null){
                fragment!!.onClickItemStar(holder.btnDelete.tag as Int)
            }
        }

        holder.btnDelete.tag = position

        // 可以添加其他逻辑...
        holder.btnDelete.setOnClickListener{
            if (fragment != null){
                fragment!!.onClickItemDelete(holder.btnDelete.tag as Int)
            }
        }

        holder.btnShare.setOnClickListener{
            if (fragment != null){

            }
        }
    }

    // 返回数据项数量
    override fun getItemCount(): Int {
        return trackList.size
    }

    // 其他方法，例如添加删除项的方法，用于与 ItemTouchHelper 配合实现左滑删除
    // ...

}
