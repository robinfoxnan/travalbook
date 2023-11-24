package com.bird2fish.travelbook.helper


import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import android.location.Location
import android.location.LocationManager
import androidx.core.content.edit
import androidx.preference.PreferenceManager
import com.bird2fish.travelbook.core.*
import com.bird2fish.travelbook.ui.data.model.LoggedInUser


/* Puts a Double value in SharedPreferences */
fun SharedPreferences.Editor.putDouble(key: String, double: Double) = putLong(key, java.lang.Double.doubleToRawLongBits(double))


/* gets a Double value from SharedPreferences */
fun SharedPreferences.getDouble(key: String, default: Double) = java.lang.Double.longBitsToDouble(getLong(key, java.lang.Double.doubleToRawLongBits(default)))

/*
 * PreferencesHelper object
 */
object PreferencesHelper {

    /* Define log tag */
    private val TAG: String = LogHelper.makeLogTag(PreferencesHelper::class.java)

    /* The sharedPreferences object to be initialized */
    private lateinit var sharedPreferences: SharedPreferences


    /* Initialize a single sharedPreferences object when the app is launched */
    fun Context.initPreferences() {
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
    }

    fun init(context: Context){
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
    }


    /* Loads zoom level of map */
    fun loadZoomLevel(): Double {
        // load zoom level
        return sharedPreferences.getDouble(Keys.PREF_MAP_ZOOM_LEVEL, Keys.DEFAULT_ZOOM_LEVEL)
    }


    /* Saves zoom level of map */
    fun saveZoomLevel(zoomLevel: Double) {
        // save zoom level
        sharedPreferences.edit { putDouble(Keys.PREF_MAP_ZOOM_LEVEL, zoomLevel) }
    }


    /* Loads tracking state */
    fun loadTrackingState(): Int {
        // load tracking state
        return sharedPreferences.getInt(Keys.PREF_TRACKING_STATE, Keys.STATE_TRACKING_NOT)
    }


    /* Saves tracking state */
    fun saveTrackingState(trackingState: Int) {
        // save tracking state
        sharedPreferences.edit { putInt(Keys.PREF_TRACKING_STATE, trackingState) }
    }


    /* Loads length unit system - metric or imperial */
    fun loadUseImperialUnits(): Boolean {
        // load length unit system
        return sharedPreferences.getBoolean(Keys.PREF_USE_IMPERIAL_UNITS, LengthUnitHelper.useImperialUnits())
    }


    /* Loads length unit system - metric or imperial */
    fun loadGpsOnly(): Boolean {
        // load length unit system
        return sharedPreferences.getBoolean(Keys.PREF_GPS_ONLY, false)
    }

//    /* Loads accuracy threshold used to determine if location is good enough */
//    fun loadAccuracyThreshold(): Int {
//        // load tracking state
//        return sharedPreferences.getInt(Keys.PREF_LOCATION_ACCURACY_THRESHOLD, Keys.DEFAULT_THRESHOLD_LOCATION_ACCURACY)
//    }



//    /* Loads state of recording accuracy */
//    fun loadRecordingAccuracyHigh(): Boolean {
//        // load current setting
//        return sharedPreferences.getBoolean(Keys.PREF_RECORDING_ACCURACY_HIGH, false)
//    }


    /* Loads current accuracy multiplier */
    fun loadAccuracyMultiplier(): Int {
        // load current setting
        val recordingAccuracyHigh: Boolean = sharedPreferences.getBoolean(Keys.PREF_RECORDING_ACCURACY_HIGH, false)
        // return multiplier based on state
        return if (recordingAccuracyHigh) 2 else 1
    }


//    /* Load altitude smoothing value */
//    fun loadAltitudeSmoothingValue(): Int {
//        // load current setting
//        return sharedPreferences.getInt(Keys.PREF_ALTITUDE_SMOOTHING_VALUE, Keys.DEFAULT_ALTITUDE_SMOOTHING_VALUE)
//    }


