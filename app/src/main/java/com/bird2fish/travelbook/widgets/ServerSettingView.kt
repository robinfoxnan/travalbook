package com.bird2fish.travelbook.widgets

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.Gravity
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Button
import android.widget.PopupWindow
import android.widget.TextView
import com.bird2fish.travelbook.R
import com.bird2fish.travelbook.helper.PreferencesHelper

class ServerSettingView(context: Context) {

    private var popupWindow: PopupWindow

    init {
        // 填充布局 R.layout.pop_location
        val popupView = LayoutInflater.from(context).inflate(R.layout.setting_server, null)

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
        val schemaEdit = popupView.findViewById<TextView>(R.id.tv_shema)
        val schema = PreferencesHelper.getHostSchema()
        schemaEdit.setText(schema)


        val hostEdit = popupView.findViewById<TextView>(R.id.tv_ip)
        val host = PreferencesHelper.getHostName()
        hostEdit.setText(host)

        val saveBtn = popupView.findViewById<Button>(R.id.btn_server_ok)
        val cancelBtn = popupView.findViewById<Button>(R.id.btn_server_cancel)

        saveBtn.setOnClickListener{
            val schema = schemaEdit.text.toString()
            val host = hostEdit.text.toString()
            PreferencesHelper.setHost(schema, host)
        }

        cancelBtn.setOnClickListener{
            this.dismissPopupWindow()
        }


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
