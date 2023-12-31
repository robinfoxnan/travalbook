package com.bird2fish.travelbook.core

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.bird2fish.travelbook.helper.DateTimeHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class AlarmReceiver : BroadcastReceiver() {

    private var service :TencentLocService? = null
    fun setService(s :TencentLocService?){
        this.service = s
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        // 执行任务的逻辑
        if (intent?.action == "tencent_loc_once") {
            // 执行任务的逻辑
            if (service != null){
                service!!.getOneTimeLoacation(service!!)
                service!!.setTimerNext()
            }

            // 睡眠的补救措施
            if (HttpWorker.get().getListSize() > 2){
                val tm = DateTimeHelper.getTimeStamp()
                if ((tm - GlobalData.gpxUploadTm) > GlobalData.intervalOfLocation + 60000)
                {
                    runUploadWork()
                }

            }

        }
    }

    fun runUploadWork(){
        GlobalScope.launch(Dispatchers.IO) {

            HttpWorker.get().doSendWorkOnce()
            GlobalData.gpxUploadTm = DateTimeHelper.getTimeStamp()

        }
    }

}