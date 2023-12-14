package com.bird2fish.travelbook.ui.fav

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bird2fish.travelbook.R
import com.bird2fish.travelbook.core.FavLocation
import com.bird2fish.travelbook.core.TracklistElement
import com.bird2fish.travelbook.core.UiHelper
import com.bird2fish.travelbook.ui.contact.CommonViewHolder
import com.bird2fish.travelbook.ui.contact.ListViewAdapter
import com.bird2fish.travelbook.ui.tracks.maptrack

//class FavItemAdapter (context: Context?, datas: List<FavLocation?>?) :
//    ListViewAdapter<FavLocation?>(context!!, datas!!, R.layout.fav_list_item) {
//
//    private var fragment : favFragment? = null
//
//
//    fun setView(view : favFragment?){
//        this.fragment = view
//    }
//
//    override fun convert(holder: CommonViewHolder?, item: FavLocation?) {
//
//        if (item == null)
//            return
//        val imgIcon = (holder!!.getView(R.id.img_fav_icon) as ImageView)
//        val tvUid =  (holder!!.getView(R.id.tv_fav_uid) as TextView)
//        val tvNick = (holder!!.getView(R.id.tv_nick) as TextView)
//        val tvDate = (holder!!.getView(R.id.tv_fav_date) as TextView)
//        val tvTitle = (holder!!.getView(R.id.tv_fav_title) as TextView)
//        val tvDes = (holder!!.getView(R.id.tv_fav_des) as TextView)
//
//        val id = UiHelper.getIconResId(item!!.uIcon)
//        imgIcon.setImageResource(id)
//
//        tvUid.setText(item!!.uId)
//        tvUid.visibility = View.INVISIBLE
//        tvNick.setText(item!!.uNick)
//        tvDate.setText(item!!.tmStr)
//        tvTitle.setText(item!!.title)
//        tvDes.setText(item!!.des)
//
//    }
//
//}

class FavItemAdapter(private val dataList: List<FavLocation>) : RecyclerView.Adapter<FavItemAdapter.FavViewHolder>() {

    private var fragment : favFragment? = null


    fun setView(view : favFragment?){
        this.fragment = view
    }

    // 创建 ViewHolder
    class FavViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        // ViewHolder 中的视图元素，例如 TextView、ImageView 等
        val imgIcon :ImageView = itemView.findViewById(R.id.img_fav_icon)
        val tvUid : TextView =  itemView.findViewById(R.id.tv_fav_uid)
        val tvNick: TextView  = itemView.findViewById(R.id.tv_nick)
        val tvDate : TextView = itemView.findViewById(R.id.tv_fav_date)
        val tvTitle : TextView = itemView.findViewById(R.id.tv_fav_title)
        val tvDes : TextView = itemView.findViewById(R.id.tv_fav_des)
        val tvDelete :TextView = itemView.findViewById(R.id.tv_fav_delete)

    }

    // 创建 ViewHolder
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FavViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.fav_list_item, parent, false)
        return FavViewHolder(itemView)
    }


    // 绑定数据到 ViewHolder
    override fun onBindViewHolder(holder: FavViewHolder, position: Int) {
        val item = dataList[position]

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
                fragment!!.onClickItemDelete(holder.tvDelete.tag as Int)
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