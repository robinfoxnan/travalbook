package com.bird2fish.travelbook.core

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.util.TypedValue
import android.view.Gravity
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import com.bird2fish.travelbook.R
import com.bird2fish.travelbook.ui.contact.Friend

public object UiHelper {

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

    // 为地图创建小图标
    fun getSmallIconBitmap(name :String, ctx: Context) :Bitmap{
        val id = getIconResId(name)
        val bitmapOld = BitmapFactory.decodeResource(ctx.resources, id)
        val bitmapIcon = Bitmap.createScaledBitmap(bitmapOld, 160, 160, false)
        return bitmapIcon
    }

    fun resizeImage(bitmap: Bitmap, width: Int, height: Int): Bitmap {
        val bmpWidth = bitmap.width
        val bmpHeight = bitmap.height

        val scaleWidth = width.toFloat() / bmpWidth
        val scaleHeight = height.toFloat() / bmpHeight

        val matrix = Matrix()
        matrix.postScale(scaleWidth, scaleHeight)

        return Bitmap.createBitmap(bitmap, 0, 0, bmpWidth, bmpHeight, matrix, true)
    }

    // 创建 ImageView 并设置参数
    fun createImageViewForBottomView(ctx: Context, friend:Friend?): ImageView {
        val imageView = ImageView(ctx)
        val sizePx = dpToPx(50f, ctx).toInt()

        // 设置图标大小
        val params = LinearLayout.LayoutParams(sizePx, sizePx)

        // 设置图标之间的间隔
        params.marginStart = dpToPx(8.0f, ctx).toInt()
        imageView.layoutParams = params

        // 设置图标资源
        var id = R.drawable.icon1
        if (friend != null){
            id = getIconResId(friend.icon)
        }
        imageView.setImageResource(id)

        // 设置圆角背景
        imageView.setBackgroundResource(R.drawable.rounded_border)

        return imageView
    }

    // 将 dp 转换为像素
    private fun dpToPx(dp: Float, context: Context): Float {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            dp,
            context.resources.displayMetrics
        )
    }

}