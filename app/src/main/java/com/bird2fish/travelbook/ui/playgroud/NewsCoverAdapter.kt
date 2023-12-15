package com.bird2fish.travelbook.ui.playgroud

import android.graphics.Rect
import androidx.recyclerview.widget.RecyclerView
import com.bird2fish.travelbook.ui.playgroud.NewsCoverAdapter.MyViewHolder
import android.view.ViewGroup
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import com.bird2fish.travelbook.R
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView.ItemDecoration
import com.bird2fish.travelbook.core.FavLocation
import com.bird2fish.travelbook.core.News
import com.bird2fish.travelbook.core.UiHelper

class NewsCoverAdapter(private val dataList: List<News>) : RecyclerView.Adapter<MyViewHolder>() {

    private var view: PlaygroundFragment? = null

    fun setView(v:PlaygroundFragment){
        this.view = v
    }
    //加载布局文件并返回MyViewHolder对象
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        //创建view对象
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.news_cover_item, parent, false)
        //创建MyViewHolder对象
        return MyViewHolder(view)
    }

    //获取数据并显示到对应控件
    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        //给我的四个控件获取一下数据，注意不同类型调用不同的方法，设置图片用setImageResource（），设置文字用setText（）
        val news = dataList[position]
        //holder.img.setImageResource()
        holder.title.setText(news.title)
        val id = UiHelper.getIconResId(news.uIcon)
        holder.head.setImageResource(id)
        holder.username.setText(news.uNick)

        var delta :Int = 0

        if (view != null){
            //val drawable = UiHelper.loadAndScaleImage(view!!.requireActivity(), R.drawable.news2, 200)
            val drawable = UiHelper. idToDrawable(view!!.requireActivity(), news.newsId)
            holder.img.setImageDrawable(drawable)
            val layoutParams = holder.img.getLayoutParams()
            delta =  UiHelper.computeHeight(drawable!!, layoutParams.width) - layoutParams.height
            layoutParams.height += delta
            holder.img.layoutParams = layoutParams


        }

        holder.img.setOnClickListener{
            if (view != null){
                UiHelper.showCenterMessage(view!!.requireActivity(), "点击了条目")
            }
        }

    }

    override fun getItemCount(): Int {
        //获取列表条目总数
        return dataList.size
    }

    inner class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        //初始化控件
        var img: ImageView
        var head: ImageView
        var title: TextView
        var username: TextView
        var container: LinearLayout

        init {
            img = itemView.findViewById(R.id.news_item_img)
            title = itemView.findViewById(R.id.news_item_title)
            head = itemView.findViewById(R.id.news_item_icon)
            username = itemView.findViewById(R.id.news_item_username)
            container = itemView.findViewById(R.id.news_item_root)
        }
    }

    internal class space_item(space: Int) : ItemDecoration() {
        //设置item的间距
        private var space = 5

        init {
            this.space = space
        }

        override fun getItemOffsets(
            outRect: Rect,
            view: View,
            parent: RecyclerView,
            state: RecyclerView.State
        ) {
            outRect.bottom = space
            outRect.top = space
        }
    }


}

