package com.bird2fish.travelbook.core

import android.app.*
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.BitmapFactory
import android.graphics.Color
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.net.ConnectivityManager
import android.os.*
import androidx.core.content.ContextCompat.getSystemService
import com.bird2fish.travelbook.R
import com.bird2fish.travelbook.helper.LogHelper
import com.bird2fish.travelbook.helper.NotificationActionReceiver
import com.bird2fish.travelbook.helper.NotificationHelper
import com.tencent.map.geolocation.TencentLocation
import com.tencent.map.geolocation.TencentLocationListener
import com.tencent.map.geolocation.TencentLocationManager
import com.tencent.map.geolocation.TencentLocationRequest


class TencentLocService : TencentLocationListener, Service() , SensorEventListener {
    private val TAG: String = LogHelper.makeLogTag(TencentLocService::class.java)

    private var locationManager: TencentLocationManager? = null    // 腾讯给的定位的服务

    private val networkChangeReceiver = NetworkChangeReceiver()   // 网络通信方式改变造成定位信号丢失


    private lateinit var notificationManager: NotificationManager   // 系统的通知管理器
    private lateinit var notificationHelper: NotificationHelper     // 辅助工具，不停的更新那个通知
    private lateinit var sensorManager: SensorManager  // 步数传感器
    private var stepCountOffset :Float = 0f   // 与传感器的偏差，首次需要设置
    private var stepCount:Float =  0f         // 当前步数计算得来的

    private var trackingState :Int  = Keys.STATE_TRACKING_NOT
    private var trackLength:Float = 0f    // 距离长
    private var trackDuration:Long = 0         // 时长

    private val binder = LocalBinder()                 // 用于绑定，返回给客户端的绑定引用

    private val handler: Handler = Handler(Looper.getMainLooper())  // 自己单次调用，而不是持续调用

    // 单例模式
    companion object {
        var instance: TencentLocService? = null
            get() {
//                if (field == null) {
//                    // 初始化权限，以及用户同意隐私协议
//                    field = TencentLocService()
//                }
                return field
            }
    }

    private fun buildNotification(): Notification{

            val notification: Notification = notificationHelper.createNotification(
                trackingState,
                this.trackLength,
                this.trackDuration,
                false
            )
            //notificationManager.notify(Keys.TRACKER_SERVICE_NOTIFICATION_ID, notification)
            return notification

    }

    // 在你的 Activity 或 Fragment 中初始化定位服务
    fun startBackGround(context: Context): Boolean {
        val mContext = this.applicationContext  // 使用服务的

        locationManager = TencentLocationManager.getInstance(mContext)
        locationManager!!.enableForegroundLocation(Keys.TRACKER_SERVICE_NOTIFICATION_ID, buildNotification())
        val locationRequest = createReq()
        val error =
            locationManager!!.requestLocationUpdates(locationRequest, this, context.mainLooper)
        return isOk(context, error)
    }

    private fun isOk(context:Context, error: Int): Boolean {
        if (error == 0) {
            System.out.printf("成功\n")
            //UiHelper().showCenterMessage(context, "");
            return true
        } else if (error == 4) {  // 先设置同意隐私
            // 定位请求失败
            System.out.printf("失败， 应先同意隐私\n")
            //Toast.makeText(this, "fail", LENGTH_SHORT).show();
            UiHelper.showCenterMessage(context, "应先同意隐私");
            return false
        } else if (error == 2) {  // 2key不对
            UiHelper.showCenterMessage(context, "应去腾讯注册并检查KEY是否正确");
            return false
        }
        return false
    }

    fun stopBackGround() {
        if (locationManager != null) {
            locationManager!!.removeUpdates(this)
            locationManager!!.disableForegroundLocation(true)
        }
    }

    private fun createReq(): TencentLocationRequest {
        // 配置定位请求
        val locationRequest = TencentLocationRequest.create()
        locationRequest.interval = GlobalData.intervalOfLocation // 定位间隔，单位毫秒
        locationRequest.isAllowGPS = true
        //是否需要获取传感器方向
        locationRequest.isAllowDirection = true
        //是否需要开启室内定位
        locationRequest.isIndoorLocationMode = true
        // 设置定位模式
        val locMode = TencentLocationRequest.HIGH_ACCURACY_MODE
        locationRequest.locMode = locMode
        locationRequest.requestLevel = TencentLocationRequest.REQUEST_LEVEL_ADMIN_AREA // 设置定位精度等级
        // 设置GPS优先
        locationRequest.isGpsFirst = true
        return locationRequest
    }

    fun startLocationService(context: Context): Boolean {
        // 创建 TencentLocationManager
        val mContext = context.applicationContext
        locationManager = TencentLocationManager.getInstance(mContext)
        val locationRequest = createReq()
        // 启动定位
        val error = locationManager!!.requestLocationUpdates(locationRequest, this)
        return isOk(context, error)
    }

    fun stopLocationService() {
        if (locationManager != null) {
            locationManager!!.removeUpdates(this)
        }
    }

    fun getOneTimeLoacation(context: Context): Boolean{
        val mContext = context.applicationContext
        locationManager = TencentLocationManager.getInstance(mContext)
        val locationRequest = createReq()
        val error = locationManager!!.requestSingleFreshLocation(locationRequest, this, Looper.getMainLooper());
        return isOk(context, error)
    }

    // 启动一个循环，自己的定制任务
    fun startLocationLoop(){
        handler.postDelayed(periodicTrackUpdate, 0)
    }

    // TODO: 需要对长期不动的情况进行优化，降低采集频率
    private val periodicTrackUpdate: Runnable = object : Runnable {
        override fun run() {

            getOneTimeLoacation(this@TencentLocService)
            //buildNotification()
            // 下一次定时
            handler.postDelayed(this, GlobalData.intervalOfLocation)
        }
    }

