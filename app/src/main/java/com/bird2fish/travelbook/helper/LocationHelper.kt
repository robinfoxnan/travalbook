package  com.bird2fish.travelbook.helper

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.os.SystemClock
import androidx.core.content.ContextCompat
import com.bird2fish.travelbook.core.*
import java.util.*
import kotlin.math.*


/*
 * Keys object
 */
object LocationHelper {

    /* Define log tag */
    private val TAG: String = LogHelper.makeLogTag(LocationHelper::class.java)


    /* 默认的位置设置为北京 */
    fun getDefaultLocation(): Location {
        val defaultLocation: Location = Location(LocationManager.NETWORK_PROVIDER)
        defaultLocation.latitude = Keys.DEFAULT_LATITUDE
        defaultLocation.longitude = Keys.DEFAULT_LONGITUDE
        defaultLocation.accuracy = Keys.DEFAULT_ACCURACY
        defaultLocation.altitude = Keys.DEFAULT_ALTITUDE
        defaultLocation.time = Keys.DEFAULT_DATE.time
        return defaultLocation
    }

    // 计算两个经纬度坐标之间的球面距离通常可以使用大圆距离（Great Circle Distance）公式，也称为Haversine公式。
    fun haversine(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val R = 6371.0 // 地球半径（单位：千米）

        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)

        val a = sin(dLat / 2) * sin(dLat / 2) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                sin(dLon / 2) * sin(dLon / 2)

        val c = 2 * atan2(sqrt(a), sqrt(1 - a))

