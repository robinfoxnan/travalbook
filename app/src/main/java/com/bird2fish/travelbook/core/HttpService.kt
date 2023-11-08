package com.bird2fish.travelbook.core

import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import android.util.Log
import com.bird2fish.travelbook.helper.LogHelper
import com.bird2fish.travelbook.helper.PreferencesHelper
import com.bird2fish.travelbook.ui.contact.Friend
import com.bird2fish.travelbook.ui.data.model.CurrentUser
import com.bird2fish.travelbook.ui.data.model.LoggedInUser
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONObject
import java.util.LinkedList


class HttpService : Service() {

    private var host :String = "" //""127.0.0.1:7787"
    private var schema:String = "http"


    fun setSchema(mode :String){
        this.schema = mode
    }
    fun setServer(url :String){
        this.host = url
    }

    fun initServer(){
        host = PreferencesHelper.getHostName()
        schema = PreferencesHelper.getHostSchema()
    }
    // 获取此类的引用
    inner class HttpBinder : Binder() {
        fun getService(): HttpService? {
            return this@HttpService
        }
    }

    //通过binder实现调用者client与Service之间的通信
    private val binder = HttpBinder()

    //private val generator: Random = Random()

    override fun onCreate() {
        Log.i("DemoLog", "TestService -> onCreate, Thread: " + Thread.currentThread().name)
        super.onCreate()
        host = PreferencesHelper.getHostName()
        schema = PreferencesHelper.getHostSchema()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.i(
            "DemoLog",
            "TestService -> onStartCommand, startId: " + startId + ", Thread: " + Thread.currentThread().name
        )
        return START_NOT_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? {
        Log.i("DemoLog", "TestService -> onBind, Thread: " + Thread.currentThread().name)
        return binder
    }

    override fun onUnbind(intent: Intent): Boolean {
        Log.i("DemoLog", "TestService -> onUnbind, from:" + intent.getStringExtra("from"))
        return false
    }

    override fun onDestroy() {
        Log.i("DemoLog", "TestService -> onDestroy, Thread: " + Thread.currentThread().name)
        super.onDestroy()
    }

    //getRandomNumber是Service暴露出去供client调用的公共方法
//    fun getRandomNumber(): Int {
//        return generator.nextInt()
//    }



    // 注册
    // http://localhost:7817/v1/user/regist?type=1&pwd=111111&username=robin
    fun register(name:String, pwd:String) : LoggedInUser {

        val fakeUser = LoggedInUser("", "", "", "0", "")
        //构建url地址
        var url = "${schema}://${host}/v1/user/regist?type=1&pwd=${pwd}&username=${name}"
        //构建Json字符串
 //       var jsonObject= JSONObject()
//        jsonObject.put("uid", UserHelper.uid)
//        //jsonObject.put("phone", userphone)
//        jsonObject.put("lat",location.latitude)
//        jsonObject.put("lon",location.longitude)
//        jsonObject.put("ele", location.altitude)
//        if (location.speed < 0.1)
//            jsonObject.put("speed", 0)
//        else
//            jsonObject.put("speed", location.speed)
//        jsonObject.put("tm", location.time / 1000)
//        jsonObject.put("tmStr", SimpleDateFormat("YY-MM-DD-hh-mm-ss").format(location.time))
//        var jsonStr=jsonObject.toString()

        //调用请求
        //val contentType = "application/json".toMediaType()
        //var requestBody = jsonStr.toRequestBody(contentType)

        try {
            var client = OkHttpClient()
            val request = Request.Builder()
                .url(url)
                .get() //以post的形式添加requestBody
                .build()
            var response = client.newCall(request).execute()
            val responseData = response.body?.string()
            if (responseData != null) {
                val jsonObject = JSONObject(responseData)
                val state = jsonObject.getString("state")
                //LogHelper.d(" upload date response $state")
                if (state == "ok")
                {
                    val user = jsonObject.getJSONObject("user")
                    if (user != null ) {
                        fakeUser.uid = user.getLong("id").toString()
                        fakeUser.userId = user.getString("name")
                        fakeUser.pwd = pwd
                        fakeUser.sid = ""
                    }
                }
            }
        }
        catch (e: Exception) {
            //LogHelper.e("Exception")
            //LogHelper.e("$e.message")
        }
        return fakeUser
    }

    // sid登录
    //http://localhost:7817/v1/user/login?uid=1005&sid=6812630841045951575&type=0
    fun loginWithSid(uid:String, sid:String, pwd:String) : LoggedInUser{
        val fakeUser = LoggedInUser("", "", "", "0", "")
        //构建url地址
        var url = "${schema}://${host}/v1/user/login?type=0&uid=${uid}&sid=${sid}"
        try {
            var client = OkHttpClient()
            val request = Request.Builder()
                .url(url)
                .get() //以post的形式添加requestBody
                .build()
            var response = client.newCall(request).execute()
            val responseData = response.body?.string()
            if (responseData != null) {
                val jsonObject = JSONObject(responseData)
                val state = jsonObject.getString("state")
                //LogHelper.d(" upload date response $state")
                if (state == "ok")
                {
                    val user = jsonObject.getJSONObject("session")
                    if (user != null ) {
                        fakeUser.uid = user.getLong("id").toString()
                        fakeUser.sid = user.getLong("sid").toString()
                        fakeUser.pwd = pwd
                    }
                }else{
                    PreferencesHelper.delUserSid()
                }
            }

            // 直接获取信息
            val userInfo = getUserInfo(fakeUser.uid, fakeUser.sid, fakeUser.uid)
            if (userInfo.uid == fakeUser.uid)
            {
                userInfo.sid = fakeUser.sid
                userInfo.pwd = fakeUser.pwd
                return userInfo
            }
        }
        catch (e: Exception) {
            PreferencesHelper.delUserSid()
            //LogHelper.e("Exception")
            LogHelper.e("$e.message")
        }

        return fakeUser
    }

    // 用户名密码登录
    // http://localhost:7817/v1/user/login?uid=1005&pwd=111111&type=1
    fun loginWithpwd(uid:String, pwd:String) : LoggedInUser {
        val fakeUser = LoggedInUser("", "", "", "0", "")
        //构建url地址
        var url1 = //"http://192.168.1.2:7817/v1/user/login?uid=1005&pwd=123456&type=1"
            "${schema}://${host}/v1/user/login?type=1&pwd=${pwd}&uid=${uid}"

        try {
            var client = OkHttpClient()
            val request = Request.Builder()
                .url(url1)  // "http://www.baidu.com"
                .get() //以post的形式添加requestBody
                .build()
            var response = client.newCall(request).execute()
            val responseData = response.body?.string()
            val jsonObject = JSONObject(responseData)
            val state = jsonObject.getString("state")

            if (state == "ok") {
                val user = jsonObject.getJSONObject("session")
                if (user != null) {
                    fakeUser.uid = user.getLong("id").toString()
                    fakeUser.sid = user.getLong("sid").toString()
                    fakeUser.pwd = pwd
                }
            }

            // 直接获取信息
            val userInfo = getUserInfo(fakeUser.uid, fakeUser.sid, fakeUser.uid)
            if (userInfo.uid == fakeUser.uid)
            {
                userInfo.sid = fakeUser.sid
                userInfo.pwd = fakeUser.pwd
                return userInfo
            }
        }
        catch (e: Exception) {
            //LogHelper.e("Exception")
            e.printStackTrace();
            LogHelper.e("$e.message")
        }


        return fakeUser
    }

    //返回数据
    //    {
    //        "state": "ok",
    //        "user": {
    //        "id": 1005,
    //        "phone": "0",
    //        "icon": "sys:icon/1.jpg",
    //        "name": "robin",
    //        "nick": "robin",
    //        "email": "momo@local.com",
    //        "pwd": "",
    //        "temppwd": "",
    //        "region": "unknow",
    //        "ipv4": "0.0.0.0",
    //        "wxid": "0",
    //        "age": 1,
    //        "gender": 1,
    //        "tm": "2023/09/20 16:04:18",
    //        "sid": 0
    //    }
    //    }
    // 获取某个用户的基本信息
    fun getUserInfo(uid: String, sid:String, fid:String) : LoggedInUser{
        val fakeUser = LoggedInUser("", "", "", "0", "")
        //构建url地址
        var url1 = "${schema}://${host}/v1/user/searchfriends?fid=${fid}"

        try {
            var client = OkHttpClient()
            val request = Request.Builder()
                .url(url1)
                .get()
                .build()
            var response = client.newCall(request).execute()
            val responseData = response.body?.string()
            val jsonObject = JSONObject(responseData)
            val state = jsonObject.getString("state")

            if (state == "ok") {
                val user = jsonObject.getJSONObject("user")
                if (user != null) {
                    fakeUser.uid = user.getLong("id").toString()
                    fakeUser.sid = user.getLong("sid").toString()
                    fakeUser.nickName = user.getString("nick")
                    fakeUser.userId = user.getString("name")
                    fakeUser.icon = user.getString("icon")

                    fakeUser.phone = user.getString("phone")
                    fakeUser.email = user.getString("email")

                    fakeUser.ip = user.getString("ipv4")
                    fakeUser.region = user.getString("region")
                    if (user.getInt("gender") == 1){
                        fakeUser.gender = "男"
                    }else
                    {
                        fakeUser.gender = "女"
                    }
                }
            }
        }
        catch (e: Exception) {
            //LogHelper.e("Exception")
            e.printStackTrace();
            LogHelper.e("$e.message")
        }

        return fakeUser
    }

    // 解析返回的粉丝数据
    private fun parseFollower(node:  JSONArray):LinkedList<Friend> {
        var lst = LinkedList<Friend>()
        for (i in 0 until node.length()) {
            try {
                val user = node.getJSONObject(i);
                var fakeUser = Friend()
                fakeUser.uid = user.getString("id")
                fakeUser.icon = user.getString("icon")
                fakeUser.nick = user.getString("alias")
                val mask = user.getInt("visible")
                fakeUser.show = (mask and 8) != 0
                fakeUser.isFriend = true

                lst.add(fakeUser)
            }catch(e: Exception){

            }
        }
        return lst
    }
    // 查询自己关注列表
    /*
        {
        "state": "ok",
        "count": 1,
        "list": [
            {
                "id": 1004,
                "icon": "sys:icon/1.jpg",
                "ctm": 1695200220490,
                "alias": "robin",
                "label": "",
                "visible": 7,
                "block": false
            }
        ]
    }
     */
    fun getFollowList(uid: String, sid:String) : LinkedList<Friend> {
        //构建url地址
        var url1 = "${schema}://${host}/v1/user/listfriends?type=1&&uid=${uid}&sid=${sid}"

        try {
            var client = OkHttpClient()
            val request = Request.Builder()
                .url(url1)
                .get()
                .build()
            var response = client.newCall(request).execute()
            val responseData = response.body?.string()
            val jsonObject = JSONObject(responseData)
            val state = jsonObject.getString("state")

            if (state == "ok") {
                val data = jsonObject.getJSONArray("list")
                var lst = parseFollower(data)
                return lst
            }
        }catch (e: Exception) {
            //LogHelper.e("Exception")
            e.printStackTrace();
            LogHelper.e("$e.message")
        }

        return LinkedList<Friend>()
    }

    fun getFollowList(): LinkedList<Friend>{
        val user = CurrentUser.getUser()
        if (user != null)
        {
            return getFollowList(user.uid, user.sid)
        }else
        {
            return LinkedList<Friend>()
        }

    }

    // 搜索人员添加好友
    fun searchForFriend(fid: String): LinkedList<Friend>{
        val user = CurrentUser.getUser()
        if (user != null)
        {

            val tempUser = getUserInfo(user.uid, user.sid, fid)
            if (tempUser.uid == fid){
                var friend = Friend()
                friend.fromUser(tempUser)
                var lst = LinkedList<Friend>()
                lst.add(friend)
                return lst
            }
        }
        return LinkedList<Friend>()

    }

    // 设置关注的好友，是否显示等信息，备注等；取消关注好友等
    fun setFollowUserInfo(friend: Friend): Boolean {

        val user = CurrentUser.getUser()
        if (user == null)
            return false
        var str = "show"
        if (friend.show == false){
            str = "ignore"
        }

        var url1 = "${schema}://${host}/v1/user/setfriendinfo?uid=${user.uid}&sid=${user.sid}&fid=${friend.uid}&param=${str}"

        try {
            var client = OkHttpClient()
            val request = Request.Builder()
                .url(url1)
                .get()
                .build()
            var response = client.newCall(request).execute()
            val responseData = response.body?.string()
            val jsonObject = JSONObject(responseData)
            val state = jsonObject.getString("state")

            if (state == "ok") {

                return true
            }
        }catch (e: Exception) {
            //LogHelper.e("Exception")
            e.printStackTrace();
            LogHelper.e("$e.message")
        }
        return false
    }

    fun removeFriend(friend: Friend) :Boolean{
        val user = CurrentUser.getUser()
        if (user == null)
            return false
        var url1 = "${schema}://${host}/v1/user/setfriendinfo?uid=${user.uid}&sid=${user.sid}&fid=${friend.uid}&param=remove"

        try {
            var client = OkHttpClient()
            val request = Request.Builder()
                .url(url1)
                .get()
                .build()
            var response = client.newCall(request).execute()
            val responseData = response.body?.string()
            val jsonObject = JSONObject(responseData)
            val state = jsonObject.getString("state")

            if (state == "ok") {

                return true
            }
        }catch (e: Exception) {
            //LogHelper.e("Exception")
            e.printStackTrace();
            LogHelper.e("$e.message")
        }
        return false

    }

    // 设置关注某人
    // http://localhost:7817/v1/user/addfriendreq?uid=1005&sid=6812630841045951575&fid=1004
    /*
    {
        "state": "ok",
        "detail": "add friend ok"
    }
     */
    fun setFollowHim(friend:Friend): Boolean {
        //构建url地址
        val user = CurrentUser.getUser()
        if (user == null)
            return false
        var url1 = "${schema}://${host}/v1/user/addfriendreq?uid=${user.uid}&sid=${user.sid}&fid=${friend.uid}"

        try {
            var client = OkHttpClient()
            val request = Request.Builder()
                .url(url1)
                .get()
                .build()
            var response = client.newCall(request).execute()
            val responseData = response.body?.string()
            val jsonObject = JSONObject(responseData)
            val state = jsonObject.getString("state")

            if (state == "ok") {

                return true
            }
        }catch (e: Exception) {
            //LogHelper.e("Exception")
            e.printStackTrace();
            LogHelper.e("$e.message")
        }
        return false
    }

    // 查询好友信息
    // http://localhost:7817/v1/gpx/position?
    //    {
    //        "uid":"1005",
    //        "sid":"6812630841045951575",
    //        "fid":"1004"
    //    }
    //    {
    //        "state": "OK",
    //        "pt": {
    //        "uid": "1004",
    //        "lat": 40,
    //        "lon": 116,
    //        "ele": 100,
    //        "speed": 0,
    //        "tm": 1007
    //        }
    //    }
    fun getUserLocation(friend: Friend) : Boolean {
        return true
    }
}