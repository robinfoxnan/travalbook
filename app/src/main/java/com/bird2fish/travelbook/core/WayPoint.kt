package com.bird2fish.travelbook.core

import android.location.Location
import android.os.Parcelable
import androidx.annotation.Keep
import com.google.gson.annotations.Expose
import kotlinx.parcelize.Parcelize
import com.bird2fish.travelbook.helper.*


/*
 * 轨迹的点数据类
 */
@Keep
@Parcelize
data class WayPoint(@Expose val provider: String,
                    @Expose val latitude: Double,
                    @Expose val longitude: Double,
                    @Expose val altitude: Double,
                    @Expose val accuracy: Float,
                    @Expose val time: Long,
                    @Expose val distanceToStartingPoint: Float = 0f,
                    @Expose val numberSatellites: Int = 0,
                    @Expose var isStopOver: Boolean = false,
                    @Expose var starred: Boolean = false): Parcelable {
   // @Expose
            /* Constructor using just Location */
    constructor(location: Location) : this (location.provider, location.latitude, location.longitude, location. altitude, location.accuracy, location.time)

    /* Constructor using Location plus distanceToStartingPoint and numberSatellites */
    constructor(location: Location, distanceToStartingPoint: Float) : this (location.provider,
        location.latitude,
        location.longitude,
        location.altitude,
        location.accuracy,
        location.time,
        distanceToStartingPoint)

    /* Converts WayPoint into Location */
    fun toLocation(): Location {
        val location: Location = Location(provider)
        location.latitude = latitude
        location.longitude = longitude
        location.altitude = altitude
        location.accuracy = accuracy
        location.time = time
        return location
    }

}
