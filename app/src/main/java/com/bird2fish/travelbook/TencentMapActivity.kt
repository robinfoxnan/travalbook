package com.bird2fish.travelbook


import android.Manifest
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.os.*
import android.view.*
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import com.bird2fish.travelbook.core.GlobalData
import com.bird2fish.travelbook.core.Keys
import com.bird2fish.travelbook.core.TrackerService
import com.bird2fish.travelbook.core.UiHelper
import com.bird2fish.travelbook.helper.DateTimeHelper
import com.bird2fish.travelbook.helper.LogHelper
import com.bird2fish.travelbook.ui.contact.Friend
import com.bird2fish.travelbook.ui.data.model.CurrentUser
import com.tencent.tencentmap.mapsdk.maps.*
import com.tencent.tencentmap.mapsdk.maps.model.BitmapDescriptorFactory
import com.tencent.tencentmap.mapsdk.maps.model.CameraPosition
import com.tencent.tencentmap.mapsdk.maps.model.LatLng
import com.tencent.tencentmap.mapsdk.maps.model.MarkerOptions


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
    private var iconsMap = mutableMapOf<String, ImageView>()

    private var isInitedInfo = false

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
        tencentMap!!.isTrafficEnabled = true;
        //tencentMap!!.setMapType(TencentMap.MAP_TYPE_SATELLITE);
        //tencentMap.setMapType(TencentMap.MAP_TYPE_NORMAL);

        initBottomView()
    }

    // 初始化底部视图，将自己的图标加进去
    private fun initBottomView(){
        var me = Friend()
        me.fromUser(CurrentUser.getUser()!!)

        val linearLayout = this.findViewById<LinearLayout>(R.id.linear_layout_icons)
        linearLayout.removeAllViews()
        val imageView = UiHelper.createImageViewForBottomView(this, me)
        imageView.tag = me
        imageView.setOnClickListener{
            onImageIconClick(me)
        }
        linearLayout.addView(imageView)

        // 信息栏部分设置
        // 获取LayoutInflater实例
//        val inflater: LayoutInflater = getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
//
//        // 加载布局文件
//        val myLayout: View = inflater.inflate(R.layout.pop_location_part_top, null)
//        // 找到要添加布局的View
//        val myViewGroup = findViewById<View>(R.id.pop_loc_info_panel)
//
//        // 将加载的布局添加到ViewGroup中
//        myViewGroup.add
    }

    fun resetZIndex(uid: String){
        if (markersMap.containsKey(uid)){
            mMarker.setZIndex(0f);
        }else{
            mMarker.setZIndex(100f);
        }
        for ((k, v) in markersMap){
            if (k == uid){
                v.setZIndex(100f)
            }else{
                v.setZIndex(0f)
            }
        }
    }

    // 底部图标点击响应事件
    private fun onImageIconClick(f: Friend){
        //UiHelper.showCenterMessage(this, "点击了 ${f.nick}")
        val tvName = this.findViewById<TextView>(R.id.tv_friend_name)
        val name = "${f.nick}(${f.uid})"
        tvName.setText(name)

        val tvInfo = this.findViewById<TextView>(R.id.tv_friend_info)
        val tvdetail = this.findViewById<TextView>(R.id.tv_detail)
        var info = "未能获取坐标，检查对方是否启动软件并关注您"
        if (f.uid == CurrentUser.getUser()!!.uid){

            tvdetail.setText("")
            if (GlobalData.currentBestLocation != null){
                info=  String.format("位置：(%.6f, %.6f) 高度：%.0f 速度：%.1f",
                    GlobalData.currentBestLocation!!.latitude,
                    GlobalData.currentBestLocation!!.longitude,
                    GlobalData.currentBestLocation!!.altitude,
                    GlobalData.currentBestLocation!!.speed)
                moveCamera(null)
                resetZIndex(f.uid)

            }else{
                info = "请打开位置定位"
            }


        }else
        {
            if (f.isShare){
                info = String.format("位置：(%.6f, %.6f) 高度：%.0f 速度：%.1f", f.lat, f.lon,
                    f.ele, f.speed)

                val detail = DateTimeHelper.convertTimestampToDateString(f.tm * 1000)
                tvdetail.setText("最后上报时间：" + detail)
                moveCamera(f)
                resetZIndex(f.uid)

            }

        }
        tvInfo.setText(info)



    }

    // 更新好友图标
    private fun updateBottomView(friendList: Map<String, Friend>){

        val linearLayout = this.findViewById<LinearLayout>(R.id.linear_layout_icons)
        // 先检查是否有不需要显示的, 不再朋友列表中，说明不需要显示了
        for ((key, icon) in iconsMap){
            if (!friendList.containsKey(key))
            {
                linearLayout.removeView(icon)
            }
        }

        // 将好友列表中的所有东西都绘制一遍
        for ((key, f) in friendList){
            if (iconsMap.containsKey(key)){
                continue
            }else{
                val imageView = UiHelper.createImageViewForBottomView(this, f)

                imageView.tag = f
                imageView.setOnClickListener{
                    onImageIconClick(f)
                }
                // 添加图标到线性布局，同时放到映射中标记存在了
                linearLayout.addView(imageView)
                iconsMap.put(key, imageView)
            }
        }

//        if (isInitedInfo == false){
//
//            onImageIconClick(mMarker.tag as Friend)
//            isInitedInfo = true
//        }
    }// end of update icons


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
        if (item.getItemId() == android.R.id.home) {
            backToMainActivity()
        }else if (item.getItemId() == R.id.memu_map_item_magnify){
            //UiHelper.showCenterMessage(this, "location")
            //moveCamera(null)
            tencentMap?.animateCamera(CameraUpdateFactory.zoomIn());

        }else if (item.getItemId() == R.id.memu_map_item_demag){
            tencentMap?.animateCamera(CameraUpdateFactory.zoomOut());
        }
        else if (item.getItemId() == R.id.memu_map_item_list){

            val overlayView = findViewById<View>(R.id.overlayView)
            overlayView.visibility = if (overlayView.visibility == View.VISIBLE) View.GONE else View.VISIBLE
        }
        return super.onOptionsItemSelected(item)
    }

    // 加载菜单到顶部，里面有2个按钮
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
        mapUiSettings.setZoomControlsEnabled(false);
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
        //mapUiSettings.isMyLocationButtonEnabled = true
        /**
         * 旋转手势
         */
        mapUiSettings.isRotateGesturesEnabled = false
        //tencentMap!!.isMyLocationEnabled = true


