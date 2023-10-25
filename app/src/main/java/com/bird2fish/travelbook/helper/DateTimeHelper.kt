package com.bird2fish.travelbook.helper


import android.content.Context
import android.location.Location
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit
import com.bird2fish.travelbook.core.Keys
import com.bird2fish.travelbook.R
/*
 * 这个工具类负责时间的各种转换
 */
object DateTimeHelper {

    /* 将毫秒数转换为  mm:ss or hh:mm:ss */
    // compactFormat紧凑的格式
    fun convertToReadableTime(context: Context, milliseconds: Long, compactFormat: Boolean = false): String {
        val hours: Long = TimeUnit.MILLISECONDS.toHours(milliseconds)
        val minutes: Long = TimeUnit.MILLISECONDS.toMinutes(milliseconds) % TimeUnit.HOURS.toMinutes(1)
        val seconds: Long = TimeUnit.MILLISECONDS.toSeconds(milliseconds) % TimeUnit.MINUTES.toSeconds(1)
        val timeString: String

        when (compactFormat) {

            // Compact tine format
            true -> {
                if (milliseconds < Keys.ONE_HOUR_IN_MILLISECONDS) {
                    // example: 23:45
                    val minutesString: String = minutes.toString()
                    val secondsString: String = seconds.toString().padStart(2, '0')
                    timeString = "$minutesString:$secondsString"
                } else {
                    // example: 1:23
                    val hoursString: String  = hours.toString()
                    val minutesString: String = minutes.toString()
                    timeString = "$hoursString:$minutesString"
                }
            }

            // Long time format
            false -> {
                if (milliseconds < Keys.ONE_HOUR_IN_MILLISECONDS) {
                    // example: 23 min 45 sec
                    val minutesString: String = minutes.toString()
                    val secondsString: String = seconds.toString()
                    val m: String = context.getString(R.string.abbreviation_minutes)
                    val s: String = context.getString(R.string.abbreviation_seconds)
                    timeString = "$minutesString $m $secondsString $s"
                } else {
                    // example: 1 hrs 23 min 45 sec
                    val hoursString: String = hours.toString()
                    val minutesString: String = minutes.toString()
                    val secondsString: String = seconds.toString()
                    val h: String = context.getString(R.string.abbreviation_hours)
                    val m: String = context.getString(R.string.abbreviation_minutes)
                    val s: String = context.getString(R.string.abbreviation_seconds)
                    timeString = "$hoursString $h $minutesString $m $secondsString $s"
                }
            }
        }

        return timeString
    }


    /* 生成日期字符串，给文件名使用  */
    fun convertToSortableDateString(date: Date): String {
        val dateFormat: SimpleDateFormat = SimpleDateFormat("yyyy-MM-dd-HH-mm-ss-SSS", Locale.CHINA)
        return dateFormat.format(date)
    }

    /* 生成日期字符串，给文件名使用  */
    // TODO: locale需要更改为系统的
    fun convertToShortDateString(date: Date): String {
        val dateFormat: SimpleDateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.CHINA)
        return dateFormat.format(date)
    }


    /* 生成日期字符串，给UI使用*/
    fun convertToReadableDate(date: Date, dateStyle: Int = DateFormat.LONG): String {
        return DateFormat.getDateInstance(dateStyle, Locale.getDefault()).format(date)
    }


    /* 生成界面里可以显示的字符串 */
    fun convertToReadableDateAndTime(date: Date, dateStyle: Int = DateFormat.SHORT, timeStyle: Int = DateFormat.SHORT): String {
        return "${DateFormat.getDateInstance(dateStyle, Locale.getDefault()).format(date)} ${DateFormat.getTimeInstance(timeStyle, Locale.getDefault()).format(date)}"
    }


    /* 计算2点位置之间的时间差，毫秒 */
    fun calculateTimeDistance(previousLocation: Location?, location: Location): Long  {
        var timeDifference: Long = 0L
        // two data points needed to calculate time difference
        if (previousLocation != null) {
            // get time difference
            timeDifference = location.time - previousLocation.time
        }
        return timeDifference
    }


}