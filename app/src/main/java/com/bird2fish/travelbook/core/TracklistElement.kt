package com.bird2fish.travelbook.core

import android.os.Parcelable
import androidx.annotation.Keep
import com.google.gson.annotations.Expose
import kotlinx.parcelize.Parcelize
import java.util.*


/*
 * 用于某一段路径的轨迹的描述信息
 */
@Keep
@Parcelize
data class TracklistElement(@Expose var name: String,
                            @Expose var date: Date,
                            @Expose var dateString: String,
                            @Expose var endTimeString:String,
                            @Expose var durationString: String,
                            @Expose var length: Float = 0.0f,
                            @Expose var points:Int = 0,
                            @Expose val trackUriString: String,
                            @Expose val gpxUriString: String,
                            @Expose var starred: Boolean = false): Parcelable {

    /* Returns unique ID for TracklistElement - currently the start date */
    fun getTrackId(): Long {
        return date.time
    }

}