        return R * c
    }


    /* 检查当前位置时间是否超过了2分钟 */
    fun isOldLocation(location: Location): Boolean {
        // check how many milliseconds the given location is old
        return GregorianCalendar.getInstance().time.time - location.time > Keys.SIGNIFICANT_TIME_DIFFERENCE
    }


    /* 返回系统保存的最后一个位置点 */
    fun getLastKnownLocation(context: Context): Location {
        // 从配置中加载最后一个保存的位置
        var lastKnownLocation: Location = PreferencesHelper.loadCurrentBestLocation()
        // try to get the last location the system has stored - it is probably more recent
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
            val lastKnownLocationGps: Location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER) ?: lastKnownLocation
            val lastKnownLocationNetwork: Location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER) ?: lastKnownLocation
            when (isBetterLocation(lastKnownLocationGps, lastKnownLocationNetwork)) {
                true -> lastKnownLocation = lastKnownLocationGps
                false -> lastKnownLocation = lastKnownLocationNetwork
            }
        }
        return lastKnownLocation
    }


    /* 检查当前新的位置，是否比之前存储的位置更合适 */
    fun isBetterLocation(location: Location, currentBestLocation: Location?): Boolean {
        // Credit: https://developer.android.com/guide/topics/location/strategies.html#BestEstimate

        if (currentBestLocation == null) {
            // a new location is always better than no location
            return true
        }

        // check whether the new location fix is newer or older
        val timeDelta: Long = location.time - currentBestLocation.time
        val isSignificantlyNewer: Boolean = timeDelta > Keys.SIGNIFICANT_TIME_DIFFERENCE
        val isSignificantlyOlder:Boolean = timeDelta < -Keys.SIGNIFICANT_TIME_DIFFERENCE

        when {
            // if it's been more than two minutes since the current location, use the new location because the user has likely moved
            isSignificantlyNewer -> return true
            // if the new location is more than two minutes older, it must be worse
            isSignificantlyOlder -> return false
        }

        // check whether the new location fix is more or less accurate
        val isNewer: Boolean = timeDelta > 0L
        val accuracyDelta: Float = location.accuracy - currentBestLocation.accuracy
        val isLessAccurate: Boolean = accuracyDelta > 0f
        val isMoreAccurate: Boolean = accuracyDelta < 0f
        val isSignificantlyLessAccurate: Boolean = accuracyDelta > 200f

        // check if the old and new location are from the same provider
        val isFromSameProvider: Boolean = location.provider == currentBestLocation.provider

        // determine location quality using a combination of timeliness and accuracy
        return when {
            isMoreAccurate -> true
            isNewer && !isLessAccurate -> true
            isNewer && !isSignificantlyLessAccurate && isFromSameProvider -> true
            else -> false
        }
    }


    /* 检查 GPS location provider 是否可用 */
    fun isGpsEnabled(locationManager: LocationManager): Boolean {
        if (locationManager.allProviders.contains(LocationManager.GPS_PROVIDER)) {
            return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
        } else {
            return false
        }
    }


    /* Checks if Network location provider is available and enabled */
    fun isNetworkEnabled(locationManager: LocationManager): Boolean {
        if (locationManager.allProviders.contains(LocationManager.NETWORK_PROVIDER)) {
            return locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
        } else {
            return false
        }
    }



    /* Checks if given location is new */
    fun isRecentEnough(location: Location): Boolean {
        val locationAge: Long = SystemClock.elapsedRealtimeNanos() - location.elapsedRealtimeNanos
        return locationAge < Keys.DEFAULT_THRESHOLD_LOCATION_AGE
    }


    /* Checks if given location is accurate */
    fun isAccurateEnough(location: Location, locationAccuracyThreshold: Int): Boolean {
        val isAccurate: Boolean
        when (location.provider) {
            LocationManager.GPS_PROVIDER -> isAccurate = location.accuracy < locationAccuracyThreshold
            else -> isAccurate = location.accuracy < locationAccuracyThreshold + 10 // a bit more relaxed when location comes from network provider
        }
        return isAccurate
    }


    /* 检查轨迹的第1个点是否合理， 也就是检查一下是否发生了跳跃  */
    fun isFirstLocationPlausible(secondLocation: Location, track: Track): Boolean {
        // speed in km/h
        val speed: Double = calculateSpeed(firstLocation = track.wayPoints[0].toLocation(), secondLocation = secondLocation, firstTimestamp = track.recordingStart.time, secondTimestamp = GregorianCalendar.getInstance().time.time)
        // plausible = speed under 250 km/h
        return speed < Keys.IMPLAUSIBLE_TRACK_START_SPEED
    }


    /* 传入2个点，然后计算速度 */
    private fun calculateSpeed(firstLocation: Location, secondLocation: Location, firstTimestamp: Long, secondTimestamp: Long, useImperial: Boolean = false): Double {
        // time difference in seconds
        val timeDifference: Long = (secondTimestamp - firstTimestamp) / 1000L
        // distance in meters
        val distance: Float = calculateDistance(firstLocation, secondLocation)
        // speed in either km/h (default) or mph
        return LengthUnitHelper.convertMetersPerSecond(distance / timeDifference, useImperial)
    }


    /* 根据距离测算是否已经足够远 */
    fun isDifferentEnough(previousLocation: Location?, location: Location, accuracyMultiplier: Int): Boolean {
        // check if previous location is (not) available
        if (previousLocation == null) return true

        // location.accuracy is given as 1 standard deviation, with a 68% chance
        // that the true position is within a circle of this radius.
        // These formulas determine if the difference between the last point and
        // new point is statistically significant.
        val accuracy: Float = if (location.accuracy != 0.0f) location.accuracy else Keys.DEFAULT_THRESHOLD_DISTANCE
        val previousAccuracy: Float = if (previousLocation.accuracy != 0.0f) previousLocation.accuracy else Keys.DEFAULT_THRESHOLD_DISTANCE
        val accuracyDelta: Double = Math.sqrt((accuracy.pow(2) + previousAccuracy.pow(2)).toDouble())
        val distance: Float = calculateDistance(previousLocation, location)

        // With 1*accuracyDelta we have 68% confidence that the points are
        // different. We can multiply this number to increase confidence but
        // decrease point recording frequency if needed.
        return distance > accuracyDelta * accuracyMultiplier
    }


    /* Calculates distance in meters between two locations */
    fun calculateDistance(previousLocation: Location?, location: Location): Float  {
        var distance: Float = 0f
        // two data points needed to calculate distance
        if (previousLocation != null) {
            // add up distance
            distance = previousLocation.distanceTo(location)
        }
        return distance
    }


    /* Calculate elevation differences */
    fun calculateElevationDifferencesOld(previousLocation: Location?, location: Location, track: Track): Pair<Double, Double> {
        // store current values
        var positiveElevation: Double = track.positiveElevation
        var negativeElevation: Double = track.negativeElevation
        if (previousLocation != null) {
            // factor is bigger than 1 if the time stamp difference is larger than the movement recording interval
            val timeDifferenceFactor: Long = (location.time - previousLocation.time) / Keys.ADD_WAYPOINT_TO_TRACK_INTERVAL
            // get elevation difference and sum it up
            val altitudeDifference: Double = location.altitude - previousLocation.altitude
            if (altitudeDifference > 0 && altitudeDifference < Keys.ALTITUDE_MEASUREMENT_ERROR_THRESHOLD * timeDifferenceFactor && location.altitude != Keys.DEFAULT_ALTITUDE) {
                positiveElevation = track.positiveElevation + altitudeDifference // upwards movement
            }
            if (altitudeDifference < 0 && altitudeDifference > -Keys.ALTITUDE_MEASUREMENT_ERROR_THRESHOLD * timeDifferenceFactor && location.altitude != Keys.DEFAULT_ALTITUDE) {
                negativeElevation = track.negativeElevation + altitudeDifference // downwards movement
            }
        }
        return Pair(positiveElevation, negativeElevation)
    }


    /* Calculate elevation differences */
    fun calculateElevationDifferences(currentAltitude: Double, previousAltitude: Double, track: Track): Track {
        if (currentAltitude != Keys.DEFAULT_ALTITUDE && previousAltitude != Keys.DEFAULT_ALTITUDE) {
            val altitudeDifference: Double = currentAltitude - previousAltitude
            if (altitudeDifference > 0) {
                track.positiveElevation += altitudeDifference // upwards movement
            }
            if (altitudeDifference < 0) {
                track.negativeElevation += altitudeDifference // downwards movement
            }
        }
        return track
    }


    /* 如果停留5分钟，就认为是停下了 */
    fun isStopOver(previousLocation: Location?, location: Location): Boolean {
        if (previousLocation == null) return false
        // check how many milliseconds the given locations are apart
        return location.time - previousLocation.time > Keys.STOP_OVER_THRESHOLD
    }


    /* 为位置数据中提取卫星个数 */
    fun getNumberOfSatellites(location: Location): Int {
        val numberOfSatellites: Int
        val extras: Bundle? = location.extras
        if (extras != null && extras.containsKey("satellites")) {
            numberOfSatellites = extras.getInt("satellites", 0)
        } else {
            numberOfSatellites = 0
        }
        return numberOfSatellites
    }


}
