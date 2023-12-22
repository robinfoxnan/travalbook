package com.bird2fish.travelbook.core

import android.content.Context
import android.os.Parcelable
import androidx.annotation.Keep
import com.google.gson.annotations.Expose
import kotlinx.parcelize.Parcelize
import com.bird2fish.travelbook.core.*
import com.bird2fish.travelbook.helper.DateTimeHelper
import java.text.SimpleDateFormat
import java.util.*


/*
 * Track data class
 */
@Keep
@Parcelize
data class Track (@Expose var trackFormatVersion: Int = Keys.CURRENT_TRACK_FORMAT_VERSION,
                  @Expose val wayPoints: MutableList<WayPoint> = mutableListOf<WayPoint>(),
                  @Expose var length: Float = 0f,
                  @Expose var duration: Long = 0L,
                  @Expose var recordingPaused: Long = 0L,
                  @Expose var stepCount: Float = 0f,
                  @Expose var recordingStart: Date = GregorianCalendar.getInstance().time,
                  @Expose var recordingStop: Date = recordingStart,
                  @Expose var maxAltitude: Double = 0.0,
                  @Expose var minAltitude: Double = 0.0,
                  @Expose var positiveElevation: Double = 0.0,
                  @Expose var negativeElevation: Double = 0.0,
                  @Expose var trackUriString: String = String(),
                  @Expose var gpxUriString: String = String(),
                  @Expose var latitude: Double = Keys.DEFAULT_LATITUDE,
                  @Expose var longitude: Double = Keys.DEFAULT_LONGITUDE,
                  @Expose var zoomLevel: Double = Keys.DEFAULT_ZOOM_LEVEL,
                  @Expose var name: String = String()): Parcelable {


    /* Creates a TracklistElement */
    fun toTracklistElement(context: Context): TracklistElement {
        val readableDateString: String = DateTimeHelper.convertToReadableDate(recordingStart)
        val readableDurationString: String = DateTimeHelper.convertToReadableTime(context, duration, true)
        var endStr:String  = DateTimeHelper.convertToReadableDateAndTime(recordingStop)
        return TracklistElement(
            name = name,
            date = recordingStart,
            dateString = readableDateString,
            endTimeString = endStr,
            length = length,
            points = wayPoints.size,
            durationString = readableDurationString,
            trackUriString = trackUriString,
            gpxUriString = gpxUriString,
            starred = true
        )
    }

    fun generateName():String{
        val dateFormat = SimpleDateFormat("yyyy-MM-dd_HH_mm")

        val start = dateFormat.format(recordingStart)
        //val end = dateFormat.format(recordingStop)
        this.name = "${start}"


        return this.name
    }


    /* Returns unique ID for Track - currently the start date */
    fun getTrackId(): Long {
        return recordingStart.time
    }


}
