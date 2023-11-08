package com.bird2fish.travelbook


import android.Manifest
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.location.Location
import android.os.*
import android.view.KeyEvent
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.LinearLayout
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import com.tencent.tencentmap.mapsdk.maps.*
import com.tencent.tencentmap.mapsdk.maps.model.CameraPosition
import com.tencent.tencentmap.mapsdk.maps.model.LatLng
import com.tencent.tencentmap.mapsdk.maps.model.MarkerOptions
import com.bird2fish.travelbook.helper.LogHelper
import com.bird2fish.travelbook.R
import com.bird2fish.travelbook.core.GlobalData
import com.bird2fish.travelbook.core.Keys
import com.bird2fish.travelbook.core.TrackerService
import com.bird2fish.travelbook.core.UiHelper
import com.bird2fish.travelbook.ui.contact.Friend

class TencentMapActivity : AppCompatActivity() {

    /* Define log tag */
    private val TAG: String = LogHelper.makeLogTag(TencentMap::class.java)

    private var mapView: TextureMapView? = null
    protected var tencentMap: TencentMap? = null


    private var bound: Boolean = false
    private val handler: Handler = Handler(Looper.getMainLooper())
    private var trackingState: Int = Keys.STATE_TRACKING_NOT
    private var gpsProviderActive: Boolean = false
    private var networkProviderActive: Boolean = false
    //private var currentBestLocation: Location?  = null
    //private lateinit var layout: MapFragmentLayoutHolder
    private  var trackerService: TrackerService? = null
    private lateinit var  mMarker:com.tencent.tencentmap.mapsdk.maps.model.Marker   // 自己的位置
    private var markersMap =  mutableMapOf<String, com.tencent.tencentmap.mapsdk.maps.model.Marker>()

    override fun onCreate(savedInstanceState: Bundle?) {
        TencentMapInitializer.setAgreePrivacy(true)

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tencent_map)

        // 取得LinearLayout 物件
        // 取得LinearLayout 物件
        val ll = findViewById<View>(R.id.layout_mapll) as LinearLayout

        val options = TencentMapOptions()
        options.isOfflineMapEnable = true

        mapView = TextureMapView(this, options)
        mapView!!.isOpaque = false
        ll.addView(mapView)

        //创建tencentMap地图对象，可以完成对地图的几乎所有操作

        //创建tencentMap地图对象，可以完成对地图的几乎所有操作
        tencentMap = mapView!!.map
        initToolBar()
        initMapOutlook()

