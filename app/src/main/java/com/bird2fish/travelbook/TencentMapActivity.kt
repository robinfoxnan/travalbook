package com.bird2fish.travelbook


import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Rect
import android.os.*
import android.view.*
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.FragmentActivity
import com.bird2fish.travelbook.core.*
import com.bird2fish.travelbook.helper.*
import com.bird2fish.travelbook.ui.contact.Friend
import com.bird2fish.travelbook.ui.data.model.CurrentUser
import com.tencent.tencentmap.mapsdk.maps.*
import com.tencent.tencentmap.mapsdk.maps.TencentMap.OnMarkerDragListener
import com.tencent.tencentmap.mapsdk.maps.model.*
import java.io.File
import java.util.LinkedList


class TencentMapActivity : AppCompatActivity() {

    /* Define log tag */
    private val TAG: String = LogHelper.makeLogTag(TencentMap::class.java)

    private var mapView: TextureMapView? = null
    protected var tencentMap: TencentMap? = null
    private val handler: Handler = Handler(Looper.getMainLooper())

    private lateinit var  mMarker:com.tencent.tencentmap.mapsdk.maps.model.Marker   // 自己的位置
    private lateinit var  mImageView:ImageView
    private var markersMap =  mutableMapOf<String, com.tencent.tencentmap.mapsdk.maps.model.Marker>()
    private var iconsMap = mutableMapOf<String, ImageView>()

    private var isInitedInfo = false

    private var favMarkers = mutableMapOf<com.tencent.tencentmap.mapsdk.maps.model.Marker, FavLocation>()  // 收藏点
    private var isFavOn :Boolean = false
    private var isFollowing :Boolean = false
    // 当前轨迹
    var wayLine: com.tencent.tencentmap.mapsdk.maps.model.Polyline? = null
    val latLngs: MutableList<LatLng> = ArrayList()  // 轨迹点
    var trackId :String = ""

    // 点星星的轨迹
    private var linesMap= mutableMapOf<String, com.tencent.tencentmap.mapsdk.maps.model.Polyline>()


    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        // 处理新的 Intent
        if (intent != null) {
            // 在这里处理传入的 Intent，可能是通知的 Intent
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        TencentMapInitializer.setAgreePrivacy(true)

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tencent_map)

        // 加载
        GlobalData.InitLoad()

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
        tencentMap!!.isTrafficEnabled = false;
        //tencentMap!!.setMapType(TencentMap.MAP_TYPE_SATELLITE);
        //tencentMap.setMapType(TencentMap.MAP_TYPE_NORMAL);



        // 先设置当前需要刷新的人物
        GlobalData.currentFriend = Friend(uid = CurrentUser.getUser()!!.uid)
        // 底部图标
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

        // 自己的位置
        initMyMarker()

        // 地图右侧边栏
        initSideToolbar()

        // 日志与轨迹文件的访问权限
        tryInitFiles()

        // 地图点击添加标记，移动标记，在标记上弹出右键菜单
        initMapEvent()

