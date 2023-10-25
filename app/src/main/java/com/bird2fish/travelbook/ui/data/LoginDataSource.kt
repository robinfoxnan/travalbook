package com.bird2fish.travelbook.ui.data

import android.content.ComponentName
import android.content.ServiceConnection
import android.os.IBinder
import android.widget.Toast
import com.bird2fish.travelbook.core.HttpService
import com.bird2fish.travelbook.helper.PreferencesHelper
import com.bird2fish.travelbook.ui.data.model.LoggedInUser
import java.io.IOException

/**
 * Class that handles authentication w/ login credentials and retrieves user information.
 */
class LoginDataSource {

    fun login(username: String, password: String, server :HttpService?): Result<LoggedInUser> {
        if (server == null){
            return Result.Error(IOException("服务未启动", null))
        }
        try {

            var id : Int? = 1000
            id = username.toIntOrNull()
            // 数字执行登录，否则执行注册
            if (id != null){
                val user = PreferencesHelper.getUserInfo()
                if (user != null && user.sid != "")
                {
                    val user1 = server.loginWithSid(username, user.sid, password)
                    if (user1.sid != "") {
                        return Result.Success(user1)
                    }

                }

                val user2 = server.loginWithpwd(username, password)
                if (user2.sid != ""){
                    return Result.Success(user2)

                }
            }
            else{  // 先注册，然后登录
                val user = server.register(username, password)

                id = user.uid.toIntOrNull()
                if (id == null || id < 1001){
                    return Result.Error(IOException("注册出现错误", null))
                }

                // 先保存一下
                PreferencesHelper.saveUserInfo(user.userId, user.pwd,
                    user.uid, "")

                // 尝试登录
                val user3 = server.loginWithpwd(user.uid, password)
                if (user3.sid != ""){
                    return Result.Success(user3)
                }
            }

            // TODO: handle loggedInUser authentication
            //val fakeUser = LoggedInUser(username, password, "robin", "1001", "12222")
            //return Result.Success(fakeUser)
            return Result.Error(IOException("注册出现错误", null))
        } catch (e: Throwable) {
            return Result.Error(IOException("Error logging in", e))
        }
    }

    fun logout() {
        // TODO: revoke authentication
    }


}