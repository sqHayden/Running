package com.idx.running.utils;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import com.idx.running.R;

/**
 * Created by steve on 17-11-17.
 */

public class BitGL {
    public static Bitmap bitmap;

    public static void init(Resources resources) {
        bitmap = BitmapFactory.decodeResource(resources, R.drawable.mofang3);
    }
}
