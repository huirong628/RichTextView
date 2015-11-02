package com.kutear.richtextview;

import android.util.Log;

/**
 * Created by kutear.guo on 2015/8/4.
 *
 */
public class L {
    private static boolean isDeBug = true;
    
    
    public static void v(String tag, String value) {
        if (isDeBug) {
            Log.v(tag, value);
        }
    }

    public static void e(String tag, String value) {
        if (isDeBug) {
            Log.e(tag, value);
        }
    }

    public static void d(String tag, String value) {
        if (isDeBug) {
            Log.d(tag, value);
        }
    }

    public static void i(String tag, String value) {
        if (isDeBug) {
            Log.i(tag, value);
        }
    }
}
