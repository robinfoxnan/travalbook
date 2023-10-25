package com.bird2fish.travelbook.ui.data.model

import android.system.Int64Ref

/**
 * Data class that captures user information for logged in users retrieved from LoginRepository
 */
data class LoggedInUser(
    var userId: String = "momo",
    var pwd: String = "",
    var nickName: String = "",
    var uid: String = "0",
    var sid:String = "",
    var email: String = "",
    var phone: String = "",
    var gender : String = "",
    var icon : String = "",
    var age : String = "",
    var ip : String = "",
    var region : String = "",
)

class CurrentUser {
    companion object {
        private var user: LoggedInUser? = null
        private var instance : CurrentUser? = null
            get() {
                if (field == null) {
                    field = CurrentUser()
                }
                return field

            }

        @Synchronized
        fun setUser(u: LoggedInUser) {
            this.user = u
        }

        @Synchronized
        fun getUser() :LoggedInUser?{
            return this.user
        }
    }


}