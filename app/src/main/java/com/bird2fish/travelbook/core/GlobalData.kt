package com.bird2fish.travelbook.core

import android.location.Location
import com.bird2fish.travelbook.ui.contact.Friend
import com.tencent.map.geolocation.TencentLocation
import java.util.LinkedList

class GlobalData {
    companion object{
        var httpServer : HttpService = HttpService()
        var isLocationEnabled = false          // 定位
        var isLocationBackgroudEnabled = false // 后台和运行
        var isBodySensorEnabled = false        // 步数
        var isRecognitionEnabled = false       // 状态

        var  intervalOfLocation: Long = 5000         // 采样间隔与上报是一样的获取好友数据
        var  intervalOfRefresh:Long = 5000           // 刷新好友位置界面的刷新率

        fun setCurrentLocation(pos :TencentLocation){
            this.currentTLocation = pos
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
//        var currentLat :Double = 0.0               // 这几个是给腾讯地图改的
//        var currentLng :Double = 0.0
//        var currentAlt:Double = 0.0
//        var currentSpeed :Double = 0.0
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