    // 启动腾讯的服务
    // 这里做了一个优化，当定位频率小于3秒，使用持续定位，如果频率低，则使用单次定位降低电池消耗。
    fun restartLocationService(){
        stopBackGround()
        if (GlobalData.intervalOfLocation <= 5000){
            startBackGround(this)
        }else{
            startLocationLoop()
        }

        //startLocationService(this)

    }

    /////////////////////////////////////////////////////////////////////////////////////
    //
    override fun onLocationChanged(tencentLocation: TencentLocation, error: Int, s: String) {
        if (error == TencentLocation.ERROR_OK) {
            // 获取经纬度信息
            val latitude = tencentLocation.latitude
            val longitude = tencentLocation.longitude
            //System.out.printf("%f, %f \n", latitude, longitude)

            GlobalData.setCurrentLocation(tencentLocation)
            // 处理获取到的经纬度
        } else {
            // 定位失败，处理失败信息
            System.out.printf("err = %d\n", error)
            checkUpdateErr(error)
        }
    }

    private fun checkUpdateErr(error: Int): Boolean{
        when(error)
        {
            0->{
                return true
            }
            1->{
                LogHelper.d("TencentLocService", "net work err")
                return false
            }
            2->{
                LogHelper.d("TencentLocService", "用户没有给权限，或者没有信号")
                return false
            }
            4->{
                LogHelper.d("TencentLocService", "坐标转换失败")
                return false
            }
        }
        return false
    }

    override fun onStatusUpdate(s: String, i: Int, s1: String) {}




    ///////////////////////////////////////////////////////
    // 服务部分继承得来的
    inner class LocalBinder : Binder() {
        val service: TencentLocService = this@TencentLocService
    }

    override fun onCreate() {
        super.onCreate()

        // 设置单例模式
        TencentLocService.instance = this

        TencentLocationManager.setUserAgreePrivacy(true)

        // 注册广播接收器
        //val filter = IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION)
        //registerReceiver(networkChangeReceiver, filter)
        // 注册广播接收者
        registerReceiver(networkChangeReceiver, IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION))

        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationHelper = NotificationHelper(this)
        sensorManager = this.getSystemService(Context.SENSOR_SERVICE) as SensorManager    // 计步器


        startForeground(Keys.TRACKER_SERVICE_NOTIFICATION_ID, buildNotification());

    }

    override fun onDestroy() {
        // 取消注册广播接收器，以避免内存泄漏
        TencentLocService.instance = null
        unregisterReceiver(networkChangeReceiver)
        super.onDestroy()
        System.out.println("服务退出了")
        LogHelper.d("TencentLocService", "服务退出了")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        // SERVICE RESTART (via START_STICKY)
        // 可以通过检查 intent 是否为 null 来判断是否是重新启动的
        if (intent == null)
        {
            restartLocationService()


            if (trackingState == Keys.STATE_TRACKING_ACTIVE) {
                LogHelper.w(TAG, "重启动服务.")

            }
        }
        else if (Keys.ACTION_INIT == intent.action)
        {
            // 启动服务的时候，先启动服务，
            restartLocationService()
        }
        else if (Keys.ACTION_STOP == intent.action)
        {
            stopTrack()
        }
        else if (Keys.ACTION_START == intent.action)
        {
            startTrack()

        }
        else if (Keys.ACTION_RESUME == intent.action)
        {
            resumeTrack()
        }

        //还可以其他的方式获取命令
        //val command = intent!!.getStringExtra("command");



        // 具体来说，START_STICKY 表示如果服务进程被异常终止（例如由于内存不足），
        // 系统会尝试重新创建服务并调用 onStartCommand 方法，
        // 但不会重新传递最后一次的 Intent。
        // 服务会被重新启动，但 Intent 对象将为 null。
        return START_STICKY
    }

    override fun onBind(intent: Intent): IBinder? {
        return binder
    }
    /* Overrides onRebind from Service */
    override fun onRebind(intent: Intent?) {

    }

    /* Overrides onUnbind from Service */
    override fun onUnbind(intent: Intent?): Boolean {
        return true
    }

    /////////////////////////////////////////////////////////////////////////////
    // 步数传感器部分
    private fun startStepCounter() {
        val stepCounterAvailable = sensorManager.registerListener(this,
            sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER),
            SensorManager.SENSOR_DELAY_UI)

        if (!stepCounterAvailable) {
            LogHelper.w(TAG, "Pedometer sensor not available.")
            this.stepCount = -1f
        }
    }

    // 计算传感器的消息函数1
    override fun onSensorChanged(sensorEvent: SensorEvent?) {
        var steps: Float = 0f
        if (sensorEvent != null) {
            if (stepCountOffset == 0f) {
                // store steps previously recorded by the system
                stepCountOffset = (sensorEvent.values[0] - 1) - this.stepCount // subtract any steps recorded during this session in case the app was killed
            }
            // calculate step count - subtract steps previously recorded
            steps = sensorEvent.values[0] - stepCountOffset
        }
        // update step count in track
        this.stepCount = steps
    }

    // 计算传感器的消息函数2
    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        TODO("Not yet implemented")
    }
    /////////////////////////////////////////////////////////////////////
    // 下面用来管理路程
    fun startTrack(){
        UiHelper.showMessage(this, "开始")
    }

    fun stopTrack(){
        UiHelper.showMessage(this, "停止")
    }

    fun resumeTrack(){
        UiHelper.showMessage(this, "继续")
    }

    // 通知
    fun noticeNetworkChange(){
        UiHelper.showMessage(this, "网络改变")
    }

}