package com.bird2fish.travelbook.helper


import java.math.BigDecimal
import java.math.RoundingMode
import java.text.NumberFormat
import java.util.*


/*
 * LengthUnitHelper object
 */
object LengthUnitHelper {


    /* 按照给定的单位，转为距离字符串 */
    fun convertDistanceToString(distance: Float, useImperial: Boolean = false): String {
        return convertDistanceToString(distance.toDouble(), useImperial)
    }


    /* 需要确定是使用英制，或者公制 */
    fun convertDistanceToString(distance: Double, useImperial: Boolean = false): String {
        val readableDistance: Double
        val unit: String
        val numberFormat = NumberFormat.getNumberInstance()

        // check for locale and set unit system accordingly
        when (useImperial) {
            // CASE: miles and feet
            true -> {
                if (distance > 1610) {
                    // convert distance to miles
                    readableDistance = distance * 0.000621371192f
                    // set measurement unit
                    unit = "mi"
                    // set number precision
                    numberFormat.maximumFractionDigits = 2
                } else {
                    // convert distance to feet
                    readableDistance = distance * 3.28084f
                    // set measurement unit
                    unit = "ft"
                    // set number precision
                    numberFormat.maximumFractionDigits = 0
                }
            }
            // CASE: kilometer and meter
            false -> {
                if (distance >= 1000) {
                    // convert distance to kilometer
                    readableDistance = distance * 0.001f
                    // set measurement unit
                    unit = "km"
                    // set number precision
                    numberFormat.maximumFractionDigits = 2
                } else {
                    // no need to convert
                    readableDistance = distance
                    // set measurement unit
                    unit = "m"
                    // set number precision
                    numberFormat.maximumFractionDigits = 0
                }
            }
        }

        // format distance according to current locale
        return "${numberFormat.format(readableDistance)} $unit"
    }


    /* 检查是否使用英制单位，英制单位就那么几个而已 */
    fun useImperialUnits(): Boolean {
        // America (US), Liberia (LR), Myanmar(MM) use the imperial system
        val imperialSystemCountries = Arrays.asList("US", "LR", "MM")
        val countryCode = Locale.getDefault().country
        return imperialSystemCountries.contains(countryCode)
    }


    /* 计算速度，传入时间长，停顿的时间长，距离，返回速度  km/h */
    fun convertToVelocityString(trackDuration: Long, trackRecordingPause: Long, trackLength: Float, useImperialUnits: Boolean = false) : String {
        var speed: String = "0"

        // duration minus pause in seconds
        val duration: Long = (trackDuration - trackRecordingPause) / 1000L

        if (duration > 0L) {
            // speed in km/h / mph
            val velocity: Double = convertMetersPerSecond((trackLength / duration), useImperialUnits)
            // create readable speed string
            var bd: BigDecimal = BigDecimal.valueOf(velocity)
            bd = bd.setScale(1, RoundingMode.HALF_UP)
            speed = bd.toPlainString()
        }

        when (useImperialUnits) {
            true -> return "$speed mph"
            false -> return "$speed km/h"
        }
    }


    /* 速度的米/秒 转换为 km/h or mph */
    fun convertMetersPerSecond(metersPerSecond: Float, useImperial: Boolean = false): Double {
        if (useImperial) {
            // mph
            return metersPerSecond * 2.2369362920544
        } else {
            // km/h
            return metersPerSecond * 3.6
        }
    }


}

