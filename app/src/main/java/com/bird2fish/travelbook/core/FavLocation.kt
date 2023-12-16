package com.bird2fish.travelbook.core

import android.os.Parcelable
import androidx.annotation.Keep
import com.bird2fish.travelbook.helper.DateTimeHelper
import com.google.gson.annotations.Expose
import kotlinx.parcelize.Parcelize
import java.util.LinkedList

@Keep
@Parcelize
data class FavLocation (@Expose var favId: Long = 0,
                        @Expose var uId: String = "1000",
                        @Expose var uNick: String = "1000",
                        @Expose var uIcon: String = "1000",
                        @Expose var lat: Double = 0.0,
                        @Expose var lon: Double = 0.0,
                        @Expose var alt: Double = 0.0,
                        @Expose var tm:Long = System.currentTimeMillis(),
                        @Expose var tmStr:String = "",
                        @Expose var title:String = "",
                        @Expose var des: String = ""): Parcelable {


}

@Keep
@Parcelize
data class FavLocationList(
    @Expose var locations: LinkedList<FavLocation> = LinkedList<FavLocation>(),
    @Expose var tm: String = DateTimeHelper.getTimeStampLongString(),
): Parcelable {


}