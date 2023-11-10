package com.bird2fish.travelbook.ui.contact

import com.bird2fish.travelbook.helper.DateTimeHelper
import com.bird2fish.travelbook.ui.data.model.LoggedInUser

data  class Friend (
    var nick: String = "",
    var icon:String = "",
    var uid:String = "",
    var isShare: Boolean = false,
    var lat :Double = 0.0,
    var lon: Double = 0.0,
    var ele : Double = 0.0,
    var speed: Double = 0.0,
    var permission:String = "",
    var age: Int = 0,
    var gender: String = "未知",
    var ip:String = "未知",
    var region:String = "未知",
    var level:String = "",
    var show: Boolean = false,
    var isFriend: Boolean = false,
    var tm: Long = DateTimeHelper.getTimestamp(),
    var msg:String = ""
){
    fun fromUser(user: LoggedInUser){
        this.uid = user.uid
        this.icon = user.icon
        this.isShare = false
        this.nick = user.nickName
        this.show = false
        this.isFriend = false
    }
}