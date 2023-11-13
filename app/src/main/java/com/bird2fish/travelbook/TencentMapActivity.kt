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
import com.bird2fish.travelbook.core.*
import com.bird2fish.travelbook.helper.DateTimeHelper
import com.bird2fish.travelbook.helper.LogHelper
import com.bird2fish.travelbook.helper.PermissionHelper
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
        val ll = findViewById<View>(R.id.layout_mapll) as LinearLayout

        val options = TencentMapOptions()
        options.isOfflineMapEnable = true

        mapView = TextureMapView(this, options)
        mapView!!.isOpaque = false

        ll.addView(mapView)

        //创建tencentMap地图对象，可以完成对地图的几乎所有操作
        tencentMap = mapView!!.map
        initToolBar()
        initMapOutlook()
        tencentMap!!.isTrafficEnabled = true;
        //tencentMap!!.setMapType(TencentMap.MAP_TYPE_SATELLITE);
        //tencentMap.setMapType(TencentMap.MAP_TYPE_NORMAL);

        initBottomView()

        // 开始请求权限
        if (PermissionHelper.checkLoctionPermission(this))
        {
            startTencentLocaionService()
        }else{
            val ret = PermissionHelper.requestLocationPermission(this)
            if (ret){
                startTencentLocaionService()
            }
        }
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
    }

    // 对点击的marker设置为顶层
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

    // 显示提示
    private fun showMsg(str:String){
        val tvMsg = this.findViewById<TextView>(R.id.tv_friend_msg)
        tvMsg.setText(str)
        //UiHelper.showMessage(this, str)
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
        var detail = ""
        if (f.uid == CurrentUser.getUser()!!.uid){


            if (GlobalData.currentTLocation != null){
                info=  String.format("位置：(%.6f, %.6f) 高度：%.0f 速度：%.1f",
                    GlobalData.currentTLocation!!.latitude,
                    GlobalData.currentTLocation!!.longitude,
                    GlobalData.currentTLocation!!.altitude,
                    GlobalData.currentTLocation!!.speed)


                val pos = LatLng(GlobalData.currentTLocation!!.latitude, GlobalData.currentTLocation!!.longitude)

                mMarker.position = pos
                mMarker.snippet = "${pos.latitude}, ${pos.longitude}"
                mMarker.refreshInfoWindow()
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

                val tmStr = DateTimeHelper.convertTimestampToDateString(f.tm * 1000)
                val span = DateTimeHelper.formatTimeDifference(f.tm * 1000);
                detail = "上报时间：" + tmStr + span
                moveCamera(f)
                resetZIndex(f.uid)

            }

        }
        tvInfo.setText(info)
        tvdetail.setText(detail)

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

        // 首测获得位置，需要初始化我的信息栏，并移动镜头
        if (!isInitedInfo && GlobalData.currentTLocation != null){

            var friend = Friend()
            friend.fromUser(CurrentUser.getUser()!!)
            onImageIconClick(friend)

            mMarker.isVisible = true
            moveCamera(null)
            isInitedInfo = true
        }
    }// end of update icons


    private fun initToolBar() {
        val toolbar = findViewById<View>(R.id.toolbarMap) as Toolbar
        toolbar.title = "动态位置" //设置主标题名称
        //toolbar.subtitle = "" //设置副标题名称
        setSupportActionBar(toolbar)
        toolbar.setNavigationIcon(R.drawable.msg_photo_settings);//是左边的图标样式

    }

    // 手动返回主页，主要是需要关闭那个导航抽屉
    fun backToMainActivity(){
        //finish()
        var intent: Intent = Intent()
        intent.setClass(this, MainActivity::class.java)
        startActivity(intent)
        if (MainActivity.topActivity != null)
        {
            MainActivity.topActivity!!.closeDrawer()
        }

    }

    // 自定义工具条图标点击的事件：放大，缩小，工具条
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
            // 这里节省了一个清空按钮
            val tvMsg = this.findViewById<TextView>(R.id.tv_friend_msg)
            tvMsg.setText("")
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

        initMyMarker()
        //addLine();
    }

    protected fun updateMarkers(){
        // 获取目前的好友位置列表
        val mapFriend = HttpWorker.get().getLastPointMap()
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
                val msg = "未能获取${f.uid} ${f.nick} 的位置,${f.msg}"
                showMsg(msg)
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
                val msg = "未能获取${f.uid} ${f.nick} 的位置,${f.msg}"
                showMsg(msg)
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
        //options.snippet("当前速度${friend.speed},高度${friend.ele}") //标注的InfoWindow的内容

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
        else if (GlobalData.currentTLocation != null)
        {
            lat = GlobalData.currentTLocation!!.latitude
            lon = GlobalData.currentTLocation!!.longitude
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
        //通过MarkerOptions配置,出生地在天安门
        var lat:Double = 39.908710
        var lon:Double = 116.397499
        var title = "正在获取位置……"

        if (GlobalData.currentTLocation != null)
        {
            lat = GlobalData.currentTLocation!!.latitude
            lon = GlobalData.currentTLocation!!.longitude
            title = "当前位置"
        }

        val position = LatLng(lat, lon)

        val options = MarkerOptions(position)
        options.infoWindowEnable(false) //默认为true
        options.title(title) //标注的InfoWindow的标题
        //options.snippet("地址: 北京市东城区东长安街") //标注的InfoWindow的内容

        val bitmapIcon = UiHelper.getSmallIconBitmap(CurrentUser.getUser()!!.icon, this)
        val custom = BitmapDescriptorFactory.fromBitmap(bitmapIcon)
        options.icon(custom)

        this.mMarker = tencentMap!!.addMarker(options)
        //开启信息窗口
//        mMarker.isInfoWindowEnable = true
//        mMarker.title = title

        mMarker.position = position
        val cameraSigma = CameraUpdateFactory.newCameraPosition(
            CameraPosition(
                position,  //中心点坐标，地图目标经纬度
                12f,  //目标缩放级别
                0f,  //目标倾斜角[0.0 ~ 45.0] (垂直地图时为0)
                0f
            )
        ) //目标旋转角 0~360° (正北方为0)
        tencentMap!!.moveCamera(cameraSigma) //移动地图
        mMarker.showInfoWindow()

    }


    // 定时器会调用这里
    protected fun updateMyMarker(){

        var title = "当前位置"
        if (GlobalData.currentTLocation != null)
        {
            val lat: Double = GlobalData.currentTLocation!!.latitude
            val lon: Double  = GlobalData.currentTLocation!!.longitude

            val pos = LatLng(lat, lon)

            mMarker.title = "${pos.latitude}, ${pos.longitude}"
            mMarker.snippet = GlobalData.currentTLocation!!.street
            mMarker.showInfoWindow()
            mMarker.isInfoWindowEnable = true
            mMarker.refreshInfoWindow()
        }
    }

    protected  fun updateViews(){
        updateMyMarker()   // 更新marker
        // 更新好友位置
        updateMarkers()    // 内部调用updateBottomView更新图标信息
    }

    // 请求权限的回调函数
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {

        when (requestCode) {
            PermissionHelper.PERMISSION_REQUEST_CODE_LOCATION -> {
                // 检查用户是否授予 ACTIVITY_RECOGNITION 权限
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    GlobalData.isLocationEnabled = true
                    PermissionHelper.requestBackgroundPermission(this)
                } else {
                    // 用户拒绝了活动识别权限，需要处理相应逻辑
                    GlobalData.isLocationEnabled = false
                    UiHelper.showCenterMessage(this, "目前无法定位！请开始软件定位权限为一直允许，并在耗电选项设置为不限制")
                }
            }
            PermissionHelper.PERMISSION_REQUEST_CODE_BACKGROUND ->{

                GlobalData.isLocationBackgroudEnabled = grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED
                startTencentLocaionService()
            }
            PermissionHelper.PERMISSION_REQUEST_CODE_BODY_SENSORS ->{
                GlobalData.isBodySensorEnabled = grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED
            }
            PermissionHelper.PERMISSION_REQUEST_CODE_RECOGNITION ->{
                GlobalData.isRecognitionEnabled = grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED
            }
            // 处理其他权限请求结果...
        }

        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }


    // 启动服务和HTTPworker
    fun startTencentLocaionService(){
        // 这里检查申请权限的结果
        if (GlobalData.isLocationBackgroudEnabled){
            TencentLocService.instance!!.startBackGround(this)
        }else if (GlobalData.isLocationEnabled){
            TencentLocService.instance!!.startBackGround(this)
        }else{
            UiHelper.showCenterMessage(this, "目前无法定位！请开始软件定位权限为一直允许，并在耗电选项设置为不限制")
        }

        // 开启后台上传与更新
        HttpWorker.get().startWorker()

        // 界面刷新
        startRefreshInfo()
    }

    // 启动轮训机制
    fun startRefreshInfo(){
        handler.postDelayed(periodicLocationRequestRunnable, 1000)
    }

    // 轮询的函数
    private val periodicLocationRequestRunnable: Runnable = object : Runnable {
        override fun run() {
            // 更新好友信息，刷新
            updateViews()
            handler.postDelayed(this, GlobalData.intervalOfRefresh)
        }
    }

    /**
     * mapview的生命周期管理
     */
    override fun onStart() {
        super.onStart()
        mapView!!.onStart()
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

}