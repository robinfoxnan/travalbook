package com.bird2fish.travelbook.core

import android.os.Parcelable
import androidx.annotation.Keep
import com.google.gson.annotations.Expose
import kotlinx.parcelize.Parcelize
import com.bird2fish.travelbook.core.TracklistElement
import com.bird2fish.travelbook.helper.*
import java.util.*


/*
 * Tracklist data class
 */
@Keep
@Parcelize
data class Tracklist (@Expose val tracklistFormatVersion: Int = Keys.CURRENT_TRACKLIST_FORMAT_VERSION,
                      @Expose val tracklistElements: MutableList<TracklistElement> = mutableListOf<TracklistElement>(),
                      @Expose var modificationDate: Date = Date(),
                      @Expose var totalDistanceAll: Float = 0f,
                      @Expose var totalDurationAll: Long = 0L,
                      @Expose var totalRecordingPausedAll: Long = 0L,
                      @Expose var totalStepCountAll: Float = 0f): Parcelable {

    /* Return trackelement for given track id */
    fun getTrackElement(trackId: Long): TracklistElement? {
        tracklistElements.forEach { tracklistElement ->
            if (TrackHelper.getTrackId(tracklistElement) == trackId) {
                return tracklistElement
            }
        }
        return null
    }

    /* Create a deep copy */
    fun deepCopy(): Tracklist {
        return Tracklist(tracklistFormatVersion, mutableListOf<TracklistElement>().apply { addAll(tracklistElements) }, modificationDate)
    }

}
