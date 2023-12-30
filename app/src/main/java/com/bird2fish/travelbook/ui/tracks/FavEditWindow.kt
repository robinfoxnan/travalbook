package com.bird2fish.travelbook.ui.tracks

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.text.Spannable
import android.text.SpannableString
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import android.widget.PopupWindow
import android.widget.TextView
import com.bird2fish.travelbook.R
import com.bird2fish.travelbook.core.FavLocation
import com.bird2fish.travelbook.core.GlobalData
import com.bird2fish.travelbook.core.TracklistElement

// 这个类是用于一个弹出消息的底部窗口
class FavEditWindow(private val context: Context, id: Int, txt : String) {

    private var popupWindow: PopupWindow
    private var titleInfo:TextView? = null
    private var desInfo :TextView?= null
    private var view:Context? = null
    private var onDismissListener: PopupWindow.OnDismissListener? = null

    init {
        view = context
        // 填充布局 R.layout.pop_location
        val popupView = LayoutInflater.from(context).inflate(id, null)

        // 创建 PopupWindow 实例
        popupWindow = PopupWindow(
            popupView,
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            true
        )

        // 设置 PopupWindow 的背景
        popupWindow.setBackgroundDrawable(ColorDrawable(Color.WHITE))

        // 设置 PopupWindow 的位置（在底部）
        popupWindow.showAtLocation(popupView, Gravity.TOP, 0, 0)

        // 设置 PopupWindow 进入和退出的动画效果（可选）
        popupWindow.animationStyle = R.style.PopupWindowAnimation

        // 设置点击外部不关闭PopupWindow
        popupWindow.isOutsideTouchable = false
        // 设置背景为null，防止点击外部传递到下面的View
        popupWindow.setBackgroundDrawable(null)

        // 添加监听器，处理关闭事件
        popupWindow.setOnDismissListener {
            // 在这里添加处理 PopupWindow 关闭时的逻辑
            if (onDismissListener != null){
                onDismissListener!!.onDismiss()
            }
        }

        // 如果需要处理点击事件等，可以在这里找到对应的 View 并设置相应的监听器
        this.titleInfo = popupView.findViewById<TextView>(R.id.tv_fav_edit_title_v)
        this.desInfo = popupView.findViewById<TextView>(R.id.tv_fav_edit_des_v)
        val btnSave = popupView.findViewById<TextView>(R.id.btn_fav_edit_s)
        btnSave.setOnClickListener{
            if (favLoc != null){
                favLoc!!.title = titleInfo!!.text.toString()
                favLoc!!.des = desInfo!!.text.toString()

                GlobalData.saveFavLocations(view!!)
            }

            if (marker != null){
                marker!!.title = titleInfo!!.text.toString()
                marker!!.showInfoWindow()
            }

            if (trackItem != null){
                trackItem!!.title = titleInfo!!.text.toString()
                trackItem!!.content = desInfo!!.text.toString()

                GlobalData.saveTrackList(view!!)
            }


            this.dismissPopupWindow()
        }

    }

    fun setOnCloseListener(listener: PopupWindow.OnDismissListener?){
        this.onDismissListener = listener
    }

    // 用于关闭保存时候设置
    private var favLoc :FavLocation? = null
    private var marker: com.tencent.tencentmap.mapsdk.maps.model.Marker? = null

    private var trackItem : TracklistElement? = null

    fun setTrack(item: TracklistElement){
        this.trackItem = item

        this.titleInfo!!.text = item.title
        this.desInfo!!.text =  item.content
    }

    fun setLocation(loc :FavLocation, m: com.tencent.tencentmap.mapsdk.maps.model.Marker){
        this.favLoc = loc
        this.marker = m

        //this.setClearBtnWithText(this.titleInfo!!, loc.title)
        //this.setClearBtnWithText(this.desInfo!!, loc.des)
        this.titleInfo!!.text = loc.title
        this.desInfo!!.text =  loc.des
    }

    fun setClearBtnWithText(textView: TextView, str:String){
        // 创建一个 SpannableString
        val spannableString = SpannableString("${str}  清空")

        // 创建一个 ClickableSpan，用于处理点击事件
        val clearClick = object : ClickableSpan() {
            override fun onClick(widget: View) {
                // 在这里处理清空操作
                textView.text = ""
            }
        }

        // 将 ClickableSpan 应用到 SpannableString 中的指定位置
        spannableString.setSpan(clearClick, spannableString.length - 2, spannableString.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)

        // 设置 TextView 的文本
        textView.text = spannableString

        // 设置 TextView 可点击，并设置点击事件处理
        textView.movementMethod = LinkMovementMethod.getInstance()
        textView.highlightColor = Color.TRANSPARENT  // 去除点击时的高亮效果
    }

    // 如果需要在外部调用显示 PopupWindow 的方法，可以添加一个公共方法
    fun showPopupWindow() {
        if (!popupWindow.isShowing) {
            popupWindow.showAtLocation(
                popupWindow.contentView,
                Gravity.BOTTOM,
                0,
                0
            )
        }
    }

    // 添加关闭 PopupWindow 的方法
    fun dismissPopupWindow() {
        if (popupWindow.isShowing) {
            popupWindow.dismiss()
        }
    }
}