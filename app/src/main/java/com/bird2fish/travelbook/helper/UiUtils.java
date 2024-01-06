package com.bird2fish.travelbook.helper;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.drawable.Drawable;

public class UiUtils {
    public static Bitmap getBitmapFromResource(Context context, int resourceId) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.ARGB_8888; // 选择色彩模式
        return BitmapFactory.decodeResource(context.getResources(), resourceId, options);
    }

}