        // 监听输入法显示和隐藏事件
        val rootView = findViewById<View>(android.R.id.content)
        rootView.viewTreeObserver.addOnPreDrawListener {
            val rect = Rect()
            rootView.getWindowVisibleDisplayFrame(rect)
            val screenHeight = rootView.height
            val keypadHeight = screenHeight - rect.bottom

            if (keypadHeight > screenHeight * 0.15) {
                // 输入法显示
                //movePopupWindowUp(keypadHeight)
                //UiHelper.showCenterMessage(this, "up move")
            } else {
                // 输入法隐藏
                //movePopupWindowDown()
            }

            return@addOnPreDrawListener true
        }

    }

    private fun initMapEvent(){
        // 设置标记的拖动监听器
        tencentMap!!.setOnMarkerDragListener(object : OnMarkerDragListener {
            override fun onMarkerDragStart(marker: Marker) {
                // 标记开始拖动时的处理

            }

            override fun onMarkerDrag(marker: Marker) {
                // 标记拖动过程中的处理
            }

            override fun onMarkerDragEnd(marker: Marker) {
                // 标记拖动结束时的处理
                val latLng = marker.position
                // newPosition 就是标记的新位置坐标
                //Log.d("Marker", "New Position: " + newPosition.toString());
                val buffer = StringBuffer()
                buffer.append(latLng.latitude)
                buffer.append(",")
                buffer.append(latLng.longitude)
                val loc = favMarkers[marker]
                if (loc != null){
                    loc.lat = latLng.latitude
                    loc.lon = latLng.longitude
                }
                UiHelper.showCenterMessage(this@TencentMapActivity, buffer.toString())
            }
        })

        // 设置长按时候添加标签
        tencentMap!!.setOnMapLongClickListener { latLng->
            addFavMarker(latLng)
        }

//        tencentMap!!.setOnMapClickListener { latLng->
//            addFavMarker(latLng)
//        }

        // 设置标记的点击监听器
        tencentMap!!.setOnMarkerClickListener { clickedMarker ->
            // 点击了我们添加的标记
            // 在这里执行选中标记后的操作，例如显示信息窗口

            if (clickedMarker.tag == null) {
                return@setOnMarkerClickListener false
            }

            if (!isFavOn) {
                return@setOnMarkerClickListener false
            }

            val str = clickedMarker.tag as? String
            if (str == "fav") {
                //clickedMarker.snippet = "选中"
                clickedMarker.showInfoWindow()
                showMarkerPopupMenu(clickedMarker)
                return@setOnMarkerClickListener true // 返回 true 表示消费了点击事件
            }

            return@setOnMarkerClickListener false
        }

    }

    // 显示marker的 PopupWindow 的方法
    private fun showMarkerPopupMenu(marker: Marker) {
        // 创建布局
        val popupView: View = layoutInflater.inflate(R.layout.popup_menu, null)

        // 创建 PopupWindow
        val popupWindow = PopupWindow(
            popupView,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            true
        )

        // 设置菜单项点击事件
        val deleteTextView = popupView.findViewById<TextView>(R.id.tv_delete_mark)
        deleteTextView.setOnClickListener {
            popupWindow.dismiss() // 关闭 菜单
            // 在这里执行删除标记的操作
            val loc = favMarkers[marker]
            marker.remove()
            if (loc != null){
                GlobalData.removeFavLocation(this, loc!!)
            }

        }

        val editTextView = popupView.findViewById<TextView>(R.id.tv_mark_info)
        editTextView.setOnClickListener {
            popupWindow.dismiss() // 关闭 菜单
            showEditMarkerInfo(marker)  // 开始编辑信息

        }

        // 显示 PopupWindow
        // 显示 PopupWindow 在标记位置上方
        val markerLatLng = marker.position

// 将地理坐标转换为屏幕坐标
        val screenLocation = tencentMap!!.projection.toScreenLocation(markerLatLng)

// 现在，screenLocation 包含了 Marker 在屏幕上的坐标
        val screenX = screenLocation.x
        val screenY = screenLocation.y
        val offsetX = 50
        val offsetY = 50
        //popupWindow.showAtLocation(mapView, Gravity.NO_GRAVITY, (int) position.x + offsetX, (int) position.y - offsetY);
        //popupWindow.showAsDropDown(marker.getMarkerView());
        popupWindow.showAtLocation(
            mapView,
            Gravity.NO_GRAVITY,
            screenX + offsetX,
            screenY + offsetY
        )
    }

    // 编辑自定义收藏点的信息
    fun showEditMarkerInfo(marker: Marker){
        val window = FavEditWindow(this, R.layout.fav_edit_info, "")

        val loc = favMarkers[marker]
        if (loc == null){
            UiHelper.showCenterMessage(this, "图标没有对应的信息，奇怪")
            return
        }
        window.setLocation(loc!!, marker)

        // 显示输入法
        //val inputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        //inputMethodManager.showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT)
        window.showPopupWindow()


    }

