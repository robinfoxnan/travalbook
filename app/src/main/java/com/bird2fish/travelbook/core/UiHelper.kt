package com.bird2fish.travelbook.core

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Build
import android.util.TypedValue
import android.view.Gravity
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.bird2fish.travelbook.R
import com.bird2fish.travelbook.ui.contact.Friend
import com.google.android.material.internal.ViewUtils.dpToPx


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

    fun getIconResIndex(name :String) :Int{
        if (name.startsWith("sys:")) {
            val idStr = name.substring(4)
            val index = idStr.toIntOrNull()
            if (index != null) {
                if (index > iconIds.size || index <1)
                    return 0
                return index -1
            }
        }
        return 0
    }

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

    fun getSmallIconBitmap(id: Int, ctx: Context) :Bitmap{
        val bitmapOld = BitmapFactory.decodeResource(ctx.resources, id)
        val bitmapIcon = Bitmap.createScaledBitmap(bitmapOld, 120, 120, false)
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
        var id = com.bird2fish.travelbook.R.drawable.icon1
        if (friend != null){
            id = getIconResId(friend.icon)
        }
        imageView.setImageResource(id)

        // 设置圆角背景
        imageView.setBackgroundResource(com.bird2fish.travelbook.R.drawable.rounded_border)

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

    fun formatSpeed(speed: Float): String {
        if (speed < 5.0){
            return String.format("%.1f 米/秒", speed)
        }else{
            return String.format("%.1f 千米/时", speed * 3.6 )
        }
    }


    //加载资源
    fun idToDrawable(context:Context, id: Int): Drawable? {
        return ContextCompat.getDrawable(context, id)
    }

    fun computeHeight(img :Drawable, width: Int):Int{
        val scaleFactor = width * 1.0 / img.intrinsicWidth
        val height = scaleFactor * img.intrinsicHeight

        return height.toInt()
    }

    // 将imageview中图片的不透明的部分变为另一种颜色，用于按钮点击
    fun replaceOpaqueWithColor(context: Context, resourceId: Int, replacementColorResId: Int, imageView: ImageView) {
        // 原始图片
        val originalBitmap = BitmapFactory.decodeResource(context.resources, resourceId)

        // 创建一个新的Bitmap，用于修改颜色
        val modifiedBitmap = originalBitmap.copy(Bitmap.Config.ARGB_8888, true)

        // 获取要替换的颜色
        val replacementColor = ContextCompat.getColor(context, replacementColorResId)

        // 遍历所有像素
        for (x in 0 until modifiedBitmap.width) {
            for (y in 0 until modifiedBitmap.height) {
                // 获取像素颜色
                val pixel = modifiedBitmap.getPixel(x, y)

                // 判断是否为不透明的颜色
                if (Color.alpha(pixel) == 255) {
                    // 将不透明部分替换为指定颜色
                    modifiedBitmap.setPixel(x, y, replacementColor)
                }
            }
        }

        // 将修改后的Bitmap显示在ImageView中
        imageView.setImageBitmap(modifiedBitmap)
    }

    fun loadAndScaleImage(context: Context, resourceId: Int): Drawable? {
        try {
            // 从资源中加载原始图片
            val options = BitmapFactory.Options()
            options.inJustDecodeBounds = true
            BitmapFactory.decodeResource(context.resources, resourceId, options)

            // 计算缩放比例
            val targetWidth = 24
            val targetHeight = 24
            val scaleFactor = Math.min(
                options.outWidth / targetWidth,
                options.outHeight / targetHeight
            )

            // 设置缩放比例
            options.inJustDecodeBounds = false
            options.inSampleSize = scaleFactor

            // 重新加载图片并缩放
            val scaledBitmap = BitmapFactory.decodeResource(context.resources, resourceId, options)

            // 创建 Drawable
            return BitmapDrawable(context.resources, scaledBitmap)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return null
    }

    fun loadAndScaleImage(context: Context, resourceId: Int, width:Int): Drawable? {
        try {
            // 从资源中加载原始图片
            val options = BitmapFactory.Options()
            options.inJustDecodeBounds = true
            BitmapFactory.decodeResource(context.resources, resourceId, options)

            // 计算缩放比例
            val targetWidth = width

            val scaleFactor = options.outWidth / width
            var targetHeight = options.outHeight * scaleFactor



            // 设置缩放比例
            options.inJustDecodeBounds = false
            options.inSampleSize = scaleFactor

            // 重新加载图片并缩放
            val scaledBitmap = BitmapFactory.decodeResource(context.resources, resourceId, options)

            // 创建 Drawable
            return BitmapDrawable(context.resources, scaledBitmap)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return null
    }

}