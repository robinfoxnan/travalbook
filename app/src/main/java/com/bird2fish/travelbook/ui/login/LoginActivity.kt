package com.bird2fish.travelbook.ui.login

import android.app.Activity
import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.os.StrictMode
import android.os.StrictMode.ThreadPolicy
import android.os.StrictMode.VmPolicy
import android.text.*
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.view.View
import android.view.Window
import android.view.inputmethod.EditorInfo
import android.widget.*
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.bird2fish.travelbook.BottomWindow
import com.bird2fish.travelbook.MainActivity
import com.bird2fish.travelbook.R
import com.bird2fish.travelbook.TencentMapActivity
import com.bird2fish.travelbook.core.HttpService
import com.bird2fish.travelbook.core.UiHelper
import com.bird2fish.travelbook.databinding.ActivityLoginBinding
import com.bird2fish.travelbook.helper.AgreementReader
import com.bird2fish.travelbook.helper.LogHelper
import com.bird2fish.travelbook.helper.PreferencesHelper
import java.io.File


class LoginActivity : AppCompatActivity() {

    private lateinit var loginViewModel: LoginViewModel
    private lateinit var binding: ActivityLoginBinding
    private lateinit var checkPrivacy: CheckBox

    override fun onCreate(savedInstanceState: Bundle?) {


        super.onCreate(savedInstanceState)

        val fileEx: File? = this.getExternalFilesDir(null)
        if (fileEx != null){
            val dir = fileEx.absolutePath
            LogHelper.setLogDir(dir)
        }

        // 这里先这样初始化
        PreferencesHelper.init(this)

        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val username = binding.username
        val password = binding.password
        val login = binding.login
        val loading = binding.loading
        var imageview = binding.imageViewTitle
        imageview!!.setImageResource(R.drawable.logo)

        this.checkPrivacy = binding.checkBoxAgree!!

        loginViewModel = ViewModelProvider(this, LoginViewModelFactory())
            .get(LoginViewModel::class.java)

        loginViewModel.loginFormState.observe(this@LoginActivity, Observer {
            val loginState = it ?: return@Observer

            // disable login button unless both username / password is valid
            login.isEnabled = loginState.isDataValid

            if (loginState.usernameError != null) {
                username.error = getString(loginState.usernameError)
            }
            if (loginState.passwordError != null) {
                password.error = getString(loginState.passwordError)
            }
        })

        // 登录成功与否监听者
        loginViewModel.loginResult.observe(this@LoginActivity, Observer {
            val loginResult = it ?: return@Observer
            loading.visibility = View.INVISIBLE

            if (loginResult.error != null) {
                showLoginFailed(loginResult.error)
            }
            if (loginResult.success != null) {
                //loading.visibility = View.GONE

                // 成功
                // 跳转到
                var intent: Intent = Intent()
                //intent.setClass(this, MainActivity::class.java)
                intent.setClass(this, TencentMapActivity::class.java)
                startActivity(intent)
                updateUiWithUser(loginResult.success)
                setResult(Activity.RESULT_OK)
            }


            //Complete and destroy login activity once successful
            //finish()
        })

        username.afterTextChanged {
            loginViewModel.loginDataChanged(
                username.text.toString(),
                password.text.toString()
            )
        }

        password.apply {
            afterTextChanged {
                loginViewModel.loginDataChanged(
                    username.text.toString(),
                    password.text.toString()
                )
            }

            setOnEditorActionListener { _, actionId, _ ->
                when (actionId) {
                    EditorInfo.IME_ACTION_DONE ->
                    {
                        if (checkPrivacy.isChecked())
                        {
                            loginViewModel.login(
                                username.text.toString(),
                                password.text.toString(),
                                service
                            )
                        }
                        else{
                            UiHelper.showMessage(this@LoginActivity, "请查看并同意相关协议")
                        }

                    }

                }
                false
            }

            login.setOnClickListener {
                if (checkPrivacy.isChecked())
                {
                    loading.visibility = View.VISIBLE
                    loginViewModel.login(
                        username.text.toString(),
                        password.text.toString(),
                        service
                    )
                }
                else{
                    UiHelper.showMessage(this@LoginActivity, "请查看并同意相关协议")
                }
            }
        }

        // 绑定
        bindHttp()

        // 加载之前的用户
        val olduser = PreferencesHelper.getUserInfo()
        if (olduser != null && olduser.sid != ""){
            username.setText(olduser.uid)
            password.setText(olduser.pwd)
        }

        // 设置标题栏颜色
//        if (this is FragmentActivity) {
//            supportActionBar?.setBackgroundDrawable(resources.getDrawable(R.color.toolbar_gray))
//        } else {
//            requestWindowFeature(Window.FEATURE_NO_TITLE)
//            window.statusBarColor = resources.getColor(R.color.toolbar_gray)
//        }

        val privacyPolicyTextView = findViewById<TextView>(R.id.tv_privacy_agree)
        privacyPolicyTextView.text = getAgreementText()
        privacyPolicyTextView.movementMethod = LinkMovementMethod.getInstance()
    }

