package com.bird2fish.travelbook.helper

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.bird2fish.travelbook.core.GlobalData

object PermissionHelper {
    // 请求定位权限的请求代码
    val PERMISSION_REQUEST_CODE_LOCATION = 1
    val PERMISSION_REQUEST_CODE_BACKGROUND = 2
    val PERMISSION_REQUEST_CODE_BODY_SENSORS = 3
    val PERMISSION_REQUEST_CODE_RECOGNITION = 4
    val PERMISSION_REQUEST_CODE_STORAGE = 5
    // IN_VEHICLE：用户正在驾驶车辆。
    //ON_BICYCLE：用户正在骑自行车。
    //ON_FOOT：用户正在步行。
    //RUNNING：用户正在跑步。
    //STILL：用户静止不动。
    //TILTING：设备的方向处于倾斜状态。
    //UNKNOWN：无法识别用户的当前活动。
    /*
    activity需要重写：
     override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            REQUEST_ACTIVITY_RECOGNITION_PERMISSION -> {
                // 检查用户是否授予 ACTIVITY_RECOGNITION 权限
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // 用户已授予活动识别权限，可以开始使用活动识别
                    // 进行相关操作
                } else {
                    // 用户拒绝了活动识别权限，需要处理相应逻辑
                }
            }
            // 处理其他权限请求结果...
        }
    }
     */

    fun checkLoctionPermission(activity: Activity?):Boolean {
        val b1 = ContextCompat.checkSelfPermission(
                activity!!,
                Manifest.permission.ACCESS_BACKGROUND_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        var b2 = ContextCompat.checkSelfPermission(
            activity!!,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) != PackageManager.PERMISSION_GRANTED

        return b1 && b2
    }

    // 检查并请求定位权限，在activity的回调函数中去检查代码区分请求了哪个， 精准位置权限
    fun requestLocationPermission(activity: Activity?) :Boolean{
        //if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(
                    activity!!,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    activity,
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                    PERMISSION_REQUEST_CODE_LOCATION
                )
                return false
            }else{
                GlobalData.isLocationEnabled = true
                return true
            }
       // }
    }

    fun requestBackgroundPermission(activity: Activity):Boolean{
        if (ContextCompat.checkSelfPermission(
                activity,
                Manifest.permission.ACCESS_BACKGROUND_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // 如果权限未被授予，则请求权限
            ActivityCompat.requestPermissions(
                activity,
                arrayOf(Manifest.permission.ACCESS_BACKGROUND_LOCATION),
                PERMISSION_REQUEST_CODE_BACKGROUND
            )
            return false
        }
        else{
            GlobalData.isLocationBackgroudEnabled = true
            return true
        }
    }

    //生物传感器可以包括心率监测、步数计数、睡眠监测等传感器，具体取决于设备的硬件支持。
    fun requestBodyPermission(activity: Activity) :Boolean{
        // 检查是否已经授予身体动作权限

        // 检查是否已经授予身体动作权限
        if (ContextCompat.checkSelfPermission(
                activity,
                Manifest.permission.BODY_SENSORS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // 如果权限未被授予，则请求权限
            ActivityCompat.requestPermissions(
                activity,
                arrayOf(Manifest.permission.BODY_SENSORS),
                PERMISSION_REQUEST_CODE_BODY_SENSORS
            )
            return false
        }else{
            GlobalData.isBodySensorEnabled= true
            return true
        }


    }



    fun requestRecognition(activity: Activity):Boolean {
        // 检查是否具有 ACTIVITY_RECOGNITION 权限
        if (ContextCompat.checkSelfPermission(
                activity,
                Manifest.permission.ACTIVITY_RECOGNITION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            // 已经获取了 ACTIVITY_RECOGNITION 权限，可以开始使用活动识别
            GlobalData.isRecognitionEnabled = true
            return true
        } else {
            // 请求 ACTIVITY_RECOGNITION 权限
            ActivityCompat.requestPermissions(
                activity,
                arrayOf(Manifest.permission.ACTIVITY_RECOGNITION),
                PERMISSION_REQUEST_CODE_RECOGNITION
            )
            return false
        }
    }

    // 请求写外部文件系统
    fun requestFile(activity: Activity):Boolean{
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // 检查写入外部存储的权限
            if (ContextCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED
            ) {
                // 如果权限没有被授予，请求权限
                ActivityCompat.requestPermissions(
                    activity,
                    arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                    PERMISSION_REQUEST_CODE_STORAGE
                )
            } else {
                // 权限已经被授予，可以执行写入文件的操作
                return true
            }
        } else {
            // 在 Android 6.0 以下版本，权限默认已经被授予
            // 可以执行写入文件的操作
            // ...
            return true
        }
        return false
    }




    // 另一种方式
    private fun requestPermission(activity: ComponentActivity, code: Int){
        /* Register the permission launcher for requesting location 在onstart中使用  */
        val requestLocationPermissionLauncher = activity.registerForActivityResult(
            ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                // permission was granted - re-bind service
                //this.unbindService(connection)

            } else {
                // permission denied - unbind service

            }
            //layout.toggleLocationErrorBar(gpsProviderActive, networkProviderActive)
        }

    }


}