package com.bird2fish.travelbook.ui.publish

import android.graphics.Rect
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bird2fish.travelbook.R
import com.bird2fish.travelbook.core.ImagePathPair
import com.bird2fish.travelbook.core.UiHelper

class GalleryAdapter (private val dataList: List<ImagePathPair>) : RecyclerView.Adapter<GalleryAdapter.ImageGalleryHolder>() {

    private var view: PublishImageNewsFragment? = null

    fun setView(v: PublishImageNewsFragment){
        this.view = v
    }
    //加载布局文件并返回MyViewHolder对象
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageGalleryHolder {
        //创建view对象
        val view = LayoutInflater.from(parent.context).inflate(R.layout.gallery_image_item, parent, false)
        //创建MyViewHolder对象
        return ImageGalleryHolder(view)
    }

    fun calculateDesiredWidth(recyclerViewWidth: Int):Int{
        val width = (recyclerViewWidth - 45) / 3
        return width
    }
    //获取数据并显示到对应控件
    override fun onBindViewHolder(holder: ImageGalleryHolder, position: Int) {

        val recyclerViewWidth = holder.itemView.context.resources.displayMetrics.widthPixels
        val desiredWidth: Int = calculateDesiredWidth(recyclerViewWidth) // 根据需要计算宽度

        val layoutParams = holder.itemView.layoutParams
        layoutParams.width = desiredWidth
        layoutParams.height = desiredWidth
        holder.itemView.layoutParams = layoutParams

        //给我的四个控件获取一下数据，注意不同类型调用不同的方法，设置图片用setImageResource（），设置文字用setText（）
        val dataPair = dataList[position]

        if (dataPair.localPath == "add"){
            holder.img.setImageResource(R.drawable.add_image)
            holder.img.setOnClickListener{
                if (view != null){
                    view!!.onClickItemAdd(position)
                }
            }
        }else{
            //val picasso = PicassoHelper.getInstance(view!!.requireActivity())
            val uri = Uri.parse(dataPair.localPath)
            System.out.println(uri)
            try {
                holder.img.setImageURI(uri)
            } catch (e: Exception) {
                e.printStackTrace()
            }



            holder.img.setOnClickListener{
                val location = IntArray(2)
                holder.img.getLocationOnScreen(location)

                val x = location[0] // 获取View在屏幕上的X坐标
                val y = location[1] // 获取View在屏幕上的Y坐标
                if (view != null){
                    view!!.showPopupMenu(position, x, y)
                }
            }

        }

        // 状态
        if (dataPair.state == ""){
            holder.state.visibility = View.INVISIBLE
        }else if (dataPair.state == "uploading"){
            holder.state.visibility = View.VISIBLE
            holder.state.setImageResource(R.drawable.matrix_ic_r10_settings_night)
        }else if (dataPair.state == "ok"){
            holder.state.visibility = View.VISIBLE
            holder.state.setImageResource(R.drawable.ok)
        }else{
            holder.state.visibility = View.VISIBLE
            holder.state.setImageResource(R.drawable.error)
        }



    }



    private fun setErrorImage(holder: ImageGalleryHolder, imageId:Int){
        var delta :Int = 0
        val drawable = UiHelper.loadAndScaleImage(view!!.requireActivity(), imageId, 200)
        holder.img.setImageDrawable(drawable)

    }

    override fun getItemCount(): Int {
        //获取列表条目总数
        return dataList.size
    }

    // 每个元素的控件
    inner class ImageGalleryHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        //初始化控件
        var img: ImageView
        var state:ImageView

        init {
            img = itemView.findViewById(R.id.img_item)
            state = itemView.findViewById(R.id.img_state)
        }
    }

    internal class space_item(space: Int) : RecyclerView.ItemDecoration() {
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