package com.bird2fish.travelbook.helper

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.bird2fish.travelbook.core.UiHelper
import com.bird2fish.travelbook.helper.NotificationActionReceiver

class NotificationActionReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != null && intent.action == ACTION_BUTTON_CLICK_STOP) {
            // 在这里调用单例模式实例中的方法
            UiHelper.showCenterMessage(context, "hello notification")
        }
    }

    companion object {
        const val ACTION_BUTTON_CLICK_STOP = "com.bird2fish.travelbook.stoptracking"
    }
}