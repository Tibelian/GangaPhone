package com.tibelian.gangaphone;

import android.app.Application;
import android.content.Context;
import android.util.Log;

/**
 * This class is used to manage the app's life cycle
 */
public class App extends Application {

    // singleton context
    private static Context mContext;

    /**
     * this method is called only one time
     * when the application is opened
     */
    @Override
    public void onCreate() {
        super.onCreate();
        // we save the context as singleton
        mContext = this;
    }

    /**
     * Everybody can access this context
     * @return Context
     */
    public static Context getContext(){
        return mContext;
    }

}
