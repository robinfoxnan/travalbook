package com.bird2fish.travelbook.core

import android.os.Parcel
import android.os.Parcelable
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class News(
    @SerializedName("nid") @Expose val nid: String,
    @SerializedName("uid") @Expose val uid: String,
    @SerializedName("nick") @Expose val nick: String,
    @SerializedName("icon") @Expose val icon: String,
    @SerializedName("lat") @Expose val lat: Double,
    @SerializedName("log") @Expose val log: Double,
    @SerializedName("alt") @Expose val alt: Double,
    @SerializedName("tm") @Expose val tm: Long,
    @SerializedName("title") @Expose var title: String,
    @SerializedName("content") @Expose var content: String,
    @SerializedName("images") @Expose var images: List<String>,
    @SerializedName("tags") @Expose var tags: List<String>,
    @SerializedName("type") @Expose var type: String,
    @SerializedName("trackfile") @Expose var trackFile: String,
    @SerializedName("likes") @Expose var likes: Int,
    @SerializedName("favs") @Expose var favs: Int,
    @SerializedName("deleted") @Expose var deleted: Boolean,
    @SerializedName("deltm") @Expose var delTm: Long
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readDouble(),
        parcel.readDouble(),
        parcel.readDouble(),
        parcel.readLong(),
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.createStringArrayList() ?: listOf(),
        parcel.createStringArrayList() ?: listOf(),
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readInt(),
        parcel.readInt(),
        parcel.readByte() != 0.toByte(),
        parcel.readLong()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(nid)
        parcel.writeString(uid)
        parcel.writeString(nick)
        parcel.writeString(icon)
        parcel.writeDouble(lat)
        parcel.writeDouble(log)
        parcel.writeDouble(alt)
        parcel.writeLong(tm)
        parcel.writeString(title)
        parcel.writeString(content)
        parcel.writeStringList(images)
        parcel.writeStringList(tags)
        parcel.writeString(type)
        parcel.writeString(trackFile)
        parcel.writeInt(likes)
        parcel.writeInt(favs)
        parcel.writeByte(if (deleted) 1 else 0)
        parcel.writeLong(delTm)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<News> {
        override fun createFromParcel(parcel: Parcel): News {
            return News(parcel)
        }

        override fun newArray(size: Int): Array<News?> {
            return arrayOfNulls(size)
        }
    }
}

data class ImagePathPair(
    @SerializedName("lpath") @Expose var localPath: String,
    @SerializedName("rname") @Expose var remoteName: String,
    @SerializedName("state") @Expose var state: String,
){

}

data class Comment(
    @SerializedName("nid") @Expose val nid: String,
    @SerializedName("cid") @Expose val cid: String,
    @SerializedName("uid") @Expose val uid: String,
    @SerializedName("nick") @Expose val nick: String,
    @SerializedName("icon") @Expose val icon: String,
    @SerializedName("pnid") @Expose val pnid: String,
    @SerializedName("tm") @Expose val tm: Long,
    @SerializedName("toid") @Expose val toId: String,
    @SerializedName("tonick") @Expose val toNick: String,
    @SerializedName("content") @Expose val content: String,
    @SerializedName("images") @Expose val images: List<String>,
    @SerializedName("likes") @Expose val likes: Int,
    @SerializedName("deleted") @Expose val deleted: Boolean
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readLong(),
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.createStringArrayList() ?: listOf(),
        parcel.readInt(),
        parcel.readByte() != 0.toByte()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(nid)
        parcel.writeString(cid)
        parcel.writeString(uid)
        parcel.writeString(nick)
        parcel.writeString(icon)
        parcel.writeString(pnid)
        parcel.writeLong(tm)
        parcel.writeString(toId)
        parcel.writeString(toNick)
        parcel.writeString(content)
        parcel.writeStringList(images)
        parcel.writeInt(likes)
        parcel.writeByte(if (deleted) 1 else 0)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Comment> {
        override fun createFromParcel(parcel: Parcel): Comment {
            return Comment(parcel)
        }

        override fun newArray(size: Int): Array<Comment?> {
            return arrayOfNulls(size)
        }
    }
}

data class NewsFav(
    @SerializedName("nid") val nid: String,
    @SerializedName("uid") val uid: String,
    @SerializedName("like") var like: Boolean,
    @SerializedName("fav") var fav: Boolean,
    @SerializedName("hate") var hate: Boolean,
    @SerializedName("reason") var reason: Int
)

//data class ShareDataItem(
//    @SerializedName("p") var latlon: MutableList<Double> = mutableListOf<Double>()
//)

data class ShareData(
    @SerializedName("uid") val uid: String = "",
    @SerializedName("nick") var nick: String = "",
    @SerializedName("icon") var icon: String = "sys:1",
    @SerializedName("points") var points: MutableList<MutableList<Double>> = mutableListOf<MutableList<Double> >(),
    @SerializedName("title") var title: String = ""
)
