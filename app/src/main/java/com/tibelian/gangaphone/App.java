package com.tibelian.gangaphone;

import android.app.Application;
import android.content.Context;
import android.util.Log;

public class App extends Application {

    private static Context mContext;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.e("App", "onCreate setting context");
        mContext = this;
    }

    public static Context getContext(){
        return mContext;
    }

}
