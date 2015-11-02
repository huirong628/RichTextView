package com.kutear.richtextview;

import android.app.Application;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

/**
 * Created by kutear on 15-11-2.
 */
public class AppApplication extends Application {
    private static RequestQueue mQueue;

    @Override
    public void onCreate() {
        super.onCreate();
        mQueue = Volley.newRequestQueue(this);
    }

    public static void startRequest(Request request){
        mQueue.add(request);
    }
}
