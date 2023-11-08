package com.bird2fish.travelbook

import android.Manifest
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.IBinder
import android.view.Menu
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import com.google.android.material.navigation.NavigationView
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import androidx.drawerlayout.widget.DrawerLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import com.bird2fish.travelbook.R
import com.bird2fish.travelbook.core.HttpService
import com.bird2fish.travelbook.core.TrackerService
import com.bird2fish.travelbook.core.UiHelper
import com.bird2fish.travelbook.databinding.ActivityMainBinding
import com.bird2fish.travelbook.helper.LogHelper
import com.bird2fish.travelbook.helper.PreferencesHelper
import com.bird2fish.travelbook.ui.data.model.CurrentUser

class MainActivity : AppCompatActivity() {

    /* Define log tag */
    private val TAG: String = LogHelper.makeLogTag(MainActivity::class.java)

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding
    private  var lastFramentid : Int = 0


    companion object {
        var topActivity: MainActivity? = null
    }

    public fun openDrawer(){
        binding.drawerLayout.openDrawer(GravityCompat.START);
    }

    public fun closeDrawer(){
        binding.drawerLayout.closeDrawer(GravityCompat.START)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        topActivity = this

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.appBarMain.toolbar)

        binding.appBarMain.fab.setOnClickListener {
            openDrawer();
//                view ->
//            Snackbar.make(view, "自定义快捷键", Snackbar.LENGTH_LONG)
//                .setAction("Action", null).show()

        }

        // activity_main.xml 的根节点
        val drawerLayout: DrawerLayout = binding.drawerLayout
        // 侧边栏导航视图
        val navView: NavigationView = binding.navView
        // 如果是彩色图标，重新设置图标颜色
        // navView.itemIconTintList = null;

        // 这是用于切换的空白空间
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.nav_newgroup, R.id.nav_home,  R.id.nav_playground, R.id.nav_favourite,
                R.id.nav_map, R.id.nav_track,
                R.id.nav_contract, R.id.nav_setting
            ), drawerLayout
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)


        navController.addOnDestinationChangedListener { _, destination, label ->
                if (destination.id != R.id.nav_map)
                    lastFramentid = destination.id

        }

        // 边栏上侧用户信息部分
        setUserInfo()
        // 启动位置服务
        startLocationService()

    }

    private fun setUserInfo(){

        // 侧边栏
        var navigationView = this.findViewById<NavigationView>(R.id.nav_view);
        // 侧边栏的头部栏的信息设置
        var headerLayout = navigationView.getHeaderView(0);
        var userNameview = headerLayout.findViewById<TextView>(R.id.textNick);
        var userInfoview = headerLayout.findViewById<TextView>(R.id.textInfo);
        var userIcon = headerLayout.findViewById<ImageView>(R.id.imageViewIcon)

        // 保存到全局的单件模式中
        val user = CurrentUser.getUser()
        if (user == null){
            userNameview.setText("未登录");      // 如果不用该方法会报错null
            userInfoview.setText("未知");
        }else
        {
            val info = "${user.userId} (${user.uid})"
            userNameview.setText(user.nickName);
            userInfoview.setText(info);
            val iconId = UiHelper.getIconResId(user.icon)
            userIcon.setImageResource(iconId)
        }
    }

    // 顶部的设置按钮
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }


    ///////////////////////////////////////////////////////////////////////
    private var bound: Boolean = false
    private  var trackerService: TrackerService? = null  // 为了保证随时上报信息

    /*
    * Defines callbacks for service binding, passed to bindService()
    */
    private val connection = object : ServiceConnection {
        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            bound = true
            // 获取一个引用
            val binder = service as TrackerService.LocalBinder
            trackerService = binder.service
            trackerService!!.startTracking()

        }
        override fun onServiceDisconnected(arg0: ComponentName) {
            // 服务崩溃或者被系统杀掉之后
            //handleServiceUnbind()
            bound = false
            trackerService = null
        }
    }

    /* Register the permission launcher for requesting location 在onstart中使用  */
    private val requestLocationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
        if (isGranted) {
            // permission was granted - re-bind service
            //this.unbindService(connection)
            this.bindService(Intent(this, TrackerService::class.java),  connection,  Context.BIND_AUTO_CREATE)
            LogHelper.i(TAG, "Request result: Location permission has been granted.")

        } else {
            // permission denied - unbind service
            this.unbindService(connection)
        }
        //layout.toggleLocationErrorBar(gpsProviderActive, networkProviderActive)
    }

    // 启动服务的入口
    private fun startLocationService() {

        // 请求位置权限，在requestLocationPermissionLauncher中启动了连接动作
        if (ContextCompat.checkSelfPermission(this as Context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_DENIED) {
            requestLocationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
        else{
            // bind to TrackerService
            if (!bound){
                this.bindService(Intent(this, TrackerService::class.java), connection, Context.BIND_AUTO_CREATE)
            }
        }
    }

    //////////////////////////////////////////////////////////////////
    private fun bindHttp(){
        val intent = Intent(this, HttpService::class.java)
        bindService(intent, conn, AppCompatActivity.BIND_AUTO_CREATE);
    }

    // 绑定到消息
    private var serviceHttp: HttpService? = null
    private var isBoundHttp = false

    private val conn: ServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName, binder: IBinder) {
            isBoundHttp = true
            val httpBinder = binder as HttpService.HttpBinder
            serviceHttp = httpBinder.getService()
            //Log.i("DemoLog", "ActivityA onServiceConnected")

        }

        override fun onServiceDisconnected(name: ComponentName) {
            isBoundHttp = false
            serviceHttp = null
            //Log.i("DemoLog", "ActivityA onServiceDisconnected")
        }
    }
}