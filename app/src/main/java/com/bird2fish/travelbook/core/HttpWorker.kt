package com.bird2fish.travelbook.core

import android.location.Location
import com.bird2fish.travelbook.helper.LogHelper
import com.bird2fish.travelbook.helper.PreferencesHelper
import com.bird2fish.travelbook.ui.data.model.CurrentUser
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.text.SimpleDateFormat


class HttpWorker {
    private var bRunning: Boolean = false
    private var gpxList0 = ArrayList<Location>(60)
    private var gpxList1 = ArrayList<Location>(60)
    private var listIndex = 0
    private var uid = "13800138000"
    val lock = Any()

    private var host :String = "" //""127.0.0.1:7787"
    private var schema:String = "http"

    public fun setUser(phone: String){
        uid = phone
    }
    fun setSchema(mode :String){
        this.schema = mode
    }
    fun setServer(url :String){
        this.host = url
    }


    public fun startWorker(){

        host = PreferencesHelper.getHostName()
        schema = PreferencesHelper.getHostSchema()

        if (bRunning)
            return
        bRunning = true
        Thread {
            doWork()
            //Thread.sleep(1000)
        }.start()
    }


    public fun stopWorker(){
        bRunning = false
    }

    companion object {
        private var instance: HttpWorker? = null

        //使用同步锁注解
        @Synchronized
        fun get(): HttpWorker{
            if (instance == null) {
                instance = HttpWorker()
            }
            return instance!!
        }
    }

    public fun pushGpx(location: Location){
        synchronized(lock) {
            if (listIndex == 0)
                gpxList0.add(location)
            else
                gpxList1.add(location)
        }
    }

    // 这里使用双队列切换
    private fun doWork(){
        var lastIndex: Int = 0
        while (bRunning){
            // 先同步切换入队的队列，
            synchronized(lock) {
                lastIndex = listIndex
                if (listIndex == 0)
                {
                    listIndex = 1
                }
                else
                {
                    listIndex = 0
                }
            }

            // 将空闲队列清空
            if (lastIndex == 0)
            {
                for(loc in gpxList0)
                {
                    uploadLocation(loc)
                }
                gpxList0.clear()

            }
            else
            {
                for(loc in gpxList1)
                {
                    uploadLocation(loc)
                }
                gpxList1.clear()
            }

            // 长时间工作后需要检查是否需要退出
            if (!bRunning){
                return
            }
            Thread.sleep(2000)
        }
    }

//    {
//        "uid": "1007",
//        "lat" : 40,
//        "lon":116,
//        "ele" :100,
//        "speed" :0,
//        "tm":0
//    }

    // http://localhost:7817/v1/gpx/updatepoint
    private fun uploadLocation(location: Location){
        val user = CurrentUser.getUser()
        if (user == null)
        {
            return
        }

        //构建url地址
        var url = "${schema}://${host}/v1/gpx/updatepoint"

        //构建Json字符串
        var jsonObject= JSONObject()
        jsonObject.put("uid", user.uid)
        //jsonObject.put("phone", userphone)
        jsonObject.put("lat",location.latitude)
        jsonObject.put("lon",location.longitude)
        jsonObject.put("ele", location.altitude)
        if (location.speed < 0.1)
            jsonObject.put("speed", 0)
        else
            jsonObject.put("speed", location.speed)
        jsonObject.put("tm", location.time / 1000)
        jsonObject.put("tmStr", SimpleDateFormat("YY-MM-DD-hh-mm-ss").format(location.time))
        var jsonStr=jsonObject.toString()

        //调用请求
        val contentType = "application/json".toMediaType()
        var requestBody = jsonStr.toRequestBody(contentType)

        try {
            val client = OkHttpClient()
            val request = Request.Builder()
                .url(url)
                .post(requestBody) //以post的形式添加requestBody
                .build()
            var response = client.newCall(request).execute()
            val responseData = response.body?.string()
            if (responseData != null) {
                val jsonObject = JSONObject(responseData)
                val state = jsonObject.getString("state")
                LogHelper.d(" upload date response $state")

            }
        }
        catch (e: Exception) {
            LogHelper.e("Exception")
            LogHelper.e("$e.message")
        }

    }// end of upload
}