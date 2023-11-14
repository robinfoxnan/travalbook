package com.bird2fish.travelbook.core

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Color
import android.os.Build
import com.bird2fish.travelbook.R
import com.bird2fish.travelbook.helper.LogHelper
import com.bird2fish.travelbook.helper.NotificationActionReceiver
import com.tencent.map.geolocation.TencentLocation
import com.tencent.map.geolocation.TencentLocationListener
import com.tencent.map.geolocation.TencentLocationManager
import com.tencent.map.geolocation.TencentLocationRequest


class TencentLocService : TencentLocationListener {
    private var locationManager: TencentLocationManager? = null
    private var notificationManager: NotificationManager? = null
    private var isCreateChannel = false

    // 添加通知栏
    private fun buildNotification(context: Context): Notification? {
        var builder: Notification.Builder? = null
        var notification: Notification? = null
        if (Build.VERSION.SDK_INT >= 26) {
            //Android O上对Notification进行了修改，如果设置的targetSDKVersion>=26建议使用此种方式创建通知栏
            if (notificationManager == null) {
                notificationManager =
                    context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            }
            val channelId = context.packageName
            if (!isCreateChannel) {
                val notificationChannel = NotificationChannel(
                    channelId,
                    NOTIFICATION_CHANNEL_NAME, NotificationManager.IMPORTANCE_DEFAULT
                )
                notificationChannel.enableLights(true) //是否在桌面icon右上角展示小圆点
                notificationChannel.lightColor = Color.BLUE //小圆点颜色
                notificationChannel.setShowBadge(true) //是否在久按桌面图标时显示此渠道的通知
                notificationManager!!.createNotificationChannel(notificationChannel)
                isCreateChannel = true
            }
            builder = Notification.Builder(context.applicationContext, channelId)
        } else {
            builder = Notification.Builder(context.applicationContext)
        }
        builder.setSmallIcon(R.drawable.ic_notification_icon_small_24dp)
            .setContentTitle("知途")
            .setContentText("正在后台运行")
            .setLargeIcon(
                BitmapFactory.decodeResource(
                    context.resources,
                     R.drawable.ic_bar_stop_24dp
                )
            )
//            .setSmallIcon(
//                BitmapFactory.decodeResource(
//                    context.resources,
//                    R.drawable.ic_bar_stop_24dp
//                )
//            )
            .setWhen(System.currentTimeMillis())


        // 添加一个按钮，广播
        val buttonIntent = Intent(NotificationActionReceiver.ACTION_BUTTON_CLICK_STOP)
        val buttonPendingIntent = PendingIntent.getBroadcast(
            context,
            0,
            buttonIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or  PendingIntent.FLAG_IMMUTABLE
        )

        val action: Notification.Action = Notification.Action.Builder(
            com.bird2fish.travelbook.R.drawable.ic_bar_stop_24dp,
            "停止",
            buttonPendingIntent
        ).build()

        // 在通知构建器中添加按钮
        builder.addAction(action)

        notification = if (Build.VERSION.SDK_INT >= 16) {
            builder.build()
        } else {
            builder.notification
        }
        return notification
    }

    // // 在你的 Activity 或 Fragment 中初始化定位服务
    fun startBackGround(context: Context): Boolean {
        val mContext = context.applicationContext
        locationManager = TencentLocationManager.getInstance(mContext)
        locationManager!!.enableForegroundLocation(LOC_NOTIFICATIONID, buildNotification(context))
        val locationRequest = createReq()
        val error =
            locationManager!!.requestLocationUpdates(locationRequest, this, context.mainLooper)
        return isOk(context, error)
    }

    private fun isOk(context:Context, error: Int): Boolean {
        if (error == 0) {
            System.out.printf("成功\n")
            //UiHelper().showCenterMessage(context, "");
            return true
        } else if (error == 4) {  // 先设置同意隐私
            // 定位请求失败
            System.out.printf("失败， 应先同意隐私\n")
            //Toast.makeText(this, "fail", LENGTH_SHORT).show();
            UiHelper.showCenterMessage(context, "应先同意隐私");
            return false
        } else if (error == 2) {  // 2key不对
            UiHelper.showCenterMessage(context, "应去腾讯注册并检查KEY是否正确");
            return false
        }
        return false
    }

    fun stopBackGround() {
        if (locationManager != null) {
            locationManager!!.removeUpdates(this)
            locationManager!!.disableForegroundLocation(true)
        }
    }

    private fun createReq(): TencentLocationRequest {
        // 配置定位请求
        val locationRequest = TencentLocationRequest.create()
        locationRequest.interval = GlobalData.intervalOfLocation // 定位间隔，单位毫秒
        locationRequest.isAllowGPS = true
        //是否需要获取传感器方向
        locationRequest.isAllowDirection = true
        //是否需要开启室内定位
        locationRequest.isIndoorLocationMode = true
        // 设置定位模式
        val locMode = TencentLocationRequest.HIGH_ACCURACY_MODE
        locationRequest.locMode = locMode
        locationRequest.requestLevel = TencentLocationRequest.REQUEST_LEVEL_ADMIN_AREA // 设置定位精度等级
        // 设置GPS优先
        locationRequest.isGpsFirst = true
        return locationRequest
    }

    fun startLocationService(context: Context): Boolean {
        // 创建 TencentLocationManager
        val mContext = context.applicationContext
        locationManager = TencentLocationManager.getInstance(mContext)
        val locationRequest = createReq()
        // 启动定位
        val error = locationManager!!.requestLocationUpdates(locationRequest, this)
        return isOk(context, error)
    }

    fun stopLocationService() {
        if (locationManager != null) {
            locationManager!!.removeUpdates(this)
        }
    }

    /////////////////////////////////////////////////////////////////////////////////////
    //
    override fun onLocationChanged(tencentLocation: TencentLocation, error: Int, s: String) {
        if (error == TencentLocation.ERROR_OK) {
            // 获取经纬度信息
            val latitude = tencentLocation.latitude
            val longitude = tencentLocation.longitude
            //System.out.printf("%f, %f \n", latitude, longitude)

            GlobalData.setCurrentLocation(tencentLocation)
            // 处理获取到的经纬度
        } else {
            // 定位失败，处理失败信息
            System.out.printf("err = %d\n", error)
            checkUpdateErr(error)
        }
    }

    private fun checkUpdateErr(error: Int): Boolean{
        when(error)
        {
            0->{
                return true
            }
            1->{
                LogHelper.d("TencentLocService", "net work err")
                return false
            }
            2->{
                LogHelper.d("TencentLocService", "用户没有给权限，或者没有信号")
                return false
            }
            4->{
                LogHelper.d("TencentLocService", "坐标转换失败")
                return false
            }
        }
        return false
    }

    override fun onStatusUpdate(s: String, i: Int, s1: String) {}

    companion object {
        var instance: TencentLocService? = null
            get() {
                if (field == null) {
                    // 初始化权限，以及用户同意隐私协议
                    TencentLocationManager.setUserAgreePrivacy(true)
                    field = TencentLocService()
                }
                return field
            }
            private set
        private const val LOC_NOTIFICATIONID = 1 // 你自己定义的唯一标识符
        private const val NOTIFICATION_CHANNEL_NAME = "启停跟踪"
    }
}