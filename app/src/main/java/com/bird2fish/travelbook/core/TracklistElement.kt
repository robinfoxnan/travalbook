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
                            @Expose val date: Date,
                            @Expose val dateString: String,
                            @Expose val durationString: String,
                            @Expose val length: Float,
                            @Expose val trackUriString: String,
                            @Expose val gpxUriString: String,
                            @Expose var starred: Boolean = false): Parcelable {

    /* Returns unique ID for TracklistElement - currently the start date */
    fun getTrackId(): Long {
        return date.time
    }

}