    /* Loads the state of a map */
    fun loadCurrentBestLocation(): Location {
        val provider: String = sharedPreferences.getString(Keys.PREF_CURRENT_BEST_LOCATION_PROVIDER, LocationManager.NETWORK_PROVIDER) ?: LocationManager.NETWORK_PROVIDER
        // create location
        return Location(provider).apply {
            // load location attributes
            latitude = sharedPreferences.getDouble(Keys.PREF_CURRENT_BEST_LOCATION_LATITUDE, Keys.DEFAULT_LATITUDE)
            longitude = sharedPreferences.getDouble(Keys.PREF_CURRENT_BEST_LOCATION_LONGITUDE, Keys.DEFAULT_LONGITUDE)
            accuracy = sharedPreferences.getFloat(Keys.PREF_CURRENT_BEST_LOCATION_ACCURACY, Keys.DEFAULT_ACCURACY)
            altitude = sharedPreferences.getDouble(Keys.PREF_CURRENT_BEST_LOCATION_ALTITUDE, Keys.DEFAULT_ALTITUDE)
            time = sharedPreferences.getLong(Keys.PREF_CURRENT_BEST_LOCATION_TIME, Keys.DEFAULT_TIME)
        }

    }


    /* Saves the state of a map */
    fun saveCurrentBestLocation(currentBestLocation: Location) {
        sharedPreferences.edit {
            // save location
            putDouble(Keys.PREF_CURRENT_BEST_LOCATION_LATITUDE, currentBestLocation.latitude)
            putDouble(Keys.PREF_CURRENT_BEST_LOCATION_LONGITUDE, currentBestLocation.longitude)
            putFloat(Keys.PREF_CURRENT_BEST_LOCATION_ACCURACY, currentBestLocation.accuracy)
            putDouble(Keys.PREF_CURRENT_BEST_LOCATION_ALTITUDE, currentBestLocation.altitude)
            putLong(Keys.PREF_CURRENT_BEST_LOCATION_TIME, currentBestLocation.time)
        }
    }


    /* Load currently selected app theme */
    fun loadThemeSelection(): String {
        return sharedPreferences.getString(Keys.PREF_THEME_SELECTION, Keys.STATE_THEME_FOLLOW_SYSTEM) ?: Keys.STATE_THEME_FOLLOW_SYSTEM
    }


    /* Checks if housekeeping work needs to be done - used usually in DownloadWorker "REQUEST_UPDATE_COLLECTION" */
    fun isHouseKeepingNecessary(): Boolean {
        return sharedPreferences.getBoolean(Keys.PREF_ONE_TIME_HOUSEKEEPING_NECESSARY, true)
    }


    /* Saves state of housekeeping */
    fun saveHouseKeepingNecessaryState(state: Boolean = false) {
        sharedPreferences.edit { putBoolean(Keys.PREF_ONE_TIME_HOUSEKEEPING_NECESSARY, state) }

    }

    /* Start watching for changes in shared preferences - context must implement OnSharedPreferenceChangeListener */
    fun registerPreferenceChangeListener(listener: SharedPreferences.OnSharedPreferenceChangeListener) {
        sharedPreferences.registerOnSharedPreferenceChangeListener(listener)
    }

    /* Stop watching for changes in shared preferences - context must implement OnSharedPreferenceChangeListener */
    fun unregisterPreferenceChangeListener(listener: SharedPreferences.OnSharedPreferenceChangeListener) {
        sharedPreferences.unregisterOnSharedPreferenceChangeListener(listener)
    }

    // 保存登录用户的基本信息
     fun saveUserInfo(name :String, pwd:String, id: String, sid:String) {
        val editor = sharedPreferences.edit()
        //得到Editor后，写入需要保存的数据
        editor.putString("username", name)
        editor.putString("pwd", pwd)
        editor.putString("uid", id)
        editor.putString("sid", sid)
        editor.commit() //提交修改
        //Log.i(TAG, "保存用户信息成功")
    }

