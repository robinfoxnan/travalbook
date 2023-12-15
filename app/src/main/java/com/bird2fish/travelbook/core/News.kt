package com.bird2fish.travelbook.core

import android.os.Parcelable
import androidx.annotation.Keep
import com.google.gson.annotations.Expose
import kotlinx.android.parcel.Parcelize

@Keep
@Parcelize
data class News (@Expose var newsId: Int = 0,
                 @Expose var uId: String = "1000",
                 @Expose var uNick: String = "1000",
                 @Expose var uIcon: String = "1000",
                 @Expose var tm:Long = System.currentTimeMillis(),
                 @Expose var tmStr:String = "",
                 @Expose var title:String = "",
                 @Expose var coverImg: String = ""
            ): Parcelable {


}