// 获取地图控件
        //地图上设置定位数据源
        //tencentMap!!.setLocationSource(this)
        //设置当前位置可见
        //tencentMap!!.isMyLocationEnabled = true
        //tencentMap!!.set

        initMyMarker()
        //addLine();
    }

    inner class mylocationListener: TencentMap.OnMyLocationClickListener {
        override fun onMyLocationClicked(var1: LatLng?): Boolean{
            //moveCamera(null)
            UiHelper.showCenterMessage(this@TencentMapActivity, "test it ")
            return  true
        }
    }

    protected fun updateMarkers(){
        if (trackerService==null){
            UiHelper.showMessage(this, "未启动位置服务！请开启位置服务与授权，并重启程序")
            return
        }
        // 获取目前的好友位置列表
        val mapFriend = trackerService!!.getLastPointMap()
        // 更新底部状态条
        updateBottomView(mapFriend)

        // 不需要显示的删除，没有获取到位置的删除，能获取到位置的更新位置
        for (key:String in markersMap.keys) {
            //println("$key -> $value")
            if (!mapFriend.containsKey(key)){
                markersMap.remove(key)
                continue
            }
            val f = mapFriend[key]
            if (!f!!.isShare)  // 未加好友，或者无数据
            {
                UiHelper.showCenterMessage(this, "未能获取${f.uid} ${f.nick} 的位置,${f.msg}")
                markersMap.remove(key)
                continue
            }
            // 更新位置
            val marker = markersMap[key]
            marker!!.isInfoWindowEnable = true
            marker!!.title = "${f.nick}"
            val position = LatLng(f.lat, f.lon)
            marker!!.position = position

            // 删除，已经处理过了
            mapFriend.remove(key)
        }

        // 把剩下的新建一个标记
        for ((k, f) in mapFriend) {
            if (!f.isShare){
                UiHelper.showCenterMessage(this, "未能获取${f.uid} ${f.nick} 的位置,${f.msg}")
                continue
            }
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

        val bitmapIcon = UiHelper.getSmallIconBitmap(friend!!.icon, this)
        val custom = BitmapDescriptorFactory.fromBitmap(bitmapIcon)
        options.icon(custom)

        val marker = tencentMap!!.addMarker(options)

        //开启信息窗口
        marker.isInfoWindowEnable = true
        //marker.title = title
        marker.position = position
        marker.showInfoWindow()
        return  marker
    }

    // 移动镜头
    public  fun moveCamera(friend: Friend?){
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
        }else{
            return
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

        val bitmapIcon = UiHelper.getSmallIconBitmap(CurrentUser.getUser()!!.icon, this)
        val custom = BitmapDescriptorFactory.fromBitmap(bitmapIcon)
        options.icon(custom)

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

            //tencentMap?.myLocation?.set(GlobalData.currentBestLocation)
            mMarker.position = pos
            mMarker.title = title
            mMarker.snippet = ""
            mMarker.refreshInfoWindow()
        }

        // 更新好友位置
        updateMarkers()
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
            handler.postDelayed(periodicLocationRequestRunnable, 3000)
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