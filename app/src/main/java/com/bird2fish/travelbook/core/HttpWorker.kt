package com.bird2fish.travelbook.core

import android.location.Location
import com.bird2fish.travelbook.helper.DateTimeHelper
import com.bird2fish.travelbook.helper.LogHelper
import com.bird2fish.travelbook.helper.PreferencesHelper
import com.bird2fish.travelbook.ui.contact.Friend
import com.bird2fish.travelbook.ui.data.model.CurrentUser
import com.tencent.map.geolocation.TencentLocation
import com.tencent.map.lib.models.ReturnInfoModelClass.BaseFloatReturnInfo
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.LinkedList


class HttpWorker {
    private var bRunning: Boolean = false
    private var gpxList0 = ArrayList<TencentLocation>(60)
    private var gpxList1 = ArrayList<TencentLocation>(60)
    private var listIndex = 0
    private var uid = "13800138000"
    val lock = Any()

    val lockFriendMap = Any()
    private var lastpointList :LinkedList<Friend> = LinkedList()

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

    public fun pushGpx(location: TencentLocation){
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
        var lastUpdateTm:Long = 0
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

            // 定期更新好友位置信息
            val tm = DateTimeHelper.getTimeStamp()
            if ((tm - lastUpdateTm) > GlobalData.intervalOfRefresh)
            {
                getLastPoint()
                lastUpdateTm = tm
            }

            // 长时间工作后需要检查是否需要退出
            if (!bRunning){
                return
            }
            Thread.sleep(1000)
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
    private fun uploadLocation(location: TencentLocation){
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
        jsonObject.put("accuracy", location.accuracy)
        jsonObject.put("src", location.sourceProvider)
        jsonObject.put("direction", location.direction)

        if (location.city != null)  jsonObject.put("city", location.city)
        if (location.address != null)  jsonObject.put("addr", location.address)
        if (location.street != null)  jsonObject.put("street", location.street)
        if (location.streetNo != null) jsonObject.put("streetNo", location.streetNo)

        if (location.speed < 0.1)
            jsonObject.put("speed", 0)
        else
            jsonObject.put("speed", location.speed)
        jsonObject.put("tm", location.time / 1000)
        val tmStr = DateTimeHelper.convertTimestampToDateString(location.time)
        //System.out.println(tmStr)
        jsonObject.put("tmStr", tmStr)
        var jsonStr=jsonObject.toString()
        //System.out.println(jsonStr)

        //调用请求
        val contentType = "application/json".toMediaType()
        var requestBody = jsonStr.toRequestBody(contentType)

        try {
            var client = HttpsUtil.getClient()
            val request = Request.Builder()
                .url(url)
                .post(requestBody) //以post的形式添加requestBody
                .build()
            var response = client.newCall(request).execute()
            val responseData = response.body?.string()
//            System.out.println(
//                "=================>" + responseData
//            )

            if (responseData != null) {
                val jsonObject = JSONObject(responseData)
                if (jsonObject != null)
                {
                    val state = jsonObject.getString("state")
                    LogHelper.d(" upload data response $state")
                }else{
                    LogHelper.d("reponse data = $responseData")
                }

            }
        }
        catch (e: Exception) {
            LogHelper.e("Exception")
            LogHelper.e("$e.message")


            System.out.println(e.printStackTrace())
        }

    }// end of upload

    // 返回一个map,方便查看
    fun getLastPointMap() : MutableMap<String, Friend>{
        var friendMap = mutableMapOf("0" to  Friend())
        friendMap.clear()
        synchronized(lockFriendMap){
            for (i in 0 until lastpointList.size){
                val fid = lastpointList[i].uid
                friendMap.put(fid, lastpointList[i])
            }
        }
        return friendMap
    }

    // 针对每一个关注的好友，获取位置
    private fun getLastPoint(friend: Friend) :Boolean {
        //构建url地址
        val user = CurrentUser.getUser()
        if (user == null)
            return false

        var url1 = "${schema}://${host}/v1/gpx/position?uid=${user.uid}&sid=${user.sid}&fid=${friend.uid}"

        try {
            var client = HttpsUtil.getClient()
            val request = Request.Builder()
                .url(url1)
                .get()
                .build()
            var response = client.newCall(request).execute()
            val responseData = response.body?.string()
            val jsonObject = JSONObject(responseData)
            val state = jsonObject.getString("state")

            if (state == "ok") {
                val pt = jsonObject.getJSONObject("pt")
                if (pt != null){
                    friend.isShare = true
                    friend.lon = pt.getDouble("lon")
                    friend.lat = pt.getDouble("lat")
                    friend.ele = pt.getDouble("ele")
                    friend.speed = pt.getDouble("speed")
                    friend.tm = pt.getLong("tm")
                    return true
                }
            }else{
                val code = jsonObject.getString("code")
                friend.isShare = false
                friend.msg = "不是对方好友"
                LogHelper.d("user id=${user.uid} return positon err = ${code}")
            }
        }catch (e: Exception) {
            //LogHelper.e("Exception")
            e.printStackTrace();
            LogHelper.e("$e.message")
            friend.isShare = false
            friend.msg = "网络错误" + "$e.message"
        }
        return false
    }


    // 按照列表更新位置信息
    private fun getLastPoint(){
        if (!GlobalData.shouldViewFriendLocation) {return }

        if (GlobalData.followList == null || GlobalData.followList.isEmpty()){
            val lst = GlobalData.getHttpServ().getFollowList()
            GlobalData.setFollowers(1, lst)
        }

        var lst = GlobalData.getCopyOfFriendList()
        for (i in 0 until lst.size){
            getLastPoint(lst[i])
        }

        synchronized(lockFriendMap){
            lastpointList = lst
        }
    }
}