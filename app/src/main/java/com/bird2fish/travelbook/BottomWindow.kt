package com.bird2fish.travelbook

import android.content.Context
import android.content.res.Resources
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*

// 这个类是用于一个弹出消息的底部窗口
class BottomWindow(private val context: Context, id: Int, txt : String) {

    private var popupWindow: PopupWindow

    init {
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
        popupWindow.showAtLocation(popupView, Gravity.BOTTOM, 0, 0)

        // 设置 PopupWindow 进入和退出的动画效果（可选）
        popupWindow.animationStyle = R.style.PopupWindowAnimation

        // 设置点击外部不关闭PopupWindow
        popupWindow.isOutsideTouchable = false
        // 设置背景为null，防止点击外部传递到下面的View
        popupWindow.setBackgroundDrawable(null)

        // 添加监听器，处理关闭事件
        popupWindow.setOnDismissListener {
            // 在这里添加处理 PopupWindow 关闭时的逻辑
        }

        // 如果需要处理点击事件等，可以在这里找到对应的 View 并设置相应的监听器
        val textInfo = popupView.findViewById<TextView>(R.id.tv_main_info)
        textInfo.setText(txt)

        var btnClose = popupView.findViewById<Button>(R.id.btn_close_pop)
        btnClose.setOnClickListener{
            this.dismissPopupWindow()
        }

        val buttonHeight = btnClose.height // 获取按钮的高度
        val windowHeight = Resources.getSystem().displayMetrics.heightPixels // 获取整个窗口的高度

// 计算ScrollView的高度，减去按钮的高度
        val scrollViewHeight = windowHeight - buttonHeight - 150

// 设置ScrollView的高度
        val scrollView = popupView.findViewById<ScrollView>(R.id.scrview_pop_container)
        val layoutParams = scrollView.layoutParams
        layoutParams.height = scrollViewHeight
        scrollView.layoutParams = layoutParams
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