//    private fun movePopupWindowUp(keypadHeight: Int) {
//        val layoutParams = popupWindow.contentView.layoutParams as ViewGroup.MarginLayoutParams
//        layoutParams.bottomMargin = keypadHeight
//        popupWindow.contentView.layoutParams = layoutParams
//    }
//
//    private fun movePopupWindowDown() {
//        val layoutParams = popupWindow.contentView.layoutParams as ViewGroup.MarginLayoutParams
//        layoutParams.bottomMargin = 0
//        popupWindow.contentView.layoutParams = layoutParams
//    }

    // 如果有了权限则直接初始化，否则，需要在异步处理返回的地方处理
    private fun tryInitFiles(){

        // 请求文件权限
        val ret = PermissionHelper.requestFile(this)
        if (ret){
            initFiles()
        }

    }

    // 加载
    private fun initFiles(){
        if (GlobalData.isFileInited()){
            return
        }
        // 初始化目录
        val fileEx: File? = this.getExternalFilesDir(null)
        if (fileEx != null){
            val dir = fileEx.absolutePath
            LogHelper.setLogDir(dir)
            val ret = GlobalData.setRootDir(dir)
            if (!ret){
                UiHelper.showCenterMessage(this, "轨迹存储路径${dir}无法访问，请检查读写权限")
                return
            }
            // 异步
            GlobalData.loadTrackList(this)

            // 同步加载收藏点
            GlobalData.loadFavLocations(this)
            // 界面上添加收藏的标签
            updateFavMarkers()

        }else{
            UiHelper.showCenterMessage(this, "获取存储位置出错！")
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
        this.mImageView = imageView
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
//    private fun showMsg(str:String){
//        val tvMsg = this.findViewById<TextView>(R.id.tv_friend_msg)
//        tvMsg.setText(str)
//        //UiHelper.showMessage(this, str)
//    }

    // 底部图标点击响应事件
    // 设置了当前好友之后，一直刷新他的信息
    private fun onImageIconClick(f: Friend){
        if (f == null){
            GlobalData.currentFriend = Friend(uid = CurrentUser.getUser()!!.uid)
        }else
        {
            GlobalData.currentFriend = f
        }

        // 设置底部信息
        updateFriendInfo()

        // 设置显示层级
        resetZIndex(f.uid)
        // 移动相机
        moveCamera(f)


    }

    // 这是一个补救，如果后台服务由于某种原因，长时间无数据，则应该尝试重启
    private fun restartService(){
        val intent = Intent(this, TencentLocService::class.java)
        //intent.putExtra("command", "start"); // 通过Intent传递命令
        intent.action = Keys.ACTION_INIT

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            this.startForegroundService(intent);
        } else {
            this.startService(intent);
        }
        val detail = String.format("设置GPS采集间隔为 %d 秒", GlobalData.intervalOfLocation / 1000)
        UiHelper.showCenterMessage(this, detail)
    }

    // 根据当前选择的用户，更新底部状态窗口口
    private fun updateFriendInfo(){
        if (GlobalData.currentFriend == null)
        {
            GlobalData.currentFriend = Friend(uid = CurrentUser.getUser()!!.uid)
        }
        var f = GlobalData.currentFriend!!

        //UiHelper.showCenterMessage(this, "点击了 ${f.nick}")
        val tvName = this.findViewById<TextView>(R.id.tv_friend_name)
        val tvInfo = this.findViewById<TextView>(R.id.tv_friend_info)
        val tvdetail = this.findViewById<TextView>(R.id.tv_detail)

        // 设置名字
        val name = "${f.nick}(${f.uid})"
        tvName.setText(name)

        var info = ""
        var detail = ""
        if (f.uid == CurrentUser.getUser()!!.uid)
        {
                if (GlobalData.currentTLocation != null){

                    var speedStr = UiHelper.formatSpeed(GlobalData.currentTLocation!!.speed)

                    info=  String.format("位置：(%.6f, %.6f) 海拔：%.0f米 速度：%s",
                        GlobalData.currentTLocation!!.latitude,
                        GlobalData.currentTLocation!!.longitude,
                        GlobalData.currentTLocation!!.altitude,
                        speedStr)
                    if (GlobalData.currentTLocation!!.address != null &&  !GlobalData.currentTLocation!!.address.equals("Unknow"))
                    {
                        detail = GlobalData.currentTLocation!!.address
                    }

                    detail += ", 获取定位时间："  + DateTimeHelper.formatTimeDifference(GlobalData.currentTm)


            }else{
                if (GlobalData.isLocationEnabled)
                {
                    info = "等待定位信号"
                }else{
                    info = "请打开定位权限"
                }

            }
        }else
        {
            if (f.isShare){

                info = String.format("位置：(%.6f, %.6f) 海拔：%.0f 速度：%.1f", f.lat, f.lon,
                    f.ele, f.speed)

                val tmStr = DateTimeHelper.convertTimestampToDateString(f.tm * 1000)
                val span = DateTimeHelper.formatTimeDifference(f.tm * 1000);
                detail = "上报时间：${tmStr} (${span}) "
                if (GlobalData.currentTLocation != null){
                    val distance = LocationHelper.haversine(f.lat, f.lon,
                        GlobalData.currentTLocation!!.latitude, GlobalData.currentTLocation!!.longitude)

                    detail += f.street
                    detail += " 距离 %.2f km".format(distance)
                }
            }
            else{
                detail = f.msg
            }

        }



        tvInfo.setText(info)
        tvdetail.setText(detail)

    }

    // 更新好友图标
    private fun updateBottomView(friendList: Map<String, Friend>){
        val linearLayout = this.findViewById<LinearLayout>(R.id.linear_layout_icons)
        // 先检查是否有不需要显示的, 不再朋友列表中，通信簿设置后说明不需要显示了
        var keys = LinkedList<String>()
        keys.addAll(iconsMap.keys)  // 防止遍历时候删除造成失效

        for (key in keys){
            if (!friendList.containsKey(key))
            {
                linearLayout.removeView(iconsMap[key])
                iconsMap.remove(key)
            }
        }

        // 将好友列表中的所有东西都绘制一遍
        for ((key, f) in friendList){
            if (iconsMap.containsKey(key)){
                val friend = iconsMap[key]!!.tag as Friend
                friend.setValue(f)
                continue
            }else{
                val imageView = UiHelper.createImageViewForBottomView(this, f)

                imageView.tag = f
                imageView.setOnClickListener{
                    onImageIconClick(imageView.tag!! as Friend)
                }
                // 添加图标到线性布局，同时放到映射中标记存在了
                linearLayout.addView(imageView)
                iconsMap.put(key, imageView)
            }
        }

    }// end of update icons


    val iconIds = intArrayOf(
        R.drawable.hike,
        R.drawable.run,
        R.drawable.bike,
        R.drawable.motorbike,
        R.drawable.car,
        R.drawable.lasy
    )
    // 左侧工具条
    private fun initToolBar() {
        val toolbar = findViewById<View>(R.id.toolbarMap) as Toolbar
        toolbar.title = "动态位置" //设置主标题名称
        //toolbar.subtitle = "" //设置副标题名称
        setSupportActionBar(toolbar)
        // R.drawable.msg_photo_settings

        var id = GlobalData.sportMode.intValue;
        if (id< 1){
            id = 1
        }else if (id > 6){
            id = 6
        }
        val icon = UiHelper.loadAndScaleImage(this, iconIds[id-1])

        toolbar.setNavigationIcon(icon);//是左边的图标样式

        // 设置标题栏颜色
        if (this is FragmentActivity) {
            supportActionBar?.setBackgroundDrawable(resources.getDrawable(R.color.gray_title))
        } else {
            requestWindowFeature(Window.FEATURE_NO_TITLE)
            window.statusBarColor = resources.getColor(R.color.gray_title)
        }

    }

    // 手动返回主页，主要是需要关闭那个导航抽屉
    fun backToMainActivity(){
        var intent: Intent = Intent()
        intent.setClass(this, MainActivity::class.java)
        // 设置自定义参数
        intent.putExtra("action", "openDrawer");
        startActivity(intent)
//        if (MainActivity.topActivity != null)
//        {
//            //MainActivity.topActivity!!.closeDrawer()
//            MainActivity.topActivity!!.openDrawer()
//        }

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

    private fun returnToDesktop() {
        val intent = Intent(Intent.ACTION_MAIN)
        intent.addCategory(Intent.CATEGORY_HOME)
        startActivity(intent)
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean
    {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            // 按下返回键时
            returnToDesktop();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    override fun onKeyUp(keyCode: Int, event: KeyEvent?): Boolean
    {
//        if (keyCode == KeyEvent.KEYCODE_BACK) {
//            //backToMainActivity()
//            return true;
//        }
        return super.onKeyDown(keyCode, event);
    }

    // 将右侧工具条设置按钮
    private fun initSideToolbar(){
        var btnLayer = findViewById<ImageButton>(R.id.btn_layer)
        btnLayer.setOnClickListener {
            if (tencentMap!!.mapType == TencentMap.MAP_TYPE_SATELLITE)
            {
                tencentMap!!.setMapType(TencentMap.MAP_TYPE_NORMAL)
            }else
            {
                tencentMap!!.setMapType(TencentMap.MAP_TYPE_SATELLITE)
            }
        }

        // 点击才进入
        var btnMark = findViewById<ImageButton>(R.id.btn_addmark)
        //btnMark.visibility = View.GONE
        btnMark.setOnClickListener {
            isFavOn =  !isFavOn
            if (isFavOn){
                btnMark.setImageResource(android.R.drawable.ic_menu_compass)
                UiHelper.showCenterMessage(this, "开启收藏点编辑模式")

                for ((m, loc) in favMarkers){
                    m.isDraggable = true
                }
            }else{
                btnMark.setImageResource(android.R.drawable.ic_menu_add)
                UiHelper.showCenterMessage(this, "退出编辑收藏点模式")
                for ((m, loc) in favMarkers){
                    m.isDraggable = false
                }
            }
        }





        var btnRecord = findViewById<ImageButton>(R.id.btn_record)
        btnRecord.setOnClickListener {
            if (GlobalData.isRecording)  // 正在录制，改为停止，显示录制按钮
            {
                GlobalData.stopTrack(this)

                // 移动到合适的位置并切换
                moveCamera(this.latLngs)
                screenSnapShot()

                this.latLngs.clear()
                this.wayLine!!.remove()
                this.wayLine = null

                UiHelper.showCenterMessage(this, "停止当前轨迹记录")
                updateAllStartedTracks()

            }else{
                GlobalData.startTrack(this)
                updateWayPoint()
                UiHelper.showCenterMessage(this, "开始新轨迹记录")

            }
            updateRecordButton()
        }

        updateRecordButton()


        // 测试
        var btnTrafic = findViewById<ImageButton>(R.id.btn_trafic)
        btnTrafic.setOnClickListener {
            //UiHelper.showCenterMessage(this, "分享")
            //val info = "共${latLngs.size}个点"
            tencentMap!!.isTrafficEnabled = !tencentMap!!.isTrafficEnabled
            if (tencentMap!!.isTrafficEnabled){
                btnTrafic.setImageResource(R.drawable.trafic1)
                //UiHelper.showCenterMessage(this, "将显示路况")
            }else{
                //UiHelper.showCenterMessage(this, "停止显示路况")
                btnTrafic.setImageResource(R.drawable.trafics)
            }

        }

        var btnShot = findViewById<ImageButton>(R.id.btn_screen_shot)
        btnShot.setOnClickListener {
            //UiHelper.showCenterMessage(this, "导入")
            //test_addLine()
            screenSnapShot()
        }

        // 跟随模式
        var btnFollow = findViewById<ImageButton>(R.id.btn_follow_friend)
        btnFollow.setOnClickListener {
            isFollowing = !isFollowing
            if (isFollowing){
                btnFollow.setImageResource(android.R.drawable.ic_menu_view)
                UiHelper.showCenterMessage(this, "镜头将自动跟随您的位置")
            }else{
                btnFollow.setImageResource(android.R.drawable.ic_menu_mapmode)
                UiHelper.showCenterMessage(this, "退出镜头自动跟随模式")
            }
        }

    }

    // 保存当前截图
    private fun screenSnapShot(){
        tencentMap!!.snapshot(object : TencentMap.SnapshotReadyCallback {
            // 截图准备完成
            override fun onSnapshotReady(bitmap: Bitmap) {
                //imgView.setImageBitmap(bitmap)
                FileHelper.saveImageInDCIM(this@TencentMapActivity, bitmap);
            }
        }, Bitmap.Config.ARGB_8888)
    }

    // 显示所有的点星星的轨迹线
    private fun updateAllStartedTracks(){
        if (GlobalData.trackList == null)
            return
        for (t in GlobalData.trackList!!.tracklistElements){
            if (t.starred && !linesMap.containsKey(t.name)){
                addTrackLine(t)
            }

            // 删除不显示的
            if (!t.starred && linesMap.containsKey(t.name)){
                val line = linesMap[t.name]
                linesMap.remove(t.name)
                line!!.remove()
            }
        }
    }

    // 添加一条轨迹
    private fun addTrackLine(t: TracklistElement){
        val track = FileHelper.readTrack(this, t.trackUriString)
        if (track.wayPoints.size < 2){
            return
        }

        val pts: MutableList<LatLng> = ArrayList()
        for (p in track.wayPoints){
            pts.add(LatLng(p.latitude, p.longitude))
        }

        // 构造 PolylineOpitons
        val polylineOptions = PolylineOptions()
            .addAll(pts) // 折线设置圆形线头
            .lineCap(true) //.lineType(PolylineOptions.LineType.LINE_TYPE_DOTTEDLINE)
            // 纹理颜色
            //.color()
            .color(PolylineOptions.Colors.DARK_BLUE)
            .width(15f)

        // 绘制折线
        val polyline = tencentMap!!.addPolyline(polylineOptions)
        linesMap.put(t.name, polyline)

    }



    // 记录的按钮
    private fun updateRecordButton(){

        var btnRecord = findViewById<ImageButton>(R.id.btn_record)
        if (!GlobalData.isRecording){
            btnRecord.setImageResource(R.drawable.ic_bar_record_24dp)
        }
        else
        {
            btnRecord.setImageResource(R.drawable.ic_bar_stop_24dp)
        }
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
        mapUiSettings.isRotateGesturesEnabled = true
        //tencentMap!!.isMyLocationEnabled = true


        //addLine();
    }

    // 更新所有的
    protected fun updateAllMarkers( mapFriend :MutableMap<String, Friend>){

        // 删除不需要显示的markers，
        val keys = LinkedList<String>()
        keys.addAll(markersMap.keys)   // 重建一个是防止在遍历时候出错

        for (key:String in keys) {
            if (!mapFriend.containsKey(key)){
                markersMap[key]!!.remove()  // 地图上移除marker
                markersMap.remove(key)
                continue
            }
            // 还需要显示的
            val f = mapFriend[key]
            if (!f!!.isShare)  // 未加好友，或者无数据
            {
                val msg = "未能获取${f.uid} ${f.nick} 的位置,${f.msg}"
                //showMsg(msg)
                f.msg = msg
                markersMap[key]!!.remove()
                markersMap.remove(key)
                continue
            }
            // 更新位置
            val marker = markersMap[key]
            marker!!.isInfoWindowEnable = true
            marker!!.title = "${f.nick}"
            marker!!.snippet = "${f.street}"
            val position = LatLng(f.lat, f.lon)
            marker!!.position = position

        }

        // 反向查找，找剩下的
        for ((k, f) in mapFriend) {
            if (markersMap.containsKey(k)){
                continue;
            }

            if (!f.isShare){
                val msg = "未能获取${f.uid} ${f.nick} 的位置,${f.msg}"
                //showMsg(msg)
                f.msg = msg
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
        marker.tag = friend.uid

        //开启信息窗口
        marker.isInfoWindowEnable = true
        marker.title = title
        marker.position = position
        marker.showInfoWindow()
        return  marker
    }

    // 初始化时候添加或者移除收藏的点，为了同步方便，在点界面不再提供删除功能，只是显示
    private fun updateFavMarkers(){
        for (loc in GlobalData.getLocations()){
            val latLng = LatLng(loc.lat, loc.lon)

            val options = MarkerOptions(latLng)
            options.infoWindowEnable(true) //默认为true
            options.title(loc.title)

            val bitmapIcon = UiHelper.getSmallIconBitmap(R.drawable.flag, this)
            val custom = BitmapDescriptorFactory.fromBitmap(bitmapIcon)
            options.icon(custom)

            val marker = tencentMap!!.addMarker(options)
            marker.tag = "fav"
            favMarkers.put(marker, loc)
        }
    }
    // 手动长按添加的标签
    protected fun addFavMarker(latLng: LatLng){
        if (!isFavOn){
            return
        }

        val buffer = StringBuffer()
        buffer.append(latLng.latitude)
        buffer.append(",")
        buffer.append(latLng.longitude)
        // UiHelper.showCenterMessage(this@TencentMapActivity, buffer.toString())

        val options = MarkerOptions(latLng)
        options.infoWindowEnable(false) //默认为true
        options.title(buffer.toString()) //标注的InfoWindow的标题
        //options.snippet("当前速度${friend.speed},高度${friend.ele}") //标注的InfoWindow的内容

        val bitmapIcon = UiHelper.getSmallIconBitmap(R.drawable.flag, this)
        val custom = BitmapDescriptorFactory.fromBitmap(bitmapIcon)
        options.icon(custom)

        val marker = tencentMap!!.addMarker(options)
        marker.tag = "fav"
        val user = CurrentUser.getUser()
        var loc = FavLocation(DateTimeHelper.getTimestamp(),
            user!!.uid, user!!.nickName, user!!.icon, latLng.latitude,
            latLng.longitude, 0.0,
            DateTimeHelper.getTimestamp(),
            DateTimeHelper.getTimeStampLongString(),
            "",
            ""
        )
        favMarkers.put(marker, loc)
        GlobalData.addFavLocation(this, loc)

        //开启信息窗口
        marker.isInfoWindowEnable = true
        marker.isDraggable = true
        //marker.title = ""
        //marker.position = latLng
        //marker.showInfoWindow()
    }

    // 移动镜头
    public  fun moveCamera(friend: Friend?){
        var lat = 39.908710
        var lon = 116.397499
        var pos  = LatLng(lat, lon)
        if (friend != null){
            if (friend.uid == CurrentUser.getUser()!!.uid)
            {
                lat = GlobalData.currentTLocation!!.latitude
                lon = GlobalData.currentTLocation!!.longitude
                mMarker.position = LatLng(lat, lon)
            }else
            {
                if (!friend.isShare ){
                    return // 没有位置信息的，不应该切换镜头
                }
                lat = friend.lat
                lon = friend.lon
            }
        }
        else if (GlobalData.currentTLocation != null)
        {
            lat = GlobalData.currentTLocation!!.latitude
            lon = GlobalData.currentTLocation!!.longitude

        }else{
            // 未知自己位置信息
            return
        }

        pos = LatLng(lat, lon)

        // 如果已经放大了，则不调整
        var level = tencentMap!!.cameraPosition.zoom
        if (level < GlobalData.defaultZoomLevel)
        {
            level = GlobalData.defaultZoomLevel
        }

        val cameraSigma = CameraUpdateFactory.newCameraPosition(
            CameraPosition(
                pos,  //中心点坐标，地图目标经纬度
                level,  //目标缩放级别
                0f,  //目标倾斜角[0.0 ~ 45.0] (垂直地图时为0)
                0f
            )
        ) //目标旋转角 0~360° (正北方为0)
        tencentMap!!.moveCamera(cameraSigma) //移动地图
    }

    fun moveCamera(lat:Double, lon:Double){
        val pos = LatLng(lat, lon)

        // 如果已经放大了，则不调整
        var level = tencentMap!!.cameraPosition.zoom
        if (level < GlobalData.defaultZoomLevel)
        {
            level = GlobalData.defaultZoomLevel
        }

        val cameraSigma = CameraUpdateFactory.newCameraPosition(
            CameraPosition(
                pos,  //中心点坐标，地图目标经纬度
                level,  //目标缩放级别
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

        // 首次视野
        moveCamera(lat, lon)

        val position = LatLng(lat, lon)
        val options = MarkerOptions(position)
        options.infoWindowEnable(true) //默认为true
        options.title(title) //标注的InfoWindow的标题
        //options.snippet("地址: 北京市东城区东长安街") //标注的InfoWindow的内容

        val bitmapIcon = UiHelper.getSmallIconBitmap(CurrentUser.getUser()!!.icon, this)
        val custom = BitmapDescriptorFactory.fromBitmap(bitmapIcon)
        options.icon(custom)

        this.mMarker = tencentMap!!.addMarker(options)
        mMarker.tag = "me"
        //开启信息窗口
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

            mMarker.position = pos
            mMarker.title = title
            mMarker.snippet = ""  //"${pos.latitude}, ${pos.longitude}"
            mMarker.isInfoWindowEnable = true
            mMarker.refreshInfoWindow()

            // 获得信号后，如果是跟随模式
            if (isInitedInfo && isFollowing && (!isInView(lat, lon))){
                moveCamera(lat, lon)
            }

        }else{
            if (GlobalData.isLocationEnabled)
                mMarker.snippet = "等待定位信号"
            else
                mMarker.snippet= "请打开定位权限"
        }
    }

    // 要判断的目标经纬度
    private fun isInView(targetLat: Double, targetLng :Double):Boolean{
        // 获取地图当前显示范围的经纬度坐标
        val visibleBounds: LatLngBounds = tencentMap!!.projection.visibleRegion.latLngBounds
        val southwest: LatLng = visibleBounds.southwest // 左下角经纬度
        val northeast: LatLng = visibleBounds.northeast // 右上角经纬度


        // 判断目标经纬度是否在当前显示范围内
        if (targetLat in southwest.latitude..northeast.latitude &&
            targetLng in southwest.longitude..northeast.longitude) {
            // 目标经纬度在当前地图显示范围内
            return true
        }

        // 目标经纬度不在当前地图显示范围内
        return false
    }

    // 更新的入口函数
    protected  fun updateViews(){
        updateMyMarker()   // 更新我自己的位置
        // 先更新markers，在f中标记了部分内容，然后

        // 记录轨迹时候显示
        updateWayPoint()

        // 拷贝一份目前的好友位置列表
        val mapFriend = HttpWorker.get().getLastPointMap()
        updateAllMarkers(mapFriend)    // 更新后，f.msg中含有错误消息
        updateBottomView(mapFriend) // 更新底部状态条图标
        updateFriendInfo()  // 当前用户的基本信息

        // 首测获得自己的位置，需要初始化我的信息栏，并移动镜头
        if (!isInitedInfo && GlobalData.currentTLocation != null){

            var friend = Friend()
            friend.fromUser(CurrentUser.getUser()!!)
            onImageIconClick(friend)
            isInitedInfo = true
        }
        return
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
            PermissionHelper.PERMISSION_REQUEST_CODE_STORAGE ->{
                GlobalData.isFileReadWriteEnabaled  = grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED

                if (GlobalData.isFileReadWriteEnabaled){
                    initFiles()
                }else{
                    UiHelper.showCenterMessage(this, "未授权文件读写则无法记录轨迹数据")
                }
            }
            // 处理其他权限请求结果...
        }

        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }


    // 启动位置服务和HTTPworker
    fun startTencentLocaionService(){
        // 这里检查申请权限的结果
        if (GlobalData.isLocationBackgroudEnabled){
            //TencentLocService.instance!!.startBackGround(this)
            //bindLocation()
            startLocation()
        }else if (GlobalData.isLocationEnabled){
            //TencentLocService.instance!!.startBackGround(this)
            //bindLocation()
            startLocation()
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
        GlobalData.shouldRefresh = true
        handler.postDelayed(periodicLocationRequestRunnable, 10)
    }

    fun stopRefreshInfo() {
        GlobalData.shouldRefresh = false
        handler.removeCallbacks(periodicLocationRequestRunnable)
    }

    // 轮询的函数
    private val periodicLocationRequestRunnable: Runnable = object : Runnable {
        override fun run() {
            // 更新好友信息，刷新
            updateViews()
            handler.postDelayed(this, GlobalData.intervalOfRefresh)
        }
    }

    // 如果个人信息部分编辑了头像，这里需要更改
    private fun updateAvarta(){
        val user = CurrentUser.getUser()
        if (user!!.isChanged){
            val bitmapIcon = UiHelper.getSmallIconBitmap(CurrentUser.getUser()!!.icon, this)
            val custom = BitmapDescriptorFactory.fromBitmap(bitmapIcon)
            this.mMarker.setIcon(custom)

            // 获取底部列表
            val linearLayout = this.findViewById<LinearLayout>(R.id.linear_layout_icons)
            // 先检查是否有不需要显示的, 不再朋友列表中，通信簿设置后说明不需要显示了

            if (this.mImageView != null){
                val icon = CurrentUser.getUser()!!.icon
                val id = UiHelper.getIconResId(icon)
                this.mImageView.setImageResource(id)
            }


            user!!.isChanged = false
        }
    }



    /**
     * mapview的生命周期管理
     */
    override fun onStart() {
        super.onStart()
        mapView!!.onStart()
    }

    // Activity 完全可见并处于前台时，系统会调用 onResume() 方法
    override fun onResume() {
        super.onResume()
        initToolBar()
        mapView!!.onResume()

        // 在恢复时重新启动任务
        startRefreshInfo()

        // 查看头像是否更改
        updateAvarta()

        // 更新录制按钮
        updateRecordButton()

        // 更新需要显示的轨迹
        updateAllStartedTracks()

        // 检查是否中断了，这里是一个补救
        var tm = DateTimeHelper.getTimestamp()
        // 如果比预设值超过了30秒，还没有数据，那么后台可能出现问题了
//        if (isInitedInfo){
//            var delta = tm - GlobalData.currentTm
//            if (delta > (GlobalData.intervalOfLocation + 30000) ){
//                restartService()
//            }
//        }
    }

    // 当一个 Activity 即将失去焦点并进入后台时，系统会调用 onPause() 方法。
    override fun onPause() {
        super.onPause()
        mapView!!.onPause()
        // 在暂停时停止任务
        stopRefreshInfo()
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


///////////////////////////////////////////////////////////////////////////////
    private fun startLocation(){
        if (TencentLocService.instance == null)
        {
            val intent = Intent(this, TencentLocService::class.java)
            //intent.putExtra("command", "start"); // 通过Intent传递命令
            intent.action = Keys.ACTION_INIT

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                this.startForegroundService(intent);
            } else {
                this.startService(intent);
            }
        }
    }

    // 如果没有数据了，重启一下服务
    private fun checkLocationService(){
        UiHelper.showMessage(this, "没有数据，尝试重启服务")

        val intent = Intent(this, TencentLocService::class.java)
        //intent.putExtra("command", "start"); // 通过Intent传递命令
        intent.action = Keys.ACTION_INIT

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            this.startForegroundService(intent);
        } else {
            this.startService(intent);
        }

    }

    // 更新当前的轨迹线
    private fun updateWayPoint(){
        if (!GlobalData.isRecording ){
            return
        }

        // 先检查是否是同一线路，如果不是需要清空
        val id = GlobalData.curTrack.getTrackId().toString()
        // 开始了一个新的轨迹
        if (id != this.trackId){
                this.trackId = id
                if (this.wayLine!= null)
                {
                    this.wayLine!!.remove()
                }

                this.wayLine = null
                this.latLngs.clear()
        }


        GlobalData.copyWaypoints(latLngs)

        // 新绘制折线，或者添加一个点
        if (this.wayLine == null){

            if (latLngs.size > 1)
            {
                val polylineOptions = PolylineOptions()
                    .addAll(latLngs) // 折线设置圆形线头
                    .lineCap(true) //.lineType(PolylineOptions.LineType.LINE_TYPE_DOTTEDLINE)
                    // 纹理颜色
                    .color(PolylineOptions.Colors.GRAYBLUE)
                    .width(20f)
                this.wayLine = tencentMap!!.addPolyline(polylineOptions)
            }

        } else
        {
            val index = this.wayLine!!.points.size
            for (i in index until latLngs.size){
                this.wayLine!!.appendPoint(latLngs[i])
            }

        }
        return
    }

    fun moveCamera(points :MutableList<LatLng>){
        if (points == null)
            return

        if (points.size < 2)
            return

        tencentMap!!.moveCamera(
            CameraUpdateFactory.newLatLngBounds(
                LatLngBounds.Builder()
                    .include(points).build(),
                100
            )
        )
    }

    fun test_addLine() {
        // 构造折线点串
        val latLngs: MutableList<LatLng> = ArrayList()
        latLngs.add(LatLng(39.984864, 116.305756))
        latLngs.add(LatLng(39.983618, 116.305848))
        latLngs.add(LatLng(39.982347, 116.305966))
        latLngs.add(LatLng(39.982412, 116.308111))
        latLngs.add(LatLng(39.984122, 116.308224))
        latLngs.add(LatLng(39.984955, 116.308099))

//        // 构造 PolylineOpitons
//        PolylineOptions polylineOptions = new PolylineOptions()
//                .addAll(latLngs)
//                // 折线设置圆形线头
//                .lineCap(true)
//                // 折线的颜色为绿色
//                .color(0xff00ff00)
//                // 折线宽度为25像素
//                .width(15)
//                // 还可以添加描边颜色
//                //.borderColor(0xffff0000)
//                // 描边颜色的宽度，线宽还是 25 像素，不过填充的部分宽度为 `width` - 2 * `borderWidth`
//                .borderWidth(5);

        // 构造 PolylineOpitons
        val polylineOptions = PolylineOptions()
            .addAll(latLngs) // 折线设置圆形线头
            .lineCap(true) //.lineType(PolylineOptions.LineType.LINE_TYPE_DOTTEDLINE)
            // 纹理颜色
            .color(PolylineOptions.Colors.GRAYBLUE)
            .width(25f)

// 绘制折线
        val polyline = tencentMap!!.addPolyline(polylineOptions)

// 将地图视野移动到折线所在区域(指定西南坐标和东北坐标)，设置四周填充的像素
        tencentMap!!.moveCamera(
            CameraUpdateFactory.newLatLngBounds(
                LatLngBounds.Builder()
                    .include(latLngs).build(),
                100
            )
        )
    }

//    private var isBoundLocation: Boolean = false
//    private  var trackerService: TencentLocService? = null  // 为了保证随时上报信息


//    private val connection = object : ServiceConnection {
//        override fun onServiceConnected(className: ComponentName, serviceBinder: IBinder) {
//            isBoundLocation = true
//            // 获取一个引用
//            val binder = serviceBinder as TencentLocService.LocalBinder
//            trackerService = binder.service
//            trackerService!!.startBackGround(this@TencentMapActivity)
//
//        }
//        override fun onServiceDisconnected(arg0: ComponentName) {
//            // 服务崩溃或者被系统杀掉之后
//            //handleServiceUnbind()
//            isBoundLocation = false
//            trackerService = null
//        }
//    }
//
//    // 连接方式启动
//    private fun bindLocation(){
//        val intent = Intent(this, TencentLocService::class.java)
//        bindService(intent, connection, AppCompatActivity.BIND_AUTO_CREATE);
//    }

    // 注意需要在manifest中注册，否则无法启动



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