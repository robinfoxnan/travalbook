package com.bird2fish.travelbook

import android.content.Context
import android.graphics.drawable.ColorDrawable
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.PopupWindow

class BottomWindow(private val context: Context) {

    private var popupWindow: PopupWindow

    init {
        // 填充布局
        val popupView = LayoutInflater.from(context).inflate(R.layout.pop_location, null)

        // 创建 PopupWindow 实例
        popupWindow = PopupWindow(
            popupView,
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            true
        )

        // 设置 PopupWindow 的背景
        popupWindow.setBackgroundDrawable(ColorDrawable(android.graphics.Color.TRANSPARENT))

        // 设置 PopupWindow 的位置（在底部）
        popupWindow.showAtLocation(popupView, Gravity.BOTTOM, 0, 0)

        // 设置 PopupWindow 进入和退出的动画效果（可选）
        popupWindow.animationStyle = R.style.PopupWindowAnimation

        // 设置点击外部不关闭PopupWindow
        popupWindow.isOutsideTouchable = false
        // 设置背景为null，防止点击外部传递到下面的View
        popupWindow.setBackgroundDrawable(null)

        // 添加监听器，处理关闭事件
        popupWindow.setOnDismissListener(null)

        // 如果需要处理点击事件等，可以在这里找到对应的 View 并设置相应的监听器
        val linearLayoutIcons: LinearLayout = popupView.findViewById(R.id.linear_layout_icons)
        val icon1: ImageView = popupView.findViewById(R.id.btmv_ico1)
        val icon2: ImageView = popupView.findViewById(R.id.btmv_ico2)

        // 添加其他图标和处理点击事件等...

        // 如果你希望 PopupWindow 一直显示，可以在合适的时机手动关闭
        // popupWindow.dismiss()
    }

    // 如果需要在外部调用显示 PopupWindow 的方法，可以添加一个公共方法
    fun showPopupWindow() {
        if (!popupWindow.isShowing) {
            popupWindow.showAtLocation(
                LayoutInflater.from(context).inflate(R.layout.activity_main, null),
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
