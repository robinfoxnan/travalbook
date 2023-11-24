package com.bird2fish.travelbook.core

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.NetworkInfo
import android.util.Log
import android.widget.Toast
import com.bird2fish.travelbook.helper.LogHelper

class NetworkChangeReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        if (cm != null) {
            val network = cm.activeNetwork
            val capabilities = cm.getNetworkCapabilities(network)

            if (capabilities != null) {
                if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                    LogHelper.d("BroadcastReceiver", "WiFi connected")
                    //Log.d("NetworkChangeReceiver", "WiFi connected")
                    // 处理 WiFi 连接的情况
                } else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) {
                    LogHelper.d("NetworkChangeReceiver", "Mobile data connected")
                    // 处理移动数据连接的情况
                    if (TencentLocService.instance != null){
                        TencentLocService.instance!!.noticeNetworkChange()
                    }
                }
            } else {
                LogHelper.d("NetworkChangeReceiver", "No active network")
                // 处理网络断开的情况
            }
        }

        //网络广播接收者


    }
}

//class NetworkChangeReceiver : BroadcastReceiver() {
//    override fun onReceive(context: Context, intent: Intent) {
//        if (intent.action == ConnectivityManager.CONNECTIVITY_ACTION) {
//            val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
//            val activeInfo: NetworkInfo? = connectivityManager.activeNetworkInfo
//            val netInfo: NetworkInfo? = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE)
//            val wifiInfo: NetworkInfo? = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI)
//
//            if (activeInfo != null) {
//                // 网络可用
//                if (activeInfo.state == NetworkInfo.State.CONNECTED) {
//                    // 判断移动数据
//                    if (netInfo?.state == NetworkInfo.State.CONNECTED) {
//                        Toast.makeText(context, "您正在使用移动数据", Toast.LENGTH_SHORT).show()
//                    }
//                    // 判断 Wifi 数据
//                    if (wifiInfo?.state == NetworkInfo.State.CONNECTED) {
//                        Toast.makeText(context, "您正在使用Wifi数据", Toast.LENGTH_SHORT).show()
//                    }
//                } else {
//                    Toast.makeText(context, "请检查网络是否已联网", Toast.LENGTH_SHORT).show()
//                }
//            }
//        }
//    }
//}
