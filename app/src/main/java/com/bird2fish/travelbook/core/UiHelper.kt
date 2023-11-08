package com.bird2fish.travelbook.core

import android.content.Context
import android.view.Gravity
import android.widget.Toast
import com.bird2fish.travelbook.R

object UiHelper {

    fun showMessage(applicationContext: Context, str:CharSequence){
        var toast = Toast.makeText(
            applicationContext,
            str,
            Toast.LENGTH_LONG
        )
        toast.setGravity(Gravity.CENTER, 0, 0)
        toast.show()
    }

    fun showCenterMessage(applicationContext: Context, str:CharSequence){
       var toast=  Toast.makeText(
            applicationContext,
            str,
            Toast.LENGTH_SHORT
        )
        toast.setGravity(Gravity.CENTER, 0, 0)
        toast.show()
    }
    // 35个
    val iconIds = intArrayOf(
        R.drawable.widget_avatar_2,
        R.drawable.widget_avatar_3,
        R.drawable.widget_avatar_4,
        R.drawable.widget_avatar_6,
        R.drawable.widget_avatar_7,
        R.drawable.icon1,
        R.drawable.icon2,
        R.drawable.icon3,
        R.drawable.icon4,
        R.drawable.icon5,
        R.drawable.icon6,
        R.drawable.icon7,
        R.drawable.icon8,
        R.drawable.icon9,
        R.drawable.icon10,
        R.drawable.icon11,
        R.drawable.icon12,
        R.drawable.icon13,
        R.drawable.icon14,
        R.drawable.icon15,
        R.drawable.icon16,
        R.drawable.icon17,
        R.drawable.icon18,
        R.drawable.icon19,
        R.drawable.icon20,
        R.drawable.icon21,
        R.drawable.icon22,
        R.drawable.icon24,
        R.drawable.icon25,
        R.drawable.icon26,
        R.drawable.icon27,
        R.drawable.icon30,
        R.drawable.icon31,
        R.drawable.icon32,
        R.drawable.icon33
    )
    fun getIconResId(name :String) :Int{
        if (name.startsWith("sys:")){
            val idStr = name.substring(4)
            val index = idStr.toIntOrNull()
            if (index != null){
                if (index > iconIds.size || index <1)
                    return R.drawable.icon1

                return iconIds[index-1]
            }
        }

        return  R.drawable.icon1
    }
}