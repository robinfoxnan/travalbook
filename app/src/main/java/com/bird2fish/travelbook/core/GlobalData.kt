package com.bird2fish.travelbook.core

import android.content.Context
import android.location.Location
import androidx.core.net.toUri
import com.bird2fish.travelbook.helper.DateTimeHelper
import com.bird2fish.travelbook.helper.FileHelper
import com.bird2fish.travelbook.helper.PreferencesHelper
import com.bird2fish.travelbook.helper.TrackHelper
import com.bird2fish.travelbook.ui.contact.Friend
import com.tencent.map.geolocation.TencentLocation
import com.tencent.tencentmap.mapsdk.maps.model.LatLng
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.File
import java.time.LocalDateTime
import java.util.*

class GlobalData {
    enum class SportModeEnum(val intValue: Int) {
        SPORT_MODE_HIKE(1),
        SPORT_MODE_RUN(2),
        SPORT_MODE_BIKE(3),
        SPORT_MODE_MOTOR(4),
        SPORT_MODE_CAR(5),
        SPORT_MODE_LAZY(6)
    }

    companion object{
        var httpServer : HttpService = HttpService()
        @Volatile
        var isLocationEnabled = false          // 定位

        @Volatile
        var isLocationBackgroudEnabled = false // 后台和运行

        @Volatile
        var isBodySensorEnabled = false        // 步数

        @Volatile
        var isRecognitionEnabled = false       // 状态

        @Volatile
        var isFileReadWriteEnabaled = false    // 文件读写

        var currentFriend :Friend? = null

        @Volatile
        var  intervalOfLocation: Long = 2000         // 采样间隔与上报是一样的获取好友数据

        @Volatile
        var  intervalOfRefresh:Long = 2000           // 刷新好友位置,界面的刷新率与httpworker中一致，后台则不刷新

        @Volatile
        var  sportMode :SportModeEnum = SportModeEnum.SPORT_MODE_HIKE

        @Volatile
        var  shouldRefresh :Boolean = true          // 是否获取好友信息
        var  defaultZoomLevel = 13f

        @Volatile
        var shouldRefreshDistance:Boolean = true

        @Volatile
        var  isRecording:Boolean = false
        var curTrack :Track = Track()     // 当前的轨迹
        private var tracklock = Any()

        var trackList: Tracklist? = null
        private var trackListPath:String = ""
        private var rootDir :String = ""
        private var trackListlock = Any()
        // /storage/emulated/0/Android/data/com.bird2fish.travelbook/files/tracks

        // 收藏点的列表
        private var favLocations : FavLocationList = FavLocationList()

        fun loadFavLocations(context: Context){
            favLocations = FileHelper.readFavLocationlist(context)
        }

        fun addFavLocation(context: Context, loc: FavLocation): Boolean{
            // 先查重
            for (item in this.favLocations.locations){
                if (loc.favId == item.favId){
                    return false
                }
            }
            favLocations.locations.add(loc)
            saveFavLocations(context)
            return true
        }

        fun removeFavLocation(context: Context, loc: FavLocation): Boolean{
            for (index in  0 until this.favLocations.locations.size){
                if (loc.favId == this.favLocations.locations[index].favId){
                    favLocations.locations.removeAt(index)
                    saveFavLocations(context)
                    return true
                }
            }

            return true
        }

        fun saveFavLocations(context: Context){
            FileHelper.writeFavLocationlist(context, this.favLocations)
        }

        fun getLocations(): LinkedList<FavLocation>{
            return this.favLocations.locations
        }


        fun isFileInited():Boolean{
            if (rootDir == ""){
                return false
            }
            return true
        }
        fun setRootDir(dir :String):Boolean{
            this.rootDir = dir
            val ret = FileHelper.createDir(this.rootDir, "tracks")
            return ret
        }

        fun loadTrackList(context: Context){
            if (rootDir == "")
                return

            // 协程中运行，也许会比较大
            GlobalScope.launch{
                val list = FileHelper.readTracklistSuspended(context)
                synchronized(trackListlock){
                    GlobalData.trackList = list
                }
            }

        }

        fun saveTrackList(context: Context){
            if (rootDir == "")
                return

            if (GlobalData.trackList == null){
                return
            }
            // 协程中运行，也许会比较大
            GlobalScope.launch{
                FileHelper.saveTracklistSuspended(context, GlobalData.trackList!!, Date())
            }
        }

        fun removeTrack(context: Context, pos: Int){
            if (GlobalData.trackList == null){
                return
            }

            GlobalData.trackList!!.tracklistElements.removeAt(pos)
            GlobalScope.launch{
                FileHelper.saveTracklistSuspended(context, GlobalData.trackList!!, Date())
            }
        }


        // 在第一个被加载的页面中使用
        fun InitLoad(){
            // 这句得来的比较小，默认是
            GlobalData.intervalOfLocation = PreferencesHelper.getCurrentPosInterval()
            try {
                GlobalData.sportMode =  GlobalData.SportModeEnum.valueOf(PreferencesHelper.getSportMode())
            }catch (e: Exception){
                GlobalData.sportMode = GlobalData.SportModeEnum.SPORT_MODE_HIKE
            }

            // 根据类型加载
            when (GlobalData.sportMode) {

                GlobalData.SportModeEnum.SPORT_MODE_HIKE->{
                    GlobalData.intervalOfLocation = PreferencesHelper.getModeHikePosInterval()
                }

                GlobalData.SportModeEnum.SPORT_MODE_RUN->{
                    GlobalData.intervalOfLocation = PreferencesHelper.getModeRunPosInterval()
                }

                GlobalData.SportModeEnum.SPORT_MODE_BIKE->{
                    GlobalData.intervalOfLocation= PreferencesHelper.getModeBikePosInterval()
                }
                GlobalData.SportModeEnum.SPORT_MODE_MOTOR->{
                    GlobalData.intervalOfLocation = PreferencesHelper.getModeMotorPosInterval()
                }
                GlobalData.SportModeEnum.SPORT_MODE_CAR->{
                    GlobalData.intervalOfLocation = PreferencesHelper.getModeCarPosInterval()
                }
                GlobalData.SportModeEnum.SPORT_MODE_LAZY->{
                    GlobalData.intervalOfLocation = PreferencesHelper.getModeLasyPosInterval()
                }
            }

            // 地图刷新频率
            GlobalData.intervalOfRefresh =  PreferencesHelper.getRefreshInterval()
            if (GlobalData.intervalOfRefresh < 2000){
                GlobalData.intervalOfRefresh = 2000
            }else if (GlobalData.intervalOfRefresh > 60000){
                GlobalData.intervalOfRefresh = 60000
            }
        }

        fun setCurrentLocation(pos :TencentLocation){
            this.currentTLocation = pos
            currentTm = DateTimeHelper.getTimestamp()
            HttpWorker.get().pushGpx(pos);

            // 添加到轨迹列表
            synchronized(tracklock){
                if(isRecording){
                    val loc = TLocation2Location(pos)
                    TrackHelper.addWayPointToTrack(curTrack, loc, 1, false)
                }
            }
        }

        // 返回当前的轨迹的
        fun getDistance(): String {
            synchronized(tracklock){
                val len = curTrack.length / 1000.0
                val dis =  "%.3f".format(len)
                //val count = curTrack.wayPoints.size.toString()
                //val info = "${dis} 点${count}个"
                return dis
            }
        }

        fun getDuration(context:Context):String{
            synchronized(tracklock) {
                return DateTimeHelper.convertToReadableTime(context, curTrack.duration, false)
            }
        }

        fun getHttpServ(): HttpService{
            return this.httpServer
        }

        // 搜索得到的，关注列表，粉丝列表
        var friendSearchList: LinkedList<Friend> = LinkedList<Friend>()
        var followList :LinkedList<Friend> =LinkedList<Friend>()
        var funList:LinkedList<Friend> =LinkedList<Friend>()

        // 控件绑定到这个
        var friendList :LinkedList<Friend> =LinkedList<Friend>()
        var friendListIndex :Int = 0

        // 自己当前最好的位置
        //var currentBestLocation: Location?  = null  // createLocation(0.0, 0.0)
//        @Volatile
//        var shouldViewFriendLocation :Boolean = true   // 标记是否需要更新，不显示地图，则不需要更新，省电

        @Volatile
        var currentTLocation : TencentLocation? = null

        @Volatile
        var currentTm :Long = 0


        private var glock = Any()
        fun getFollowListSize(): Int{
            synchronized(glock) {
                return followList!!.size
            }
        }

        // 类型转换
        private fun TLocation2Location(pos :TencentLocation): Location {
            var provider = pos.provider
            if (provider == null){
                provider = "manual_provider"
            }
            val loc = Location(provider)
            loc.latitude = pos.latitude
            loc.longitude = pos.longitude
            loc.altitude = pos.altitude
            loc.accuracy = pos.accuracy
            loc.speed = pos.speed
            loc.time = pos.time
            loc.elapsedRealtimeNanos = pos.elapsedRealtime

            return loc
        }

        fun createLocation(latitude: Double, longitude: Double): Location {
            val location = Location("manual_provider") // 你可以指定一个提供者名称，这里使用 "manual_provider" 作为示例
            location.latitude = latitude
            location.longitude = longitude
            return location
        }

        // 计算副本
        fun getCopyOfFriendList():LinkedList<Friend>{
            synchronized(glock) {
                val lst = LinkedList<Friend>()
                for (i  in followList.size-1 downTo 0)
                {
                    if (followList[i].show){
                        lst.add(followList[i])
                    }
                }
                return lst
            }
        }

        // 查看是否在关注列表中
        fun isFriend(friend:Friend):Boolean{
            for (i  in followList.size-1 downTo 0)
            {
                if (followList[i].uid == friend.uid){
                    return true
                }
            }
            return false
        }

        // 对搜索到的用户检查是否已经关注
        fun checkSearchResult(){
            for (i in 0 until  friendSearchList.size){
                if (isFriend(friendSearchList[i]))
                    friendSearchList[i].isFriend = true
            }
        }

        // 设置使用当前内存数据
        fun setFollowerUseType(index: Int){
            synchronized(glock) {
                friendListIndex = index
                if (index == 0){
                    friendList.clear()
                    friendSearchList.clear()
                    //friendList.addAll(friendSearchList)
                }else if (index == 2){
                    friendList.clear()
                    friendList.addAll(funList)
                }else{
                    friendList.clear()
                    friendList.addAll(followList)
                 }
            }
        }
        fun getFollowerUseType():Int{
            return friendListIndex
        }

        // 更新数据
        fun setFollowers(index: Int, lst :LinkedList<Friend>){
            synchronized(glock) {
                if (index == 0){
                    friendSearchList = lst
                    checkSearchResult()
                }else if (index == 2){
                    funList = lst
                }else{
                    followList = lst
                }
                friendList.clear()
                friendList.addAll(lst)
            }
        }

        // 添加好友
        fun addFollowrs(friend: Friend) :Boolean{
            synchronized(glock) {
                for (i in 0 until followList.size) {
                    if (followList[i].uid == friend.uid){
                        return false
                    }
                }
                followList.add(friend)
            }
            return true
        }

        // 移除好友
        fun removeFollowers(friend: Friend):Boolean{
            synchronized(glock) {
                for (i  in followList.size-1 downTo 0)
                {
                    if (followList[i].uid == friend.uid){
                        followList.removeAt(i)
                    }
                }


                for (i  in friendList.size-1 downTo 0)
                {
                    if (friendList[i].uid == friend.uid){
                        friendList.removeAt(i)
                        return true
                    }
                }

            }
            return false
        }

        // 开始记录
        fun startTrack(context: Context){
            this.isRecording = true
            synchronized(tracklock){
                curTrack = Track()
                if (currentTLocation != null){
                    val loc = TLocation2Location(currentTLocation!!)
                    TrackHelper.addWayPointToTrack(curTrack, loc, 1, false)
                }
            }

        }

        fun stopTrack(context: Context){
            this.isRecording = false
            if (trackList == null){
                return
            }

            synchronized(tracklock){
                val name = curTrack.generateName()

                val ret = FileHelper.createDir(this.rootDir, "tracks")
                if (ret == false){
                    return
                }

                val dir = File(rootDir, "tracks")
                val file = File(dir, name + ".json")
                curTrack.trackUriString = file.toString()
                curTrack.gpxUriString = File(dir, name + ".gpx").toString()
                //UiHelper.showCenterMessage(context, curTrack.trackUriString)
                FileHelper.saveTrack(curTrack, false)

            }
            // 更新列表
            synchronized(trackListlock){
                FileHelper.addTrackAndSave(context, trackList!!, curTrack)
            }
        }

        // 拷贝当前新添加的点
        fun copyWaypoints(latLngs: MutableList<LatLng>){
            synchronized(tracklock){
                for (index in latLngs.size until curTrack.wayPoints.size){
                    val p = curTrack.wayPoints[index]
                    latLngs.add(LatLng(p.latitude, p.longitude))
                }

            }// end syn
            return
        }

    }// end object
}