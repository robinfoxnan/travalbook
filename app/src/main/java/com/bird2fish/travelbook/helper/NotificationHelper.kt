package com.bird2fish.travelbook.helper


import android.app.*
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.app.NotificationCompat
import androidx.core.graphics.drawable.toBitmap
import com.bird2fish.travelbook.core.Keys
import com.bird2fish.travelbook.R
import com.bird2fish.travelbook.TencentMapActivity
import com.bird2fish.travelbook.core.TencentLocService

/*
 * 通知栏的辅助类
 */
class NotificationHelper(private val trackerService: TencentLocService) {

    /* 定义日志标签 */
    private val TAG: String = LogHelper.makeLogTag(NotificationHelper::class.java)

    /* 系统 */
    private val notificationManager: NotificationManager = trackerService.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    /* 创建通知 */
    fun createNotification(trackingState: Int, trackLength: Float, duration: Long, useImperial: Boolean): Notification {

        // 如果需要，创建通知渠道
        if (shouldCreateNotificationChannel()) {
            createNotificationChannel()
        }

        // 构建通知
        val builder = NotificationCompat.Builder(trackerService, Keys.NOTIFICATION_CHANNEL_RECORDING)
        //builder.setContentIntent(showActionPendingIntent)
        //builder.setSmallIcon(R.drawable.ic_notification_icon_small_24dp)
        builder.setContentText(getContentString(trackerService, duration, trackLength, useImperial))

        // 添加停止、恢复和显示的图标和操作
        when (trackingState) {
            Keys.STATE_TRACKING_ACTIVE -> {
                builder.setContentTitle(trackerService.getString(R.string.notification_title_trackbook_running))
                builder.addAction(stopAction)
                builder.setLargeIcon(AppCompatResources.getDrawable(trackerService, R.drawable.ic_notification_icon_large_tracking_active_48dp)!!.toBitmap())
            }
            else -> {
                builder.setContentTitle(trackerService.getString(R.string.notification_title_trackbook_not_running))
                builder.addAction(resumeAction)
                //builder.addAction(showAction)
               builder.setLargeIcon(AppCompatResources.getDrawable(trackerService, R.drawable.ic_notification_icon_large_tracking_stopped_48dp)!!.toBitmap())
            }
        }

        return builder.build()
    }

    /* 为通知构建上下文文本 */
    private fun getContentString(context: Context, duration: Long, trackLength: Float, useImperial: Boolean): String {
        return "${LengthUnitHelper.convertDistanceToString(trackLength, useImperial)} • ${DateTimeHelper.convertToReadableTime(context, duration)}"
    }

    /* 检查是否应该创建通知渠道 */
    private fun shouldCreateNotificationChannel() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && !nowPlayingChannelExists()

    /* 检查通知渠道是否存在 */
    @RequiresApi(Build.VERSION_CODES.O)
    private fun nowPlayingChannelExists() = notificationManager.getNotificationChannel(Keys.NOTIFICATION_CHANNEL_RECORDING) != null

    /* 创建通知渠道 */
    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel() {
        val notificationChannel = NotificationChannel(Keys.NOTIFICATION_CHANNEL_RECORDING,
            trackerService.getString(R.string.notification_channel_recording_name),
            NotificationManager.IMPORTANCE_LOW)
            .apply { description = trackerService.getString(R.string.notification_channel_recording_description) }
        notificationManager.createNotificationChannel(notificationChannel)
    }

    /* 通知的待定意图 */
    private val stopActionPendingIntent = PendingIntent.getService(
        trackerService,
        14,
        Intent(trackerService.applicationContext, TencentLocService::class.java).setAction(Keys.ACTION_STOP),
        PendingIntent.FLAG_IMMUTABLE)

    private val resumeActionPendingIntent = PendingIntent.getService(
        trackerService.applicationContext,
        16,
        Intent(trackerService.applicationContext, TencentLocService::class.java).setAction(Keys.ACTION_RESUME),
        PendingIntent.FLAG_IMMUTABLE)

//    private val showActionPendingIntent: PendingIntent? = TaskStackBuilder.create(trackerService.applicationContext).run {
//        addNextIntentWithParentStack(Intent(trackerService.applicationContext, TencentMapActivity::class.java).apply {
//            // 添加 FLAG_ACTIVITY_SINGLE_TOP 标志
//            addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
//            addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
//        })
//        getPendingIntent(10, PendingIntent.FLAG_IMMUTABLE)
//    }






    /* 通知操作 */
    private val stopAction = NotificationCompat.Action(
        R.drawable.ic_notification_action_stop_24dp,
        trackerService.getString(R.string.notification_pause),
        stopActionPendingIntent)

    private val resumeAction = NotificationCompat.Action(
        R.drawable.ic_notification_action_resume_36dp,
        trackerService.getString(R.string.notification_resume),
        resumeActionPendingIntent)

//    private val showAction = NotificationCompat.Action(
//        R.drawable.ic_notification_action_show_36dp,
//        trackerService.getString(R.string.notification_show),
//        showActionPendingIntent)
}

