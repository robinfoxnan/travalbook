package com.bird2fish.travelbook.ui.playgroud

import android.graphics.Bitmap
import android.graphics.Rect
import android.graphics.drawable.Drawable
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
import com.bird2fish.travelbook.core.GlobalData
import com.bird2fish.travelbook.core.News
import com.bird2fish.travelbook.core.UiHelper
import com.bird2fish.travelbook.helper.LogHelper
import com.bird2fish.travelbook.helper.PicassoHelper
import com.squareup.picasso.Callback
import com.squareup.picasso.MemoryPolicy
import com.squareup.picasso.NetworkPolicy
import com.squareup.picasso.Picasso
import com.squareup.picasso.Target

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
        val id = UiHelper.getIconResId(news.icon)
        holder.head.setImageResource(id)
        holder.username.setText(news.nick)
        val likeCount = news.likes.toString()
        holder.likes.setText(likeCount)

        // 创建一个实现了 Target 接口的匿名类对象
        val target = object : Target {
            override fun onBitmapLoaded(bitmap: Bitmap?, from: Picasso.LoadedFrom?) {
                // 图片加载成功，可以在这里执行操作
                if (bitmap != null) {
                    holder.img.setImageBitmap(bitmap)
                }
            }

            override fun onBitmapFailed(e: Exception?, errorDrawable: Drawable?) {
                setErrorImage(holder, R.drawable.noimg)
            }

            override fun onPrepareLoad(placeHolderDrawable: Drawable?) {
                // 图片加载之前的准备工作
            }
        }

        if (news.images != null && news.images.size > 0){
            val url = GlobalData.getHttpServ().getImageUrl(news.images[0])

            val picasso = PicassoHelper.getInstance(view!!.requireActivity())
            picasso.load(url).error(R.drawable.noimg)
                //.resize(200, 200)
                //.networkPolicy(NetworkPolicy.NO_CACHE)  // Disable cache for network requests
                //.memoryPolicy(MemoryPolicy.NO_CACHE)    // Disable cache in memory
                .into(target)
        }else{
            if (view != null){
                setErrorImage(holder, R.drawable.point1)
            }
        }

        holder.img.setOnClickListener{
            if (view != null){
                view!!.onClickItem(position)
            }
        }

    }

    private fun setErrorImage(holder: MyViewHolder, imageId:Int){
        var delta :Int = 0
        val drawable = UiHelper.loadAndScaleImage(view!!.requireActivity(), imageId, 200)

        holder.img.setImageDrawable(drawable)
        val layoutParams = holder.img.getLayoutParams()
        delta =  UiHelper.computeHeight(drawable!!, layoutParams.width) - layoutParams.height
        layoutParams.height += delta
        holder.img.layoutParams = layoutParams
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

        var likes:TextView

        init {
            img = itemView.findViewById(R.id.news_item_img)
            title = itemView.findViewById(R.id.news_item_title)
            head = itemView.findViewById(R.id.news_item_icon)
            username = itemView.findViewById(R.id.news_item_username)
            container = itemView.findViewById(R.id.news_item_root)
            likes = itemView.findViewById(R.id.home_item_count)
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