    private fun getAgreementText(): SpannableString {
        val agreementText = "注册登录视为同意《隐私协议》与《用户协议》"
        val spannableString = SpannableString(agreementText)

        // 创建 ClickableSpan
        val privacyClickableSpan = object : ClickableSpan() {
            override fun onClick(widget: View) {
                // 处理点击《隐私协议》的逻辑
                showPrivacyPolicy()
            }

            override fun updateDrawState(ds: TextPaint) {
                super.updateDrawState(ds)
                // 设置链接文字的样式，例如颜色、下划线等
                //ds.isUnderlineText = true
                ds.color = ContextCompat.getColor(this@LoginActivity, R.color.gray)
            }
        }

        val userAgreementClickableSpan = object : ClickableSpan() {
            override fun onClick(widget: View) {
                // 处理点击《用户协议》的逻辑
                showUserAgreement()
            }

            override fun updateDrawState(ds: TextPaint) {
                super.updateDrawState(ds)
                // 设置链接文字的样式，例如颜色、下划线等
                //ds.isUnderlineText = true
                ds.color = ContextCompat.getColor(this@LoginActivity, R.color.gray)
            }
        }

        // 将 ClickableSpan 应用到指定范围的文字
        val privacyStart = agreementText.indexOf("《隐私协议》")
        val privacyEnd = privacyStart + "《隐私协议》".length
        spannableString.setSpan(privacyClickableSpan, privacyStart, privacyEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

        val userAgreementStart = agreementText.indexOf("《用户协议》")
        val userAgreementEnd = userAgreementStart + "《用户协议》".length
        spannableString.setSpan(userAgreementClickableSpan, userAgreementStart, userAgreementEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

        return spannableString
    }


    private fun showPrivacyPolicy() {
        // 启动一个新的界面或弹窗，显示隐私协议的内容
        val agreementReader = AgreementReader(resources)
        val privacyPolicyText = agreementReader.readPrivacyPolicy()
        val popWindow = BottomWindow(this, R.layout.pop_privacy, privacyPolicyText)
        popWindow.showPopupWindow()

        //UiHelper.showMessage(this, privacyPolicyText)
    }

    private fun showUserAgreement() {
        val agreementReader = AgreementReader(resources)
        //UiHelper.showMessage(this, "同意")
        val userAgreementText = agreementReader.readUserAgreement()

        val popWindow = BottomWindow(this, R.layout.pop_privacy, userAgreementText)
        popWindow.showPopupWindow()
    }

    private fun updateUiWithUser(model: LoggedInUserView) {
        val welcome = getString(R.string.welcome)
        val displayName = model.displayName
        // TODO : initiate successful logged in experience
        Toast.makeText(
            applicationContext,
            "$welcome $displayName",
            Toast.LENGTH_LONG
        ).show()
    }

    private fun showLoginFailed(@StringRes errorString: Int) {
        Toast.makeText(applicationContext, errorString, Toast.LENGTH_SHORT).show()
    }

    private fun bindHttp(){
        val intent = Intent(this, HttpService::class.java)
        bindService(intent, conn, BIND_AUTO_CREATE);
    }

    // 绑定到消息
    private var service: HttpService? = null
    private var isBound = false

    private val conn: ServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName, binder: IBinder) {
            isBound = true
            val httpBinder = binder as HttpService.HttpBinder
            service = httpBinder.getService()
            //Log.i("DemoLog", "ActivityA onServiceConnected")

        }

        override fun onServiceDisconnected(name: ComponentName) {
            isBound = false
            service = null
            //Log.i("DemoLog", "ActivityA onServiceDisconnected")
        }
    }
}

/**
 * Extension function to simplify setting an afterTextChanged action to EditText components.
 */
fun EditText.afterTextChanged(afterTextChanged: (String) -> Unit) {
    this.addTextChangedListener(object : TextWatcher {
        override fun afterTextChanged(editable: Editable?) {
            afterTextChanged.invoke(editable.toString())
        }

        override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

        override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
    })
}