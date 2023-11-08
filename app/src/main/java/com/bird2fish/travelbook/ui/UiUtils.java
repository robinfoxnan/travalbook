package com.bird2fish.travelbook.ui;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.drawable.Drawable;

public class UiUtils {
    // Construct avatar drawable: use bitmap if it is not null,
    // otherwise use name & address to create a LetterTileDrawable.
//    public static Drawable avatarDrawable(Context context, Bitmap bmp, String name, String address, boolean disabled) {
//        if (bmp != null) {
//            Drawable drawable = new RoundImageDrawable(context.getResources(), bmp);
//            if (disabled) {
//                // Make avatar grayscale
//                ColorMatrix matrix = new ColorMatrix();
//                matrix.setSaturation(0);
//                drawable.setColorFilter(new ColorMatrixColorFilter(matrix));
//            }
//            return drawable;
//        } else {
//            LetterTileDrawable drawable = new LetterTileDrawable(context);
//            drawable.setContactTypeAndColor(
//                            Topic.isP2PType(address) ?
//                                    LetterTileDrawable.ContactType.PERSON :
//                                    LetterTileDrawable.ContactType.GROUP, disabled)
//                    .setLetterAndColor(name, address, disabled)
//                    .setIsCircular(true);
//            return drawable;
//        }
//    }
}