    // 检查当前存储的基本信息
    fun getUserInfo() : LoggedInUser?{
        var username = sharedPreferences.getString("username", "")
        var pwd = sharedPreferences.getString("pwd", "")
        var uid = sharedPreferences.getString("uid", "")
        var sid = sharedPreferences.getString("sid", "")
        if (username == null || pwd == null || uid == null || sid ==null){
            return null
        }
        val fakeUser = LoggedInUser(username, pwd, "", uid, sid)
        return fakeUser
    }
    fun delUserSid(){
        val editor = sharedPreferences.edit()
        editor.putString("sid", "")
        editor.commit() //提交修改
    }

    fun getHostName() : String{
        var host= sharedPreferences.getString("hostname", "8.140.203.92:7817")
        return host!!
    }

    fun getHostSchema():String{
        var schema = sharedPreferences.getString("hostschema", "https")
        return schema!!
    }

    fun setHost(host:String, schema:String){
        val editor = sharedPreferences.edit()
        //得到Editor后，写入需要保存的数据
        editor.putString("hostname", host)
        editor.putString("hostschema", schema)

        editor.commit() //提交修改
    }
    /////////////////////////////////////////////////////////////////////////////////////////////
    // 徒步30秒
    fun getModeHikePosInterval() :Long {
        var t = sharedPreferences.getLong("hikePosInterval", 30000)
        if (t < 1000){
            return 1000
        }
        return t
    }

    fun setModeHikePosInterval(t :Long){
        val editor = sharedPreferences.edit()
        editor.putLong("hikePosInterval", t)
        editor.commit() //提交修改
    }

    // 跑步10秒
    fun getModeRunPosInterval() :Long {
        var t = sharedPreferences.getLong("runPosInterval", 10000)
        if (t < 1000){
            return 1000
        }
        return t
    }

    fun setModeRunPosLongerval(t :Long){
        val editor = sharedPreferences.edit()
        editor.putLong("runPosInterval", t)
        editor.commit() //提交修改
    }


    // 自行车7秒
    fun getModeBikePosInterval() :Long {
        var t = sharedPreferences.getLong("bikePosInterval", 7000)
        if (t < 1000){
            return 1000
        }
        return t
    }

    fun setModeBikePosInterval(t :Long){
        val editor = sharedPreferences.edit()
        editor.putLong("bikePosInterval", t)
        editor.commit() //提交修改
    }

    // 摩托车5秒
    fun getModeMotorPosInterval() :Long {
        var t = sharedPreferences.getLong("motorPosInterval", 5000)
        if (t < 1000){
            return 1000
        }
        return t
    }

    fun setModeMotorPosInterval(t :Long){
        val editor = sharedPreferences.edit()
        editor.putLong("motorPosInterval", t)
        editor.commit() //提交修改
    }

    // 汽车5秒
    fun getModeCarPosInterval() :Long {
        var t = sharedPreferences.getLong("carPosInterval", 5000)
        if (t < 1000){
            return 1000
        }
        return t
    }

    fun setModeCarPosInterval(t :Long){
        val editor = sharedPreferences.edit()
        editor.putLong("carPosInterval", t)
        editor.commit() //提交修改
    }

    // 节能模式
    fun getModeLasyPosInterval() :Long {
        var t = sharedPreferences.getLong("lasyPosInterval", 60000)
        if (t < 1000){
            return 1000
        }
        return t
    }

    fun setModeLasyPosInterval(t :Long){
        val editor = sharedPreferences.edit()
        editor.putLong("lasyPosInterval", t)
        editor.commit() //提交修改
    }
/////////////////////////////////////////////////////////////////////////////
    // 运行模式
    fun setSportMode(t :String) {
        val editor = sharedPreferences.edit()
        editor.putString("sportMode", t)
        editor.commit() //提交修改
    }

    fun getSportMode(): String{
        var t = sharedPreferences.getString("sportMode", "")

        return t!!
    }

    fun setCurrentPosInterval(t :Long){
        val editor = sharedPreferences.edit()
        editor.putLong("currentModeInterval", t)
        editor.commit() //提交修改
    }

    fun getCurrentPosInterval(): Long{
        var t = sharedPreferences.getLong("currentModeInterval", 3000)
        if (t < 1000){
            t = 1000
        }
        return t
    }

}
