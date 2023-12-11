package com.bird2fish.travelbook

import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.view.Menu
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.google.android.material.navigation.NavigationView
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import androidx.drawerlayout.widget.DrawerLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import com.bird2fish.travelbook.core.HttpService
import com.bird2fish.travelbook.core.UiHelper
import com.bird2fish.travelbook.databinding.ActivityMainBinding
import com.bird2fish.travelbook.helper.LogHelper
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

        // 点击悬浮按钮
        binding.appBarMain.fab.setOnClickListener {
            //openDrawer();
            val intent = Intent(this, TencentMapActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
            startActivity(intent)

        }

        // 左侧导航栏里面的菜单在这里
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
                // 这里先隐藏一组，暂时用不这么多
                //R.id.nav_newgroup,
                R.id.nav_home,  R.id.nav_playground, R.id.nav_favourite,
                R.id.nav_map, R.id.nav_track,
                R.id.nav_me, R.id.nav_contract, R.id.settingFragment
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

        // 设置标题栏颜色
//        if (this is FragmentActivity) {
//            supportActionBar?.setBackgroundDrawable(resources.getDrawable(R.color.toolbar_gray))
//        } else {
//            requestWindowFeature(Window.FEATURE_NO_TITLE)
//            window.statusBarColor = resources.getColor(R.color.toolbar_gray)
//        }


        drawerLayout.addDrawerListener(object : DrawerLayout.DrawerListener {
            override fun onDrawerSlide(drawerView: View, slideOffset: Float) {
                // 当侧边栏滑动时调用
            }

            override fun onDrawerOpened(drawerView: View) {
                // 当侧边栏打开时调用
                // 这里可以处理在侧边栏打开时需要执行的逻辑
                val user = CurrentUser.getUser()
                if (user != null){
                    setUserInfo()
                }
            }

            override fun onDrawerClosed(drawerView: View) {
                // 当侧边栏关闭时调用
                // 这里可以处理在侧边栏关闭时需要执行的逻辑
            }

            override fun onDrawerStateChanged(newState: Int) {
                // 当侧边栏状态发生变化时调用
            }
        })

    }

    private fun setUserInfo(){

        // 侧边栏
        var navigationView = this.findViewById<NavigationView>(R.id.nav_view);
        // 侧边栏的头部栏的信息设置
        var headerLayout = navigationView.getHeaderView(0);
        var userNameview = headerLayout.findViewById<TextView>(R.id.tvName);
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