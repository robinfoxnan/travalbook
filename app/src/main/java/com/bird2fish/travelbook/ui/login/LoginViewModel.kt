package com.bird2fish.travelbook.ui.login

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import android.util.Patterns
import com.bird2fish.travelbook.ui.data.LoginRepository
import com.bird2fish.travelbook.ui.data.Result

import com.bird2fish.travelbook.R
import com.bird2fish.travelbook.core.HttpService
import com.bird2fish.travelbook.ui.data.model.LoggedInUser
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.lang.Double.parseDouble

class LoginViewModel(private val loginRepository: LoginRepository) : ViewModel() {

    private val _loginForm = MutableLiveData<LoginFormState>()
    val loginFormState: LiveData<LoginFormState> = _loginForm

    private val _loginResult = MutableLiveData<LoginResult>()
    var loginResult: LiveData<LoginResult> = _loginResult

    fun login(username: String, password: String, server : HttpService?) {
        // can be launched in a separate asynchronous job

//        val coroutineScope = CoroutineScope(Dispatchers.Default)
//        coroutineScope.launch {
//            // 在这里执行协程的异步操作
//            var result = loginRepository.login(username, password, server)
//            if (result is Result.Success) {
//                    _loginResult.value =
//                        LoginResult(success = LoggedInUserView(displayName = result.data.displayName))
//                } else {
//                    _loginResult.value = LoginResult(error = R.string.login_failed)
//                }
//        }
        // 此处会等待协程完成


        object : Thread() {
            override fun run() {
                //网络操作连接的代码
                val result = loginRepository.login(username, password, server)
                //val fakeUser = LoggedInUser("111", "", "", "0", "")
                //val result = Result.Success(fakeUser)

                if (result is Result.Success) {
                    var name = "${result.data.nickName}(${result.data.uid})"
                    var res =  LoginResult(success = LoggedInUserView(displayName = name))
                    _loginResult.postValue(res)

                } else {
                    _loginResult.postValue(LoginResult(error = R.string.login_failed))
                }
            }
        }.start()

    }

    fun loginDataChanged(username: String, password: String) {
        if (!isUserNameValid(username)) {
            _loginForm.value = LoginFormState(usernameError = R.string.invalid_username)
        } else if (!isPasswordValid(password)) {
            _loginForm.value = LoginFormState(passwordError = R.string.invalid_password)
        } else {
            _loginForm.value = LoginFormState(isDataValid = true)
        }
    }

    // A placeholder username validation check
    private fun isUserNameValid(username: String): Boolean {
//        return if (username.contains('@')) {
//            Patterns.EMAIL_ADDRESS.matcher(username).matches()
//        } else {
//            username.isNotBlank()
//        }

        if (username == null){
            return false
        }

        var id : Int? = 1000
        id = username.toIntOrNull()
        if (id == null){
            return true
        }

        return true

    }

    // A placeholder password validation check
    private fun isPasswordValid(password: String): Boolean {
        return password.length > 5
    }
}