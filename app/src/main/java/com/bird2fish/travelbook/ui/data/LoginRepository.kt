package com.bird2fish.travelbook.ui.data

import com.bird2fish.travelbook.core.GlobalData
import com.bird2fish.travelbook.core.HttpService
import com.bird2fish.travelbook.helper.PreferencesHelper
import com.bird2fish.travelbook.ui.data.model.CurrentUser
import com.bird2fish.travelbook.ui.data.model.LoggedInUser


/**
 * Class that requests authentication and user information from the remote data source and
 * maintains an in-memory cache of login status and user credentials information.
 */

class LoginRepository(val dataSource: LoginDataSource) {

    // in-memory cache of the loggedInUser object
    var user: LoggedInUser? = null
        private set

    val isLoggedIn: Boolean
        get() = user != null

    init {
        // If user credentials will be cached in local storage, it is recommended it be encrypted
        // @see https://developer.android.com/training/articles/keystore
        user = null
    }

    fun logout() {
        user = null
        dataSource.logout()
    }

    fun login(username: String, password: String, server :HttpService?): Result<LoggedInUser> {
        // handle login
        val result = dataSource.login(username, password, server)
        if (result is Result.Success) {
            setLoggedInUser(result.data)

        }
        return result
    }

    // 记录当前登录的用户
    private fun setLoggedInUser(loggedInUser: LoggedInUser) {
        this.user = loggedInUser
        // 保存到全局的单件模式中
        CurrentUser.setUser(loggedInUser)
        GlobalData.getHttpServ().initServer()

        // 写到配置文件中
        PreferencesHelper.saveUserInfo(loggedInUser.userId, loggedInUser.pwd,
            loggedInUser.uid, loggedInUser.sid)

    }



}