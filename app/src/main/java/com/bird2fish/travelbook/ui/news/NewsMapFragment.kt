package com.bird2fish.travelbook.ui.news

import android.graphics.Bitmap
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.bird2fish.travelbook.databinding.FragmentNewsMapBinding
import com.bird2fish.travelbook.R
import com.bird2fish.travelbook.core.*
import com.bird2fish.travelbook.helper.DateTimeHelper
import com.bird2fish.travelbook.helper.FileHelper
import com.bird2fish.travelbook.helper.UiUtils
import com.tencent.tencentmap.mapsdk.maps.CameraUpdateFactory
import com.tencent.tencentmap.mapsdk.maps.TencentMap
import com.tencent.tencentmap.mapsdk.maps.TencentMapOptions
import com.tencent.tencentmap.mapsdk.maps.TextureMapView
import com.tencent.tencentmap.mapsdk.maps.model.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class NewsMapFragment : Fragment() {

    private var _binding: FragmentNewsMapBinding? = null
    private val binding get() = _binding!!

    private var mapView: TextureMapView? = null
    protected var tencentMap: TencentMap? = null

    private var newsContent: News? = null
    private var track :Track? = null
    private var points :MutableList<LatLng> = ArrayList<LatLng>()
    private var trackLine: com.tencent.tencentmap.mapsdk.maps.model.Polyline? = null
    private var markerMap =  mutableMapOf<String, com.tencent.tencentmap.mapsdk.maps.model.Marker>()

    private var _changed : MutableLiveData<Long> = MutableLiveData(DateTimeHelper.getTimestamp())
    var changed : LiveData<Long> =  _changed

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {

        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        //return inflater.inflate(R.layout.fragment_news_map, container, false)

        _binding = FragmentNewsMapBinding.inflate(inflater, container, false)
        val root: View = binding.root


        initMap()
        initMapOutlook()

        this.changed.observe(requireActivity(), Observer {
            addTrackLine()
        })

        return root
    }

    private fun initMap(){
        // 取得LinearLayout 物件
        val ll = binding.layoutMapContainer

        val options = TencentMapOptions()
        options.isOfflineMapEnable = true

        mapView = TextureMapView(this.requireActivity(), options)
        mapView!!.isOpaque = false

        ll.addView(mapView)

        //创建tencentMap地图对象，可以完成对地图的几乎所有操作
        tencentMap = mapView!!.map
        initMapOutlook()
        tencentMap!!.isTrafficEnabled = false;
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

    private fun clear(){
        if (trackLine != null){
            trackLine!!.remove()
            trackLine = null
        }

        for (name in markerMap.keys){
            val m = markerMap[name]
            m!!.remove()
        }
        markerMap.clear()

        points.clear()
    }


    private fun AddMarker(lat: Double, lon :Double, opt:Int){

        var title = "标记点"
        if (opt ==0){
            title = "起点"
        }

        val position = LatLng(lat, lon)
        val options = MarkerOptions(position)
        options.infoWindowEnable(false) //默认为true
        options.title(title) //标注的InfoWindow的标题

        var id =R.drawable.mark_red
        if (opt == 0){
            id = R.drawable.start
        }else if (opt > 0){
            id = R.drawable.end
        }
        val bitmapIcon = UiUtils.getBitmapFromResource(this.requireActivity(), id)
        val bitMapIconSmall = Bitmap.createScaledBitmap(bitmapIcon, 100, 100, true);
        val custom = BitmapDescriptorFactory.fromBitmap(bitMapIconSmall)
        options.icon(custom)

        val marker = tencentMap!!.addMarker(options)
        marker.showInfoWindow()
        markerMap.put(opt.toString(), marker)
    }

    private fun moveCamera(lat: Double, lon :Double){
        val pos = LatLng(lat, lon)

        // 如果已经放大了，则不调整
        var level = 15.0f   //tencentMap!!.cameraPosition.zoom
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

    fun moveCamera(){
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

    // 添加一条轨迹
    private fun addTrackLine(){

        if (track == null)
            return
        if (track!!.wayPoints == null)
            return

        if (track!!.wayPoints.size < 1){
            UiHelper.showCenterMessage(this.requireActivity(),"轨迹中没有标记点，无法显示轨迹")
            return
        }

        // 设置Activity的标题
        //(activity as? AppCompatActivity)?.supportActionBar?.title = track!!.name
        // 标记起点
        AddMarker(track!!.wayPoints[0].latitude, track!!.wayPoints[0].longitude, 0)

        val index = track!!.wayPoints.size - 1
        AddMarker(track!!.wayPoints[index].latitude, track!!.wayPoints[index].longitude, index)

        if (track!!.wayPoints.size < 2){
            UiHelper.showCenterMessage(this.requireActivity(),"点少于2个，无法显示轨迹")
            return
        }

        this.points.clear()
        for (p in track!!.wayPoints){
            this.points.add(LatLng(p.latitude, p.longitude))
        }

        // 构造 PolylineOpitons
        val polylineOptions = PolylineOptions()
            .addAll(this.points) // 折线设置圆形线头
            .lineCap(true) //.lineType(PolylineOptions.LineType.LINE_TYPE_DOTTEDLINE)
            // 纹理颜色
            //.color()
            .color(PolylineOptions.Colors.RED)
            .width(15f)

        // 绘制折线
        this.trackLine = tencentMap!!.addPolyline(polylineOptions)
        //linesMap.put(t.name, polyline)


        moveCamera()

    }

    fun updateData(){
        clear()
        if (newsContent == null)
            return

        if (newsContent!!.type == "point"){

            // 设置Activity的标题

            AddMarker(newsContent!!.lat, newsContent!!.log, -1)
            moveCamera(newsContent!!.lat, newsContent!!.log)
        }else if (newsContent!!.type == "track"){
            // 异步加载路径
            CoroutineScope(Dispatchers.IO).launch {
                // 在后台执行异步任务
                val result = doGetTrackFile()
                _changed.postValue(DateTimeHelper.getTimestamp())
            }
        }else{  // localtrack
            this.track = FileHelper.readTrack(requireActivity(), newsContent!!.trackFile)
            _changed.postValue(DateTimeHelper.getTimestamp())
        }

        (activity as? AppCompatActivity)?.supportActionBar?.title = newsContent!!.title
    }

    // 下载文件到内存
    fun doGetTrackFile(){
        this.track = GlobalData.getHttpServ().getGpxFile(newsContent!!.trackFile)
    }

    override fun onResume() {
        super.onResume()
        mapView!!.onResume()

        // 获取传递的 Intent
        val arg = getArguments()
        if (arg != null) {

            val obj = arg!!.getParcelable<News>("news")
            newsContent = obj
            //UiHelper.showCenterMessage(requireActivity(), str!!)
            updateData()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mapView!!.onDestroy()
    }

    override fun onStart() {
        super.onStart()
        mapView!!.onStart()
    }

    override fun onStop() {
        super.onStop()
        mapView!!.onStop()
    }

    override fun onPause(){
        super.onPause()
        mapView!!.onPause()
    }



}