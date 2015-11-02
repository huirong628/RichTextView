package com.kutear.richtextview;

import android.content.res.Resources;
import android.util.DisplayMetrics;

/**
 * Created by kutear.guo on 2015/10/28.
 * 基本工具包
 */
public class AndroidUtils {

    public static float convertPixelsToDp(float px) {
        DisplayMetrics metrics = Resources.getSystem().getDisplayMetrics();
        float dp = px / (metrics.densityDpi / 160f);
        return Math.round(dp);
    }

    public static float convertDpToPixel(float dp) {
        DisplayMetrics metrics = Resources.getSystem().getDisplayMetrics();
        float px = dp * (metrics.densityDpi / 160f);
        return Math.round(px);
    }
}
