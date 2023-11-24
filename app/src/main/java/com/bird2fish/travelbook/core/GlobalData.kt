package com.bird2fish.travelbook.core

import android.location.Location
import com.bird2fish.travelbook.helper.DateTimeHelper
import com.bird2fish.travelbook.helper.PreferencesHelper
import com.bird2fish.travelbook.ui.contact.Friend
import com.tencent.map.geolocation.TencentLocation
import java.util.LinkedList

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
        var isLocationEnabled = false          // 定位
        var isLocationBackgroudEnabled = false // 后台和运行
        var isBodySensorEnabled = false        // 步数
        var isRecognitionEnabled = false       // 状态
        var isFileReadWriteEnabaled = false    // 文件读写

        var isRecordingTrack = false         // 是否在录制轨迹
        var currentFriend :Friend? = null

        var  intervalOfLocation: Long = 2000         // 采样间隔与上报是一样的获取好友数据
        var  intervalOfRefresh:Long = 2000           // 刷新好友位置,界面的刷新率与httpworker中一致，后台则不刷新
        var  sportMode :SportModeEnum = SportModeEnum.SPORT_MODE_HIKE
        var  shouldRefresh :Boolean = true          // 是否获取好友信息
        var  defaultZoomLevel = 13f

        // 在第一个被加载的页面中使用
        fun InitLoad(){
            this.intervalOfLocation = PreferencesHelper.getCurrentPosInterval()
            try {
                GlobalData.sportMode =  GlobalData.SportModeEnum.valueOf(PreferencesHelper.getSportMode())
            }catch (e: Exception){
                GlobalData.sportMode = GlobalData.SportModeEnum.SPORT_MODE_HIKE
            }
        }

        fun setCurrentLocation(pos :TencentLocation){
            this.currentTLocation = pos
            currentTm = DateTimeHelper.getTimestamp()
            HttpWorker.get().pushGpx(pos);
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
        var shouldViewFriendLocation :Boolean = true   // 标记是否需要更新，不显示地图，则不需要更新，省电

        @Volatile
        var currentTLocation : TencentLocation? = null
        var currentTm :Long = 0


        private var glock = Any()
        fun getFollowListSize(): Int{
            synchronized(glock) {
                return followList!!.size
            }
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

    }// end object
}