package com.bird2fish.travelbook.core

import android.os.Parcelable
import androidx.annotation.Keep
import com.google.gson.annotations.Expose
import kotlinx.parcelize.Parcelize

@Keep
@Parcelize
data class FavLocation (@Expose var favId: Int = 0,
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