        // list
        markersMap.clear()
        val lst = GlobalData.getCopyOfFriendList()

    }

    private fun initToolBar() {
        val toolbar = findViewById<View>(R.id.toolbarMap) as Toolbar
        toolbar.title = "好友地图" //设置主标题名称
        //toolbar.subtitle = "" //设置副标题名称
        setSupportActionBar(toolbar)
        toolbar.setNavigationIcon(R.drawable.calls_back);//是左边的图标样式


    }

    // 手动返回主页，主要是需要关闭那个导航抽屉
    fun backToMainActivity(){
        finish()
        MainActivity.topActivity!!.closeDrawer()
    }
    // 自定义工具条左侧图标的事件
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.getItemId() === android.R.id.home) {
            backToMainActivity()
        }
        return super.onOptionsItemSelected(item)
    }

    // 右侧按钮
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_map_main, menu)
        return true
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean
    {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            //backToMainActivity()
            return true;
        }
        return false;
    }

    override fun onKeyUp(keyCode: Int, event: KeyEvent?): Boolean
    {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            backToMainActivity()
            return true;
        }
        return false;
    }


    private fun initMapOutlook() {
        /**
         * 腾讯地图提供了UiSettings类以方便开发者对地图手势及SDK提供的控件的控制，以定制自己想要的视图效果。
         * UiSettings类的实例化也是通过TencentMap来获取。
         */
        val mapUiSettings = tencentMap!!.uiSettings
        /**
         * 可以控制地图的缩放级别，每次点击改变1个级别，此控件默认打开，
         * 可以通过UiSettings.setZoomControlsEnabled(boolean)接口控制此控件的显示和隐藏。
         */
        //mapUiSettings.setZoomControlsEnabled(true);
        // 比例尺
        mapUiSettings.isScaleViewEnabled = true
        mapUiSettings.setScaleViewPosition(100)
        mapUiSettings.setScaleViewFadeEnable(false)
        /**
         * 此控件可以指示地图的南北方向，默认的视图状态下不显示，只有在地图的偏航角或俯仰角不为0时才会显示，
         * 并且该控件的默认点击事件会将地图视图的俯仰角和偏航角动画到0的位置。
         * 可以通过UiSettings.setCompassEnabled(boolean)接口控制此控件的显示和隐藏。
         */
        mapUiSettings.isCompassEnabled = true
        mapUiSettings.setCompassExtraPadding(25, 200)
        mapUiSettings.setLogoScale(0.7f)
        /**
         * 当通过TencentMap.setLocationSource(locationSource)设置好地图的定位源后，
         * 点击此按钮可以在地图上标注一个蓝点指示用户的当前位置。
         * 可以通过UiSettings.setMyLocationButtonEnabled()接口设置此控件的显示和隐藏。
         */
        mapUiSettings.isMyLocationButtonEnabled = true
        /**
         * 旋转手势
         */
        mapUiSettings.isRotateGesturesEnabled = false
        tencentMap!!.isMyLocationEnabled = true

        //地图上设置定位数据源
        //tencentMap!!.setLocationSource(this)
        //设置当前位置可见
        tencentMap!!.isMyLocationEnabled = true
        initMyMarker()
        //addLine();
    }

    protected fun updateMarkers(){
        if (trackerService==null){
            UiHelper.showMessage(this, "未启动位置服务！请开启位置服务与授权，并重启程序")
            return
        }

        val mapFriend = trackerService!!.getLastPointMap()

        // 删除多余的控件，更新部分
        for (key:String in markersMap.keys) {
            //println("$key -> $value")
            if (!mapFriend.containsKey(key)){
                markersMap.remove(key)
            }
            val f = mapFriend[key]
            if (!f!!.isShare)  // 未加好友，或者无数据
            {
                UiHelper.showCenterMessage(this, "未能获取${f.uid} ${f.nick} 的位置")
                markersMap.remove(key)
            }
            // 更新位置
            val marker = markersMap[key]
            marker!!.isInfoWindowEnable = true
            marker!!.title = "${f.nick}"
            val position = LatLng(f.lat, f.lon)
            marker!!.position = position

            // 删除
            mapFriend.remove(key)
        }

        // 把剩下的新建一个标记
        for ((k, f) in mapFriend) {
            val marker = createMarker(f)
            markersMap.put(k, marker)
        }

    }

    // 根据好友信息新建一个标记控件
    protected fun createMarker(friend: Friend): com.tencent.tencentmap.mapsdk.maps.model.Marker
    {
        var lat = friend.lat
        var lon = friend.lon
        var title = "${friend.nick}"

        val position = LatLng(lat, lon)
        val options = MarkerOptions(position)
        options.infoWindowEnable(false) //默认为true
        options.title(title) //标注的InfoWindow的标题
        options.snippet("当前速度${friend.speed},高度${friend.ele}") //标注的InfoWindow的内容

        val marker = tencentMap!!.addMarker(options)

        //开启信息窗口
        marker.isInfoWindowEnable = true
        //marker.title = title
        marker.position = position
        marker.showInfoWindow()
        return  marker
    }

    // 移动镜头
    protected  fun moveCamera(friend: Friend?){
        var lat = 39.908710
        var lon = 116.397499
        var pos  = LatLng(lat, lon)
        if (friend != null){
            lat = friend.lat
            lon = friend.lon
            pos = LatLng(lat, lon)
        }
        else if (GlobalData.currentBestLocation != null)
        {
            lat = GlobalData.currentBestLocation!!.latitude
            lon = GlobalData.currentBestLocation!!.longitude
            pos = LatLng(lat, lon)
        }

        val cameraSigma = CameraUpdateFactory.newCameraPosition(
            CameraPosition(
                pos,  //中心点坐标，地图目标经纬度
                12f,  //目标缩放级别
                0f,  //目标倾斜角[0.0 ~ 45.0] (垂直地图时为0)
                0f
            )
        ) //目标旋转角 0~360° (正北方为0)
        tencentMap!!.moveCamera(cameraSigma) //移动地图
    }

    protected fun initMyMarker() {
        //通过MarkerOptions配置
        var lat = 39.908710
        var lon = 116.397499
        var title = "我的位置未知"

        if (GlobalData.currentBestLocation != null)
        {
            lat = GlobalData.currentBestLocation!!.latitude
            lon = GlobalData.currentBestLocation!!.longitude
            title = "我的位置"
        }

        val position = LatLng(lat, lon)

        val options = MarkerOptions(position)
        options.infoWindowEnable(false) //默认为true
        options.title(title) //标注的InfoWindow的标题
        //options.snippet("地址: 北京市东城区东长安街") //标注的InfoWindow的内容

        //options.icon(BitmapDescriptorFactory.fromResource(R.drawable.marker));//设置自定义Marker图标
        this.mMarker = tencentMap!!.addMarker(options)

    //开启信息窗口
        mMarker.isInfoWindowEnable = true
        mMarker.title = title
        val pos = position
        mMarker.position = pos
        val cameraSigma = CameraUpdateFactory.newCameraPosition(
            CameraPosition(
                pos,  //中心点坐标，地图目标经纬度
                12f,  //目标缩放级别
                0f,  //目标倾斜角[0.0 ~ 45.0] (垂直地图时为0)
                0f
            )
        ) //目标旋转角 0~360° (正北方为0)
        tencentMap!!.moveCamera(cameraSigma) //移动地图
        mMarker.showInfoWindow()
    }

    // 定时器会调用这里
    protected fun updateMarker(){

        var lat = 39.908710
        var lon = 116.397499
        var title = "我的位置"
        if (GlobalData.currentBestLocation != null)
        {
            lat = GlobalData.currentBestLocation!!.latitude
            lon = GlobalData.currentBestLocation!!.longitude

            val pos = LatLng(lat, lon)
            mMarker.position = pos
            mMarker.title = title
            mMarker.snippet = ""
            mMarker.refreshInfoWindow()
        }

        // 更新好友位置
        updateMarkers()

        moveCamera(null)
    }




    /* Register the permission launcher for requesting location 在onstart中使用  */
    private val requestLocationPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
        if (isGranted) {
            // permission was granted - re-bind service
            //this.unbindService(connection)
            this.bindService(Intent(this, TrackerService::class.java),  connection,  Context.BIND_AUTO_CREATE)
            //LogHelper.i(TAG, "Request result: Location permission has been granted.")
            startTracking()
        } else {
            // permission denied - unbind service
            this.unbindService(connection)
        }
        //layout.toggleLocationErrorBar(gpsProviderActive, networkProviderActive)
    }

    /**
     * mapview的生命周期管理
     */
    override fun onStart() {
        super.onStart()
        mapView!!.onStart()

        // 请求位置权限，在requestLocationPermissionLauncher中启动了连接动作
        if (ContextCompat.checkSelfPermission(this as Context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_DENIED) {
            requestLocationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
        else{
            // bind to TrackerService
            this.bindService(Intent(this, TrackerService::class.java), connection, Context.BIND_AUTO_CREATE)
        }

    }

    override fun onResume() {
        super.onResume()
        mapView!!.onResume()
    }

    override fun onPause() {
        super.onPause()
        mapView!!.onPause()
    }

    override fun onStop() {
        super.onStop()
        mapView!!.onStop()
    }

    override fun onDestroy() {
        super.onDestroy()
        mapView!!.onDestroy()
    }

    override fun onRestart() {
        super.onRestart()
        mapView!!.onRestart()
    }


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
            // get state of tracking and update button if necessary
            trackingState = trackerService!!.trackingState
            //layout.updateMainButton(trackingState)
            // 注册配置改变的监听器
            //PreferencesHelper.registerPreferenceChangeListener(sharedPreferenceChangeListener)
            // 位置更新的监听器
            handler.removeCallbacks(periodicLocationRequestRunnable)
            handler.postDelayed(periodicLocationRequestRunnable, 1000)
        }
        override fun onServiceDisconnected(arg0: ComponentName) {
            // 服务崩溃或者被系统杀掉之后
            //handleServiceUnbind()
            bound = false
            trackerService = null
        }
    }



    /* Register the permission launcher for starting the tracking service */
    private val startTrackingPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
        logPermissionRequestResult(isGranted)
        // start service via intent so that it keeps running after unbind
        if (bound == false){
            startTrackerService()
        }
        if (trackerService != null){
            trackerService!!.startTracking()
        }

    }

    /* Register the permission launcher for resuming the tracking service */
    private val resumeTrackingPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
        logPermissionRequestResult(isGranted)
        // start service via intent so that it keeps running after unbind
        if (bound == false){
            startTrackerService()
        }
        if (trackerService != null){
            trackerService!!.resumeTracking()
        }
    }

    /* Logs the request result of the Activity Recognition permission launcher */
    private fun logPermissionRequestResult(isGranted: Boolean) {
        if (isGranted) {
            LogHelper.i(TAG, "Request result: Activity Recognition permission has been granted.")
        } else {
            LogHelper.i(TAG, "Request result: Activity Recognition permission has NOT been granted.")
        }
    }

    /* 开始记录 */
    private fun startTracking() {
        // request activity recognition permission on Android Q+ if denied
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && ContextCompat.checkSelfPermission(this as Context,
//                Manifest.permission.ACTIVITY_RECOGNITION) == PackageManager.PERMISSION_DENIED) {
//            startTrackingPermissionLauncher.launch(Manifest.permission.ACTIVITY_RECOGNITION)
//        } else {
//            // start service via intent so that it keeps running after unbind
//            startTrackerService()
//            trackerService.startTracking()
//        }

       if (bound == false){
           startTrackerService()
       }

    }

    /* 启动后台跟踪服务 */
    private fun startTrackerService() {
        val intent = Intent(this, TrackerService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // ... start service in foreground to prevent it being killed on Oreo
            this.startForegroundService(intent)
        } else {
            this.startService(intent)
        }
    }

    /* Overrides onYesNoDialog from YesNoDialogListener */
//    override fun onYesNoDialog(type: Int, dialogResult: Boolean, payload: Int, payloadString: String) {
//        super.onYesNoDialog(type, dialogResult, payload, payloadString)
//        when (type) {
//            Keys.DIALOG_EMPTY_RECORDING -> {
//                when (dialogResult) {
//                    // user tapped resume
//                    true -> {
//                        trackerService.resumeTracking()
//                    }
//                }
//            }
//            Keys.DIALOG_DELETE_CURRENT_RECORDING -> {
//                when (dialogResult) {
//                    true -> {
//                        trackerService.clearTrack()
//                    }
//                }
//            }
//        }
//    }

    /*
   * Runnable: Periodically requests location
   */
    private val periodicLocationRequestRunnable: Runnable = object : Runnable {
        override fun run() {
            // pull current state from service
            if (trackerService != null){
                //GlobalData.currentBestLocation = trackerService!!.currentBestLocation
                //track = trackerService.track
                gpsProviderActive = trackerService!!.gpsProviderActive
                networkProviderActive = trackerService!!.networkProviderActive
                trackingState = trackerService!!.trackingState
                updateMarker()
            }

            // update location and track
            //layout.markCurrentPosition(currentBestLocation, trackingState)
            //layout.overlayCurrentTrack(track, trackingState)
            //layout.updateLiveStatics(length = track.length, duration = track.duration, trackingState = trackingState)
            // center map, if it had not been dragged/zoomed before
            //if (!layout.userInteraction) { layout.centerMap(currentBestLocation, true)}
            // show error snackbar if necessary
            //layout.toggleLocationErrorBar(gpsProviderActive, networkProviderActive)
            // use the handler to start runnable again after specified delay
            handler.postDelayed(this, Keys.REQUEST_CURRENT_LOCATION_INTERVAL)
        }
    }

}